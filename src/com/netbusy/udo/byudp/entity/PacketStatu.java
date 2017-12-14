package com.netbusy.udo.byudp.entity;

public class PacketStatu {
    public static final int WAIT =0,OVER =1,UNKNOW=2;
    public static String toString(int statu){
        switch (statu){
            case PacketStatu.WAIT:
                return "WAIT";
            case PacketStatu.OVER:
                return "OVER";
            default:
                return "UNKNOW";
        }
    }
}
