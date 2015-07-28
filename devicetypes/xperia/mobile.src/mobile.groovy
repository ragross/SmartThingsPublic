metadata {
	// Automatically generated. Make future change here.
	definition (name: "SmartSense Camera", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Image Capture"

		fingerprint profileId: "FC01", deviceId: "0134"
	}

	simulator {
		status "image": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"

		reply "take C45F5708D89A4F3CB1A7EEEE2E0C73D9": "raw:C45F5708D89A4F3CB1A7EEEE2E0C73D900, image:C45F5708D89A4F3CB1A7EEEE2E0C73D9, result:00"
	}

	tiles {
		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "", action: "", icon: "st.camera.camera", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.take-photo", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Acquiring', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.take-photo", backgroundColor: "#FFFFFF", nextState:"taking"
		}


		main "camera"
		details(["cameraDetails", "take"])
	}
}

def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def isStateChange = isStateChange(device, name, value)

	def results = [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	log.debug "Parse results for $device: $results"

	results
}

def take() {
	def uuid = java.util.UUID.randomUUID().toString()
	uuid = uuid.replaceAll('-', '').toUpperCase()
	"take $uuid".toString()
}

private String parseName(String description) {
	if (isSupportedDescription(description)) {
		if (description.contains("image")) {
			return "image"
		}
	}
	null
}

private Map getResultCodes() {
	[
		"00": "Successful image capture",
		"01": "Could not open socket to server",
		"02": "Timeout in write to server",
		"03": "Timeout in receiving packets from camera",
		"04": "Image data arriving from wrong end device (multiple cameras?)",
		"05": "Another image acquisition is already in progress",
		"06": "Image is too large to buffer"
	]
}

private String parseValue(String description) {
	if (!isSupportedDescription(description)) {
		return description
	} else {
		def parts = description.split(',')*.trim()
		def image = ""
		def resultCode = ""
		parts.each {
			if (it.startsWith("image")) {
				image = it.replaceAll("image:", "")
			} else if (it.startsWith("result")) {
				resultCode = it.replaceAll("result:", "")
			}
		}

		if (resultCode == "00") {
			return "smartthings-smartsense-camera: ${image}.jpg".toString()
		} else {
			return "result:$resultCode"
		}
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	if (!isSupportedDescription(description)) {
		return value
	}

	if (value.contains(".jpg")) {
		return "$linkText captured an image"
	} else if (value.startsWith("result")) {
		def resultCode = value.replaceAll("result:", "")
		return resultCodes[resultCode]
	} else {
		return ""
	}
}
