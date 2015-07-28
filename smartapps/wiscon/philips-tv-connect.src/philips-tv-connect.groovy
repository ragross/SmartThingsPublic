/**
 *  Philips TV Service Manager
 *
 *  Author: Geurt Wisselink
 *  Date: 2014-11-29
 */
definition(
    name: "Philips TV (Connect)",
    namespace: "Wiscon",
    author: "Geurt Wisselink",
    description: "Allows you to integrate your Philips TV with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "http://a5.mzstatic.com/eu/r30/Purple/v4/95/c4/e5/95c4e5de-d3c6-e22a-4e5d-7eea775328da/icon_64.png",
    iconX2Url: "http://a5.mzstatic.com/eu/r30/Purple/v4/95/c4/e5/95c4e5de-d3c6-e22a-4e5d-7eea775328da/icon_128.png"
)

preferences {
	page(name:"firstPage", title:"Philips TV Device Setup", content:"firstPage")
}

private discoverAllPhilipsTVTypes()
{
log.debug "Discover :: lan discovery urn:schemas-upnp-org:device:MediaRenderer:1"
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:MediaRenderer:1", physicalgraph.device.Protocol.LAN))
}

private getFriendlyName(String deviceNetworkId, String ssdpPath) {
	sendHubCommand(new physicalgraph.device.HubAction("""GET ${ssdpPath} HTTP/1.1
HOST: ${deviceNetworkId}

""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

private verifyDevices() {
	def switches = getPhilipsTVSwitches().findAll { it?.value?.verified != true }
	def devices = switches
	devices.each {
		getFriendlyName((it.value.ip + ":" + it.value.port), it.value.ssdpPath)
	}
}

def firstPage()
{
	if(canInstallLabs())
	{
		int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
		state.refreshCount = refreshCount + 1
		def refreshInterval = 5

		log.debug "REFRESH COUNT :: ${refreshCount}"

		if(!state.subscribe) {
			subscribe(location, null, locationHandler, [filterEvents:false])
			state.subscribe = true
		}

		//ssdp request every 25 seconds
		if((refreshCount % 5) == 0) {
			discoverAllPhilipsTVTypes()
		}

		//setup.xml request every 5 seconds except on discoveries
		if(((refreshCount % 1) == 0) && ((refreshCount % 5) != 0)) {
			verifyDevices()
		}

		def switchesDiscovered = switchesDiscovered()

		return dynamicPage(name:"firstPage", title:"Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: selectedSwitches != null ) {
			section("Select a device...") {
				input "selectedSwitches", "enum", required:false, title:"Select Philips TV Switches \n(${switchesDiscovered.size() ?: 0} found)", multiple:true, options:switchesDiscovered
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"firstPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}

def devicesDiscovered() {
	def switches = getPhilipsTVSwitches()
	def list = []

	list = devices?.collect{ [app.id, it.ssdpUSN].join('.') }
}

def switchesDiscovered() {
	def switches = getPhilipsTVSwitches().findAll { it?.value?.verified == true }
	def map = [:]
	switches.each {
		def value = it.value.name ?: "Philips TV Switch ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = it.value.mac
		map["${key}"] = value
	}
	map
}

def getPhilipsTVSwitches()
{
	if (!state.switches) { state.switches = [:] }
	state.switches
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()

	runIn(5, "subscribeToDevices") //initial subscriptions delayed by 5 seconds
	runIn(10, "refreshDevices") //refresh devices, delayed by 10 seconds
	runIn(900, "doDeviceSync" , [overwrite: false]) //setup ip:port syncing every 15 minutes

	// SUBSCRIBE responses come back with TIMEOUT-1801 (30 minutes), so we refresh things a bit before they expire (29 minutes)
	runIn(1740, "refresh", [overwrite: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()

	runIn(5, "subscribeToDevices") //subscribe again to new/old devices wait 5 seconds
	runIn(10, "refreshDevices") //refresh devices again, delayed by 10 seconds
}

def resubscribe() {
	log.debug "Resubscribe called, delegating to refresh()"
	refresh()
}

def refresh() {
	log.debug "refresh() called"
	//reschedule the refreshes
	runIn(1740, "refresh", [overwrite: false])
	refreshDevices()
}

def refreshDevices() {
	log.debug "refreshDevices() called"
	def devices = getAllChildDevices()
	devices.each { d ->
		log.debug "Calling refresh() on device: ${d.id}"
		d.refresh()
	}
}

def subscribeToDevices() {
	log.debug "subscribeToDevices() called"
	def devices = getAllChildDevices()
	devices.each { d ->
		d.subscribe()
	}
}

def addSwitches() {
	log.debug "addSwitches() called"
	def switches = getPhilipsTVSwitches()

	selectedSwitches.each { dni ->
		def selectedSwitch = switches.find { it.value.mac == dni } ?: switches.find { "${it.value.ip}:${it.value.port}" == dni }
		def d
		if (selectedSwitch) {
			d = getChildDevices()?.find {
				it.dni == selectedSwitch.value.mac || it.device.getDataValue("mac") == selectedSwitch.value.mac
			}
		}

		if (!d) {
			log.debug "Creating Philips TV Switch with dni: ${selectedSwitch.value.mac}"
			d = addChildDevice("Wiscon", "Philips TV Switch", selectedSwitch.value.mac, selectedSwitch?.value.hub, [
				"label": selectedSwitch?.value?.name ?: "Philips TV Switch",
				"data": [
					"mac": selectedSwitch.value.mac,
					"ip": selectedSwitch.value.ip,
					"port": selectedSwitch.value.port,
					"controlPort": 785 //=port 1925
				]
			])

			log.debug "Created ${d.displayName} with id: ${d.id}, dni: ${d.deviceNetworkId}"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
	}
}

def initialize() {
	// remove location subscription afterwards
	 unsubscribe()
	 state.subscribe = false

	if (selectedSwitches)
	{
		addSwitches()
	}

}

def locationHandler(evt) {
log.info "LOCATION HANDLER: $evt.description"
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseDiscoveryMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("schemas-upnp-org:device:MediaRenderer")) {

		def switches = getPhilipsTVSwitches()

		if (!(switches."${parsedEvent.ssdpUSN.toString()}"))
		{ //if it doesn't already exist
			switches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // just update the values

			log.debug "Device was already found in state..."

			def d = switches."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false

			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				d.ssdpPath = parsedEvent.ssdpPath
				deviceChangedValues = true
				log.debug "Device's port or ip changed..."
			}

			if (deviceChangedValues) {
				def children = getChildDevices()
				log.debug "Found children ${children}"
				children.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.debug "updating ip and port, and resubscribing, for device ${it} with mac ${parsedEvent.mac}"
						it.subscribe(parsedEvent.ip, parsedEvent.port)
					}
				}
			}

		}

	}
	else if (parsedEvent.headers && parsedEvent.body) {
		
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		if (bodyString.contains("urn:schemas-upnp-org:device:MediaRenderer:1")) {
			def body = new XmlSlurper().parseText(bodyString)

			if (body?.device?.deviceType?.text().startsWith("urn:schemas-upnp-org:device:MediaRenderer:1"))
			{
				def switches = getPhilipsTVSwitches()
				def PhilipsTVSwitch = switches.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (PhilipsTVSwitch)
				{
					PhilipsTVSwitch.value << [name:body?.device?.friendlyName?.text(), verified: true]
				}
				else
				{
					log.error "/setup.xml returned a Philips TV that didn't exist"
				}
			}
		}
	}
}

private def parseDiscoveryMessage(String description) {
	def device = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			device.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				device.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				device.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				device.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				device.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				device.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				device.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				device.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				device.body = valueString
			}
		}
	}

	device
}

def doDeviceSync(){
	log.debug "Doing Device Sync!"
	runIn(900, "doDeviceSync" , [overwrite: false]) //schedule to run again in 15 minutes

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	discoverAllPhilipsTVTypes()
}

def pollChildren() {
	def devices = getAllChildDevices()
	devices.each { d ->
		//only poll switches?
		d.poll()
	}
}

def delayPoll() {
	log.debug "Executing 'delayPoll'"

	runIn(5, "pollChildren")
}

/*def poll() {
	log.debug "Executing 'poll'"
	runIn(600, "poll", [overwrite: false]) //schedule to run again in 10 minutes

	def lastPoll = getLastPollTime()
	def currentTime = now()
	def lastPollDiff = currentTime - lastPoll
	log.debug "lastPoll: $lastPoll, currentTime: $currentTime, lastPollDiff: $lastPollDiff"
	setLastPollTime(currentTime)

	doDeviceSync()
}


def setLastPollTime(currentTime) {
	state.lastpoll = currentTime
}

def getLastPollTime() {
	state.lastpoll ?: now()
}

def now() {
	new Date().getTime()
}*/

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}
