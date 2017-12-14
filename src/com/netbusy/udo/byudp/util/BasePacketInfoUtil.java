package com.netbusy.udo.byudp.util;

import com.netbusy.udo.byudp.entity.BasePacketInfo;
import com.netbusy.util.datautil.DTC;

import java.util.Date;

public class BasePacketInfoUtil {
    public static byte[] toBytes(BasePacketInfo info){
        byte[] re = new byte[64];
        if(info==null){
            return null;
        }
        int pos = 0;
        pos = DTC.O2BCopy(re,pos,info.getClientId());
        pos = DTC.O2BCopy(re,pos,info.getId());
        pos = DTC.O2BCopy(re,pos,info.getTot());
        pos = DTC.O2BCopy(re,pos,info.getNum());
        pos = DTC.O2BCopy(re,pos,info.getType());
        pos = DTC.O2BCopy(re,pos,(long)info.getSendTime().getTime());
        return re;
    }
    public static  BasePacketInfo toObject(byte [] bytes){
        BasePacketInfo info= new BasePacketInfo();
        info.setClientId((String) DTC.B2OCopy(bytes,0,36,String.class));
        info.setId((Long) DTC.B2OCopy(bytes,36,8,long.class));
        info.setTot((Integer) DTC.B2OCopy(bytes,44,4,int.class));
        info.setNum((Integer) DTC.B2OCopy(bytes,48,4,int.class));
        info.setType((Integer) DTC.B2OCopy(bytes,52,4,int.class));
        info.setSendTime(new Date((Long)( DTC.B2OCopy(bytes,56,8,long.class))));
        return info;
    }
}
