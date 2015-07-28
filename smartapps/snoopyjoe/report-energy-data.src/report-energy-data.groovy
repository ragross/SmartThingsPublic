/**
 *  Notify and reset power data from energy meter
 *
 *  Author: Snoopyjoe
 *
 */

definition(
    name: "Report energy data",
    namespace: "SnoopyJoe",
    author: "Thompson Garner",
    description: "Notify user of power consumption then reset power data from energy meter via SMS or PUSH on schedule chosen by user",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
		  )
preferences 
{
  page(name: "MainPage")
  page(name: "DailyPage")
  page(name: "WeeklyPage")
  page(name: "MonthlyPage")
  page(name: "FinalPage")
}


def MainPage() 
{
  dynamicPage(name: "MainPage", title: "Report usage from energy meter", nextPage: "FinalPage", uninstall: true) 
  {
   section()
    {
      input "meter","capability.powerMeter", title: "Send report from this meter", required: true, multiple: false, submitOnChange: true, refreshAfterSelection: true
    }
    
      section 
      {
      	def DailyDescription
        def WeeklyDescription
        def MonthlyDescription
        input(name: "repeater", type: "enum", title: "When to send report", options:["Daily","Weekly","Monthly"], refreshAfterSelection: true, required: true)
       
       if(repeatDaily == null)
        {
        DailyDescription = "Tap to set time"
		} 
       if(repeatDaily != null)
        {
        DailyDescription = "Everyday at ${repeatDaily}00"
        }
       if(repeater == "Daily")
        {
        href(name: "toDailyPage", title: "Send report Daily", page: "DailyPage", description: "${DailyDescription}",refreshAfterSelection: true)
        }
        
        
        if(repeatDaily == null || repeatWeekly == null)
        {
        WeeklyDescription = "Tap to set time"
		} 
        if(repeatWeekly != null)
        {
        WeeklyDescription = "Every ${repeatWeekly} at ${repeatDaily}00"
        }
        if(repeater == "Weekly")
        {
        href(name: "toWeeklyPage", page: "WeeklyPage", title: "Send report Weekly", description:"${WeeklyDescription}", refreshAfterSelection: true)
        }
        
        
        if(repeatDaily == null || repeatMonthly == null)
        {
        MonthlyDescription = "Tap to set time"
		} 
        if(repeatMonthly != null)
        {
        MonthlyDescription = "Day ${repeatMonthly} of month at ${repeatDaily}00"
        }
        if(repeater == "Monthly")
        {
        href(name: "toMonthlyPage", page: "MonthlyPage", title: "Send report Monthly", description:"${MonthlyDescription}", refreshAfterSelection: true)
        }
      }
   }
}

    def FinalPage()
    {
    	dynamicPage(name: "FinalPage", title: "Chose notification method to complete", install: true)
    	{
		 section 
			{
        	input("recipients", "contact", title: "Send notifications to") 
        		{
        		input(name: "sms", type: "phone", title: "Send A SMS Text To", description: null, required: false)
       			input(name: "pushNotification", type: "bool", title: "Send a PUSH notification", description: null, defaultValue: true)
        		}
   	 		}
        }
    }
    
    def DailyPage()
    {
    	dynamicPage(name: "DailyPage", title: "Chose HOUR to send daily report", install: false, required: true)
    	{
        	section
            {
            paragraph "All times must be military time."
        	input(name: "repeatDaily", type: "enum", title: "Daily at this (hour)", options:["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"],required: true, refreshAfterSelection: true)	            
       		}
      }
   }
    
    def WeeklyPage()
    {
    	dynamicPage(name: "WeeklyPage", title: "Chose DAY to send weekly report")
    	{
        	section
            {
       		input(name: "repeatWeekly", type: "enum", title: "Weekly on this (DAY)",options:["MON","TUE","WED","THU","FRI","SAT","SUN"],required: true)
            paragraph "All times must be military time."
        	input(name: "repeatDaily", type: "enum", title: "At this (hour)", options: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"],required: true, refreshAfterSelection: true)			
        	}
        }
    }
    
    def MonthlyPage()
    {
    	dynamicPage(name: "MonthlyPage", title: "Chose DATE to send monthly report")
    	{
        	section
            {
        	input(name: "repeatMonthly", type: "number", title: "Monthly on this (Date)",required: true)
            paragraph "All times must be military time."
        	input(name: "repeatDaily", type: "enum", title: "At this (hour)", options: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"],required: true, refreshAfterSelection: true)			
        	}
        }
    }

def installed()
{
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() 
{
	log.debug "Updated with settings: ${settings}"
	unschedule()
    initialize()
}

def initialize()
{        

		if(repeater == "Daily")
        {
    		def RepeaterDaily = "0 0 ${repeatDaily} * * ?"
    		schedule(RepeaterDaily, meterHandler)
       	}
        
        if(repeater == "Weekly")
        {
    		def RepeaterWeekly = "0 0 ${repeatDaily} ? * ${repeatWeekly}"
        	schedule(RepeaterWeekly, meterHandler)

		}
        
        if(repeater == "Monthly")
        {
    		def RepeaterMonthly = "0 0 ${repeatDaily} ${repeatMonthly} * ?"
        	schedule(RepeaterMonthly, meterHandler)
		}
}

def meterHandler()
{
  	def msg = "${meter} used ${meter.latestValue("energy")}kWh during ${repeater} period."
    sendMessage(msg)
    meter.reset()
}

def sendMessage(msg) 
{
    if (location.contactBookEnabled)
    {
        sendNotificationToContacts(msg, recipients)
    }
    else 
    {
        if (sms)
        {
            sendSms(sms, msg)
        }
        if (pushNotification) 
        {
            sendPush(msg)
        }
    }
}