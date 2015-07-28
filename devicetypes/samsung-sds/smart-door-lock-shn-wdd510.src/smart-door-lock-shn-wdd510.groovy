/**
 *  Smart Door Lock SHN-WDD510
 *  
 *  author : SamsungSDS
 *  Date : 2015-01-22
 *  Capabilities : Actuator, Alarm, Battery, Configuration, Lock, Refresh, Temperature Measurement
*/


metadata {	
	definition (name: "Smart Door Lock SHN-WDD510", namespace: "Samsung SDS", author: "Samsung SDS") {
		capability "Actuator"
        capability "Lock"
		capability "Refresh"
        capability "Battery"
        capability "Temperature Measurement"
        capability "Configuration"
        
		fingerprint profileId: "0104", inClusters: "0000, 0001, 0002, 0003, 0004, 0005, 0009, 0020, 0101, 0B05", outClusters: "000A, 0019" 
	}
    
	simulator {
		// status messages
		status "unlocked": "lock/unlock: 1"
		status "locked": "lock/unlock: 0"

		// reply messages
        //reply "st cmd 0x${device.deviceNetworkId} 1 0x0101 1 {}" : "lock/unlock: 1"
       
	}

	// UI tile definitions
	tiles {
		standardTile("lock", "device.lock", width: 2, height: 2, canChangeIcon: true) {
			state "locked", label: 'locked', action: "lock.unlock", icon: "st.locks.lock.locked", backgroundColor: "#3DB8DC", nextState:"unlocking"
			state "unlocked", label: 'unlocked', action: "lock.lock", icon: "st.locks.lock.unlocked", backgroundColor: "#F65F47", nextState:"locking"
			state "locking", label: 'closing', icon: "st.locks.lock.locked", backgroundColor: "#F65F47"
			state "unlocking", label: 'opening', icon: "st.locks.lock.unlocked", backgroundColor: "#3DB8DC"            
		}
		standardTile("refresh", "device.lock", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery',unit:""
		}
        valueTile("temperature", "device.temperature") {
			state("temperature", label:'${currentValue}°', 
            backgroundColors:[
					[value: 40, color: "#026582"],
					[value: 59, color: "#1196BC"],
					[value: 86, color: "#1FBA77"],
					[value: 95, color: "#FA6B1C"],
					[value: 99, color: "#F33D1F"]					
				]
			)
		}
        
		main "lock"
		details(["lock", "refresh", "battery" , "temperature"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "[parse] Parse description $description"
    def results = []
    
    if (description?.startsWith("read attr -")) {
    		results = parseReportAttributeMessage(description)
        } 
       	else if (description?.startsWith('catchall:')) {
        	results = parseCatchAllMessage(description)
        }
        else if (description?.startsWith("lock/unlock:")) {
        	def name = "lock"
        	def value = description?.endsWith(" 1") ? "on" : "off"
            sendEvent(name: name, value: value)          
        }
}

private Map parseCatchAllMessage(String description) {
	def linkText = getLinkText(device)
    def results = [:]
    def cluster = zigbee.parse(description)
    log.trace cluster    
   
    if (shouldProcessMessage(cluster)) {    	
       	if(cluster.clusterId == 0x0001) {
        	results << createEvent(getBatteryResult(cluster.data.last()))
        }
        else if(cluster.clusterId == 0x0009){
        	if(cluster.data[0] == 0x05){
                log.debug "[parseCatchAllMessage] Tamper Alert"	
                sendEvent(name: "alarm", value: "siren",descriptionText : "${linkText} Damage Detected")
            }        
        }
        else if(cluster.clusterId == 0x0101){
            log.debug cluster.data[1]        
        	switch(cluster.data[1]){
            	case 0x01:	//OP_EV_LOCK
                case 0x07:	//OP_EV_ONE_TOUCH_LOCK           
                case 0x08:	//OP_EV_KEY_LOCK                 
                case 0x0A:	//OP_EV_AUTO_LOCK                
                case 0x0B:	//OP_EV_SCHEDULE_LOCK            
                case 0x0D:	//OP_MANUAL_LOCK
                    	sendEvent(name: "lock", value: "locked", descriptionText : "${linkText} locked")
                    break
                case 0x02:	//OP_EV_UNLOCK
                case 0x09:	//OP_EV_KEY_UNLOCK
                case 0x0C:	//OP_EV_SCHEDULE_UNLOCK          
                case 0x0E:	//OP_MANUAL_UNLOCK               
                    	sendEvent(name: "lock", value: "unlocked", descriptionText : "${linkText} unlocked")
                    break
			}                   
    	}
    }
    return resultMap
}

private boolean shouldProcessMessage(cluster) {
    // 0x0B is default response indicating message got through
    // 0x07 is bind message
    //log.debug "[shouldProcessMessage] cluster : ${cluster}"
    boolean ignoredMessage = cluster.profileId != 0x0104 || 
        cluster.command == 0x0B ||
        cluster.command == 0x07 ||
        (cluster.data.size() > 0 && cluster.data.first() == 0x3e)
    return !ignoredMessage
}

def parseReportAttributeMessage(String description) {
	def linkText = getLinkText(device)
	log.debug "[parseReportAttributeMessage] Read attr: $description"
    Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
    	def nameAndValue = param.split(":")
    	map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
	
	def results = []
	if (descMap.cluster == "0101" && descMap.attrId == "0000") {
    	log.debug "[parseReportAttributeMessage] Received Lock Status"
        
        if(descMap.value == "00" || descMap.value == "01"){
        	log.debug "Lock is closed."
        	//results = createEvent(name : 'lock' , values : "locked" , descriptionText : "Closed Lock.")
            sendEvent(name: 'lock', value: "locked",display: true, descriptionText : "${linkText} Locked")             
        }
        else if(descMap.value == "02") {
        	log.debug "Lock is open."
        	//results = createEvent(name : 'lock' , values : "unlocked" , display : true , descriptionText : "Lock is open")  
            sendEvent(name: 'lock', value: "unlocked",display: true, descriptionText : "${linkText} Unlocked")
        }
      }else if (descMap.cluster == "0001" && descMap.attrId == "0020") {
      	log.debug "[parseReportAttributeMessage] Received Battery Level Report"
        results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
      }else if (descMap.cluster == "0002"){
      	log.debug "[parseReportAttributeMessage] Received Temperature "
      	def value = getTemperature(descMap.value)
      	results = createEvent(getTemperatureResult(value))
      }
           
      return results
}

private Map getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
    def result = [
    	name: 'battery'
    ]
    
	def volts = rawValue / 10
	def descriptionText
	if (volts > 6.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 4.0
    	def maxVolts = 6.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
       	result.descriptionText = "${linkText} battery is ${result.value}%"
        log.debug "${linkText} battery is ${result.value}%"
	}
	return result
}

def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16)    
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

private Map getTemperatureResult(value) {
	//log.debug 'Temperature'
	def linkText = getLinkText(device)
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def descriptionText = "${linkText} is ${value}°${temperatureScale}"
    log.debug "${descriptionText}"
	return [
		name: 'temperature',
		value: value,
        descriptionText: descriptionText
	]
}

// Commands to device
def lock() {
	log.debug "[lock] lock()"
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0101 0 {}"
}

def unlock() {
	log.debug "[unlock] unlock()"
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0101 1 {}"
}

def refresh() {
	log.debug "[refresh] Lock Status / Battery Level / Temperature "
    def refreshCmds = [
    "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0101 0x0000","delay 300",
    "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0001 0x0020","delay 300",
    "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0002 0x0000"]
    
    log.debug refreshCmds
    return refreshCmds
}

def updated()
{
	
}

def configure() {
		log.debug "Configuring Reporting, IAS CIE, and Bindings."
		def configCmds = [
        "zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500", 
        
        "zcl global send-me-a-report 1 0x20 0x20 600 3600 {01}", "delay 200",
		"send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",

		"zcl global send-me-a-report 0 0 0x29 300 3600 {6400}", "delay 200",
		"send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",        
		
        "zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x101 {${device.zigbeeId}} {}"			
		]
        
		log.debug configCmds         
        log.debug "Confuguring Finish"
        return configCmds + refresh()
}


private getEndpointId() 
{
	new BigInteger(device.endpointId, 16).toString()
}


