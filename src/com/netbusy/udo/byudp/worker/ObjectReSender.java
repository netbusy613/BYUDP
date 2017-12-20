package com.netbusy.udo.byudp.worker;


import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.BasePacket;
import com.netbusy.udo.byudp.entity.SendObject;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.util.PacketUtil;
import com.netbusy.util.threadutil.ThreadUtil;

import java.util.Date;

public class ObjectReSender implements Runnable{

    public ObjectReSender(Object control, ByUdpI byUdpI) {
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
        ByLog.log("ObjectReSender runing.......");
        while (runing){
            SendObject sendObject = byUdpI.pullReSendObject();
            if(sendObject!=null){
                byUdpI.sendObject(sendObject);
                sendOver(sendObject);
            }else {
                ByLog.log("No ReObject to send.");
                ThreadUtil.waits(control);
            }
        }
        ByLog.log("ObjectReSender daed!.......");
    }


    private void sendOver(SendObject sendObject){
        BasePacket bp = PacketUtil.grSendOver(sendObject.getAddress(),sendObject.getInfo());
        byUdpI.sendCmd(bp);
    }

}
