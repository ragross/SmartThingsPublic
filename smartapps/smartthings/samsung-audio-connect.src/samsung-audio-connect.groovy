/**
 *  Samsung Audio (Connect)
 *
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
    name: "Samsung Audio (Connect)",
    namespace: "smartthings",
    author: "olivia ju",
    description: "Samsung Audio",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	page(name: "audioDiscovery", title:"Samsung Wireless Audio Multiroom Setup", content:"audioDiscovery", refreshTimeout:3)
	page(name: "setNameAudio", title: "Set name of Samsung Audio")
	page(name: "addAudio", title: "Add Samsung Audio")
}

def audioDiscovery() {
    int audioRefreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
    state.bridgeRefreshCount = audioRefreshCount + 1

    def options = audiosDiscovered() ?: []
    def numFound = options.size() ?: 0

    if(!state.subscribe) {
        subscribe(location, null, locationHandler, [filterEvents:false])    
        state.subscribe = true
    }

    // Audio discovery request every 15 seconds
    if((audioRefreshCount % 5) == 0) {
        findAudio()
        state.audios.clear()
    }

    return dynamicPage(name:"audioDiscovery", title:"Samsung Audio Search Started!", nextPage:"setNameAudio", refreshInterval:3, uninstall: true) {
  		section("Please wait while we discover your Samsung Audio. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered."){
        }
  		section("on this hub...") {
    		input "theHub", "hub", multiple: false, required: true
		}
        section("") {
            input "selectedAudio", "enum", required:false, title:"Select Samsung Audio (${numFound} found)", multiple:false, options:options
        }
    }
}

def setNameAudio() {
	log.debug "setNameAudio"
    return dynamicPage(name:"setNameAudio", title:"Set the name of Samsung Audio", nextPage:"addAudio") {
		section{
			input "nameAudio", "text", title:"Enter the name of Samsung Audio", required:false
		}
    }
}

def addAudio() {
	log.debug "addAudio"
    def existingDevice  = getChildDevice(selectedAudio)
    def entry = state.audios[selectedAudio]
    /*
    if(!existingDevice && theHub && entry)
    {
        existingDevice = addChildDevice("smartthings", "SamsungAudio", selectedAudio, theHub.id, [name:nameAudio, label:name])
        existingDevice.updateDataValue("mac", entry.mac)
        existingDevice.updateDataValue("ip", entry.ip)
        existingDevice.updateDataValue("port", entry.port)
        existingDevice.updateDataValue("uuid", entry.uuid)
    }
    */
    
    if(!existingDevice && theHub && entry)
    {
        if(!nameAudio)
    	{
	        existingDevice = addChildDevice("smartthings", "SamsungAudio", selectedAudio, theHub.id, [name:"SamsungAudio", label:name])
    	} 
		else
		{
	        existingDevice = addChildDevice("smartthings", "SamsungAudio", selectedAudio, theHub.id, [name:nameAudio, label:name])		
		}
        existingDevice.updateDataValue("mac", entry.mac)
        existingDevice.updateDataValue("ip", entry.ip)
        existingDevice.updateDataValue("port", entry.port)
        existingDevice.updateDataValue("uuid", entry.uuid)
    }
    else if(existingDevice && entry)
    {
    	if(entry.ip != existingDevice.getDataValue("ip"))
        {
        	log.trace "existd device and other ip"
    		existingDevice.updateDataValue("ip", entry.ip)
        }
    }
    else
    {
    	log.debug "Device already created"
    }
    	return dynamicPage(name:"addAudio", title:"Adding Samsung Audio(${nameAudio}) is done.", install:true) {
    }
}

Map audiosDiscovered() {
    def audios = getSamsungAudios()
    def map = [:]
    
    //TODO commented out
    audios.each {
        def key = it.value.mac
        def value = "Samsung ${it.value.mac}(${convertHexToIP(it.value.ip)})"
     // def value = "Samsung Audio (${convertHexToIP(it.value.ip)})"
        map["${key}"] = value
    }
    map
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
	// TODO: subscribe to attributes, devices, locations, etc.
    unsubscribe()
    state.subscribe = false

	scheduleActions()
    
    
    def existingDevice  = getChildDevice(selectedAudio)
    def entry = state.audios[selectedAudio]
    if(!existingDevice && theHub && entry)
    {
        existingDevice = addChildDevice("smartthings", "SamsungAudio", selectedAudio, theHub.id, [name:nameAudio, label:name])
        existingDevice.updateDataValue("mac", entry.mac)
        existingDevice.updateDataValue("ip", entry.ip)
        existingDevice.updateDataValue("uuid", entry.uuid)
    }
    else if(existingDevice && entry)
    {
    	if(entry.ip != existingDevice.getDataValue("ip"))
        {
    		existingDevice.updateDataValue("ip", entry.ip)
        }
    }
    else
    {
    	log.debug "Device already created"
    }
    
    scheduledActionsHandler()
}

def scheduledActionsHandler() {
	log.trace "scheduledActionsHandler()"
	refreshAll()
}

private scheduleActions() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	//def cron = "$sec 0/5 * * * ?"	// every 5 min
    def cron = "$sec 0/1 * * * ?"	// every 1 min
	log.debug "schedule('$cron', scheduledActionsHandler)"
	schedule(cron, scheduledActionsHandler)
}

private refreshAll(){
	log.trace "refreshAll()"
	childDevices*.refresh()
	log.trace "/refreshAll()"
}

def uninstalled() {
	//unsubscribe()
	state.subscribe = false
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def getSamsungAudios()
{
    state.audios = state.audios ?: [:]
    log.warn state.audios
	return state.audios
}

// TODO: implement event handlers
private findAudio() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:samsung.com:device:RemoteControlReceiver:1", physicalgraph.device.Protocol.LAN))
}

def locationHandler(evt) {
	log.debug evt.description
	def upnpResult = parseEventMessage(evt.description)

    if (upnpResult?.ssdpTerm?.contains("urn:samsung.com:device:RemoteControlReceiver:1")) {
		log.debug "upnpResult: ${upnpResult}"
		
        def audios = getSamsungAudios()
        audios << ["${upnpResult.mac.toString()}" : [mac:upnpResult.mac, ip:upnpResult.ip, port: upnpResult.port, uuid:upnpResult.uuid]]
    }
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')

	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
                def splitedValue = valueString.split('::')
                splitedValue.each { value ->
                	value = value.trim()
                    if (value.startsWith('uuid:'))
                    {
                        value -= "uuid:"
                        def uuidString = value.trim()
                        if (uuidString) {
							event.uuid = uuidString

						}
                    }
                }
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
	}
	event
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}