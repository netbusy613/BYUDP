package com.netbusy.udo.byudp.worker;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.exception.CRC32CheckException;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.BasePacketInfoUtil;
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
                BasePacketInfo info = basePacket.getInfo();
                ByLog.log("Received a packet! {"+info+"}");
                switch (info.getType()){
                    case DataType.Data:
                        receivedData(basePacket);
                        break;
                        case DataType.Copy:
                            doCopy(basePacket);
                            break;
                    default:
                        sendCopy(basePacket);
                        if(!byUdpI.ifReceived(basePacket)) {
                            byUdpI.pushCmd(basePacket);
                        }
                        break;
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
        if(!byUdpI.ifReceived(basePacket)){
            SendObject sendObject = byUdpI.receiveData(basePacket);
            if(sendObject!=null){
                ByLog.log("get A Send Object len="+sendObject.getData().length);
                String msg = new String(sendObject.getData(), Charset.forName("UTF-8"));
                ByLog.log(msg);
            }
        }
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
