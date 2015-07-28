/**
 *  Beacon and Sonos
 *
 *  Copyright 2014 Physical Graph Corporation
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
	name: "Beacon and Sonos",
	category: "SmartThings Internal",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Play a message on your Sonos when someone enters or leaves a beacon monitored region",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol@2x.png"
)

preferences {
	page(name: "mainPage", title: "Play a message on your Sonos when someone enters or leaves a beacon monitored region", install: true, uninstall: true)
	page(name: "chooseTrack", title: "Select a song or station")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage", install: true, uninstall: true) {

		section("Where do you want to watch?") {
			input name: "beacons", type: "capability.beacon", title: "Select your beacon(s)", 
				multiple: true, required: true
		}

		section("Who do you want to watch for?") {
			input name: "phones", type: "device.mobilePresence", title: "Select your phone(s)", 
				multiple: true, required: true
		}

		section("Do you want to trigger on arrival or departure?") {
			input "arrivalSonos", "enum", title: "Trigger action on", required: true, options: [
				"Arrival",
				"Departure"
			]                
		}

		section {
			input "sonos", "capability.musicPlayer", title: "On this Sonos player", required: true
		}
        
		section{
			input "actionType", "enum", title: "Action?", required: true, defaultValue: "Custom Message", options: [
				"Custom Message",
				"Bell 1",
				"Bell 2",
				"Dogs Barking",
				"Fire Alarm",
				"The mail has arrived",
				"A door opened",
				"There is motion",
				"Smartthings detected a flood",
				"Smartthings detected smoke",
				"Someone is arriving",
				"Piano",
				"Lightsaber"]
			input "message","text",title:"Play this message", required:false, multiple: false
		}
        
		section("More options", hideable: true, hidden: true) {
			input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true
			href "chooseTrack", title: "Or play this music or radio station", description: song ? state.selectedSong?.station : "Tap to set", state: song ? "complete" : "incomplete"

			input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			if (settings.modes) {
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
        
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

// Lifecycle management
def installed() {
	log.debug "<beacon-control> Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "<beacon-control> Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
	subscribe(beacons, "presence", beaconHandler)

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}

	if (song) {
		saveSelectedSong()
	}

	loadText()
}

// Event handlers
def beaconHandler(evt) {
	log.debug "<beacon-control> beaconHandler: $evt"

	if (allOk) {
        log.trace "allOk"
        def lastTime = state[frequencyKey(evt)]
        if (oncePerDayOk(lastTime)) {
            if (frequency) {
                if (lastTime == null || now() - lastTime >= frequency * 60000) {
                    takeAction(evt)
                }
                else {
                    log.debug "Not taking action because $frequency minutes have not elapsed since last action"
                }
            }
            else {
                def data = new groovy.json.JsonSlurper().parseText(evt.data)
                log.debug "<beacon-control> data: $data - phones: " + phones*.deviceNetworkId

                def beaconName = getBeaconName(evt)
                log.debug "<beacon-control> beaconName: $beaconName"

                def phoneName = getPhoneName(data)
                log.debug "<beacon-control> phoneName: $phoneName"
                if (phoneName != null) {
                    def action = data.presence == "1" ? "Arrival" : "Departure"
					log.trace "$action - $arrivalSonos"
                    if (action == arrivalSonos) {
                        takeAction(evt)
                    }
                    
                }            
                
            }
        }
        else {
            log.debug "Not taking action because it was already taken today"
        }
	}
}

def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section{
			input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
		}
	}
}

private songOptions() {

	// Make sure current selection is in the set

	def options = new LinkedHashSet()
	if (state.selectedSong?.station) {
		options << state.selectedSong.station
	}
	else if (state.selectedSong?.description) {
		// TODO - Remove eventually? 'description' for backward compatibility
		options << state.selectedSong.description
	}

	// Query for recent tracks
	def states = sonos.statesSince("trackData", new Date(0), [max:30])
	def dataMaps = states.collect{it.jsonValue}
	options.addAll(dataMaps.collect{it.station})

	log.trace "${options.size()} songs in list"
	options.take(20) as List
}

private saveSelectedSong() {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			state.selectedSong = data
			log.debug "Selected song = $state.selectedSong"
		}
		else if (song == state.selectedSong?.station) {
			log.debug "Selected existing entry '$song', which is no longer in the last 20 list"
		}
		else {
			log.warn "Selected song '$song' not found"
		}
	}
	catch (Throwable t) {
		log.error t
	}
}

def scheduledTimeHandler() {
	eventHandler(null)
}

def appTouchHandler(evt) {
	takeAction(evt)
}

private getBeaconName(evt) {
	def beaconName = beacons.find { b -> b.id == evt.deviceId }
	return beaconName
}

private getPhoneName(data) {    
	def phoneName = phones.find { phone ->
		// Work around DNI bug in data
		def pParts = phone.deviceNetworkId.split('\\|')
		def dParts = data.dni.split('\\|')
        pParts[0] == dParts[0]
	}
	return phoneName
}

private takeAction(evt) {

	log.trace "takeAction()"

	if (song) {
		sonos.playSoundAndTrack(state.sound.uri, state.sound.duration, state.selectedSong, volume)
	}
	else if (resumePlaying){
		sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
	}
	else {
		sonos.playTrackAndRestore(state.sound.uri, state.sound.duration, volume)
	}

	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
	}
	log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private getTimeLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}

// TODO - End Centralize
private loadText() {
	switch ( actionType) {
		case "Bell 1":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
		case "Bell 2":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3", duration: "10"]
			break;
		case "Dogs Barking":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3", duration: "10"]
			break;
		case "Fire Alarm":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3", duration: "17"]
			break;
		case "The mail has arrived":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/the+mail+has+arrived.mp3", duration: "1"]
			break;
		case "A door opened":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3", duration: "1"]
			break;
		case "There is motion":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/there+is+motion.mp3", duration: "1"]
			break;
		case "Smartthings detected a flood":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+a+flood.mp3", duration: "2"]
			break;
		case "Smartthings detected smoke":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+smoke.mp3", duration: "1"]
			break;
		case "Someone is arriving":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/someone+is+arriving.mp3", duration: "1"]
			break;
		case "Piano":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3", duration: "10"]
			break;
		case "Lightsaber":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3", duration: "10"]
			break;
		default:
			if (message) {
				state.sound = textToSpeech(message instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
			}
			else {
				state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
			}
			break;
	}
}
