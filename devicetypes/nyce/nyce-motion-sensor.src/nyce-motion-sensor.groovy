/**
 *  NYCE Motion Sensor
 *
 *  Copyright 2015 NYCE Sensors Inc.
 *
 *	File: nyce-motion-sensor.groovy
 *	Version: v1.0.0
 *	Last Edited: 7 May 2015
 *	By: RC
 *
 */
 
metadata {
	definition (name: "NYCE Motion Sensor", namespace: "NYCE", author: "NYCE") {
		capability "Battery"
		capability "Configuration"
		capability "Motion Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		
		command "enrollResponse"
		
		fingerprint inClusters: "0000,0001,0003,0020,0402,0405,0406,0500", manufacturer: "NYCE", model: "3041"
	}
	
	simulator {
		
	}
	
	tiles {
		standardTile("battery_state", "device.battery_state") {
			state("ok", label:'Battery OK', icon:"st.Appliances.appliances17", backgroundColor:"#40dd00")
			state("low", label:'Battery Low', icon:"st.Appliances.appliances17", backgroundColor:"#ffcc00")
			state("failed", label:'Battery Failed', icon:"st.Appliances.appliances17", backgroundColor:"#cc0022")
		}
		
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'Occupied', icon:"st.motion.motion.active", backgroundColor:"#ffa81e")
			state("inactive", label:'Vacant', icon:"st.motion.motion.inactive", backgroundColor:"#79b821")
		}
		
		valueTile("temperature", "device.temperature") {
			state("temperature", label:'${currentValue}${unit}', unit:'°', icon:"st.Weather.weather2", backgroundColor:"#00adc6")
		}
		
		valueTile("humidity", "device.humidity") {
			state("humidity", label:'${currentValue}${unit}', unit:'%', icon:"st.Weather.weather12", backgroundColor:"#00adc6")
		}
		
		main (["motion"])
		details(["motion","battery_state","temperature","humidity"])
	}
}
 
def parse(String description) {
	Map map = [:]
	
	List listMap = []
	List listResult = []
	
	log.debug "parse: Parse message: ${description}"
	
	if (description?.startsWith("enroll request")) {
		List cmds = enrollResponse()
		
		log.debug "parse: enrollResponse() ${cmds}"
		listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	else {
		if (description?.startsWith("zone status")) {
			listMap = parseIasMessage(description)
		}
		else if (description?.startsWith("read attr -")) {
			map = parseReportAttributeMessage(description)
		}
		else if (description?.startsWith("catchall:")) {
			map = parseCatchAllMessage(description)
		}
		else if (description?.startsWith("temperature:")) {
			map = parseTemperatureMessage(description)
		}
		else if (description?.startsWith("humidity: ")) {
			map = parseHumidityMessage(description)
		}
		
		// Create events from map or list of maps, whichever was returned
		if (listMap) {
			for (msg in listMap) {
				listResult << createEvent(msg)
			}
		}
		else if (map) {
			listResult << createEvent(map)
		}
	}
	
	log.debug "parse: listResult ${listResult}"
	return listResult
}
 
private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	
	if (shouldProcessMessage(cluster)) {
		switch(cluster.clusterId) {
			default:
				break
		}
	}
	
	return resultMap
}
 
private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
							 cluster.command == 0x0B ||
							 cluster.command == 0x07 ||
							 (cluster.data.size() > 0 && cluster.data.first() == 0x3e)
							 
	return !ignoredMessage
}
 
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param -> def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Map resultMap = [:]
	
	log.debug "parseReportAttributeMessage: descMap ${descMap}"
	
	switch(descMap.cluster) {
		default:
			break
	}
	
	return resultMap
}
 
private Map parseTemperatureMessage(String description) {
	Map resultMap = [:]
	
	def tempC = Float.parseFloat((description - "temperature: ").trim())
	
	resultMap = getTemperatureResult(getConvertedTemperature(tempC))
	
	log.debug "parseTemperatureMessage: Temp resultMap: ${resultMap}"
	
	return resultMap
}
 
private Map parseHumidityMessage(String description) {
	Map resultMap = [:]
	
	def hum = Float.parseFloat((description - "humidity: " - "%").trim()).round()
	
	resultMap = getHumidityResult(hum)
	
	log.debug "parseHumidityMessage: Hum resultMap: ${resultMap}"
	
	return resultMap
}
 
def getConvertedTemperature(value) {
	if(getTemperatureScale() == "C"){
		return value.toDouble().round()
	}
	else {
		return celsiusToFahrenheit(value).toDouble().round()
	}
}
 
private Map getTemperatureResult(value) {
	return [
		name: "temperature",
		value: value,
		unit: "°" + getTemperatureScale()
	]
}
 
private Map getHumidityResult(value) {
	return [
		name: "humidity",
		value: value,
		unit: "%RH"
	]
}
 
private List parseIasMessage(String description) {
	List parsedMsg = description.split(" ")
	String msgCode = parsedMsg[2]
	
	List resultListMap = []
	Map resultMap_battery = [:]
	Map resultMap_battery_state = [:]
	Map resultMap_sensor = [:]
	
	// Relevant bit field definitions from ZigBee spec
	def BATTERY_BIT = ( 1 << 3 )
	def TROUBLE_BIT = ( 1 << 6 )
	def SENSOR_BIT = ( 1 << 1 )		// it's ALARM2 bit from the ZCL spec
	
	// Convert hex string to integer
	def zoneStatus = Integer.parseInt(msgCode[-4..-1],16)
	
	log.debug "parseIasMessage: zoneStatus: ${zoneStatus}"
	
	// Check each relevant bit, create map for it, and add to list
	log.debug "parseIasMessage: Battery Status ${zoneStatus & BATTERY_BIT}"
	log.debug "parseIasMessage: Trouble Status ${zoneStatus & TROUBLE_BIT}"
	log.debug "parseIasMessage: Sensor Status ${zoneStatus & SENSOR_BIT}"
	
	resultMap_battery_state.name = "battery_state"
	if (zoneStatus & TROUBLE_BIT) {
			resultMap_battery_state.value = "failed"
			
			resultMap_battery.name = "battery"
			resultMap_battery.value = 0
		}
		else {
		if (zoneStatus & BATTERY_BIT) {
			resultMap_battery_state.value = "low"
			
			// to generate low battery notification by the platform
			resultMap_battery.name = "battery"
			resultMap_battery.value = 15
		}
		else {
			resultMap_battery_state.value = "ok"
			
			// to clear the low battery state stored in the platform
			// otherwise, there is no notification sent again
			resultMap_battery.name = "battery"
			resultMap_battery.value = 80
		}
	}
	resultMap_sensor.name = "motion"
	resultMap_sensor.value = (zoneStatus & SENSOR_BIT) ? "active" : "inactive"
	
	resultListMap << resultMap_battery_state
	resultListMap << resultMap_battery
	resultListMap << resultMap_sensor
	
	return resultListMap
}
 
def refresh()
{
	log.debug "refresh: Refreshing Temperature, Humidity"
	
	[
		"st rattr 0x${device.deviceNetworkId} 1 0x0402 0x0",
		"st rattr 0x${device.deviceNetworkId} 1 0x0405 0x0"
	]
}
 
def configure() {
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	
	def configCmds = [
		// Writes CIE attribute on end device to direct reports to the hub's EUID
		"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
		
		// Set binding for temperature and humidity (use default settings)
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0402 {${device.zigbeeId}} {}",
		
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0405 {${device.zigbeeId}} {}"
	]
	
	log.debug "configure: Write IAS CIE, Binding, Read Temp/Hum"
	return configCmds + refresh()		// send refresh cmds as part of config
}
 
def enrollResponse() {
	[
		// Enrolling device into the IAS Zone
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1"
	]
}
 
private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
 
private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}
 
private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	
	return array
}
