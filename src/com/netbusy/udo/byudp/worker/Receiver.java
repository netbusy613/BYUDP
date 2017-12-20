package com.netbusy.udo.byudp.worker;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.BasePacket;
import com.netbusy.udo.byudp.entity.BasePacketInfo;
import com.netbusy.udo.byudp.entity.DataType;
import com.netbusy.udo.byudp.entity.SendObject;
import com.netbusy.udo.byudp.exception.CRC32CheckException;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.util.BasePacketInfoUtil;
import com.netbusy.udo.byudp.util.PacketUtil;

import java.io.IOException;
import java.net.DatagramPacket;

public class Receiver implements Runnable{

    public Receiver(
            ByUdpI byUdpI) {
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
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
                BasePacketInfo info = basePacket.getInfo();
                switch (info.getType()){
                    case DataType.Data:
                        if(!byUdpI.ifReceived(basePacket)) {
                            byUdpI.pushReceivePacket(basePacket);
                            ByLog.log("Received a Data packet! {"+info+"}");
                        }
                        break;
                    case DataType.Copy:
                        ByLog.log("Received a Copy packet! {"+info+"}");
                        doCopy(basePacket);
                        break;
                    default:
                        ByLog.log("Received a CMD packet! {"+info+"}");
                        sendCopy(basePacket);
                        if(!byUdpI.ifReceived(basePacket)) {
                            byUdpI.pushReceivePacket(basePacket);
                        }

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






    private void doCopy(BasePacket basePacket){
//        ByLog.err("do Copy! len"+basePacket.getBasePacketData().length+ "  info="+basePacket.getInfo());
        BasePacketInfo key = BasePacketInfoUtil.toObject(basePacket.getBasePacketData());
        ByLog.log("do Copy! "+basePacket.getInfo());
        byUdpI.setAndNofifyReplayControl(key,true);
    }
    private void sendCopy(BasePacket basePacket){
        BasePacket copy = PacketUtil.grCopy(basePacket);
        try {
            byUdpI.getSocket().send(copy.getDatagramPacket());
            ByLog.log("send Copy! "+copy.getInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
