/**
 *  Open Windows & Save Energy
 *
 */
definition(
    name: "Open Windows & Save Energy",
    namespace: "accuweather",
    author: "accuweather",
    description: "Your ideal conditions are in the forecast.  Open your windows to reduce energy use.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

    section("Select your AccuWeather device") {
        // todo: request capability.forecast from SmartThings
        input "weather", "device.accuWeather"
    }

    section("Weather prediction") {
        input(name: "timeframe", type: "enum", title: "Clear Forecast", options: ["1hr","2hr","3hr","4hr","6hr","7hr","8hr","9hr","10hr","11hr","12hr"])
    }
    
    section("Monitor this window") {
        input "contact", "capability.contactSensor", required: true
    }

    section("Select Thermostat") {
        input "thermostat", "capability.thermostat", required: false
    }

    section("Manual Temperature Settings"){
        input "heatingSetpoint", "number", title: "Heat setting degrees?", required: false
        input "coolingSetpoint", "number", title: "AC setting degrees?", required: false
    }

    section("Notify by text message at this number (or by push notification if not specified") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
        }
    }
}

def installed() {
    log.trace "installed()"
    subscribe()
}

def updated() {
    log.trace "updated()"
    unsubscribe()
    subscribe()
}

def subscribe() {
    subscribe(weather, "temperature", tempChange)
    tempChange()
}

// subscribe to temperature changes
def tempChange() {    
    // request forecast to confirm that there's currently no rain
    def forecastJson = weather.currentValue("forecast")
    // log.debug "$forecastJson"
    def forecast = new groovy.json.JsonSlurper().parseText(forecastJson)
    def precipState = 0
    
    for(int i = 1; i <= forecast.size(); i++) {
        String hr = "${i}hr"
        precipState += forecast[hr]?.precipitation ? forecast[hr]?.precipitation : 0
        if(hr == timeframe) 
        break
    }

    log.debug "Predicted Rain: $precipState"

    // if it's not going to rain in the next hour
    if(precipState == 0) {

        def contactState = contact.currentValue("contact")
        log.debug contactState
        // confirm that the window is closed
        if (contactState == "closed") {

            def outdoorTemp = weather.currentValue("temperature")
            
            // use thermostat to get setpoints
            if(thermostat) {
               if(outdoorTemp > thermostat.heatingSetpoint && outdoorTemp < thermostat.coolingSetpoint) {
                    // test that the outdoor weather is in our desired range
                    log.debug "a good time to open a window"
                    // send notification
					sendMessage()
                }
            }
            // use manual setpoints
            else {
				float heat = heatingSetpoint
                float cool = coolingSetpoint
                if(outdoorTemp > heat && outdoorTemp < cool) {
                    // test that the outdoor weather is in our desired range
                    log.debug "a good time to open a window"
                    // send notification
                    sendMessage()
                }
            }
        } else {
            log.warn "contact is open:  doing nothing"
        }
    }
}

// send the notification of ideal conditions
void sendMessage() {
    def deltaSeconds = 60 * 60 * 6 // 6 hours
	def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss")
    // send only one notification every 6 hours
    if(state.alertExpires < now) {
        def msg = "Your ideal conditions are in the forecast. Consider opening ${contact.displayName} "
        state.alertExpires = new Date(now() + (1000 * deltaSeconds))
        log.info msg
        if (location.contactBookEnabled) {
            sendNotificationToContacts(msg, recipients)
        }
        else {
            if (phone) {
                sendSms phone, msg
            } else {
                sendPush(msg)
            }
        }
    }
}