/**
 *  Notification for Samsung SDS Smart Doorlock
 *
 *  Copyright 2015 Samsung SDS
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
definition(
    name: "Notification for Samsung SDS Smart Doorlock",
    namespace: "Samsung SDS",
    author: "Samsung SDS",
    description: "Get a push notification",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	  section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Receive push notifications?", metadata:[values:["Yes","No"]], required:false
    }
    
    // What door should this app be configured for?    
    section ("Choose one or more, when...") {
    	input "doorOpen", "capability.lock", title: "Door open", required: false, multiple: true
    	input "doorClosed", "capability.lock", title: "Door closed", required: false, multiple: true
    	input "tamperAlert", "capability.alarm",title: "Damage detected", required: false,multiple: true
    }

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
	subscribe(doorOpen, "lock.unlocked", openContactHandler)
    subscribe(doorClosed, "lock.locked", closeContactHandler)
	subscribe(tamperAlert, "alarm", eventHandler)
}

// TODO: implement event handlers
// event hanlders are passed the event itself
def openContactHandler(evt) {
    log.debug "Event value = $evt.value"
    if (evt.value == "unlocked") {
    	def message = "$evt.descriptionText"       
        log.info message
    	send(message)   
    }
}

def closeContactHandler(evt) {
    log.debug "Event value = $evt.value"
    if (evt.value == "locked") {
        def message = "$evt.descriptionText"
        log.info message
    	send(message)   
    }
}

def eventHandler(evt) {	 
    
    log.debug "Event value = $evt.value"
    log.debug "SendPushMessage = $sendPushMessage"
      
	def message = "$evt.descriptionText"
	log.info message
    send(message)
}

private send(msg) {

    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }
    log.debug msg
}