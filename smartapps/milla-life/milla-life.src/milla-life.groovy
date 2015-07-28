/**
 *  Stringify
 *
 *  Copyright 2015 Stringify
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Stringify",
    namespace: "stringify",
    author: "Stringify",
    description: "Allows Stringify to control your devices",
    category: "Convenience",
    iconUrl: "https://stringify.s3.amazonaws.com/smartthings/SmartThings4@1x.png",
    iconX2Url: "https://stringify.s3.amazonaws.com/smartthings/SmartThings4@2x.png",
    iconX3Url: "https://stringify.s3.amazonaws.com/smartthings/SmartThings4@3x.png",
    oauth: [displayName: "Stringify", displayLink: "https://www.stringify.com"])


preferences {
 section("Allow control of these things...") {
 input "myhubs", "hub", title: "Which hubs?", multiple: true, required: true
 input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
 input "switchLevels", "capability.switchLevel", title: "Which Dimmer Switches?", multiple: true, required: false 
 input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
 input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
 input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
 input "temperatureSensors", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required: false
 input "thermostats", "capability.thermostat", title: "Which thermostats?", multiple: true, required: false
 input "accelerationSensors", "capability.accelerationSensor", title: "Which Vibration Sensors?", multiple: true, required: false
 input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
 input "lightSensors", "capability.illuminanceMeasurement", title: "Which Light Sensors?", multiple: true, required: false
 input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Which Relative Humidity Sensors?", multiple: true, required: false
 input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
 input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
 input "battery", "capability.battery", title: "Which Batteries?", multiple: true, required: false
 input "buttons", "capability.button", title: "Which buttons?", multiple: true, required: false
// input "actuators", "capability.actuator", title: "Which actuators?", multiple: true, required: false
 input "carbonmonoxide", "capability.carbonMonoxideDetector", title: "Which carbon monoxide detectors?", multiple: true, required: false
 input "colorcontrol", "capability.colorControl", title: "Which devices that support color control?", multiple: true, required: false
// input "configuration", "capability.configuration" , title: "Which configuration mechanisms?", multiple: true, required: false
 input "doorcontrol", "capability.doorControl", title: "Which door controllers?", multiple: true, required: false
 input "energymeter", "capability.energyMeter", title: "Which energy meters?", multiple: true, required: false
// input "illuminance", "capability.illuminanceMeasurement", title: "Which illuminance measurement devices?", multiple: true, required: false
 input "capture", "capability.imageCapture", title: "Which image capture devices?", multiple: true, required: false
// input "indicator", "capability.indicator", title: "Which devices that support indication?", multiple: true, required: false
 input "locationmode", "capability.locationMode" , title: "Which location mode devices?", multiple: true, required: false
 input "lockcodes", "capability.lockCodes" , title: "Which locks that support codes?", multiple: true, required: false
 input "mediacontroller", "capability.mediaController" , title: "Which Media Controllers?", multiple: true, required: false
 input "momentary", "capability.momentary" , title: "Which momentary switches?", multiple: true, required: false
 input "musicplayer", "capability.musicPlayer" , title: "Which Music Players?", multiple: true, required: false
 input "power", "capability.powerMeter" , title: "Which power meters?", multiple: true, required: false 
 input "relayswitch", "capability.relaySwitch", title: "Which relay switches?", multiple: true, required: false 
 input "sleepsensor", "capability.sleepSensor", title: "Which sleep sensors?", multiple: true, required: false 


 }
}

mappings {

 path("/listhubs") {
 action: [
 GET: "listhubs"
 ]
 }
 path("/version") {
 action: [
 GET: "showVersion"
 ]
 } 
 path("/:deviceType") {
 action: [
 GET: "list"
 ]
 }
 path("/listdevices") {
 action: [
 GET: "listdevices"
 ]
 }
 path("/:deviceType/states") {
 action: [
 GET: "listStates"
 ]
 }
 path("/:deviceType/subscription") {
 action: [
 POST: "addSubscription"
 ]
 }
 path("/:deviceType/subscriptions/:id") {
 action: [
 DELETE: "removeSubscription"
 ]
 }
 path("/:deviceType/:id") {
 action: [
 GET: "show",
 PUT: "update"
 ]
 }
 path("/subscriptions") {
 action: [
 GET: "listSubscriptions"
 ]
 } 
}

def showVersion() {
 log.debug "[MillaLife v1.0]"
 }



def installed() {
 log.debug settings
}

def updated() {
 log.debug settings
}


def listhubs() {
	myhubs?.collect {
    	[hubId: it.id, hubName: it.name]
    } ?: []
}

def list() {
 def type = params.deviceType
 def devices = settings[type]
 devices?.collect{
     def cap = it.capabilities
     def attributeName = attributeFor(type)
     def s = it.currentState(attributeName)
 	 deviceState(it, s, type, cap)
 } ?: []
}



def listdevices() {
	def capabilityList = ["actuators", "carbonmonoxide", "colorcontrol", "configuration", "doorcontrol", "energymeter", "illuminance", "capture", "indicator", "locationmode", "lockcodes", "mediacontroller", "momentary", "musicplayer", "power", "relayswitch", "sleepsensor", "switches", "switchLevel", "motionSensors", "contactSensors", "presenceSensors", "temperatureSensors", "thermostats", "accelerationSensors", "waterSensors", "lightSensors", "humiditySensors", "alarms", "locks", "battery", "buttons"]
    def listarray = capabilityList.collect {
         def devices = settings[it]
         def type = it
		 def devicesarray = devices?.collect {
             def cap = it.capabilities
             def attributeName = attributeFor(type)
             def s = it.currentState(attributeName)
             log.debug "for ${type} adding ${it.displayName}"
             deviceState(it, s, type, cap)
         }
		 
		 if (devicesarray){ 
			devicesarray.removeAll([null])
			devicesarray ?: []  
         }
     } 
     listarray.removeAll([null])
     listarray = listarray.findAll { item -> !item.isEmpty() }
     listarray ?: []
     
}


def listStates() {
 log.debug "[String] states, params: ${params}"
 def type = params.deviceType
 def attributeName = attributeFor(type)
 settings[type]?.collect{deviceState(it, it.currentState(attributeName))} ?: []
}

def listSubscriptions() {
 state
}

def update() {
 def type = params.deviceType
 def data = request.JSON
 def devices = settings[type]
 def command = data.command
 def value = data.value
 log.debug "[String] update, params: ${params}, request: ${data}, devices: ${devices*.id}"
 if (command) {
 def device = devices?.find { it.id == params.id }
 log.debug "[String] Sending $command to $device"
if (!device) {
 httpError(404, "Device not found")
 } else {
	if (!value) {
	    device."$command"()
	} else {
    	if (command == "setLevel") {
			device."$command"(value.toInteger())
        } else {
        	device."$command"("$value")
        }
	}
}
 }
}

def show() {
 def type = params.deviceType
 def devices = settings[type]
 def device = devices.find { it.id == params.id }
 def cap = device.capabilities 
 log.debug "[String] show, params: ${params}, devices: ${devices*.id}"
 if (!device) {
 httpError(404, "Device not found")
 }
 else {
 def attributeName = attributeFor(type)
 def s = device.currentState(attributeName)
 deviceState(device, s, type, cap)
 }
}



def addSubscription() {
 log.debug "[String] addSubscription"
 def type = params.deviceType
 def data = request.JSON
 def attribute = attributeFor(type)
 //def devices = settings[attribute]
 def devices = settings[type]
 def deviceId = data.deviceId
 def callbackUrl = data.callbackUrl
 def device = devices.find { it.id == deviceId }

 log.debug "[PROD] addSubscription, params: ${params}, request: ${data}, device: ${device}"
 if (device) {
 log.debug "Adding switch subscription " + callbackUrl
 state[deviceId] = [callbackUrl: callbackUrl]
     if (attribute == "thermostat") {
         subscribe(device, "heatingSetpoint", deviceHandler, [filterEvents: false])
         subscribe(device, "coolingSetpoint", deviceHandler, [filterEvents: false])
         subscribe(device, "thermostatSetpoint", deviceHandler, [filterEvents: false])
		 subscribe(device, "thermostatMode", deviceHandler, [filterEvents: false])
         subscribe(device, "thermostatFanMode", deviceHandler, [filterEvents: false])
         subscribe(device, "thermostatOperatingState", deviceHandler, [filterEvents: false])        
		 log.debug "[String] Setting all thermostat mode subscriptions"
} else {

        subscribe(device, attribute, deviceHandler, [filterEvents: false])
    }
}
 log.info state

}


def removeSubscription() {
 def type = params.deviceType
 def attribute = attributeFor(type)
 //def devices = settings[attribute]
 def devices = settings[type]
 def deviceId = params.id
 def device = devices.find { it.id == deviceId }

 log.debug "[String] removeSubscription, params: ${params}, request: ${data}, device: ${device}"
 if (device) {
 log.debug "Removing $device.displayName subscription"
 state.remove(device.id)
 unsubscribe(device)
 }
 log.info state
}


def deviceHandler(evt) {
  def deviceInfo = state[evt.deviceId]

  if (deviceInfo) {
    
	def sonos = musicplayer.find { it.id == evt.deviceId }    
    if (sonos != null) { 
	    def sonosCurrentStatus = sonos.currentValue("status")
		def sonosCurrentVolume = sonos.currentState("level")?.integerValue
		def sonosCurrentTrack = sonos.currentState("trackData").jsonValue 
	 	httpPostJson(uri: deviceInfo.callbackUrl, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value, status: sonosCurrentStatus, level: sonosCurrentVolume, track: sonosCurrentTrack, data: evt.data]]) {
 			log.debug "[String] Sonos Event data successfully posted"
 		}
    }

 	def switchLevel = switchLevels.find {it.id == evt.deviceId}
 	if (switchLevel != null) {
	 	httpPostJson(uri: deviceInfo.callbackUrl, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value, level: switchLevel.currentValue("level"), data: evt.data]]) {
 			log.debug "[String] Switch Level Event data successfully posted"

		}
    }

	if (sonos == null && switchLevel == null) {
		httpPostJson(uri: deviceInfo.callbackUrl, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value, level: null, data: evt.data]]) {
 				log.debug "[String] Generic Event data successfully posted"
		}
	}
    
 } else {
 	log.debug "[String] No subscribed device found"
 }

}

private deviceItem(it) {
 it ? [id: it.id, label: it.displayName] : null
}

private deviceState(device, s, type, cap) {
 if (s && device.id && device.hub){
   device && s ? [id: device.id, name: device.displayName, st_name: s.name, value: s.value, level: device.currentValue("level"), unixTime: s.date.time, type: type, hubId: device.hub.id, hubName: device.hub.name, deviceType: cap.join(","), supportedAttributes: device.supportedAttributes.join(","), supportedCommands: device.supportedCommands.join(",")] : null
 }
}



private attributeFor(type) {
 switch (type) {
 case "switches":
 log.debug "[String] switch type"
 return "switch"
 case "thermostats":
 log.debug "[String] thermostat type"
 return "thermostat"
 case "locks":
 log.debug "[String] lock type"
 return "lock"
 case "alarms":
 log.debug "[String] alarm type"
 case "buttons":
 log.debug "[String] button type"
 return "button"
 case "lightSensors":
 log.debug "[String] illuminance type"
 return "illuminance"
 default:
 log.debug "[String] other sensor type"
 return type - "Sensors"
 }
}