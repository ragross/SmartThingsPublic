/**
 *  Samsung SmartSleep
 *
 *  Copyright 2015 Anat Oren
 *
 */
definition(
    name: "Samsung SleepSense (Connect)",
    namespace: "earlysense.com",
    author: "Anat Oren",
    description: "Samsung SleepSense (Connect) is an app, which is used by the SleepSense Sensor device. ",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Samsung/samsung-remote%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Samsung/samsung-remote%403x.png",
    oauth: [displayName: "Samsung SmartSleep", displayLink: ""])

preferences {
    section {
        input "thermostat", "capability.thermostat", title: "Select a thermostat", required: false,multiple: false
    }
}

mappings {
    path("/setState/:value/:mode/:fan") { action: [ POST: "setvalue", GET: "setvalue"] }
    path("/setSleepState/:state") { action: [ POST: "setSleepState", GET: "setSleepState"] }
    path("/setBedState/:state") { action: [ POST: "setBedState", GET: "setBedState"] }
    path("/deviceID") { action: [ POST: "getDeviceID", GET: "getDeviceID" ] }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
  unsubscribe()
	initialize()
}

def initialize() {
log.trace "initialize!!!!"
	createAccessToken()
    if (getSensors().size == 0) {
		log.debug "Creating child"
        def dni = "thing-${app.id}"
        addChildDevice("earlysense.com", "SleepSense Sensor", dni, null, [label:"SleepSense Sensor"])
        log.trace "Created thing with id $dni."
    }
}

def setvalue() {
	if(!thermostat){
    	log.trace "Set value method was called - no thermostat"
    	return
    }
    
    try{
   	 	setValueHelper(params)
    }
    catch(com.netflix.hystrix.exception.HystrixRuntimeException ex){
    	log.trace ex
   		setValueHelper()
    }

    return [status:"OK"]
}

def setValueHelper(params){
	log.debug "Set value to ${params.value} and mode ${params.mode}!"
	def Sensors = getSensors()
    def currentTempVal = params.value
    if(currentTempVal == "0"){
    	currentTempVal = "--"
    }
    
    Sensors.each {
        it.setTempAndMode(params.value, params.mode)
    }
    
    suggestTemp(params.value, params.mode)
}

def suggestTemp(temp,mode) {
  if(!thermostat){
  	return
  }

  log.trace "Suggested temperature: ${temp}, ${mode}!!"
  if(mode.contains("off")){
  	log.trace "turning off the device"
    thermostat.off()
    return
  }
  
  def thermostatLastMode = thermostat.latestState('thermostatMode').stringValue
  def value = temp as Double
  if(mode.contains("cool")) {
   	 	if(!thermostatLastMode.contains("cool")) {
         	log.trace "set cool mode";
         	thermostat.cool();
    	}
    	log.trace "set cooling point"
   		thermostat.setCoolingSetpoint(value)
	} else if(mode.contains("heat")) {
    	if(!thermostatLastMode.contains("heat")) {
        	log.trace "set heat mode"
       		thermostat.heat();
    	}
    	log.trace "set heating point";
    	thermostat.setHeatingSetpoint(value);
	}
}

def setSleepState(){
	try{
		doSetSleepState(params)
 	 }
     catch(com.netflix.hystrix.exception.HystrixRuntimeException ex){
    	log.trace ex
        doSetSleepState()
    }
  	return [status:"OK"]
}

def doSetSleepState(params){
	log.debug "Set sleep state to ${params.state}"
    def Sensors = getSensors()
   	Sensors.each{it.setSleepState(params.state)}
}

def setBedState(){
	try{
		doSetBedState(params)
 	 }
     catch(com.netflix.hystrix.exception.HystrixRuntimeException ex){
    	log.trace ex
        doSetBedState()
    }
  	return [status:"OK"]
}

def doSetBedState(params){
	log.debug "Set bed state to ${params.state}"
    def Sensors = getSensors()
    Sensors.each{it.setBedState(params.state)}
}

def getSensors() {
    def Sensors = getChildDevices()
	return Sensors
}

def getDeviceID(){
	def thermostatID = "empty"
    if(thermostat){
    	thermostatID = thermostat.deviceNetworkId
 		log.trace "getting deviceID ${thermostatID}"
    }
    
    return [deviceID : thermostatID]
}
