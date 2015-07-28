/**
 *  SamsungAudio
 *
 *  Copyright 2014 Olivia Ju
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
	definition (name: "SamsungAudio", namespace: "smartthings", author: "OliviaJu", oauth: true) {
		capability "Actuator"
		capability "Music Player"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		attribute "model", "string"

		command "subscribe"
		command "getVolume"
		command "getCurrentSong"
		command "getMute"
		command "getPlayStatus"
		command "getModel"
		command "connect"
		command "tileSetLevel", ["number"]
		command "playSoundAndTrack", ["string","number","json_object","number"]
		command "playTrackAndResume", ["string","number","number"]
		command "playTrackAndRestore", ["string","number","number"]

		command "playTrackAtVolume", ["string","number"]
		command "playTextAndResume", ["string","number"]
		command "playTextAndResume", ["string","json_object","number"]
		command "playTextAndRestore", ["string","number"]
}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here

		// Main
		standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
			state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
			state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
//			state "grouped", label:'Grouped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		}

		// Row 1
		standardTile("nextTrack", "device.status", width: 1, height: 1, decoration: "flat") {
			state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
		}
		standardTile("play", "device.status", width: 1, height: 1, decoration: "flat") {
			state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
//			state "grouped", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
		}
		standardTile("previousTrack", "device.status", width: 1, height: 1, decoration: "flat") {
			state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
		}

		// Row 2
		standardTile("status", "device.status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
			state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#ffffff"
			state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
			state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
//			state "grouped", label:'Grouped', action:"", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		}
		standardTile("pause", "device.status", width: 1, height: 1, decoration: "flat") {
			state "default", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", nextState:"paused", backgroundColor:"#ffffff"
//			state "grouped", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", backgroundColor:"#ffffff"
		}
		standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
			state "unmuted", label:"", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
			state "muted", label:"", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
		}

		// Row 3
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false, range:"0..30") {
			state "level", action:"tileSetLevel", backgroundColor:"#ffffff"
		}

		// Row 4
		valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:1, width:3, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		// Row 5
		standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
		}
		//standardTile("model", "device.model", width: 1, height: 1, decoration: "flat") {
		//	state "m3", label:'M3', action:"", icon:"", backgroundColor:"#79b821"
		//	state "m5", label:'M5', action:"", icon:"", backgroundColor:"#79b821"
		//	state "m7", label:'M7', action:"", icon:"", backgroundColor:"#79b821"
		//}
        valueTile("model", "device.model", inactiveLabel : true, height:1, width:1, decoration: "flat") {
			state "default", label:'${currentValue}', action:"", backgroundColor:"#ffffff"
		}

		standardTile("connect", "device.status", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"connect", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
		}

		main "main"

		details([
			"previousTrack","play","nextTrack",
			"status","pause","mute",
			"levelSliderControl",
			"currentSong",
			"refresh","model"
		])
	}
}

//def initiallize(){
//	TRACE("initial")
//	[getCurrentSong(), getModelName()]
//}

def connect() {
	log.trace "connect"
	SendRequest("/connect")
}

def refresh() {
	log.debug "Executing 'refresh'"

//	[getVolume(), getCurrentSong(), getMute(), getPlayStatus(), getModel()]
	[getVolume(), getMute(), getPlayStatus(), getModel()]
}

// Custom commands
def on() {
	log.debug "switch on"
	play()
}

def off() {
	log.debug "switch off"
	stop()
}

def getVolume() {
	log.debug "getVolume"

	sendRequest("/UIC?cmd=<name>GetVolume</name>")
}

def getCurrentSong() {
//	log.debug "getCurrentSong"

//	sendRequest("/UIC?cmd=<name>GetMusicInfo</name>")
}

def getMute() {
	log.debug "getMute"

	sendRequest("/UIC?cmd=<name>GetMute</name>")
}

def getPlayStatus() {
	log.debug "getPlayStatus"
    
	sendRequest("/UIC?cmd=<name>GetPlayStatus</name>")
}

def getModel() {
	log.trace "getModel"

	sendRequest("/UIC?cmd=<name>GetSoftwareVersion</name>")
}

def tileSetLevel(val) {
	log.debug "tileSetLevel($val)"

//	def v = Math.round(Math.max(Math.min(Math.round(val), 100), 0)*0.3)
	def v = val
	log.debug "volume = $val"

	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetVolume%3C/name%3E%3Cp%20type=%22dec%22%20name=%22volume%22%20val=%22$v%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""
    
 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction

}

def nextTrack() {
	log.debug "Executing 'nextTrck'"

	def blah = """GET /UIC?cmd=%3Cname%3ESetTrickMode%3C/name%3E%3Cp%20type=%22str%22%20name=%22trickmode%22%20val=%22next%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def play() {
	log.debug "Executing 'play'"

	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetPlaybackControl%3C/name%3E%3Cp%20type=%22str%22%20name=%22playbackcontrol%22%20val=%22resume%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}


def previousTrack() {
	log.debug "Executing 'previousTrck'"

	def blah = """GET /UIC?cmd=%3Cname%3ESetTrickMode%3C/name%3E%3Cp%20type=%22str%22%20name=%22trickmode%22%20val=%22previous%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def pause() {
	log.debug "Executing 'pause'"

	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetPlaybackControl%3C/name%3E%3Cp%20type=%22str%22%20name=%22playbackcontrol%22%20val=%22pause%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def mute() {
	log.debug "Executing 'mute'"

	def blah = """GET /UIC?cmd=%3Cname%3ESetMute%3C/name%3E%3Cp%20type=%22str%22%20name=%22mute%22%20val=%22on%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "bHubAction Response log: ${haction}"    
	haction
}

def unmute() {
	log.debug "Executing 'unmute'"

	//sendRequest("UIC?cmd=%3Cname%3ESetMute%3C%2Fname%3E%3Cp%20type%3D%5C%22str%5C%22%20name%3D%5C%22mute%5C%22%20val%3D%5C%22off%5C%22%2F%3E%22");

	def blah = """GET /UIC?cmd=%3Cname%3ESetMute%3C/name%3E%3Cp%20type=%22str%22%20name=%22mute%22%20val=%22off%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def playTrackAndResume(uri, duration, volume=null) {
	log.debug "playTrackAndResume($uri, $duration, $volume)"

	def level = volume as Integer
	def result = []
    
    if (level) {
			def v = Math.round(Math.max(Math.min(Math.round(level), 100), 0)*0.3)
			log.debug "volume = $v"

			def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetVolume%3C/name%3E%3Cp%20type=%22dec%22%20name=%22volume%22%20val=%22$v%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

    
 			def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//		log.debug "HubAction Response log: ${haction}"    
			haction
    }


	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetUrlPlayback%3C/name%3E%3Cp%20type=%22cdata%22%20name=%22url%22%20val=%22empty%22%3E%3C![CDATA[$uri]]%3E%3C/p%3E%3Cp%20type=%22dec%22%20name=%22buffersize%22%20val=%220%22/%3E%3Cp%20type=%22dec%22%20name=%22seektime%22%20val=%220%22/%3E%3Cp%20type=%22dec%22%20name=%22resume%22%20val=%221%22/%3E HTTP/1.1
				${getHeader()}"""

	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def playTrackAndRestore(uri, duration, volume=null) {
	log.debug "playTrackAndRestore($uri, $duration, $volume)"

	def level = volume as Integer
	def result = []
    
    if (level) {
			def v = Math.round(Math.max(Math.min(Math.round(level), 100), 0)*0.3)
			log.debug "volume = $v"

			def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetVolume%3C/name%3E%3Cp%20type=%22dec%22%20name=%22volume%22%20val=%22$v%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

    
 			def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//		log.debug "HubAction Response log: ${haction}"    
			haction
    }

	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetUrlPlayback%3C/name%3E%3Cp%20type=%22cdata%22%20name=%22url%22%20val=%22empty%22%3E%3C!%5BCDATA%5B$uri%5D%5D%3E%3C/p%3E%3Cp%20type%3D%22dec%22%20name%3D%22buffersize%22%20val%3D%220%22%3E%3C/p%3E%3Cp%20type%3D%22dec%22%20name%3D%22seektime%22%20val%3D%220%22%3E%3C/p%3E%3Cp%20type=%22dec%22%20name=%22resume%22%20val=%220%22/%3E HTTP/1.1
				${getHeader()}"""

 	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def playSoundAndTrack(soundUri, duration, trackData, volume=null) {
	log.debug "playSoundAndTrack($soundUri, $duration, $trackUri, $volume)"

	def level = volume as Integer
	def result = []
    
    if (level) {
			def v = Math.round(Math.max(Math.min(Math.round(level), 100), 0)*0.3)
			log.debug "volume = $v"

			def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetVolume%3C/name%3E%3Cp%20type=%22dec%22%20name=%22volume%22%20val=%22$v%22%3E%3C/p%3E HTTP/1.1
				${getHeader()}"""

    
 			def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//		log.debug "HubAction Response log: ${haction}"    
			haction
    }


	def blah = """GET /UIC?cmd=%3Cpwron%3Eon%3C/pwron%3E%3Cname%3ESetUrlPlayback%3C/name%3E%3Cp%20type=%22cdata%22%20name=%22url%22%20val=%22empty%22%3E%3C![CDATA[$soundUri]]%3E%3C/p%3E%3Cp%20type=%22dec%22%20name=%22buffersize%22%20val=%220%22/%3E%3Cp%20type=%22dec%22%20name=%22seektime%22%20val=%220%22/%3E HTTP/1.1
				${getHeader()}"""

	def haction = new physicalgraph.device.HubAction(blah, physicalgraph.device.Protocol.LAN)
//	log.debug "HubAction Response log: ${haction}"    
	haction
}

def playTextAndResume(text, volume=null)
{
	log.debug "playTextAndResume($text, $volume)"
	def sound = textToSpeech(text)
	playTrackAndResume(sound.uri, (sound.duration as Integer) + 1,volume)
}

def playTextAndRestore(text, volume=null)
{
	log.debug "playTextAndRestore($text, $volume)"
	def sound = textToSpeech(text)
	playTrackAndRestore(sound.uri, (sound.duration as Integer) + 1, volume)
}

// parse events into attributes
def parse(String description) {
//	log.debug "Parsing '${description}'"


	def map = stringToMap(description)
//	def headerString = new String(map.headers.decodeBase64())
	def result = []


	if (map.body) {

		def bodyString = new String(map.body.decodeBase64())
		def body = new XmlSlurper().parseText(bodyString)
		log.trace "[parsed] '${body}'"
		def muteValue
		def volume
		def playStatus
		def swVersion
        
		if(body?.method?.text()== "SoftwareVersion") {
			log.trace "Got SoftwareVersion ${body?.response?.version?.text()}"

			if(body?.response?.version?.text().contains('WAM750')) {
				swVersion = "M7"
			}
			else if(body?.response?.version?.text().contains('WAM550')) {
				swVersion = "M5"
			}
			else if(body?.response?.version?.text().contains('WAM350')) {
				swVersion = "M3"
			}

			sendEvent(name: "model", value: swVersion)
		}

		if(body?.method?.text()== "MuteStatus") {
			log.trace "Got MuteStatus"
            	
			if (body.response.text() == "on") {
				muteValue = "muted"
			}
			else {
				muteValue = "unmuted"
			}

			sendEvent(name: "mute", value: muteValue)
		}
        
		// to do: parse other responses like this -> 
		if(body?.method?.text()== "VolumeLevel") {
			log.trace "Got VolumeLevel ${body.response.text()}"
//			volume = body.response.text()
//			volume = Math.round(volume.toInteger() * 3)
//			sendEvent(name: "level", value: volume)
			sendEvent(name: "level", value: body.response.text())
		}
        
		if (body?.method?.text()== "MusicInfo") {
			log.trace "Got MusicInfo ${body?.response?.title?.text()}"
             
			if (body?.response?.title?.text() == "Playing Status is not valid or MusicDB is NULL") {
				sendEvent(name: "trackDescription", value: "--")
			}
			else {
				sendEvent(name: "trackDescription", value: body?.response?.title?.text())
			}
		}
        
		if (body?.method?.text()== "PlayStatus") {
			log.trace "Got PlayStatus ${body?.response?.playstatus?.text()}"
            
			playStatus = "stopped"
			if (body.response.playstatus.text() == "play") {
				playStatus = "playing"
			}
			else if (body.response.playstatus.text() == "pause") {
				playStatus = "paused"
			}
			log.trace "playStatus $playStatus"
            
			sendEvent(name: "status", value: playStatus)
		}
		if (body?.method?.text()== "PlaybackStatus") {
			log.trace "Got PlaybackStatus ${body?.response?.playstatus?.text()}"
            
			playStatus = "stopped"
			if (body.response.playstatus.text() == "resume") {
				playStatus = "playing"
			}
			else if (body.response.playstatus.text() == "pause") {
				playStatus = "paused"
			}
			log.trace "playbackStatus $playStatus"
            
			sendEvent(name: "status", value: playStatus)
		}
	}

	result
}

def sendRequest( path ) {
	log.trace "sendRequest $path"

	def httpRequest = [
		method:		"GET",
		path: 		path,
		headers:[
					Host: getHostAddress(),
					mobileUUID: getHubUUID(),
					mobileVersion: "1.0",
					mobileIP: getCallBackAddress(),
					mobileName: "smartthings",
		]
	]

	def hubAction = new physicalgraph.device.HubAction(httpRequest)

	//log.trace "HubAction Response log: ${hubAction}"  
    hubAction
}

private getHeader() {
	def header = """Host: ${getHostAddress()}
mobileUUID: ${getHubUUID()}
mobileVersion: 1.0
mobileIP: ${getCallBackAddress()}
mobileName: smartthings

"""
	log.trace "$header"
	return header
}

private getCallBackAddress(){
	//log.trace "localIP $device.hub.getDataValue("localIP")"
	//log.trace "localSrvPortTCP $device.hub.getDataValue("localSrvPortTCP")"
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")

	//"192.168.1.2:39500"
}

private getHubUUID(){
	device.hub.id
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip, port

	if (parts.length == 2) {
		ip = parts[0]
		port = parts[1]
	} else {
		ip = getDeviceDataByName("ip")
		//port = getDeviceDataByName("port")
		port = "D6D9"	// port is fixed.
	}

//	log.debug "getHostAddress() $ip $port"
	return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private dniFromUri(uri) {
	def segs = uri.replaceAll(/http:\/\/([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+)\/.+/,'$1').split(":")
	def nums = segs[0].split("\\.")
	(nums.collect{hex(it.toInteger())}.join('') + ':' + hex(segs[-1].toInteger(),4)).toUpperCase()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private def TRACE(message) {
  log.debug message
}