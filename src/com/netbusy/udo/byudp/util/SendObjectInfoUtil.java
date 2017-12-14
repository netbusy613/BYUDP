package com.netbusy.udo.byudp.util;

import com.netbusy.udo.byudp.entity.BasePacketInfo;
import com.netbusy.udo.byudp.entity.SendObjectInfo;
import com.netbusy.util.datautil.DTC;

public class SendObjectInfoUtil {
    public static byte[] toBytes(SendObjectInfo info){
        int len = 48;
        BasePacketInfo[] basePacketInfos = info.getPacketInfos();
        if(basePacketInfos!=null){
            len = len + basePacketInfos.length*64;
        }
        byte[] re = new byte[len];
        int pos = 0;
        pos = DTC.O2BCopy(re,pos,info.getUuid());
        pos = DTC.O2BCopy(re,pos,info.getId());
        pos = DTC.O2BCopy(re,pos,info.getType());
        if(basePacketInfos!=null) {
            for (BasePacketInfo basePacketInfo : basePacketInfos) {
                pos = DTC.O2BCopy(re, pos, BasePacketInfoUtil.toBytes(basePacketInfo));
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
            int len = (bytes.length-48)/64;
            BasePacketInfo [] infos = new BasePacketInfo[len];
            for(int i=0;i<len;i++){
                byte[] infobytes = (byte[]) DTC.B2OCopy(bytes,48+64*i,64,byte[].class);
                infos[i] = BasePacketInfoUtil.toObject(infobytes);
            }
            info.setPacketInfos(infos);
        }else {
            info.setPacketInfos(null);
        }
        return info;
    }
}
