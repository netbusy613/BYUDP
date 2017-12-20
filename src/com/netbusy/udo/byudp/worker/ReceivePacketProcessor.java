package com.netbusy.udo.byudp.worker;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import com.netbusy.udo.byudp.util.SendObjectInfoUtil;
import com.netbusy.util.threadutil.ThreadUtil;

import java.nio.charset.Charset;


public class ReceivePacketProcessor implements Runnable{

    public ReceivePacketProcessor(Object control, ByUdpI byUdpI) {
        this.control = control;
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
    private Object control;
    private ByUdpI byUdpI;

    @Override
    public void run() {
        ByLog.log("ReceivePacketProcessor runing.......");
        while (runing){
            BasePacket basePacket = byUdpI.pullReceivePacket();
            if(basePacket!=null) {
                ByLog.log("ReceivePacketProcessor doing.......");
                BasePacketInfo info = basePacket.getInfo();
                switch (info.getType()) {
                    case DataType.Data:
                        receivedData(basePacket);
                        break;
                    default:
                        byUdpI.pushCmd(basePacket);
                        break;
                }
            }else {
                ThreadUtil.waits(control);
            }
        }
        ByLog.log("ReceivePacketProcessor daed!.......");
    }

    private void receivedData(BasePacket basePacket){
        SendObject sendObject = byUdpI.receiveData(basePacket);
        if(sendObject!=null){
            ByLog.log("get A Send Object len="+sendObject.getData().length);
            String msg = new String(sendObject.getData(), Charset.forName("UTF-8"));
            ByLog.log(msg);
        }
    }

}
