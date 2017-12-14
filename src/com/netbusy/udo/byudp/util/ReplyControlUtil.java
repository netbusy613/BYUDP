package com.netbusy.udo.byudp.util;

import com.netbusy.udo.byudp.entity.ReplyControl;
import com.netbusy.util.datautil.DTC;

public class ReplyControlUtil {
    public static byte[] toBytes(ReplyControl replyControl){
        byte[] sendobjectinfobytes = SendObjectInfoUtil.toBytes(replyControl.getSendObjectInfo());
        byte[] re = new byte[sendobjectinfobytes.length+4];
        int pos = 0;
        pos = DTC.O2BCopy(re,pos,sendobjectinfobytes);
        pos = DTC.O2BCopy(re,pos,replyControl.getSendOver());
        return re;
    }
    public static  ReplyControl toObject(byte [] bytes){
        ReplyControl replyControl= new ReplyControl();
        byte[] infobytes = (byte[]) DTC.B2OCopy(bytes,0,bytes.length-4,byte[].class);
        replyControl.setSendObjectInfo(SendObjectInfoUtil.toObject(infobytes) );
        replyControl.setSendOver((Integer) DTC.B2OCopy(bytes,bytes.length-4,4,int.class));
        return replyControl;
    }
}
