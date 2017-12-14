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
        BasePacketData basePacketData = new BasePacketData(ByFactory.getByUdp().clientID(),1,1,1,new Date(), DataType.ReveiveAll,new byte[0]);
        BasePacketInfo info = new BasePacketInfo(basePacketData);
        ByLog.log(info.getClientId());

        byte[] data = DTC.objectToBytes(info);
        ByLog.log(data.length);
        byte[] data1 = BasePacketInfoUtil.toBytes(info);
        ByLog.log(data1.length);
        BasePacketInfo info1 = BasePacketInfoUtil.toObject(data1);
        ByLog.log(info1.getClientId());
        BasePacketInfo[] infos = {info,info,info,info,info,info,info,info,info,info};
        SendObjectInfo sendObjectInfo = new SendObjectInfo(ByFactory.getByUdp().clientID(),1,DataType.ReveiveAll,infos);
        byte[] bytes = SendObjectInfoUtil.toBytes(sendObjectInfo);
        ByLog.log(bytes.length);
    }
}
