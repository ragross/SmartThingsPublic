/**
 *  Generic Z-Wave Motion Sensor
 *
 *  Author: Kristopher Kubicki
 *  Date: 2015-02-02
 * 
 * Adds support for an inline "refresh" button that will clear a motion sensor  
 *
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Z-Wave Motion Sensor with Refresh", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"
	}

	simulator {
		status "inactive": "command: 3003, payload: 00"
		status "active": "command: 3003, payload: FF"
	}
	
	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state("battery", label:'${currentValue}% battery', unit:"")
		}

        standardTile("clear", "device.refresh", inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		main "motion"
		details(["motion", "battery", "clear"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
    
	return result
}

def sensorValueEvent(Short value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
}

def refresh() { 
    sendEvent(name: 'motion', value: "inactive", descriptionText: "$device.displayName motion has refreshed")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []
	if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId))
			if(!state.MSR) result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else if (cmd.event == 0x07) {
			if(!state.MSR) result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
			result << sensorValueEvent(1)
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
		result << response(zwave.batteryV1.batteryGet())
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = new Date().time
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
		case 0x1E:
			map.name = "loudness"
			map.unit = cmd.scale == 1 ? "dBA" : "dB"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}
