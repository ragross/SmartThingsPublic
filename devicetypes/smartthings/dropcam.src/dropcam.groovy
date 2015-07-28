/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Dropcam", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Image Capture"
	}

	simulator {
		status "image": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"

		reply "take C45F5708D89A4F3CB1A7EEEE2E0C73D9": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"
	}

	tiles {
		standardTile("image", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
			state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.dropcam", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
		}

		main "image"
		details(["cameraDetails", "take"])
	}
}

def uninstalled() {
	parent?.removeChildFromSettings(this)
}

// parse events into attributes
def parse(String description)
{
	log.debug "Parsing '${description}'"
	// TODO: handle 'image' attribute
	// TODO: handle '' attribute

}

// handle commands
def take()
{
	log.debug "Executing 'take'"

	def dni = device.deviceNetworkId
	log.debug "Executing 'take' with dni $dni"


	log.debug "Executing 'take' with dni $dni and parent $parent with app ${parent.installedSmartApp}"

	def imageBytes = parent.takePicture(dni, null)

	log.debug "got bytes"

	if(imageBytes)
	{
		storeImage(getPictureName(), imageBytes)
	}
}

private getPictureName()
{
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	return device.deviceNetworkId + "_$pictureUuid" + ".jpg"
}
