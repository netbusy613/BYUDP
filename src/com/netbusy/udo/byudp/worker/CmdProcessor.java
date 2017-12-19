package com.netbusy.udo.byudp.worker;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.BasePacket;
import com.netbusy.udo.byudp.entity.DataType;
import com.netbusy.udo.byudp.entity.SendObject;
import com.netbusy.udo.byudp.entity.SendObjectInfo;
import com.netbusy.udo.byudp.factory.ByUdpI;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import com.netbusy.udo.byudp.util.SendObjectInfoUtil;
import com.netbusy.util.threadutil.ThreadUtil;


public class CmdProcessor implements Runnable{

    public CmdProcessor(Object control, ByUdpI byUdpI) {
        this.control = control;
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
    private Object control;
    private ByUdpI byUdpI;

    @Override
    public void run() {
        ByLog.log("CmdProcessor runing.......");
        while (runing){
            BasePacket cmdpacket = byUdpI.pullCmd();
            if(cmdpacket!=null){
                switch (cmdpacket.getInfo().getType()){
                    case DataType.SendOver:
                        doSendOver(cmdpacket);
                        break;
                    case DataType.NeedPacket:
                        doNeedPackets(cmdpacket);
                        break;
                    case DataType.ReceivedAll:
                        doReceivedAll(cmdpacket);
                        break;
                    default:
                        ByLog.err("UNKNOWN CMD.......");
                        break;
                }
            }else {
                ByLog.log("No cmdList to do.");
                ThreadUtil.waits(control);
            }
        }
        ByLog.log("ObjectSender daed!.......");
    }

    private void doSendOver(BasePacket basePacket){
        SendObjectInfo sendObjectInfo= SendObjectInfoUtil.toObject(basePacket.getBasePacketData());
        SendObject sendObject = byUdpI.getReceivedObject(sendObjectInfo);
        BasePacket cmdpacket = null;
        if(sendObject==null){
            cmdpacket = PacketUtil.grNeedPackets(basePacket.getAddress(),sendObjectInfo);
            ByLog.err("doSendOver Error do Not find Obj id="+sendObjectInfo.getId());
        }else {
            if(checkOver(sendObject.getInfo().getPacketStatus())){
                cmdpacket = PacketUtil.grReceivedAll(basePacket.getAddress(),sendObjectInfo);
            }else {
                cmdpacket = PacketUtil.grNeedPackets(basePacket.getAddress(),sendObject.getInfo());
            }
        }
        if(cmdpacket!=null) {
            byUdpI.sendCmd(cmdpacket);
        }
    }

    private void doNeedPackets(BasePacket basePacket){
        SendObjectInfo sendObjectInfo = SendObjectInfoUtil.toObject(basePacket.getBasePacketData());
        SendObject sendObject = byUdpI.findSendCache(sendObjectInfo);
        //setSendMax(sendObjectInfo,sendObject.getInfo());
        sendObject.setInfo(sendObjectInfo);
        byUdpI.pushSendObject(sendObject);
        ByLog.err("doNeedPackets "+sendObjectInfo);
    }


    private void setSendMax(SendObjectInfo receivedInfo, SendObjectInfo sendInfo){
        boolean[] Rstatus = receivedInfo.getPacketStatus();
        boolean[] Sstatus = sendInfo.getPacketStatus();
        int temp =1;
        if(Sstatus!=null) {
            if (Rstatus != null) {
                int i = 0,j=0;
                for (boolean st : Rstatus) {
                    if (!st)
                        i++;
                }
                for (boolean st : Sstatus) {
                    if (!st)
                        j++;
                }
                temp = ((int) Math.floor(i/j)) * Statics.sendMaxs / 100;
                if (temp > 1) {
                    Statics.sendMaxs = temp;
                } else {
                    Statics.sendMaxs = 1;
                }
                ByLog.err("SET SEND MAX = " + Statics.sendMaxs);
            }
        }else {
            if (Rstatus != null) {
                int i = 0;
                for (boolean st : Rstatus) {
                    if (!st)
                        i++;
                }
                temp = ((int) Math.floor(100 * i / Rstatus.length)) * Statics.sendMaxs / 100;
            }
        }
        if (temp > 1) {
            Statics.sendMaxs = temp;
        } else {
            Statics.sendMaxs = 1;
        }
        ByLog.err("SET SEND MAX = " + Statics.sendMaxs);
    }

    private void doReceivedAll(BasePacket basePacket){
        SendObjectInfo sendObjectInfo = SendObjectInfoUtil.toObject(basePacket.getBasePacketData());
        byUdpI.cleanSendCache(sendObjectInfo);
        ByLog.err("doReceivedAll "+sendObjectInfo);
    }

    private boolean checkOver(boolean[] packStatus){
        for (boolean ps:packStatus) {
            if(!ps)
                return false;
        }
        return true;
    }

}
