metadata {
	definition (name: "aeonDualSwitch", namespace: "MikeMaxwell", author: "mike maxwell") {
		capability "Switch"
		//capability "Refresh"
		//capability "Configuration"
		capability "Actuator"
		capability "Sensor"
		command "on1" 
		command "off1"
		command "on2"
        command "off2"
            
		fingerprint deviceId: "0x1001", inClusters: "0x25,0x31,0x32,0x27,0x70,0x85,0x72,0x86,0x60,0xEF,0x82" , outClusters: "0x82"
        //0 0 0x1001 0 0 0 b 0x25 0x31 0x32 0x27 0x70 0x85 0x72 0x86 0x60 0xEF 0x82
	}
	preferences {
       	input name: "param80", type: "enum", title: "State change notice:", description: "Type", required: true, options:["Off","Hail","Report"]
        input name: "param120", type: "enum", title: "Set trigger mode:", description: "Switch type", required: true, options:["Momentary","Toggle","Three Way"]
    }

	// tile definitions
	tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: false) {
				state "on", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#87CEFA"
                state "off", action: "switch.on", icon: "st.thermostat.fan-off", backgroundColor: "#ffffff"
		}
        standardTile("switch1", "device.switch", canChangeIcon: false) {
				state "on1", label: 'on', action: "off1",icon: "st.custom.buttons.add-icon", backgroundColor: "#87CEFA"
				state "off1", label: 'off', action: "on1",icon: "st.custom.buttons.add-icon", backgroundColor: "#ffffff"
		}
        standardTile("switch2", "device.switch", canChangeIcon: false) {
				state "on2", label: 'on', action: "off2",icon: "st.custom.buttons.add-icon", backgroundColor: "#87CEFA"
				state "off2", label: 'off', action: "on2",icon: "st.custom.buttons.add-icon", backgroundColor: "#ffffff"
		}
        
		main(["switch"])
		details(["switch","switch1","switch2","refresh"])
	}
}


// parse events into attributes
def parse(String description) {
   //log.debug "Parsing:${description}"

    def result = null
    def cmd = zwave.parse(description, [0x60:3, 0x25:1, 0x70:1, 0x72:1])
    if (cmd) {
        result = createEvent(zwaveEvent(cmd))
    }

    return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in or don't know about
    log.debug "udf:${cmd.inspect()}"
	return [:]
}
def zwaveEvent(physicalgraph.zwave.commands.switchallv1.SwitchAllReport cmd) {
	log.debug "${cmd.inspect()}"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "basicReport:${cmd.inspect()}"
    def value = cmd.value
	//log.info "power:${value ? "on" : "off"}"
    if (value == 0) sendEvent(name: "switch2", value: "off")
    else sendEvent(name: "switch2", value: "low")
    return [name				: "switch"
        	,value				: cmd.value ? "on" : "off"
    	]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def ep = cmd.sourceEndPoint
    def map = [:]
    if (cmd.commandClass == 37){
    	map << [ name: "switch" ]
		if (cmd.parameter == [0]) {
          map.value = "on${ep}"
        }
        if (cmd.parameter == [255]) {
            map.value = "off${ep}"
        }
    }
	return map
}

def on(){
    zwave.switchAllV1.switchAllOn().format()
}
def off(){
    zwave.switchAllV1.switchAllOff().format()
}
def on1() {
  	delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1, commandClass:37, command:2).format()
    ],500)
}
def off1() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1, commandClass:37, command:2).format()
  ],500)
}
def on2() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2, commandClass:37, command:2).format()
    ],500)
}

def off2() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2, commandClass:37, command:2).format()
  ],500)
}

def refresh() {
	delayBetween([
		zwave.basicV1.basicGet().format(),
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:1, commandClass:37, command:2).format(),
    	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2, commandClass:37, command:2).format()
	],500)
}

//def configure() {
//	def cmds = [
//		zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2]).format(),
//		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1]).format()
//	]
//	delayBetween(cmds) + "delay 5000" + refresh()
//}


//capture preference changes
def updated() {
    //log.debug "before settings: ${settings.inspect()}, state: ${state.inspect()}" 
    
    //get requested reporting preferences
    Short p80
    switch (settings.param80) {
		case "Off":
			p80 = 0
            break
		case "Hail":
			p80 = 1
            break
		default:
			p80 = 2	//Report
            break
	}    
    
	//get requested switch function preferences
    Short p120
    switch (settings.param120) {
		case "Momentary":
			p120 = 0
            break
		case "Three Way":
			p120 = 2
            break
		default:
			p120 = 1	//Toggle
            break
	}    
  
	//update if the settings were changed
    if (p80 != state.param80)	{
    	//log.debug "update 80:${p80}"
        state.param80 = p80 
        return response(zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [p80]).format())
    }
	if (p120 != state.param120)	{
    	//log.debug "update 120:${p120}"
        state.param120 = p120
        return response(zwave.configurationV1.configurationSet(parameterNumber: 120, size: 1, configurationValue: [p120]).format())
    }

	//log.debug "after settings: ${settings.inspect()}, state: ${state.inspect()}"
}

