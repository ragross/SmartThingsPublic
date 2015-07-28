/**
 *  Backyard Anemometer  
 * 
 *	Goto http://solar-current.com/BackyardAnemometer.html for the latest documentation.
 *
 *  Copyright 2014 John.Rucker@Solar-Current.com
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
	definition (name: "Backyard Anemometer", namespace: "JohnRucker", author: "John.Rucker@Solar-Current.com") {
        capability "Refresh"
        capability "Polling"
        capability "Sensor"
        capability "Configuration"
        capability "Contact Sensor"
        
        command "setOffWindSpeed"
        command "setOnWindSpeed"        
        command "setOffDelayTime"        
        command "setOnDelayTime" 
        command "resetHiLow"
         
        attribute "windSpeed","number"
        attribute "maxWind","number"
        attribute "minWind","number"
        attribute "offWindSpeed","number" 
        attribute "onWindSpeed","number"
        attribute "minOnDelay","number"
        attribute "minOffDelay","number"

    	fingerprint profileId: "0104", inClusters: "0000, 000C", outClusters: "0006"
        //fingerprint outClusters: "02 000D 0006", inClusters: "01 0000", endpointId: "38", deviceId: "0002", profileId: "0104", deviceVersion: "00"
    
	}

	// simulator metadata
	simulator {
    }

	// UI tile definitions
	tiles {   
		valueTile("windSpeed", "device.windSpeed", width: 2, height: 2) {state("windSpeed", label:'${currentValue}', 
				backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 4, color: "#1e9cbb"],
					[value: 5, color: "#90d2a7"],
					[value: 9, color: "#44b621"],
					[value: 10, color: "#f1d801"],
					[value: 29, color: "#d04e00"],
					[value: 30, color: "#bc2323"]
				]
			)
		}
		valueTile("max", "device.maxWind", decoration: "flat", inactiveLabel: false) {state("maxWind", label:'${currentValue} Hi')}
		valueTile("min", "device.minWind", decoration: "flat", inactiveLabel: false) {state("minWind", label:'${currentValue} Lo')}      
        
        valueTile("offWind", "device.offWindSpeed", decoration: "flat", height: 1, width: 3, inactiveLabel: false) {state("offWindSpeed", label:'Use above slider to set Off speed. Currently set to ${currentValue} mph.')}
        controlTile("offWindSlider", "device.offWindSpeed", "slider", height: 1, width: 3, inactiveLabel: false) {
        state "offWindSpeed", action:"setOffWindSpeed", backgroundColor:"#d04e00"}
        
        valueTile("onWind", "device.onWindSpeed", decoration: "flat", height: 1, width: 3, inactiveLabel: false) {state("onWindSpeed", label:'Use above slider to set on speed. Currently set to ${currentValue} mph.')}
        controlTile("onWindSlider", "device.onWindSpeed", "slider", height: 1, width: 3, inactiveLabel: false) {
        state "onWindSpeed", action:"setOnWindSpeed", backgroundColor:"#d04e00"}
        
        valueTile("onDelay", "device.minOnDelay", decoration: "flat", height: 1, width: 3, inactiveLabel: false) {state("minOnDelay", label:'Use above slider to set on delay. Currently set to ${currentValue} minutes before sending On command.')}
        controlTile("onDelaySlider", "device.minOnDelay", "slider", height: 1, width: 3, inactiveLabel: false) {
        state "minOnDelay", action:"setOnDelayTime", backgroundColor:"#d04e00"}        
        
        valueTile("offDelay", "device.minOffDelay", decoration: "flat", height: 1, width: 3, inactiveLabel: false) {state("minOffDelay", label:'Use above Slide to set off delay. Currently set to ${currentValue} minutes before sending Off command.')}
        controlTile("offDelaySlider", "device.minOffDelay", "slider", height: 1, width: 3, inactiveLabel: false) {
        state "minOffDelay", action:"setOffDelayTime", backgroundColor:"#d04e00"}    
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("resetHiLow", "device.resetHiLow", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Reset Hi/Low', action:"resetHiLow", icon:"st.secondary.refresh-icon"
		}
        
        standardTile("contact", "device.contact", width: 1, height: 1) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
        
		main "windSpeed"
		details (["windSpeed", "max", "min", "onWindSlider", "onWind", "offWindSlider", "offWind", "onDelaySlider", "onDelay", "offDelaySlider", "offDelay", "refresh", "resetHiLow", "contact"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "Parse description $description"
    def name = null
    def value = null
    if (description?.startsWith("read attr -")) {
        def descMap = parseDescriptionAsMap(description)
        log.debug "Read attr: $description"
        if (descMap.cluster == "000C" && descMap.attrId == "0055") {
            name = "windSpeed"
            value = getFPoint(descMap.value)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0045") { 
            name = "minWind"
            value = getFPoint(descMap.value)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0041") { 
            name = "maxWind"
            value = getFPoint(descMap.value)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0401") { 
            name = "offWindSpeed"
            value = getFPoint(descMap.value)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0402") { 
            name = "onWindSpeed"
            value = getFPoint(descMap.value)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0403") { 
            name = "minOnDelay"
            value = Integer.parseInt(descMap.value, 16)
        } else if (descMap.cluster == "000C" && descMap.attrId == "0404") { 
            name = "minOffDelay"
            value = Integer.parseInt(descMap.value, 16)    
        }
                   
    } else if (description?.startsWith("catchall: 0104 0006 38")) {
        log.debug "On/Off command received"
        name = "contact"
        value = description?.endsWith(" 01 00 0000") ? "open" : "closed"
    }
    
    def result = createEvent(name: name, value: value)
    log.debug "Parse returned ${result?.descriptionText}"
    return result
}


def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

private getFPoint(String FPointHex){						// Parsh out hex string from Value: 4089999a
    Long i = Long.parseLong(FPointHex, 16)					// Convert Hex String to Long
    Float f = Float.intBitsToFloat(i.intValue())			// Convert IEEE 754 Single-Precison floating point
    log.debug "converted floating point value: ${f}"
    def result = f

    return result
}


// Commands to device
def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0006 0x1 {}"
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0006 0x0 {}"
}

def poll(){
	log.debug "Poll is calling refresh"
	refresh()
}

def refresh() {
	log.debug "sending refresh command"
    def cmd = []
	// "st rattr 0x${device.deviceNetworkId} 0x38 0x0006 0"	// Read Destination EP 0x38, Cluster 0x0006 Attribute ID 0x0000 (On / Off value)
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0055"   // Read Current Value 
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0401"	// Read Off Wind Speed Threshold
    cmd << "delay 150"    
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0402"	// Read on Wind Speed Threshold
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0403"	// Read on Minute Delay 
    cmd << "delay 150"    
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0404"	// Read on Minute Delay 
    
    cmd
}

def setOffWindSpeed(value){
	log.debug "Setting off wind speed to ${value} MPH."
    float xFloat = value																					// Convert value to Single Float
    int xBits = Float.floatToIntBits(xFloat)																// Convert single to bits
      
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0401 0x39 {${Integer.toHexString(xBits)}}"	// Write Off Wind Speed
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0401"										// Read Off Wind Speed Threshold   
    cmd    
}

def setOnWindSpeed(value){
	log.debug "Setting on wind speed to ${value} MPH."
    float xFloat = value																					// Convert value to Single Float
    int xBits = Float.floatToIntBits(xFloat)																// Convert single to bits
      
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0402 0x39 {${Integer.toHexString(xBits)}}"	// Write On Wind Speed
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0402"										// Read On Wind Speed Threshold   
    cmd       
}

def setOnDelayTime(value){
	log.debug "Setting on delay time to ${value} minutes."
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0403 0x20 {${Integer.toHexString(value)}}"	// Write On Delay Time
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0403"										// Read on Delay Time   
    cmd        
}

def setOffDelayTime(value){
	log.debug "Setting off delay time to ${value} minutes."
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0404 0x20 {${Integer.toHexString(value)}}"	// Write Off Delay Time
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0404"										// Read off Delay Time   
    cmd        
}

def resetHiLow(){
	log.debug "Sending reset wind Hi and Low command."  
    
    sendEvent(name: "maxWind", value: "0")
    sendEvent(name: "minWind", value: "0")  
    
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0400 0x20 {0}"	// Send Command 0 (Reset Wind Hi and Low values)  
    cmd  
}

def configure() {
	log.debug "Configuring reporting for attribute 0x0055 of the Analog cluster" 
    log.debug "Binding SEP 0x38 DEP 0x01 Cluster 0x000D Analog cluster to hub"         
        def configCmds = [
        "zcl global send-me-a-report 0x000C 0x0055 0x39 0x3C 0x384 {3f800000}", "delay 200",
        "send 0x${device.deviceNetworkId} 0x01 0x38", "delay 1500",


        "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x000C {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x0006 {${device.zigbeeId}} {}", "delay 1500",        
		]
    log.info "Sending ZigBee Configuration Commands to Backyard Anemometer 2"
    return configCmds + refresh()
	// Notes for Report Configuration Commands:
  	//									Cluster. 0x000C is the Analog cluster
  	//									|	   Attribuite to report on. 0x0055 is the present value attribute for analog cluster
  	//									|      |      Attribute Data Type. 0x39 is a single precision 4 byte IEEE 754 value
  	//									|	   |      |    Minimum reporting interval in seconds. 0x3C = 60 seconds or 1 minute
  	//									|	   |      |    |    Maximum reporting interval in seconds. 0x384 = 900 seconds or 15 minutes
  	//									|      |      |    |    |    Reportable change 0x3f800000 is IEEE 754 for 1.0
  	//									|      |      |    |    |    |
 	//   "zcl global send-me-a-report 0x000C 0x0055 0x39 0x3C 0x384 {3f800000}"	
  	//										 Source End Point
  	//										 |    Destination End Point
  	//										 |    |
  	//   "send 0x${device.deviceNetworkId} 0x01 0x38"
	}






