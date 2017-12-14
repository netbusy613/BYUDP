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
                sendObject(sendObject);
                sendOver(sendObject);
            }else {
                ByLog.log("No Object to send.");
                ThreadUtil.waits(1000,control);
            }
        }
        ByLog.log("ObjectSender daed!.......");
    }

    private void sendObject(SendObject sendObject){
        BasePacket[] packets = sendObject.getPackets();
        try {
            for (BasePacket basePacket : packets) {
                byUdpI.getSocket().send(basePacket.getDatagramPacket());
                BasePacketData packet = basePacket.getBasePacketData();
                ByLog.log("ObjectSender a packet![id:" + packet.getId() + "] [tot:" + packet.getTot() + "] [num:" + packet.getNum() + "]");
            }
            byUdpI.cacheSendObjects(sendObject);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendOver(SendObject sendObject){
        try {
            Object recontrol = new Date();
            ReplyControl replyControl = new ReplyControl(sendObject.getInfo(),recontrol,PacketStatu.WAIT);
            byUdpI.pushReplayControl(replyControl);
            ByLog.log("pushReplayControl id="+replyControl.getSendObjectInfo().getId());
            BasePacket bp = PacketUtil.grSendOver(sendObject.getAddress(),sendObject.getInfo());
            for (int i=0;i<10;i++){
                byUdpI.getSocket().send(bp.getDatagramPacket());
                ByLog.log("Sender a sendOver packet! "+i+" times! id="+bp.getInfo().getId());
                ThreadUtil.waits(Statics.TimeSep,recontrol);
                if(checkSendPacketStatu(sendObject,replyControl,i)){
                    return;
                }
            }
            ByLog.err("SendObject ERROR, [id="+sendObject.getPackets()[0].getBasePacketData().getId()+"] [type="+sendObject.getType()+"]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNeedPackets(SendObject sendObject,ReplyControl replyControl){
        byUdpI.sendNeedPackates(replyControl.getSendObjectInfo());
        BasePacket bp = PacketUtil.grSendOver(sendObject.getAddress(), sendObject.getInfo());
        int ifok = PacketStatu.UNKNOW;
        for(int i= 0;i<10;i++) {
            try {
                byUdpI.getSocket().send(bp.getDatagramPacket());
                ByLog.log("Sender a ResendOver packet! " + i + " times!");
                ThreadUtil.waits(Statics.TimeSep, replyControl.getControl());
                if(checkSendPacketStatu(sendObject,replyControl,i)){
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public boolean checkSendPacketStatu(SendObject sendObject,ReplyControl replyControl,int i){
        int ifok = byUdpI.checkReplayControl(replyControl.getSendObjectInfo());
        if(ifok==PacketStatu.OVER){
            byUdpI.releaseSendCachePacks(sendObject.getInfo());
            byUdpI.releaseReplyControl(sendObject.getInfo());
            ByLog.log("Send OVER");
            return true;
        }else if(ifok==PacketStatu.NEED) {
            sendNeedPackets(sendObject,replyControl);
            ByLog.log("NEED PACKET");
            return true;
        }else if(ifok==PacketStatu.WAIT){
            ByLog.log("Send ERROR time="+i);
            return false;
        }
        return false;
    }
}
