/**
 *  SharpTools companion SmartApp for the SmartThings platform
 *  Author: Josh Lyon
 *
 */

/*
   Follow the instructions at: https://bitbucket.org/joshua_lyon/smartthings-tools/wiki/Alpha%20Instructions
 */

definition(
        name: "SharpTools",
        namespace: "Boshdirect",
        author: "Josh Lyon",
        description: "Enables the API needed for the SharpTools Android app by Boshdirect.",
        category: "SmartThings Labs",
        iconUrl: "https://sites.google.com/a/boshdirect.com/sharptools/logo_72.png",
        iconX2Url: "https://sites.google.com/a/boshdirect.com/sharptools/logo_144.png",
        oauth: [displayName: "SharpTools Beta", displayLink: "http://boshdirect.com/smartthings-tools"])


preferences {
    section("Allow SharpTools Tools to Control These Things...") {
        //SmartThings prefers the use of capability rather than device:
        //    http://docs.smartthings.com/en/latest/smartapp-developers-guide/preferences-and-settings.html#preferences-data-types
        //TODO: Updated inputs to match Logitech Harmony filtered out for devices I haven't tested.
        input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
        input "thermostats", "capability.thermostat", title: "Which thermostats?", multiple: true, required: false
        input "medias", "capability.mediaController", title: "Which media controllers?", multiple: true, required: false
        input "musics", "capability.musicPlayer", title: "Which music players?", multiple: true, required: false
        input "speeches", "capability.speechSynthesis", title: "Which speech synthesizers?", multiple: true, required: false
        input "colors", "capability.colorControl", title: "Which color controls?", multiple: true, required: false
        input "valves", "capability.valve", title: "Which valves?", multiple: true, required: false
        input "contacts", "capability.contactSensor", title: "Which contact sensors?", multiple: true, required: false
        input "waters", "capability.waterSensor", title: "Which water sensors?", multiple: true, required: false
        input "presences", "capability.presenceSensor", title: "Which presence sensors?", multiple: true, required: false
        input "temperatures", "capability.temperatureMeasurement", title: "Which temperature sensors?", multiple: true, required: false        
    }
}

mappings {
    //-----Device Endpoints-------
    path("/devices") {
        action: [
                GET: "listDevices"
        ]
    }

    path("/devices/:id") {
        action: [
                GET: "getDevice",
                PUT: "updateDevice"
        ]
    }

    //------Phrase Endpoints--------
    path("/phrases") {
        action: [
                GET: "listPhrases"
        ]
    }
    path("/phrases/:id") {
        action: [
                PUT: "executePhrase"
        ]
    }

    //------Subscription Endpoints---------
    path("/subscriptions") {
        action: [
                GET: "listSubscriptions",
                POST: "addSubscription" // {"deviceId":"xxx", "attributeName":"xxx","regID":"registrationID"}
        ]
    }
    path("/subscriptions/:id") {
        action: [
                DELETE: "removeSubscriptions",
                POST: "updateSubscriptions" //{"regID": "registrationID"} //update all subscriptions with :id to regID
        ]
    }
    path("/subscriptions/:id/:regID/:attributeName"){
        action: [
                DELETE: "removeSubscriptions"
        ]
    }
    path("/mode"){
        action: [
                GET: "getMode",
                POST: "changeMode"
        ]
    }

    path("/unsub/:id"){
        action: [
                GET: "unsub"
        ]
    }

    path("/unsub"){ //{deviceId: asdf, attributeName: asdf}
        action: [
                POST: "unsub"
        ]
    }


    path("/subscriptions2"){
        action: [
                GET: "listSubscriptions2"
        ]
    }

    //--------State-----------
    path("/state"){
        action: [
                GET: "listState"
        ]
    }
}



//------------Installation/Update/Initialization-------------
def installed() {
    log.debug "Installed with settings: ${settings}"

    //initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
}

//-------------Mode Actions-----------------
def getMode(){
    def mode = location.mode
    def modes = location.modes
    log.debug "Current mode is ${mode}"
    log.debug "Available modes: ${modes}"
    [
            "currentMode": mode,
            "availableModes": modes.collect{ it.name }
    ]
}
def changeMode(){
    def mode = request.JSON.mode
    if (location.modes?.find{it.name == mode}) {
        setLocationMode(mode)
        log.debug "Setting mode to ${mode}"
    }
    else {
        log.debug "Requested mode of ${mode} was not found"
        httpError(400, "Mode not found")
    }
}


//-------------Device Actions----------------
def listDevices() {
    log.debug "getDevices, params: ${params}"
    allDevices.collect {
        deviceItem(it)
    }
}

def getDevice() {
    log.debug "getDevice, params: ${params}"
    def device = allDevices.find { it.id == params.id }
    if (!device) {
        //render status: 404, data: '{"msg": "Device not found"}'
        httpError(404, "Device not found")
    } else {
        deviceItem(device)
    }
}

def updateDevice() {
    def data = request.JSON
    def command = data.command
    def arguments = data.arguments

    log.debug "updateDevice, params: ${params}, request: ${data}"
    if (!command) {
        httpError(400, "Command is required")
    } else {
        def device = allDevices.find { it.id == params.id }
        if (device) {
            if(command == "toggle"){
                toggleDevice(device)
            }
            else{
                if (arguments) {
                    device."$command"(*arguments)
                } else {
                    device."$command"()
                }
            }
        } else {
            httpError(404, "Device not found")
        }
    }
}

def toggleDevice(device){
    if(device.currentValue('switch') == "on")
        device.off();
    else
        device.on();
}

private getAllDevices() {
    //contactSensors + presenceSensors + temperatureSensors + accelerationSensors + waterSensors + lightSensors + humiditySensors
    ([] + switches + motions + locks + alarms + thermostats + medias + musics + speeches + colors + valves + contacts + waters + presences + temperatures)?.findAll()?.unique { it.id }
}



//------------- Device Details ------------------
private deviceItem(device) {
    [
            id: device.id,
            label: device.displayName,
            currentStates: device.currentStates,
            capabilities: device.capabilities?.collect {[
                    name: it.name
            ]},
            attributes: device.supportedAttributes?.collect {[
                    name: it.name,
                    dataType: it.dataType,
                    values: it.values
            ]},
            commands: device.supportedCommands?.collect {[
                    name: it.name,
                    arguments: it.arguments
            ]},
            type: [
                    name: device.typeName,
                    author: device.typeAuthor
            ]
    ]
}



//-------------------- Phrases -----------------------
def listPhrases() {
    location.helloHome.getPhrases()?.collect {[
            id: it.id,
            label: it.label
    ]}
}

def executePhrase() {
    log.debug "executedPhrase, params: ${params}"
    location.helloHome.execute(params.id)
    //render status: 204, data: "{}"
}



//----------------Subscription methods------------------------
def listSubscriptions() {
    log.debug "listSubscriptions()"
    app.subscriptions?.collect { //.findAll { it.device?.device && it.device.id }?
        def deviceInfo = state[it.deviceId ? it.deviceId : "location"]
        def regIDs = deviceInfo ? deviceInfo["${it.data}"] : null
        def response = [
                id: it.id,
                deviceId: it.deviceId ? it.deviceId : "location",
                attributeName: it.data,
                handler: it.handler,
                regIDs: regIDs ?: []
        ]


        response
    } ?: []
}

def listSubscriptions2() {
    log.debug "listSubscriptions2()"
    log.debug "app: ${app}"
    app.subscriptions
}

def listState() {
    state
}

//POST /subscriptions {"deviceId":"xxx", "attributeName":"xxx","regID":"registrationID"}
def addSubscription() {
    def data = request.JSON
    def attribute = data.attributeName
    def regID = data.regID

    log.debug "addSubscription, params: ${params}, request: ${data}"
    if (!attribute) {
        httpError(400, "attributeName is required")
    } else {
        def stateKey = ""
        def device = null;
        if(data.deviceId == "location" && attribute == "mode"){
            stateKey = "location"
        }
        else{
            device = allDevices.find { it.id == data.deviceId }
            log.debug "Setting stateKey to deviceId: ${device.id}"
            stateKey = device.id
        }
        if (stateKey != "") {
            if(state[stateKey] == null){ //will wipe out any previous states for this device
                log.debug "State did not exist. Adding."
                state[stateKey] = ["$attribute": [ regID ]]
            }
            else if(state[stateKey]["$attribute"] == null){
                log.debug "State exists, but attribute does not. Adding."
                state[stateKey]["$attribute"] = [ regID ]
            }
            else if(state[stateKey]["$attribute"].contains(regID)){
                httpError(400, "Registration already exists")
            }
            else{
                log.debug "Adding registration ID: $regID"
                state[stateKey]["$attribute"].add(regID) // = [regID: regID]
            }

            log.debug "state[key]: ${state[stateKey]}"
            def attrVal = state[stateKey]["$attribute"]
            log.debug "state[key][attr]: ${attrVal}"

            def subscription = null;
            def response = [];
            if(stateKey == "location"){
                log.debug "Adding subscription for location"
                subscription = subscribe(location, locationHandler)
                log.debug "subscription: ${subscription}"
                if (!subscription || !subscription.eventSubscription) {
                    subscription = app.subscriptions?.find{ it.locationId && it.locationId == location.id && it.data == attribute && it.handler == 'locationHandler' }
                    if(!subscription || !subscription.eventSubscription){
                        response = [
                                id: "pending",
                                deviceId: "location",
                                attributeName: "mode",
                                handler: 'locationHandler',
                                regID: regID
                        ]
                    }else{
                        log.debug "subToResponse: ${subscription}"
                        log.debug "id: ${subscription.id}"
                        response = subToResponse(subscription, regID)
                    }
                }
            }
            else{
                log.debug "Adding subscription for device ${device}"
                subscription = subscribe(device, attribute, deviceHandler)
                if (!subscription || !subscription.eventSubscription) {
                    subscription = app.subscriptions?.find { it.deviceId && it.deviceId == stateKey && it.data == attribute && it.handler == 'deviceHandler' }
                    if(!subscription || !subscription.eventSubscription){
                        response = [
                                id: "pending",
                                deviceId: stateKey,
                                attributeName: attribute,
                                handler: 'deviceHandler',
                                regID: regID
                        ]
                    }
                }
                else{
                	log.debug "subToResponse: ${subscription}"
                    log.debug "id: ${subscription.id}"
                	response = subToResponse(subscription, regID)
                }
            }


            response
        } else {
            httpError(404, "Device not found")
        }
    }
}

def subToResponse(subscription, regID){
    if(!subscription){
        []
    }else{
        [
                id: subscription.id,
                deviceId: subscription.deviceId ? subscription.deviceId : ( (subscription.device && subscription.device.toString() != "null") ? subscription.device.id : "location"),
                attributeName: subscription.data,
                handler: subscription.handler,
                regID: regID
        ]
    }
}

def unsub(){ //{deviceId: asdf, attributeName: asdf}
    def deviceId = request.JSON.deviceId
    def attribute = request.JSON.attributeName
    def device = allDevices.find { it.id == deviceId }

    if(params.id){
        log.debug "Using handler unsubscribe"
        def subscription = app.subscriptions?.find { it.id == params.id }
        unsubscribe(subscription.handler)
        subscription
    }
    else if(device && attribute){
        log.debug "Using device+attribute specific unsubscribe"
        unsubscribeAttr(device, attribute)
    }
    else{
        log.error "device ${device} attr ${attribute} deviceId ${deviceId}"
    }
}

//called from the web-service endpoint with the URL Path parsed params (eg. HTTP DELETE /subscription/:id
def removeSubscriptions() {
    def subscription = app.subscriptions?.find { it.id == params.id }
    def device = subscription?.device
    def regID = params.regID
    if(!regID)
        regID = request.JSON.regID
    def attribute = params.attributeName
    if(!attribute)
        attribute = request.JSON.attributeName

    log.debug "removeSubscription, params: ${params}, subscription: ${subscription}, device: ${device}"
    if (!device) {
        log.debug "Subscription or Device not found"
        log.debug "Subscription ID: ${params.id}"
        httpError(400, "Subscription not found with id: ${params.id}")
    }
    else{
        def stateKey = subscription.deviceId ? subscription.deviceId : "location"
        log.debug "Removing subscription for item/attribute ${stateKey}/${attribute}"
        if(state[stateKey]){
            log.debug "Current Registration IDs: ${state[stateKey]}"
            if(regID == null){
                log.debug "regID was null; unsubscribing event"
                state.remove(stateKey)
                if(stateKey == "location")
                    unsubscribe('locationHandler')
                else
                    unsubscribeAttr(device, attribute)
            }
            else if(state[stateKey]["$attribute"] != null){
                log.debug "state[${stateKey}][${attribute}] = ${state[stateKey]["$attribute"]}"
                if(state[stateKey]["$attribute"].contains(regID)){
                    log.debug "Removing registration ID: ${regID}"
                    state[stateKey]["$attribute"].remove(regID)
                    log.debug "Current Registration IDs for item/state: ${state[stateKey]["$attribute"]}"
                    if(state[stateKey]["$attribute"].size() == 0){
                        if(stateKey == "location"){
                            log.debug "No more subscriptions for location/${attribute}. Removing sub-state."
                            unsubscribe('locationHandler')
                        }
                        else{
                            log.debug "No more subscriptions for ${device}/${attribute}. Removing sub-state."
                            unsubscribeAttr(device, attribute)
                        }
                        state[stateKey].remove(attribute)
                        if(state[stateKey].size() == 0){
                            log.debug "No more subscriptions for item. Removing state."
                            state.remove(stateKey)
                        }
                    }
                }
                else{
                    log.debug "RegID not found; Other RegIDs still in use - not unsubscribing."
                    httpError(400, "RegID not found")
                }
            }
            else{
                log.debug "Passed RegID, but State[deviceID][attribute] is null; Unsubscribing"
                if(stateKey == "location")
                    unsubscribe('locationHandler')
                else
                    unsubscribe(device, attribute)
            }
        }
        else{
            log.debug "but State[stateKey] is null; Unsubscribing device"
            if(stateKey == "location")
                unsubscribe('locationHandler')
            else
                unsubscribe(device)
        }
    }
}

//{"regID": "registrationID"} //update all subscriptions with :id to regID
def updateSubscriptions(){
    //get the oldID from the URL /subscriptions/:id --> params.id
    def old_regID = params.id;
    //get the newID from the JSON request { regID: registrationID }
    def new_regID = request.JSON.regID
    log.debug "Updating Old ID to New ID: \r\n ${old_regID} \r\n -->\r\n     ${new_regID}"

    state.each{ device, deviceValue ->
        deviceValue.each{ attr, attrValue ->
            if(attrValue.contains(old_regID)){
                attrValue.remove(old_regID)
                attrValue.add(new_regID)
            }
        }
    }
}


// ----------- Event Handlers ----------------
def locationHandler(evt){
    deviceHandler(evt)
}

def deviceHandler(evt) {
    log.debug "event triggered for $evt.name on $evt.deviceId"
    def stateKey = evt.deviceId ? evt.deviceId : "location";
    def deviceInfo = state[stateKey]
    log.debug "device info: $deviceInfo"
    if (deviceInfo) {
        if (deviceInfo[evt.name]) {
            sendToGCM(evt, deviceInfo[evt.name])
        } else {
            log.warn "No registrations for device: ${stateKey}"
        }
    } else {
        log.warn "No subscribed device found for device: ${stateKey}"
    }
}

def sendToGCM(evt, regIDs){
    def params = [
            uri: "https://android.googleapis.com",
            path: "/gcm/send",
            headers: [
                    "Host": host,
                    "Content-Type": "application/json",
                    "Authorization": "key=AIzaSyAXvB1JDWJ2moz1hKyVrUbb2-c2qAk55Zc"
            ],
            body: [
                    registration_ids: regIDs,
                    data: [
                            source: evt.source,
                            thingID: evt.deviceId ? evt.deviceId : "none",
                            locationId: evt.locationId,
                            name: evt.name,
                            value: evt.value
                    ]
            ]
    ]
    httpPostJson(params) {
        log.debug "Event data successfully posted"
        log.debug "RegIDs triggered: ${regIDs}"
    }
}

def unsubscribeAttr(device, attributeName){
    log.debug "unsubscribing ${device}/${attributeName}"
    def subscriptions = app.subscriptions?.findAll { it.deviceId  && it.deviceId == device.id && it.data != attributeName }
    unsubscribe(device)
    subscriptions.each{
        log.debug "leaving ${it.device}/${it.data} with handler ${it.handler}"
        subscribe(it.device, it.data, it.handler)
    }
}