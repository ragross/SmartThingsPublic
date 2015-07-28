/**
 *  SmartSleep Sensor
 *
 *  Copyright 2015 Anat Oren
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
	definition (name: "SleepSense Sensor", namespace: "earlysense.com", author: "Anat Oren") {
		capability "Actuator"
		capability "Sleep Sensor"

    	attribute "targetmode", "String"
		attribute "targettemp", "string"
		attribute "bedstate", "string"
        
		command "setTempAndMode"
		command "setSleepState"
        command "setBedState"
        
	}

	// UI tile definitions
	tiles {
        standardTile("targettemp", "device.targettemp", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("targettemp", label: 'Recommended temperature: ${currentValue}°')
        }
        standardTile("targetmode", "device.targetmode", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("targetmode", label: 'Recommended mode: ${currentValue}')
        }
       standardTile("sleeping", "device.sleeping", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state("not sleeping", label: "Awake", icon:"st.Health & Wellness.health12", backgroundColor:"#79b821")
            state("sleeping", label: "Sleeping", icon:"st.Bedroom.bedroom12", backgroundColor:"#ffffff")
       }
       standardTile("bedstate", "device.bedstate", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		 	state("out of bed", label:'Out Of Bed', icon: "st.Health & Wellness.health12",  backgroundColor: '#ffffff')
            state("in bed", label:'In Bed', icon: "st.Bedroom.bedroom2",  backgroundColor: '#ffffff')
       }

		main "sleeping"
		details "sleeping", "bedstate", "targettemp", "targetmode"
	}
}

def setTempAndMode(temp, mode) {
	sendEvent(name: "targetmode", value: mode)
	sendEvent(name: "targettemp", value: temp)
}

def setSleepState(state){
	if(state.contains("notSleeping")){
    	state = "not sleeping"
    }

	sendEvent(name: "sleeping", value : state)
}

def setBedState(state){
	if(state == "inBed"){
    	state = "in bed"
    }
    else if(state == "outOfBed"){
    	state = "out of bed"
    }

	sendEvent(name: "bedstate", value: state)
}
