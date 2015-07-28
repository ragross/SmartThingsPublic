/**
 *  Windows Open & Rain is forecast
 *
 */
definition(
    name: "Windows Open & Rain is Forecast",
    namespace: "accuweather",
    author: "accuweather",
    description: "Rain is in your forecast and at least one window is open.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

	// todo: request capability
    section("Select your AccuWeather device") {
        input "weather", "device.accuWeather"
    }

    section("Weather prediction") {
        input(name: "timeframe", type: "enum", title: "Rain Forecast", options: ["1hr","2hr","3hr","4hr","6hr","7hr","8hr","9hr","10hr","11hr","12hr"])
    }
    
    section("Monitor this door or window") {
        input "contact", "capability.contactSensor"
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
    subscribe(weather, "forecast", rainChange)
    rainChange()
}

// subscribe to changes rain in forecast
def rainChange(evt) {

    // confirm that there's currently no rain
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

    // if it's going to rain in the next hour
    if(percip1hr > 0) {
        def contactState = contact.currentValue("contact")
        if (contactState.value == "open") {
            log.debug "the contact is open and it's going to rain soon"
            // send notification
            sendMessage()
        } else {
            log.warn "contact is closed:  doing nothing"
        }
    }
}

// send the notifiacation to the user
void sendMessage() {

    def msg = "${contact.displayName} is open and rain is in your forecast."
    log.info msg
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone) {
            sendSms(phone, msg)
        } else {
            sendPush(msg)
        }
    }
}