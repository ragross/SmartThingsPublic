metadata {
  // Automatically generated. Make future change here.
  definition (name: "Mobile Presence*", namespace: "smartthings", author: "SmartThings") {
    capability "Presence Sensor"
    capability "Sensor"

    command "away"
    command "present"
  }

  simulator {
    status "present": "presence: 1"
    status "not present": "presence: 0"
  }

  tiles {
    standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true, inactiveLabel: true) {
      state("present", action: "away", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
      state("not present", action: "present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff")
    }
    main "presence"
    details "presence"
  }
}

def away() {
  sendEvent(name: 'presence', value: 'not present')
}

def present() {
  sendEvent(name: 'presence', value: 'present')
}

def parse(String description) {
  def name = parseName(description)
  def value = parseValue(description)
  def linkText = getLinkText(device)
  def descriptionText = parseDescriptionText(linkText, value, description)
  def handlerName = getState(value)
  def isStateChange = isStateChange(device, name, value)

  def results = [
    name: name,
    value: value,
    unit: null,
    linkText: linkText,
    descriptionText: descriptionText,
    handlerName: handlerName,
    isStateChange: isStateChange,
    displayed: displayed(description, isStateChange)
  ]
  log.debug "Parse returned $results.descriptionText"
  return results

}

private String parseName(String description) {
  if (description?.startsWith("presence: ")) {
    return "presence"
  }
  null
}

private String parseValue(String description) {
  switch(description) {
    case "presence: 1": return "present"
    case "presence: 0": return "not present"
    default: return description
  }
}

private parseDescriptionText(String linkText, String value, String description) {
  switch(value) {
    case "present": return "$linkText has arrived"
    case "not present": return "$linkText has left"
    default: return value
  }
}

private getState(String value) {
  switch(value) {
    case "present": return "arrived"
    case "not present": return "left"
    default: return value
  }
}
