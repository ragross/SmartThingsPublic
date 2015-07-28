metadata {
	// Automatically generated. Make future change here.
	definition (name: "SNode_Ember", namespace: "Samsung_DIL", author: "doowoong") {
    	capability "Switch"
    	// added capabilities       
		fingerprint profileId: "0104", deviceId : "0002", inClusters: "0000,0006"
	}

	// UI tile definitions
	tiles {
        standardTile("led", "device.switch",width: 2, height : 2, canChangeIcon: true, canChangeBackground: true) {
        	state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#798821"
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }    
		main "led"
		details (["led"])
	}
    
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value && value != "ping" ? "response" : null
    
    if (value?.contains("sw!")) {
    	name = "switchstatus"
        if(value?.contains("on")){ value = "on"}
        else{value = "off"}
    }    
   
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
    
	return result
}

// Commands to device

def on() {
	"zcl on-off on"
}

def off() {
	"zcl on-off off"
}
