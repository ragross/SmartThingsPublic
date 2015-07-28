/**
 *  KHANH HANH V2
 *  Copyright 2015 Vo Thanh Minh
 */
definition(
    name: "KHANH HANH V2",
    namespace: "VSMART",
    author: "Vo Thanh Minh",
    description: "VSMART",
    category: "Family",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("Cảm biến chuyển động")
    {
    	input("motionPK","capability.motionSensor",title:"Cảm biến phòng khách")
    }
    section("Cảm biến hiện diện")
    {
    	input("presenceKhanh","capability.presenceSensor",title:"Cảm biến hiện diện Khánh")
    }
    section("Cảm biến đóng/mở")
    {
    	input("contactCuaChinh","capability.contactSensor",title:"Cảm biến đóng mở cửa chính")
    }
    section("Camera quan sát")
    {
    	//input("cameraPK","capability.imageCapture")
       
    }  
}
def installed() 
{
	subscribe(motionPK,"motion",motion_PK)
    subscribe(motionPK,"motion.active",takePhotos)
    subscribe(presenceKhanh,"presence",presence_Khanh)
    subscribe(contactCuaChinh,"contact",contact_CuaChinh)
}
def updated() 
{
	subscribe(motionPK,"motion",motion_PK)
    subscribe(presenceKhanh,"presence",presence_Khanh)
}
/*
def takePhotos(evt)
{
	cameraPK.take()
	(1..4) .each
	{
    	cameraPK.take(delay:(1000*it))
        log.debug "$camera.currentImage"
	}
}
*/
def presence_Khanh(evt)
{
	if (evt.value=="present")
		{
			thongbao("$evt.linkText Chào mừng bạn đã về")
    	}
	else
		{
			
            HoatDong_2();
		}	
}
//***************
def motion_PK(evt)
{	
	if(evt.value=="active")
		{
    		int kq=TT_motionPK(100);
            
    		HoatDong_1(); 
		}

	if(evt.value=="inactive")
		{
    		int kq=TT_motionPK(999);
    		HoatDong_1();
		}
}
//***************
def thongbao(msg){
	sendPush(msg)
}
//***************
private int TT_motionPK(int x)
{
	return x;
}
//***************
//Kiểm tra chuyển động trong phòng khách với hai trường hợp có bạn ở nhà hay không 
private void HoatDong_1(){
	def tt=presenceKhanh.currentValue("presence");
    if (tt=="present")
    	{
    		thongbao("Có chuyển động trong phòng khách, nhưng bạn đang ở nhà");
   		}
    else
 		{
    		thongbao("Có chuyển động trong phòng khách!");
    	}
}
// Bạn ra khỏi nhà nhưng hiện tại cửa chính đang mở.
public void HoatDong_2()
{
	def tt=presenceKhanh.currentValue("presence");
    if (tt=="not present")
    {
    	def tt_cuachinh=contactCuaChinh.currentValue("contact");
        if (tt_cuachinh=="open")
        	{
            	thongbao("Bạn ra khỏi nhà nhưng hiện tại cửa chính đang mở. Camera sẵn sàng để bạn quan sát! ")
            }
         else
         	{
        	 thongbao("Bạn đã ra khỏi nhà")
        	 }
    }
}