/**
 *	Philips TV
 *
 *	Author: Geurt Wisselink
 *	Date: 2014-11-29
 */
 
import groovy.json.JsonSlurper
 
 // for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Philips TV Switch", namespace: "Wiscon", author: "Geurt Wisselink") {
		capability "Switch"
		capability "Polling"
		capability "Refresh"

        // Custom attributes
        attribute "serialNumber", "string" 
        attribute "softwareVersion", "string"
		attribute "model", "string"
		attribute "volumeSetpoint", "string"
		attribute "pollState", string
		attribute "networkAddress", "string"

		command "volumeUp"
        command "volumeDown"
		command "subscribe"
		command "resubscribe"
		command "unsubscribe"
	}

	// simulator metadata
	simulator {}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.Electronics.electronics18", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.Electronics.electronics18", backgroundColor:"#ffffff", nextState:"off"
			state "turningOff", label:'${name}', icon:"st.Electronics.electronics18", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("volume", "device.volumeSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}', unit:""
        }
        standardTile("volumeUp", "device.volumeSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Volume Up', icon:"st.custom.buttons.add-icon", action:"volumeUp"
        }
        standardTile("volumeDown", "device.volumeSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Volume Down', icon:"st.custom.buttons.subtract-icon", action:"volumeDown"
        }
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}', height: 1, width: 2, inactiveLabel: false
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}
		valueTile("model", "device.model", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}		
		main "switch"
		details (["switch", "refresh", "volume", "volumeUp", "volumeDown", "networkAddress", "model", "serialNumber"])
	}
}

// parse events into attributes
def parse(String description) {
	TRACE("Parsing '${description}'")

	def map = stringToMap(description)
	def headerString = new String(map.headers.decodeBase64())

	def events = []

	if (map.body) {
		//device is turned on
		if (getDataValue("pollState") != "on") {
			TRACE("parse: oldPollState: ${getDataValue("pollState")} - A message was received, device is  ON")
			updateDataValue("pollState", "on")
			// this is /1/system response
			events << createEvent(name: "switch", value: "on")
			TRACE("parse: newPollState: ${getDataValue("pollState")}")
		}		
		
		def bodyString = new String(map.body.decodeBase64())
		
		if (bodyString.startsWith("<")) {
			if (bodyString.contains("<body>Ok</body>")) {
				// this is POST response - ignore
				TRACE("parseData\n${bodyString}")
			}

		} else {
			def tvstat = new JsonSlurper().parseText(bodyString)
			
			if (tvstat.containsKey("serialnumber")) {
				def evSerialNumber = [name: "serialNumber", value:  tvstat.serialnumber]
				events << createEvent(evSerialNumber)
			}
			if (tvstat.containsKey("softwareversion")) {
				def evSoftwareVersion = [name: "softwareVersion", value:  tvstat.softwareversion]
				events << createEvent(evSoftwareVersion)
			}
			if (tvstat.containsKey("model")) {
				def evModel = [name: "model", value:  tvstat.model]
				events << createEvent(evModel)
			}
			if (tvstat.containsKey("current")) {
				events << createEvent(name: "volumeSetpoint", value: tvstat.current)
			}
		}
	}

	TRACE("events: ${events}")
	events
}

////////////////////////////
private getTime() {
	// This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
	((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def ip = getDataValue("ip")
	def port = getDataValue("port")
	def controlPort = getDataValue("controlPort")
	
	if (!ip || !controlPort) {
		def parts = device.deviceNetworkId.split(":")
		if (parts.length == 2) {
			ip = parts[0]
			controlPort = parts[1]
		} else {
			log.warn "Can't figure out ip and port for device: ${device.id}"
		}
	} else {
		device.deviceNetworkId = "${ip}:${controlPort}"
	}
	
	String networkAddress = convertHexToIP(ip) + ":" + convertHexToInt(controlPort)
	if (networkAddress != getDataValue("networkAddress")) {
		sendEvent([name:"networkAddress", value:networkAddress])
	}
	
	TRACE("Using ip: ${networkAddress} for device: ${device.id}, networkId: ${device.deviceNetworkId}")
	return networkAddress
}

////////////////////////////
def on() {
	TRACE("Executing 'on' - does nothing")
}

////////////////////////////
def off() {
	TRACE("Executing 'off'")
	def json = "{\"key\": \"Standby\"}"
	sendEvent([name:"key", value:"Standby"])
	
	def sendKey = apiPost( "/1/input/key", json)
	sendKey
}

def volumeUp() {
    TRACE("volumeUp()")
	def json = "{\"key\": \"VolumeUp\"}"
	sendEvent([name:"key", value:"VolumeUp"])
		
	def sendKey = [
        apiPost( "/1/input/key", json),
        delayHubAction(2000),
        apiGet("/1/audio/volume")
    ]
	sendKey
}

def volumeDown() {
    TRACE("volumeDown()")
	def json = "{\"key\": \"VolumeDown\"}"
	sendEvent([name:"key", value:"VolumeDown"])
	
	def sendKey = [
        apiPost( "/1/input/key", json),
        delayHubAction(2000),
        apiGet("/1/audio/volume")
    ]
	sendKey
}

////////////////////////////
def refresh() {
	TRACE("Executing Philips TV  'refresh'")
	
	if (getDataValue("pollState") != "on") {
		TRACE("refresh: oldPollState: ${getDataValue("pollState")} - second time - assume device is OFF")
		sendEvent([name:"switch", value:"off"])
	}
	updateDataValue("pollState", "poll")
	sendEvent([name:"refresh", value:"on"])
	TRACE("refresh: newPollState: ${getDataValue("pollState")}")
	
	def systemInfo = [
        apiGet( "/1/system"),
        apiGet("/1/audio/volume")
    ]
	systemInfo
}

////////////////////////////
def subscribe(hostAddress) {
	TRACE("subscribe: "+hostAddress)
}

def subscribe() {
	subscribe(getHostAddress())
}

def subscribe(ip, port) {
	def existingIp = getDataValue("ip")
	def existingPort = getDataValue("port")

	if (ip && ip != existingIp) {
		log.debug "Updating ip from $existingIp to $ip"
		updateDataValue("ip", ip)
	}
	if (port && port != existingPort) {
		log.debug "Updating port from $existingPort to $port"
		updateDataValue("port", port)
	}

	subscribe("${ip}:${port}")
}

////////////////////////////
def resubscribe() {
	TRACE("Executing 'resubscribe()'")

}

////////////////////////////
def unsubscribe() {
	TRACE("unsubscribe")
}

def poll() {
	TRACE( "Executing 'poll'")
	refresh()
}

private apiGet(String path) {
    log.debug "apiGet(${path})"

    def headers = [
        HOST:       getHostAddress(),
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

    return new physicalgraph.device.HubAction(httpRequest)
}

private apiPost(String path, data) {
    log.debug "apiPost(${path}, ${data})"

    def headers = [
        HOST:       getHostAddress(),
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'POST',
        path:       path,
        headers:    headers,
        body:       data
    ]

    return new physicalgraph.device.HubAction(httpRequest)
}

private def delayHubAction(ms) {
    return new physicalgraph.device.HubAction("delay ${ms}")
}

private def TRACE(message) {
    log.debug message
}