package com.netbusy.udo.byudp.worker;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.exception.CRC32CheckException;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import com.netbusy.udo.byudp.util.ReplyControlUtil;
import com.netbusy.udo.byudp.util.SendObjectInfoUtil;
import com.netbusy.util.datautil.DTC;
import com.netbusy.util.threadutil.ThreadUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.Date;

public class Receiver implements Runnable{

    public Receiver(Object control,ByUdpI byUdpI) {
        this.control = control;
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
    private  Object control;
    private ByUdpI byUdpI;
    @Override
    public void run() {
        ByLog.log("Receiver running.......");
        while (runing) {
            byte[] buf = new byte[2000];
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            try {
                byUdpI.getSocket().receive(datagramPacket);
                BasePacket basePacket = PacketUtil.dp2bp(datagramPacket);
                BasePacketData info = basePacket.getBasePacketData();
                ByLog.log("Received a packet! [id="+info.getId()+"] [tot="+info.getTot()+"] [num="+info.getNum()+"] [type="+info.getType()+"] [len="+datagramPacket.getLength()+"]");
                if(!byUdpI.ifReceived(basePacket)) {
                    switch (info.getType()) {
                        case DataType.Data:
                            receivedData(basePacket);
                            break;
                        case DataType.SendOver:
                            receivedSendOver(basePacket);
                            break;
                        case DataType.NeedPacket:
                            receivedNeedPackets(basePacket);
                            break;
                        case DataType.Copy:
                            break;
                    }
                }else {
                    ByLog.log(" packet has bean received hashcode="+basePacket.getBasePacketData().hashCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CRC32CheckException e) {
                ByLog.err("packet Crc32 Check Error!");
                e.printStackTrace();
            }
        }
        ByLog.log("Receiver dead!");
    }

    private void receivedData(BasePacket basePacket){
        SendObject sendObject = byUdpI.receiveData(basePacket);
        if(sendObject!=null){
            ByLog.log("get A Send Object len="+sendObject.getData().length);
            String msg = new String(sendObject.getData(), Charset.forName("UTF-8"));
            ByLog.log(msg);
        }
    }
    private void receivedSendOver(BasePacket basePacket){
        byte[] dat = basePacket.getBasePacketData().getData();
        SendObjectInfo info = SendObjectInfoUtil.toObject(dat);
        ByLog.log("received a sendOver packet! oid="+info.getId());
        SendObject sendObject = byUdpI.getReceivedObject(info);
        if(sendObject!=null){
            boolean checkOver = checkSendOver(sendObject.getObjinfo());
            if(checkOver){
                BasePacket receivedAll = PacketUtil.grReceivedAll(basePacket.getAddress(),sendObject.getInfo());
                try {
                    byUdpI.getSocket().send(receivedAll.getDatagramPacket());
                    ByLog.log("send a ReceivedAll pachet! replyControl1 id ="+sendObject.getInfo().getId());
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }else {
                BasePacket needed = PacketUtil.grNeedPackets(basePacket.getAddress(),sendObject.getInfo());
                try {
                    byUdpI.getSocket().send(needed.getDatagramPacket());
                    ByLog.err("Need packets id="+info.getId());
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }else {
            ByLog.log("sendObject === null");
            BasePacket needed = PacketUtil.grNeedPackets(basePacket.getAddress(),info);
            try {
                byUdpI.getSocket().send(needed.getDatagramPacket());
                ByLog.err("Need ALL packets id="+info.getId());
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }

    }

    private void doCopy(BasePacket basePacket){
        SendObjectInfo key = SendObjectInfoUtil.toObject(basePacket.getBasePacketData().getData());
        ByLog.log("do Copy!");
        byUdpI.setAndNofifyReplayControl(key,PacketStatu.OVER);
    }

    private void receivedReceivedAll(BasePacket basePacket){
        SendObjectInfo key = SendObjectInfoUtil.toObject(basePacket.getBasePacketData().getData());
        ByLog.log("do receivedALLPackets!");
        byUdpI.setAndNofifyReplayControl(key,PacketStatu.OVER);
    }

    private void receivedNeedPackets(BasePacket basePacket){
        ByLog.log("do receivedNeedPackets");
        SendObjectInfo key = SendObjectInfoUtil.toObject(basePacket.getBasePacketData().getData());
        //byUdpI.setAndNofifyReplayControl(key,PacketStatu.NEED);
    }

    private boolean checkSendOver(BasePacketInfo[] infos){
        for (BasePacketInfo info:infos){
            if(info==null)
                return false;
        }
        return true;
    }
}
