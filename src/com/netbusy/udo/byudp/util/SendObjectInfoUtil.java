package com.netbusy.udo.byudp.util;

import com.netbusy.udo.byudp.entity.BasePacketInfo;
import com.netbusy.udo.byudp.entity.SendObjectInfo;
import com.netbusy.util.datautil.DTC;

public class SendObjectInfoUtil {
    public static byte[] toBytes(SendObjectInfo info){
        int len = 48;
        boolean[] basePacketStatus = info.getPacketStatus();
        if(basePacketStatus!=null){
            len = len + basePacketStatus.length;
        }
        byte[] re = new byte[len];
        int pos = 0;
        pos = DTC.O2BCopy(re,pos,info.getUuid());
        pos = DTC.O2BCopy(re,pos,info.getId());
        pos = DTC.O2BCopy(re,pos,info.getType());
        if(basePacketStatus!=null) {
            int i = 0;
            for (boolean packetStatu : basePacketStatus) {
                re[pos+i] = DTC.booleanToByte(packetStatu);
                i++;
            }
        }
        return re;
    }
    public static  SendObjectInfo toObject(byte [] bytes){
        SendObjectInfo info= new SendObjectInfo();
        info.setUuid((String) DTC.B2OCopy(bytes,0,36,String.class));
        info.setId((Long) DTC.B2OCopy(bytes,36,8,long.class));
        info.setType((Integer) DTC.B2OCopy(bytes,44,4,int.class));
        if(bytes.length>48){
            int len = bytes.length-48;
            boolean [] basepacketStatus = new boolean[len];
            for(int i=0;i<len;i++){
                basepacketStatus[i] = DTC.byteToBoolean(bytes[48+i]);
            }
            info.setPacketStatus(basepacketStatus);
        }else {
            info.setPacketStatus(null);
        }
        return info;
    }
}
