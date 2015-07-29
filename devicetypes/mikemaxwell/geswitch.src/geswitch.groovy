/* GE driver 1.0
 *
 *
 *	--auto re-configure after setting preferences
 *	
 *
 * Includes:
 *	preferences tile for setting:
 * 		indicator led 
 *		paddle reverse
 *		
 * Mike Maxwell
 * madmax98087@yahoo.com
 * 2014-12-13
 *
	change log
     
	GE 45609 
	0x20 Basic
	0x25 Switch Binary
	0x70 Configuration 
	0x72 Manufacturer Specific
	0x73 Powerlevel
	0x77 Node Naming
*/

metadata {
	definition (name: "geSwitch",namespace: "MikeMaxwell", author: "mmaxwell") {
		capability "Actuator"
		capability "Switch"
        capability "Sensor"
   		capability "Refresh"
    	capability "Polling"
        fingerprint deviceId: "0x1001", inClusters: "0x25, 0x27, 0x73, 0x70, 0x86, 0x72, 0x77"
	}
  	preferences {
       	input name: "param3", type: "enum", title: "Indicator LED", description: "Type", required: true, options:["On","Off"]
        input name: "param4", type: "enum", title: "Reverse paddle functions?", description: "Yes if switch was installed upside down.", required: true, options:["Yes","No"]
        input name: "toggle", type: "enum", title: "Use toggle mode?", description: "Momentary operation.", required: true, options:["Yes","No"] 
		//indicator param3, 1:when on, 0:when off	2 never
		//on position param4 0:on is top 1: off is top
		//
    }
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', backgroundColor:"#79b821"
			state "turningOff", label:'${name}', backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main(["switch"])
        details(["switch","refresh"])
	}
}


def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
    //log.debug "cmd:${cmd.inspect()}"
	if (cmd.hasProperty("value")) {
		result = createEvent(zwaveEvent(cmd))
    }
    //log.debug "res:${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//log.debug "basicReport:${cmd.inspect()}"
    return [name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in or don't know about
    //log.debug "udf:${cmd.inspect()}"
	return [:]
}

def on() {
    delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.basicV1.basicGet().format(),zwave.basicV1.basicGet().format()], 100)
}

def off() {
    delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.basicV1.basicGet().format(),zwave.basicV1.basicGet().format()], 100)
}

def poll() {
	//return zwave.configurationV1.configurationGet(parameterNumber: 4).format()
}

def refresh() {
	return zwave.basicV1.basicGet().format()
}

//capture preference changes
def updated() {
    //log.debug "before settings: ${settings.inspect()}, state: ${state.inspect()}" 
    
    //get requested reporting preferences
    Short p3
    switch (settings.param3) {
		case "Off":
			p3 = 0
            break
		default:
			p3 = 1	//on
            break
	}    
    
	//get requested switch function preferences
    Short p4
    switch (settings.param4) {
		case "No":
			p4 = 0
            break
		default:
			p4 = 1	//Yes
            break
	}    
  
	//update if the settings were changed
    if (p3 != state.param3)	{
    	//log.debug "update 3:${p3}"
        state.param3 = p3
        return response(zwave.configurationV1.configurationSet(configurationValue: [p3], parameterNumber: 3, size: 1).format())
    }
	if (p4 != state.param4)	{
    	//log.debug "update 4:${p4}"
        state.param4 = p4
        return response(zwave.configurationV1.configurationSet(configurationValue: [p4], parameterNumber: 4, size: 1).format())
    }

	//log.debug "after settings: ${settings.inspect()}, state: ${state.inspect()}"
}