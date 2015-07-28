/**
 *  AccuWeather
 *
 *  Date: 2015-02-05
 */
// for the UI
metadata {

    definition (
        name: "AccuWeather",
        namespace: "accuweather",
        author: "danny@megapixelsoftware.com"
        ) {

        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"

        attribute "zip", "string"
        
        // Current Conditions API
        attribute "temperature", "string" // Current Temperature
        attribute "localSunrise", "string"
        attribute "localSunset", "string"
        attribute "humidity", "string"
        attribute "cloudCover", "string"
        attribute "realFeel", "string"
        attribute "windSpeed", "string"
        attribute "windGusts", "string"
        attribute "windDirection", "string" // current, hourly, forecast
        attribute "uvIndex", "string" // current, forcecast
        attribute "summary", "string" // general info
        attribute "theDate", "string" // the days date
        attribute "weatherIcon", "string"
        
        // Forecast Api
        attribute "forecast", "string" // 12 hour json
        attribute "time1hr", "string"
        attribute "summary1hr", "string"
        attribute "icon1hr", "string"
        attribute "precip1hr", "string"

        attribute "time2hr", "string"
        attribute "summary2hr", "string"
        attribute "icon2hr", "string"
        attribute "precip2hr", "string"
        
        attribute "time3hr", "string"
        attribute "summary3hr", "string"
        attribute "icon3hr", "string"
        attribute "precip3hr", "string"
                
        // Alert API
        attribute "alert", "string"
        attribute "aStatus", "string"
        
        command "refresh"
        command "sendAlerts"
        command "clearAlerts"
        
    }

    tiles {
        valueTile("date", "device.theDate", decoration: "flat", width:2, height:1) {
            state "default", label:'${currentValue}', action: "refresh" //backgroundColor:f1f0f0,
        }
        
        standardTile("logo", "device.weatherIcon", decoration: "flat", width:1, height:1) {
            state "default", icon: "http://67.20.73.222/weather/AW_logo_stacked2.png"
        }
        
        valueTile("temperature", "device.temperature", width: 1, height:1, decoration:"flat") {
            /* state "default", label:'   \n   \n${currentValue}°\n' */
            state "default", label:'${currentValue}°'
        }

        valueTile("realFeel", "device.realFeel", decoration: "flat", width:1, height:1) {
            state "default", label:'   \nRealFeel®\n${currentValue}°\n'
        }
        
        standardTile("weatherIcon", "device.weatherIcon", decoration: "flat", height: 1, width:1) {
            state "1", icon:"http://67.20.73.222/weather/ic_01_sunny.png", label: ""
            state "2", icon:"http://67.20.73.222/weather/ic_02_mostly_sunny.png", label: ""
            state "3", icon:"http://67.20.73.222/weather/ic_03_partly_sunny.png", label: ""
            state "4", icon:"http://67.20.73.222/weather/ic_04_intermittent_clouds.png", label: ""
            state "5", icon:"http://67.20.73.222/weather/ic_05_hazy_sunshine.png", label: ""
            state "6", icon:"http://67.20.73.222/weather/ic_06_mostly_cloudy.png", label: ""
            state "7", icon:"http://67.20.73.222/weather/ic_07_cloudy.png", label: ""
            state "8", icon:"http://67.20.73.222/weather/ic_08_dreary.png", label: ""
            state "9", icon:"http://67.20.73.222/weather/ic_11_fog.png", label: ""
            state "10", icon:"http://67.20.73.222/weather/ic_12_showers.png", label: ""
            state "11", icon:"http://67.20.73.222/weather/ic_13_mostly_cloudy_w_showers.png", label: ""
            state "12", icon:"http://67.20.73.222/weather/ic_14_partly_sunny_w_showers.png", label: ""
            state "13", icon:"http://67.20.73.222/weather/ic_15_thunderstorms.png", label: ""
            state "14", icon:"http://67.20.73.222/weather/ic_16_mostly_cloudy_w_thunder_showers.png", label: ""
            state "15", icon:"http://67.20.73.222/weather/ic_17_partly_sunny_w_thunder_showers.png", label: ""
            state "16", icon:"http://67.20.73.222/weather/ic_18_rain.png", label: ""
            state "17", icon:"http://67.20.73.222/weather/ic_19_flurries.png", label: ""
            state "18", icon:"http://67.20.73.222/weather/ic_20_mostly_cloudy_w_flurries.png", label: ""
            state "19", icon:"http://67.20.73.222/weather/ic_21_partly_sunny_w_flurries.png", label: ""
            state "20", icon:"http://67.20.73.222/weather/ic_22_snow.png", label: ""
            state "21", icon:"http://67.20.73.222/weather/ic_23_mostly_cloudy_w_snow.png", label: ""
            state "22", icon:"http://67.20.73.222/weather/ic_24_ice.png", label: ""
            state "23", icon:"http://67.20.73.222/weather/ic_25_sleet.png", label: ""
            state "24", icon:"http://67.20.73.222/weather/ic_26_freezing_rain.png", label: ""
            state "25", icon:"http://67.20.73.222/weather/ic_29_rain_and_snow.png", label: ""
            state "26", icon:"http://67.20.73.222/weather/ic_30_hot.png", label: ""
            state "27", icon:"http://67.20.73.222/weather/ic_31_cold.png", label: ""
            state "28", icon:"http://67.20.73.222/weather/ic_32_windy.png", label: ""
            state "29", icon:"http://67.20.73.222/weather/ic_33_clear.png", label: ""
            state "30", icon:"http://67.20.73.222/weather/ic_34_mostly_clear.png", label: ""
            state "31", icon:"http://67.20.73.222/weather/ic_35_partly_cloudy.png", label: ""
            state "32", icon:"http://67.20.73.222/weather/ic_36_intermittent_clouds.png", label: ""
            state "33", icon:"http://67.20.73.222/weather/ic_37_hazy_moonlight.png", label: ""
            state "34", icon:"http://67.20.73.222/weather/ic_38_mostly_cloudy.png", label: ""
            state "35", icon:"http://67.20.73.222/weather/ic_39_partly_cloudy_w_shower.png", label: ""
            state "36", icon:"http://67.20.73.222/weather/ic_40_mostly_cloudy_w_showers.png", label: ""
            state "37", icon:"http://67.20.73.222/weather/ic_41_partly_cloudy_w_thunderstorms.png", label: ""
            state "38", icon:"http://67.20.73.222/weather/ic_42_mostly_cloudy_w_thunderstorms.png", label: ""
            state "39", icon:"http://67.20.73.222/weather/ic_43_mostly_cloudy_w_flurries.png", label: ""
            state "40", icon:"http://67.20.73.222/weather/ic_44_mostly_cloudy_w_snow.png", label: ""
        }

        valueTile("summary", "device.summary", decoration: "flat", width: 3, height: 3) {
            state "default", label:'${currentValue}', action: "refresh"
        }
        
        standardTile("refresh", "device.weather", decoration: "flat") {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }

        valueTile("alert", "device.alert", width: 3, height: 1, decoration: "flat") {
            state "default", label:'${currentValue}'
        }

        valueTile("rise", "device.localSunrise", decoration: "flat") {
            state "default", label:'${currentValue}'
        }

        valueTile("set", "device.localSunset", decoration: "flat") {
            state "default", label:'${currentValue}'
        }

        valueTile("hr1h", "device.time1hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}'
        }
        
        valueTile("hr1", "device.summary1hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}\n'
        }
        
        standardTile("weatherIcon1h", "device.icon1hr", decoration: "flat", height: 1, width:1) {
            state "1", icon:"http://67.20.73.222/weather/ic_01_sunny.png", label: ""
            state "2", icon:"http://67.20.73.222/weather/ic_02_mostly_sunny.png", label: ""
            state "3", icon:"http://67.20.73.222/weather/ic_03_partly_sunny.png", label: ""
            state "4", icon:"http://67.20.73.222/weather/ic_04_intermittent_clouds.png", label: ""
            state "5", icon:"http://67.20.73.222/weather/ic_05_hazy_sunshine.png", label: ""
            state "6", icon:"http://67.20.73.222/weather/ic_06_mostly_cloudy.png", label: ""
            state "7", icon:"http://67.20.73.222/weather/ic_07_cloudy.png", label: ""
            state "8", icon:"http://67.20.73.222/weather/ic_08_dreary.png", label: ""
            state "9", icon:"http://67.20.73.222/weather/ic_11_fog.png", label: ""
            state "10", icon:"http://67.20.73.222/weather/ic_12_showers.png", label: ""
            state "11", icon:"http://67.20.73.222/weather/ic_13_mostly_cloudy_w_showers.png", label: ""
            state "12", icon:"http://67.20.73.222/weather/ic_14_partly_sunny_w_showers.png", label: ""
            state "13", icon:"http://67.20.73.222/weather/ic_15_thunderstorms.png", label: ""
            state "14", icon:"http://67.20.73.222/weather/ic_16_mostly_cloudy_w_thunder_showers.png", label: ""
            state "15", icon:"http://67.20.73.222/weather/ic_17_partly_sunny_w_thunder_showers.png", label: ""
            state "16", icon:"http://67.20.73.222/weather/ic_18_rain.png", label: ""
            state "17", icon:"http://67.20.73.222/weather/ic_19_flurries.png", label: ""
            state "18", icon:"http://67.20.73.222/weather/ic_20_mostly_cloudy_w_flurries.png", label: ""
            state "19", icon:"http://67.20.73.222/weather/ic_21_partly_sunny_w_flurries.png", label: ""
            state "20", icon:"http://67.20.73.222/weather/ic_22_snow.png", label: ""
            state "21", icon:"http://67.20.73.222/weather/ic_23_mostly_cloudy_w_snow.png", label: ""
            state "22", icon:"http://67.20.73.222/weather/ic_24_ice.png", label: ""
            state "23", icon:"http://67.20.73.222/weather/ic_25_sleet.png", label: ""
            state "24", icon:"http://67.20.73.222/weather/ic_26_freezing_rain.png", label: ""
            state "25", icon:"http://67.20.73.222/weather/ic_29_rain_and_snow.png", label: ""
            state "26", icon:"http://67.20.73.222/weather/ic_30_hot.png", label: ""
            state "27", icon:"http://67.20.73.222/weather/ic_31_cold.png", label: ""
            state "28", icon:"http://67.20.73.222/weather/ic_32_windy.png", label: ""
            state "29", icon:"http://67.20.73.222/weather/ic_33_clear.png", label: ""
            state "30", icon:"http://67.20.73.222/weather/ic_34_mostly_clear.png", label: ""
            state "31", icon:"http://67.20.73.222/weather/ic_35_partly_cloudy.png", label: ""
            state "32", icon:"http://67.20.73.222/weather/ic_36_intermittent_clouds.png", label: ""
            state "33", icon:"http://67.20.73.222/weather/ic_37_hazy_moonlight.png", label: ""
            state "34", icon:"http://67.20.73.222/weather/ic_38_mostly_cloudy.png", label: ""
            state "35", icon:"http://67.20.73.222/weather/ic_39_partly_cloudy_w_shower.png", label: ""
            state "36", icon:"http://67.20.73.222/weather/ic_40_mostly_cloudy_w_showers.png", label: ""
            state "37", icon:"http://67.20.73.222/weather/ic_41_partly_cloudy_w_thunderstorms.png", label: ""
            state "38", icon:"http://67.20.73.222/weather/ic_42_mostly_cloudy_w_thunderstorms.png", label: ""
            state "39", icon:"http://67.20.73.222/weather/ic_43_mostly_cloudy_w_flurries.png", label: ""
            state "40", icon:"http://67.20.73.222/weather/ic_44_mostly_cloudy_w_snow.png", label: ""
        }     

        standardTile("weatherIcon2h", "device.icon2hr", decoration: "flat", height: 1, width:1) {
            state "1", icon:"http://67.20.73.222/weather/ic_01_sunny.png", label: ""
            state "2", icon:"http://67.20.73.222/weather/ic_02_mostly_sunny.png", label: ""
            state "3", icon:"http://67.20.73.222/weather/ic_03_partly_sunny.png", label: ""
            state "4", icon:"http://67.20.73.222/weather/ic_04_intermittent_clouds.png", label: ""
            state "5", icon:"http://67.20.73.222/weather/ic_05_hazy_sunshine.png", label: ""
            state "6", icon:"http://67.20.73.222/weather/ic_06_mostly_cloudy.png", label: ""
            state "7", icon:"http://67.20.73.222/weather/ic_07_cloudy.png", label: ""
            state "8", icon:"http://67.20.73.222/weather/ic_08_dreary.png", label: ""
            state "9", icon:"http://67.20.73.222/weather/ic_11_fog.png", label: ""
            state "10", icon:"http://67.20.73.222/weather/ic_12_showers.png", label: ""
            state "11", icon:"http://67.20.73.222/weather/ic_13_mostly_cloudy_w_showers.png", label: ""
            state "12", icon:"http://67.20.73.222/weather/ic_14_partly_sunny_w_showers.png", label: ""
            state "13", icon:"http://67.20.73.222/weather/ic_15_thunderstorms.png", label: ""
            state "14", icon:"http://67.20.73.222/weather/ic_16_mostly_cloudy_w_thunder_showers.png", label: ""
            state "15", icon:"http://67.20.73.222/weather/ic_17_partly_sunny_w_thunder_showers.png", label: ""
            state "16", icon:"http://67.20.73.222/weather/ic_18_rain.png", label: ""
            state "17", icon:"http://67.20.73.222/weather/ic_19_flurries.png", label: ""
            state "18", icon:"http://67.20.73.222/weather/ic_20_mostly_cloudy_w_flurries.png", label: ""
            state "19", icon:"http://67.20.73.222/weather/ic_21_partly_sunny_w_flurries.png", label: ""
            state "20", icon:"http://67.20.73.222/weather/ic_22_snow.png", label: ""
            state "21", icon:"http://67.20.73.222/weather/ic_23_mostly_cloudy_w_snow.png", label: ""
            state "22", icon:"http://67.20.73.222/weather/ic_24_ice.png", label: ""
            state "23", icon:"http://67.20.73.222/weather/ic_25_sleet.png", label: ""
            state "24", icon:"http://67.20.73.222/weather/ic_26_freezing_rain.png", label: ""
            state "25", icon:"http://67.20.73.222/weather/ic_29_rain_and_snow.png", label: ""
            state "26", icon:"http://67.20.73.222/weather/ic_30_hot.png", label: ""
            state "27", icon:"http://67.20.73.222/weather/ic_31_cold.png", label: ""
            state "28", icon:"http://67.20.73.222/weather/ic_32_windy.png", label: ""
            state "29", icon:"http://67.20.73.222/weather/ic_33_clear.png", label: ""
            state "30", icon:"http://67.20.73.222/weather/ic_34_mostly_clear.png", label: ""
            state "31", icon:"http://67.20.73.222/weather/ic_35_partly_cloudy.png", label: ""
            state "32", icon:"http://67.20.73.222/weather/ic_36_intermittent_clouds.png", label: ""
            state "33", icon:"http://67.20.73.222/weather/ic_37_hazy_moonlight.png", label: ""
            state "34", icon:"http://67.20.73.222/weather/ic_38_mostly_cloudy.png", label: ""
            state "35", icon:"http://67.20.73.222/weather/ic_39_partly_cloudy_w_shower.png", label: ""
            state "36", icon:"http://67.20.73.222/weather/ic_40_mostly_cloudy_w_showers.png", label: ""
            state "37", icon:"http://67.20.73.222/weather/ic_41_partly_cloudy_w_thunderstorms.png", label: ""
            state "38", icon:"http://67.20.73.222/weather/ic_42_mostly_cloudy_w_thunderstorms.png", label: ""
            state "39", icon:"http://67.20.73.222/weather/ic_43_mostly_cloudy_w_flurries.png", label: ""
            state "40", icon:"http://67.20.73.222/weather/ic_44_mostly_cloudy_w_snow.png", label: ""       
        }
        
        valueTile("hr2h", "device.time2hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}'
        }
        
        valueTile("hr2", "device.summary2hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}\n'
        }
        
        standardTile("weatherIcon3h", "device.icon3hr", decoration: "flat", height: 1, width:1) {
            state "1", icon:"http://67.20.73.222/weather/ic_01_sunny.png", label: ""
            state "2", icon:"http://67.20.73.222/weather/ic_02_mostly_sunny.png", label: ""
            state "3", icon:"http://67.20.73.222/weather/ic_03_partly_sunny.png", label: ""
            state "4", icon:"http://67.20.73.222/weather/ic_04_intermittent_clouds.png", label: ""
            state "5", icon:"http://67.20.73.222/weather/ic_05_hazy_sunshine.png", label: ""
            state "6", icon:"http://67.20.73.222/weather/ic_06_mostly_cloudy.png", label: ""
            state "7", icon:"http://67.20.73.222/weather/ic_07_cloudy.png", label: ""
            state "8", icon:"http://67.20.73.222/weather/ic_08_dreary.png", label: ""
            state "9", icon:"http://67.20.73.222/weather/ic_11_fog.png", label: ""
            state "10", icon:"http://67.20.73.222/weather/ic_12_showers.png", label: ""
            state "11", icon:"http://67.20.73.222/weather/ic_13_mostly_cloudy_w_showers.png", label: ""
            state "12", icon:"http://67.20.73.222/weather/ic_14_partly_sunny_w_showers.png", label: ""
            state "13", icon:"http://67.20.73.222/weather/ic_15_thunderstorms.png", label: ""
            state "14", icon:"http://67.20.73.222/weather/ic_16_mostly_cloudy_w_thunder_showers.png", label: ""
            state "15", icon:"http://67.20.73.222/weather/ic_17_partly_sunny_w_thunder_showers.png", label: ""
            state "16", icon:"http://67.20.73.222/weather/ic_18_rain.png", label: ""
            state "17", icon:"http://67.20.73.222/weather/ic_19_flurries.png", label: ""
            state "18", icon:"http://67.20.73.222/weather/ic_20_mostly_cloudy_w_flurries.png", label: ""
            state "19", icon:"http://67.20.73.222/weather/ic_21_partly_sunny_w_flurries.png", label: ""
            state "20", icon:"http://67.20.73.222/weather/ic_22_snow.png", label: ""
            state "21", icon:"http://67.20.73.222/weather/ic_23_mostly_cloudy_w_snow.png", label: ""
            state "22", icon:"http://67.20.73.222/weather/ic_24_ice.png", label: ""
            state "23", icon:"http://67.20.73.222/weather/ic_25_sleet.png", label: ""
            state "24", icon:"http://67.20.73.222/weather/ic_26_freezing_rain.png", label: ""
            state "25", icon:"http://67.20.73.222/weather/ic_29_rain_and_snow.png", label: ""
            state "26", icon:"http://67.20.73.222/weather/ic_30_hot.png", label: ""
            state "27", icon:"http://67.20.73.222/weather/ic_31_cold.png", label: ""
            state "28", icon:"http://67.20.73.222/weather/ic_32_windy.png", label: ""
            state "29", icon:"http://67.20.73.222/weather/ic_33_clear.png", label: ""
            state "30", icon:"http://67.20.73.222/weather/ic_34_mostly_clear.png", label: ""
            state "31", icon:"http://67.20.73.222/weather/ic_35_partly_cloudy.png", label: ""
            state "32", icon:"http://67.20.73.222/weather/ic_36_intermittent_clouds.png", label: ""
            state "33", icon:"http://67.20.73.222/weather/ic_37_hazy_moonlight.png", label: ""
            state "34", icon:"http://67.20.73.222/weather/ic_38_mostly_cloudy.png", label: ""
            state "35", icon:"http://67.20.73.222/weather/ic_39_partly_cloudy_w_shower.png", label: ""
            state "36", icon:"http://67.20.73.222/weather/ic_40_mostly_cloudy_w_showers.png", label: ""
            state "37", icon:"http://67.20.73.222/weather/ic_41_partly_cloudy_w_thunderstorms.png", label: ""
            state "38", icon:"http://67.20.73.222/weather/ic_42_mostly_cloudy_w_thunderstorms.png", label: ""
            state "39", icon:"http://67.20.73.222/weather/ic_43_mostly_cloudy_w_flurries.png", label: ""
            state "40", icon:"http://67.20.73.222/weather/ic_44_mostly_cloudy_w_snow.png", label: ""
        }
        
        valueTile("hr3h", "device.time3hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}'
        }
        
        valueTile("hr3", "device.summary3hr", decoration: "flat", width:1, height:1) {
            state "default", label:'${currentValue}\n'
        }

        standardTile("alertToggle", "device.aStatus", canChangeBackground: true) {
            state "default", label: "alerts off", action: "sendAlerts"
            state "off", label: "alerts off", action: "sendAlerts"
            state "on", label: "alerts on", action: "clearAlerts", backgroundColor:'#D0F0C0'
        }

        main(["temperature", "weatherIcon","realFeel"])
        details(["date","logo", "temperature", "realFeel", "weatherIcon","summary","hr1h","hr1","weatherIcon1h", "hr2h","hr2","weatherIcon2h","hr3h","hr3","weatherIcon3h", "alertToggle","refresh"])}
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def installed() {
    // runPeriodically(20*60, poll)
}

// handle commands
def poll() {
    log.debug "Run 'poll', location: ${location.name}"

    // poll the parent
    parent.refresh()
}

def sendAlerts() {
    
    sendEvent(name: "aStatus", value: "on")
    log.debug "sendAlerts"
}

def clearAlerts() {
    
    sendEvent(name: "aStatus", value: "off")
    log.debug "clearAlerts"
}

def refresh() {
    poll()
}

def configure() {
    poll()
}

