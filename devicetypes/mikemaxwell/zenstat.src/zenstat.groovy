/**
Zen Thermostat
*
Author: Zen Within
Date: 2015-02-21

change log:
Mike Maxwell 2015-04-28
--enabled thermostatOperatingState tile
--correctly mapped 0000 (idle) and 0004 (heating) modes
--change operatingMode and thermostatOperatingState icons
--added temp hack for floating point nonsense
Tim Slagle 2015-05-04
--removed "fronttile" value tile and replaced with temperature tile
--colored temperature tile based on current temp
--removed button toggles and added slider controls... makes for much better controls
--limited sliders... (currently there is a platform issue that fudges with the dsiplay of these... might remove later)
Patrick Stuart (admin) 2015-05-04
--removed extra spaces and testing merging in gitlab
--cleaned up formating for consistency
Tim Slagle 2015-05-04
--tightened up command delays.  I think i found the butter zone.  200ms for most....100ms for others.  Makes switching modes much quicker!
--commented some stuff that isn't needed at the moment. Moved to bottom of code but kept incase we need it again.
Tim Slagle 2015-05-23
--reintroduced working buttons and removed sliders to keep universal US/UK device type
--cleaned up delays more.
--fixed null active setpoint when thermostat mode is "off"... still shows degree symbol... but, better then "null"?
Mike Maxwell 2015-05-25
--Moved temperature settings into preferences, added updated method to handle changes.
--Removed tempertureUnt tile from device display
*/

metadata {
	// Automatically generated. Make future change here.
	definition (name: "zenStat", namespace: "MikeMaxwell", author: "ZenWithin") {
	capability "Actuator"
	//mm this needs to be enabled...
	capability "Temperature Measurement"
	capability "Thermostat"
	capability "Configuration"
	capability "Refresh"
	capability "Sensor"
	fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0004,0005,0020,0201,0202,0204,0B05", outClusters: "000A, 0019"
	attribute "temperatureUnit", "number"
	command "setpointUp"
	command "setpointDown"
	command "setCelsius"
	command "setFahrenheit"
	command "quickSetHeat"
	command "quickSetCool"
}

// simulator metadata
simulator { }

preferences{
	input name: "tempScale", type: "enum", title: "Thermostat temperature scale", description: "Scale", required: true, options:["Fahrenheit","Celsius"]	
}

tiles {
	valueTile("temperature", "device.temperature") {
		state("temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		)
	}
	standardTile("fanMode", "device.thermostatFanMode", decoration: "flat") {
		state "auto", action:"thermostat.setThermostatFanMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-auto"
		state "on", action:"thermostat.setThermostatFanMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-on"
	}
	standardTile("temperatureUnit", "device.temperatureUnit", decoration: "flat") {
		state "C", label: "°C", icon: "st.alarm.temperature.normal", action:"setFahrenheit"
		state "F",  label: "°F", icon: "st.alarm.temperature.normal", action:"setCelsius"
	}
		//mm changed icons
	standardTile("mode", "device.thermostatMode", decoration: "flat") {
		state "off", action:"thermostat.setThermostatMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.heating-cooling-off"
		state "cool", action:"thermostat.setThermostatMode", backgroundColor:"#90d0e8", icon:"st.thermostat.auto-cool"
		state "heat", action:"thermostat.setThermostatMode", backgroundColor:"#ff6e7e", icon:"st.thermostat.heat-auto"
		//state "auto", action:"thermostat.setThermostatMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.auto"
	}
	valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2) {
		state "off", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
		state "heat", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
		state "cool", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
	}
	valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) {
		state "heat", label:'${currentValue}° heat', unit:"F", backgroundColor:"#ffffff"
	}
	valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) {
		state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
	}
		//mm changed icons
	standardTile("thermostatOperatingState", "device.thermostatOperatingState", decoration: "flat") {
		state "heating", backgroundColor:"#ff6e7e", icon:"st.thermostat.heating"
		state "cooling",backgroundColor:"#90d0e8", icon:"st.thermostat.cooling"
		state "fan only",backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-on"
		state "idle",label:"idle", backgroundColor:"#ffffff"	//, icon:"st.thermostat.heating-cooling-off"
	}
	/*controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false,range:"(66..84)") {
		state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
	}
	controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false,range:"(66..84)") {
		state "setHeatingSetpoint", action:"quickSetCool", backgroundColor:"#d04e00"
	}
	*/
	standardTile("setpointUp", "device.thermostatSetpoint", decoration: "flat") {
		state "setpointUp", action:"setpointUp", icon:"st.thermostat.thermostat-up"
	}
	standardTile("setpointDown", "device.thermostatSetpoint", decoration: "flat") {
		state "setpointDown", action:"setpointDown", icon:"st.thermostat.thermostat-down"
	}
	
	standardTile("refresh", "device.temperature", decoration: "flat") {
		state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	standardTile("configure", "device.configure", decoration: "flat") {
		state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	}
	main "temperature"
	details(["temperature", "fanMode", "mode","thermostatSetpoint", "setpointUp", "setpointDown", "heatingSetpoint","coolingSetpoint", "thermostatOperatingState", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	//log.debug "Parse description $description"
	def map = [:]
	def activeSetpoint = "--"
	if (description?.startsWith("read attr -")) 
	{
		def descMap = parseDescriptionAsMap(description)
		// Thermostat Cluster Attribute Read Response
		if (descMap.cluster == "0201" && descMap.attrId == "0000") 
		{
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
			def receivedTemperature = map.value
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "001c") 
		{
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
			if (map.value == "cool") {
				activeSetpoint = device.currentValue("coolingSetpoint")
			} else if (map.value == "heat") {
				activeSetpoint = device.currentValue("heatingSetpoint")
			}
			sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0011") 
		{
			//log.debug "COOL SET POINT"
			map.name = "coolingSetpoint"
			map.value = getTemperature(descMap.value)
			if (device.currentState("thermostatMode")?.value == "cool") {
				activeSetpoint = map.value
				//log.debug "Active set point value: $activeSetpoint"
				sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
                sendEvent("name":"coolingSetpoint", "value":activeSetpoint)
			}
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0012") 
		{
			//log.debug "HEAT SET POINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
			if (device.currentState("thermostatMode")?.value == "heat") {
				activeSetpoint = map.value
				sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
                sendEvent("name":"heatingSetpoint", "value":activeSetpoint)
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "0029") 
		{
			//log.debug "OPERATING STATE"
            //log.info "val:${[descMap.value]}"
			map.name = "thermostatOperatingState"
			map.value = getOperatingStateMap()[descMap.value]
		}
		// Fan Control Cluster Attribute Read Response
		else if (descMap.cluster == "0202" && descMap.attrId == "0000") 
		{
			map.name = "thermostatFanMode"
			map.value = getFanModeMap()[descMap.value]
		} 
		else if (descMap.cluster == "0204" && descMap.attrId == "0000") 
		{
			map.name = "temperatureUnit"
			map.value = getTemperatureDisplayModeMap()[descMap.value]
		}
	}// End of Read Attribute Response
	def result = null
	if (map) {
		result = createEvent(map)
	}
	//log.debug "Parse returned $map"
	return result
}
// =============== Help Functions - Don't use log.debug in all these functins ===============
def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}
def getModeMap() { [
	"00":"off",
	"03":"cool",
	"04":"heat"
	]
}
def getOperatingStateMap() {
	[
	"0000":"idle",
	"0001":"heating",
	"0002":"cooling",
    "0004":"fan only",
	"0005":"heating",
	"0006":"cooling",
	"0008":"heating",
	"000C":"heating",
	"0010":"cooling",
	"0014":"cooling"
	]
}
def getFanModeMap() { 
	[
	"04":"on",
	"05":"auto"
	]
}
def getTemperatureDisplayModeMap() { 
	[
	"00":"C",
	"01":"F"
	]
}
def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16) / 100 as Double
	def returnValue = celsius
	//log.debug "Temperature value: $celsius"
	if(getTemperatureScale() == "F"){
		returnValue = (double)(celsiusToFahrenheit(celsius))
	}
	//mm quick hack for floating point nonsense, needs help, didn't completly fix the issue...
	return returnValue.round()
}
def getTemperatureScale(){
	return device.currentState("temperatureUnit")?.value
}

// =============== Setpoints ===============
def setpointUp() {
	def currentMode = device.currentState("thermostatMode")?.value
	def currentUnit = getTemperatureScale()
    if (currentMode == cool){
		// check if heating or cooling setpoint needs to be changed
		int nextLevel = device.currentValue("coolingSetpoint") + 1
		//log.debug "Next level: $nextLevel"
		// check the limits
		if(currentUnit == "C") {
			if (currentMode == "cool") {
				if(nextLevel > 36) {
					nextLevel = 36
				}
			} 
			else if (currentMode == "heat") {
				if(nextLevel > 32) {
					nextLevel = 32
				}
			}
		}
		else //in degF unit
		{
			if (currentMode == "cool") {
				if(nextLevel > 96) {
					nextLevel = 96
				}
			} else if (currentMode == "heat") {
				if(nextLevel > 89) {
					nextLevel = 89
				}	
			}
		}
		//log.debug "setpointUp() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
		quickSetCool(nextLevel)
    }
    else if (currentMode == heat){
		// check if heating or cooling setpoint needs to be changed
		int nextLevel = device.currentValue("heatingSetpoint") + 1
		//log.debug "Next level: $nextLevel"
		// check the limits
		if(currentUnit == "C") {
			if (currentMode == "cool") {
				if(nextLevel > 36) {
					nextLevel = 36
				}
			} 
			else if (currentMode == "heat") {
				if(nextLevel > 32) {
					nextLevel = 32
				}
			}
		}
		else //in degF unit
		{
			if (currentMode == "cool") {
				if(nextLevel > 96) {
					nextLevel = 96
				}
			} else if (currentMode == "heat") {
				if(nextLevel > 89) {
					nextLevel = 89
				}	
			}
		}
		//log.debug "setpointUp() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
		quickSetHeat(nextLevel)
    }
}
def setpointDown() {
	def currentMode = device.currentState("thermostatMode")?.value
	def currentUnit = getTemperatureScale()
	if (currentMode == cool){
		// check if heating or cooling setpoint needs to be changed
		int nextLevel = device.currentValue("coolingSetpoint") - 1
		// check the limits
		if (currentUnit == "C") {
			if (currentMode == "cool") {
				if(nextLevel < 8) {
					nextLevel = 8
				}
			} else if (currentMode == "heat") {
				if(nextLevel < 10) {
					nextLevel = 10
				}
			}
		}
		else  //in degF unit
		{
			if (currentMode == "cool") {
				if (nextLevel < 47) {
					nextLevel = 47
				}
			} else if (currentMode == "heat") {
				if (nextLevel < 50) {
					nextLevel = 50
				}
			}
		}
		//log.debug "setpointDown() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
   	 	quickSetCool(nextLevel)
   	 }
     else if (currentMode == heat){
		// check if heating or cooling setpoint needs to be changed
		int nextLevel = device.currentValue("heatingSetpoint") - 1
		// check the limits
		if (currentUnit == "C") {
			if (currentMode == "cool") {
				if(nextLevel < 8) {
					nextLevel = 8
				}
			} else if (currentMode == "heat") {
				if(nextLevel < 10) {
					nextLevel = 10
				}
			}
		}
		else  //in degF unit
		{
			if (currentMode == "cool") {
				if (nextLevel < 47) {
					nextLevel = 47
				}
			} else if (currentMode == "heat") {
				if (nextLevel < 50) {
					nextLevel = 50
				}
			}
		}
		//log.debug "setpointDown() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
   	 	quickSetHeat(nextLevel)
   	 }
}


//TS added quick set heat commands for slider controls and seperated setting of heat and cool lines 649-693
def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees)
}
def quickSetCool(degrees) {
	setCoolingSetpoint(degrees)
}
def setHeatingSetpoint(degrees) {
	def temperatureScale = getTemperatureScale()
	def currentMode = device.currentState("thermostatMode")?.value
	def degreesDouble = degrees as Double
	sendEvent("name":"heatingSetpoint", "value":degreesDouble)
    sendEvent("name":"thermostatSetpoint", "value":degreesDouble)
	//log.debug "New set point: $degreesDouble"
	def celsius = (getTemperatureScale() == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100) + "}"
}
def setCoolingSetpoint(degrees) {
	def temperatureScale = getTemperatureScale()
	def degreesDouble = degrees as Double
	sendEvent("name":"coolingSetpoint", "value":degreesDouble)
    sendEvent("name":"thermostatSetpoint", "value":degreesDouble)
	//log.debug "New set point: $degreesDouble"
	def celsius = (getTemperatureScale() == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	//log.debug "$celsius"
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius*100) + "}"
}
//TS commented out incase we want to add button toggles back in

// =============== Thermostat Mode ===============
def modes() {
	["off", "heat", "cool"]
}
def setThermostatMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def index = modeOrder.indexOf(currentMode)
	def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
	//log.debug "setThermostatMode - switching from $currentMode to $next"
	"$next"()
}
def setThermostatMode(String value) {
	"$value"()
}
def off() {
	sendEvent("name":"thermostatMode", "value":"off")
    sendEvent("name":"thermostatSetpoint", "value":"--")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {00}"
}
def cool() {
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	//log.debug "Cool set point: $coolingSetpoint"
	sendEvent("name":"thermostatMode", "value":"cool")
	sendEvent("name":"thermostatSetpoint","value":coolingSetpoint)
	[
	  "st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {03}", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x11"
	]
}
def heat() {
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	//log.debug "Heat set point: $heatingSetpoint"
	sendEvent("name":"thermostatMode","value":"heat")
	sendEvent("name":"thermostatSetpoint","value":heatingSetpoint)
	[
	  "st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {04}", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x12"
	]
}
// =============== Fan Mode ===============
def setThermostatFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	def returnCommand
	switch (currentFanMode) {
	case "auto":
	  returnCommand = on()
	  break
	case "on":
	  returnCommand = auto()
	  break
	}
	if(!currentFanMode) { 
	  returnCommand = auto() 
	}
	//log.debug "setThermostatFanMode - switching from $currentFanMode to $returnCommand"
	returnCommand
}
def setThermostatFanMode(String value) {
	"$value"()
}
def on() {
	fanOn()
}
def fanOn() {
	sendEvent("name":"thermostatFanMode", "value":"on")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {04}"
}
def auto() {
	fanAuto()
}
def fanAuto() {
	sendEvent("name":"thermostatFanMode", "value":"auto")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}
// =============== Display Unit - degF or degC ===============
def setFahrenheit() {
	// Update Display Unit 
	sendEvent("name":"temperatureUnit", "value":"F")
	// send zigbee command
	[
	"raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100",
	"st wattr 0x${device.deviceNetworkId} 1 0x204 0 0x30 {01}", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 100",
	"raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100"
	]
}
def setCelsius() {
	// Update Display Unit 
	sendEvent("name":"temperatureUnit", "value":"C")
	// send zigbee command
	[
		"raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100",
	"st wattr 0x${device.deviceNetworkId} 1 0x204 0 0x30 {00}", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 100",
	"raw 0x0020 {11 00 02 1C 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100"
	]
}
// =============== SmartThings Default Fucntions: refresh, configure, poll ===============
def refresh() {
	//log.debug "refresh() - update attributes "
	[
	//Set long poll interval to 2 qs
	"raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100",
	//This is sent in this specific order to ensure that the temperature values are received after the unit/mode
	"st rattr 0x${device.deviceNetworkId} 1 0x204 0", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x1C", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x201 0x29", "delay 100",
	"st rattr 0x${device.deviceNetworkId} 1 0x202 0", "delay 100",
	//Set long poll interval to 28 qs (7 seconds)
	"raw 0x0020 {11 00 02 1C 00 00 00}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 100"
	]
}
def configure() {
	//log.debug "configure() - binding & attribute report"
	[
	  //Set long poll interval to 2 qs
	  "raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  //Bindings for Thermostat and Fan Control
	  "zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}", "delay 100",
	  "zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}", "delay 100",
	  "zdo bind 0x${device.deviceNetworkId} 1 1 0x204 {${device.zigbeeId}} {}", "delay 100",
	  //Thermostat - Configure Report
	  "zcl global send-me-a-report 0x201 0 0x29 1 300 {6400}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  "zcl global send-me-a-report 0x201 0x0011 0x29 1 300 {6400}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  "zcl global send-me-a-report 0x201 0x0012 0x29 1 300 {6400}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  "zcl global send-me-a-report 0x201 0x001C 0x30 1 300 {}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  "zcl global send-me-a-report 0x201 0x0029 0x19 1 300 {}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  //Fan Control - Configure Report
	  "zcl global send-me-a-report 0x202 0 0x30 1 300 {}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  //Thermostat UI - Read Attribute Report (attribute not reportable)
	  "st rattr 0x${device.deviceNetworkId} 1 0x204 0", "delay 100",
	  //Update values
	  "st rattr 0x${device.deviceNetworkId} 1 0x204 0", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x1C", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x201 0x29", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x202 0", "delay 100",
	 //Set long poll interval to 28 qs (7 seconds)
	  "raw 0x0020 {11 00 02 1C 00 00 00}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100"
	]
}
//def poll()
//{
	// leave it out because it will kill the battery
//}
private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}    

def updated() {
	def tScale = settings.tempScale ?: 'Fahrenheit'
    switch (tScale) {
		case "Fahrenheit":
			setFahrenheit()
			break
		case "Celsius":
			setCelsius()
		break
	}

}

