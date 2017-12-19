package com.netbusy.udo.byudp.worker;


import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import com.netbusy.util.threadutil.ThreadUtil;
import java.io.IOException;
import java.util.Date;

public class ObjectSender implements Runnable{

    public ObjectSender(Object control, ByUdpI byUdpI) {
        this.control = control;
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
    private Object control;
    private ByUdpI byUdpI;


    public void stop(){
        runing = false;
    }
    @Override
    public void run() {
        ByLog.log("ObjectSender runing.......");
        while (runing){
            SendObject sendObject = byUdpI.pullSendObject();
            if(sendObject!=null){
                byUdpI.sendObject(sendObject);
                sendOver(sendObject);
            }else {
                ByLog.log("No Object to send.");
                ThreadUtil.waits(control);
            }
        }
        ByLog.log("ObjectSender daed!.......");
    }


    private void sendOver(SendObject sendObject){
        Object recontrol = new Date();
        BasePacket bp = PacketUtil.grSendOver(sendObject.getAddress(),sendObject.getInfo());
        byUdpI.sendCmd(bp);
    }

}
