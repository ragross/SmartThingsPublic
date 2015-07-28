/**
 *  Accuweather Connect
 *
 *  Date: 2015-02-02
 */

definition(
    name: " AccuWeather Connect",
    namespace: "accuweather",
    author: "danny@megapixelsoftware.com",
    description: "AccuWeather SmartApp",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather@2x.png",
) {
    appSetting "apikey"
}

preferences {
    page(name: "Setup", title: "", content: "authPage", install: true)
}

// get our api url
def getapiurl() {
   return "http://apidev.accuweather.com"
}

// get our device name
def getDeviceName() {
    return "AccuWeather"
}

// get our namespace
def getNameSpace() {
    return "accuweather"
}

// get our apikey from settings
def getapikey() {
   return appSettings.apikey
}

// perform the intial creation of our child device
def installed() {
    log.debug "Installed with settings: ${settings}"
    if (locationIsDefined()) {
        addDevice()
    }    
    initialize()
}

// update our app when settings change
def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

// initialize the schedule of polling and setup Alerts
def initialize() {
    runEvery1Hour("poll")
    refresh()
    // if phone is set, trigger alerts by default
    if(phone) {
      childDevice?.sendEvent(name:"alertStatus", value: "on")
    }
}
// uninstall the connect app and child devices
def uninstalled() {
    removeChildDevices(getChildDevices())
}

// remove child devices
private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

// build our settings page
def authPage() {
    return dynamicPage(name: "Setup", title: "", nextPage: null, uninstall: true, install:false) {
        section ("Weather reporting for this location...") {
            input "zipcode", "number", title: "Zip Code", required: true
        }

        section("Severe Weather & Reminder Push Notifications?") {
            input "pushAlert", "bool", required: true,
                  title: "Send Push Notification when Opened?"
        }
        
        section("Severe Weather & Reminder Text Alerts?") {
            input "phone", "phone", title: "Phone number", required: false
        }

        section("AccuWeather Terms of Usage & Privacy Policy") {
            href(name: "hrefNotRequired",
                 required: false,
                 style: "external",
                 url: "http://m.accuweather.com/en/legal",
                 description: "By installing one or more AccuWeather SmartApps, You accept and agree to AccuWeather’s Terms of Usage and Privacy Policy")
            input(name: "agree", type: "enum", title: "Accept & Agree", description:"", options: ["Accept & Agree"], required: true)
        }
    }
}

// confirm that location is defined
def locationIsDefined() {
    zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

// validate zipcode
def zipcodeIsValid() {
    zipcode && zipcode.size() == 5
}

// create our child device
def addDevice() {
    def dni = app.id + ":" + zipcode
    def childDevice = addChildDevice(getNameSpace(), getDeviceName(), dni, null, [label:"AccuWeather (${zipcode})", completedSetup: true])

    // Get Location ID
    state.zipcode = zipcodeIsValid() ? zipcode : location.zipcode;
    def params = [
      uri:  getapiurl() + "/locations/v1/search?q=" + state.zipcode + "&apikey=" + getapikey()
    ]
    httpGet(params) { response ->
        log.debug "${response.data}"
        state.pin = response.data[0].Key
    }

    log.trace "created ${d.displayName} with id $dni"
    poll()

}

// get weather alert data
def alertAPI() {
    def devices = getChildDevices()
    devices.each {
        def childDevice = getChildDevice(it.deviceNetworkId)
        if (childDevice) {

            // Alert API
            def params = [
                uri: getapiurl() + "/alerts/v1/" + state.pin + ".json?language=en&details=true&apikey=" + getapikey(),
            ]

            httpGet(params) { response ->

                // Schedule to pull data again after it expires
                def expires = new Date().parse("EEE, dd MMM yyyy hh:mm:ss zzz", response.headers.Expires)
                def expiresString = expires.format("EEEE,\n MMM d, YYYY",location?.timeZone)
                log.debug "alertAPI Expires: $expires"

                runOnce(expires, "alertAPI");
                state.alertExpires = expires;
                
                log.debug response.data

                def alerts = "";
                def links = "";
                response.data.each {
                    alerts += it.Description.English + "\n"
                    links += it.MobileLink + "\n"
                }

                // if we have alerts active
                // check to see if this alert has changed
                def currentAlert = childDevice?.currentValue("alert")
                // check the alert status
                def alertStatus = childDevice?.currentValue("alertStatus")

                // log.debug "status: $alertStatus for $currentAlert | $alerts"
                if (currentAlert != alerts) {
                    // if the alert has a value, let's send
                    if(alerts != "" && alertStatus == "on") {
                    // @todo - test including link
                        if(phone) {
                          log.debug "send sms to $phone"
                          sendSms(phone, "AccuWeather Alert (SmartThings): $alerts \n$links")
                        } else if(pushAlert){
                          log.debug "send push to user"
                          sendPush("AccuWeather Alert (SmartThings): $alerts")
                        }
                    }
               }
               childDevice?.sendEvent(name:"alert", value: alerts)
            }
        }
    }
}

// get current conditions data
def currentConditionsAPI() {
    log.debug "currentconditions api"
    def devices = getChildDevices()
    devices.each {
        def childDevice = getChildDevice(it.deviceNetworkId)
        if (childDevice) {

            def params = [
              uri: getapiurl() + "/currentconditions/v1/" + state.pin + ".json?language=en&details=true&apikey=" + getapikey(),
            ]

            // log.debug params.uri
            httpGet(params) { response ->

                // Schedule to pull data again after it expires
                def expires = new Date().parse("EEE, dd MMM yyyy hh:mm:ss zzz", response.headers.Expires)
                def expiresString = expires.format("EEEE,\n MMM d, YYYY",location?.timeZone)
                log.debug "currentConditionsAPI Expires: $expires"

                runOnce(expires, "currentConditionsAPI");
                state.currentconditionsExpires = expires;

                Date now = new Date()
                def updatedTime = now.format("h:mm a",location?.timeZone)
                def theDate = now.format("EEEE,\n MMM d, YYYY",location?.timeZone)
                childDevice?.sendEvent(name:"theDate", value: "$theDate\nupdated at ${updatedTime}\n↻")

                childDevice?.sendEvent([name:"weather", value: response.data.WeatherText[0]])
                childDevice?.sendEvent([name:"weatherIcon", value: response.data.WeatherIcon[0]])
                childDevice?.sendEvent([name:"temperature", value: response.data.Temperature.Imperial.Value[0]])
                childDevice?.sendEvent([name:"humidity", value: response.data.RelativeHumidity[0]])
                childDevice?.sendEvent([name:"cloudCover", value: response.data.CloudCover[0]])
                childDevice?.sendEvent([name:"realFeel", value: response.data.RealFeelTemperature.Imperial.Value[0]])
                childDevice?.sendEvent([name:"windSpeed", value: response.data.Wind.Speed.Imperial.Value[0]])
                childDevice?.sendEvent([name:"windGusts", value: response.data.WindGust.Speed.Imperial.Value[0]])
                childDevice?.sendEvent([name:"windDirection", value: response.data.Wind.Direction.English[0]])
                childDevice?.sendEvent([name:"uvIndex", value: response.data.UVIndex[0]])

                def summary = "UV Index.............................................${response.data.UVIndex[0]}(of 9)\n"
                summary += "Humidity.................................................${response.data.RelativeHumidity[0]}%\n"
                summary += "Wind Speed...................................${response.data.Wind.Speed.Imperial.Value[0]} MPH\n"
                summary += "Wind Gusts....................................${response.data.WindGust.Speed.Imperial.Value[0]} MPH\n"
                summary += "Wind Direction..........................................${response.data.Wind.Direction.English[0]}\n"

                // sunrise and sunset data - DAILY API
                def rise = childDevice?.currentValue("localSunrise")
                def set = childDevice?.currentValue("localSunset")
                summary += "Sunrise.............................................$rise\n"
                summary += "Sunset..............................................$set \n\n"


                def alert = childDevice?.currentValue("alert")
                if (alert != "") {
                  summary += "\n\n$alert"
                }

                childDevice?.sendEvent(name:"summary", value: summary)
            }
        }
    }
}

// get forecast data
def forecastAPI() {
   log.debug "forecast api"
   // Forecast API
    def devices = getChildDevices()
    devices.each {
        def childDevice = getChildDevice(it.deviceNetworkId)
        if (childDevice) {

           def params = [
              uri: getapiurl() + "/forecasts/v1/hourly/12hour/" + state.pin + ".json?language=en&details=true&apikey=" + getapikey(),
           ]

           log.debug params.uri
           httpGet(params) { response ->

                // Schedule to pull data again after it expires
                def expires = new Date().parse("EEE, dd MMM yyyy hh:mm:ss zzz", response.headers.Expires)
                def expiresString = expires.format("EEEE,\n MMM d, YYYY",location?.timeZone)
                log.debug "ForecastAPI Expires: $expires"

                runOnce(expires, "forecastAPI");
                state.forecastExpires = expires;

                // expand to 6, store values as well
                def map = [:]
                for(int i = 0; i < 12; i++) {

                    //summary by hour
                    def dTime = new Date().parse("yyyy-MM-dd'T'HH:mm:ss", response.data[i].DateTime)
                    def hourTime = dTime.format("        \nEEEE\nh:mm a\n      ") //,location?.timeZone
                    def temp = response.data[i].Temperature.Value
                    def real = response.data[i].RealFeelTemperature.Value
                    def hourSummary = "${temp}°\nReal Feel® ${real}°"


                    def hourIcon = response.data[i].WeatherIcon
                    def precip = response.data[i].Rain.Value

                    if (i < 3) {
                        childDevice?.sendEvent([name:"summary${i+1}hr", value: hourSummary])
                        childDevice?.sendEvent([name:"time${i+1}hr", value: hourTime])
                        childDevice?.sendEvent([name:"icon${i+1}hr", value: hourIcon])
                        childDevice?.sendEvent([name:"precip${i+1}hr", value: precip])
                    }

                    map["${i+1}hr"] = ["precipitation": precip, "temperature": temp, "realFeel": real]
                }

                def forecast = new groovy.json.JsonBuilder(map).toString()
                log.debug "forecast: $forecast"
                childDevice?.sendEvent([name:"forecast", value: forecast])
            }
        }
    }
}

// populate sunrise and sunset data
def sunriseAPI() {
    log.debug "sunrise api"
    def devices = getChildDevices()
    devices.each {
        def childDevice = getChildDevice(it.deviceNetworkId)
        if (childDevice) {

            def riseAndSet = getSunriseAndSunset()
            def rise = riseAndSet.sunrise;
            def set = riseAndSet.sunset;

            def sunRise = rise.format("h:mma",location?.timeZone)
            def sunSet = set.format("h:mma",location?.timeZone)

            log.debug "Rise and Set: $riseAndSet"
            childDevice?.sendEvent([name:"localSunrise", value: sunRise])
            childDevice?.sendEvent([name:"localSunset", value: sunSet])
        }
    }
}

// poll set for every 1 hour, to update data if scheduled updates failed
def poll() {
    log.debug "run the poll function"
    // do these checks in case the scheduled call at expiration fails for some reason
    def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss")
    log.debug "check format ${state.alertExpires} $now"
    
    if(state.alertExpires < now) {
        log.debug "confirmed string date comparison"
        alertAPI()
    }

    if(state.currentConditionsExpires < now) {
        currentConditionsAPI()
    }

    if(state.forecastExpires < now) {
      forecastAPI()
    }
    sunriseAPI()
}

// call all of the APIs to refresh our data
def refresh() {
    log.debug "refresh called"
    sunriseAPI()
    currentConditionsAPI()
    forecastAPI()
    alertAPI()
}