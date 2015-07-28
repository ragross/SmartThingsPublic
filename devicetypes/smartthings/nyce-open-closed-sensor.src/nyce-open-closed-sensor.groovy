/**
 *  NYCE Open/Close Sensor
 *
 *  Copyright 2014 SmartThings
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
 
metadata {
	definition (name: "NYCE Open/Closed Sensor", namespace: "smartthings", author: "SmartThings") {
    	capability "Battery"
		capability "Configuration"
        capability "Contact Sensor"
		capability "Refresh"
        
        command "enrollResponse"
 
 
		fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3011"
        fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3011"
        fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3014"
        fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3014"
	}
 
	simulator {
 
	}
 
	tiles {
    	standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
    	
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
 
		main (["contact"])
		details(["contact","battery","refresh"])
	}
}
 
def parse(String description) {
	log.debug "description: $description"
    
	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('zone status')) {
    	map = parseIasMessage(description)
    }
 
	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : null
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}
 
private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) {
        switch(cluster.clusterId) {
            case 0x0001:
                log.debug 'Battery'
                resultMap.name = 'battery'
                log.debug "battery value: ${cluster.data.last()}"
                resultMap.value = getBatteryPercentage(cluster.data.last())
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

private int getBatteryPercentage(int value) {
    def minVolts = 2.1
    def maxVolts = 3.0
    def volts = value / 10
    def pct = (volts - minVolts) / (maxVolts - minVolts)
    return (int) pct * 100
}
 
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Battery"
		resultMap.name = "battery"
		resultMap.value = getBatteryPercentage(Integer.parseInt(descMap.value, 16))
	}
	else if (descMap.cluster == "0406" && descMap.attrId == "0000") {
		log.debug "occupancy not currently handled, using IAZ Zone for state change"
		resultMap.name = "occupancy"
	}
 
	return resultMap
}
 

private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]
    
    Map resultMap = [:]
    switch(msgCode) {
        case '0x0030': // Closed/No Motion/Dry
            log.debug 'Contact Status'
            resultMap.name = 'contact'
            resultMap.value = 'closed'
            break

        case '0x0031': // Open/Motion/Wet
            log.debug 'Contact Status'
            resultMap.name = 'contact'
            resultMap.value = 'open'
            break

        case '0x0032': // Tamper Alarm
            break

        case '0x0033': // Battery Alarm
            break

        case '0x0034': // Supervision Report
        	log.debug 'Contact Status'
            resultMap.name = 'contact'
            resultMap.value = 'closed'
            break

        case '0x0035': // Restore Report
        	log.debug 'Contact Status'
            resultMap.name = 'contact'
            resultMap.value = 'open'
            break

        case '0x0036': // Trouble/Failure
            break

        case '0x0038': // Test Mode
            break
    }
    return resultMap
}
 
def refresh()
{
	log.debug "Refreshing Temperature and Battery"
	[
		"st rattr 0x${device.deviceNetworkId} 1 1 0x20"

	]
}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting, IAS CIE, and Bindings."
	def configCmds = [
    	// Writes CIE attribute on end device to direct reports to the hub's EUID
		"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        //Configure reporting for battery voltage level
        "zcl global send-me-a-report 1 0x20 0x20 0x3600 0x3600 {01}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
       	//Set binding for battery voltage level
		"zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}"
	]
    return configCmds + refresh() + enrollResponse() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
    [	
    //Enrolling device into the IAS Zone	
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
