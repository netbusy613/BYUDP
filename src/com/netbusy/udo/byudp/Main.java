package com.netbusy.udo.byudp;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.factory.ByFactory;
import com.netbusy.udo.byudp.util.BasePacketInfoUtil;
import com.netbusy.udo.byudp.util.SendObjectInfoUtil;
import com.netbusy.util.datautil.DTC;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        BasePacketInfo info = new BasePacketInfo(ByFactory.getByUdp().clientID(),1,1,1, DataType.ReceivedAll);
        ByLog.log(info.getClientId());

        byte[] data = DTC.objectToBytes(info);
        ByLog.log(data.length);
        byte[] data1 = BasePacketInfoUtil.toBytes(info);
        ByLog.log(data1.length);
        BasePacketInfo info1 = BasePacketInfoUtil.toObject(data1);
        ByLog.log(info1.getClientId());
        boolean[] infos = {true,true,true,true,true,true,true,true,true,false};
        SendObjectInfo sendObjectInfo = new SendObjectInfo(ByFactory.getByUdp().clientID(),1,DataType.ReceivedAll,infos);
        byte[] bytes = SendObjectInfoUtil.toBytes(sendObjectInfo);
        ByLog.log(bytes.length);
    }
}
