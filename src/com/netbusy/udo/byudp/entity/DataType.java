package com.netbusy.udo.byudp.entity;

public class DataType {
    public static final int Data=0,SendOver=1,NeedPacket=2,Copy=3;
    public static String toString(int statu){
        switch (statu){
            case DataType.Data:
                return "Data";
            case DataType.SendOver:
                return "SendOver";
            case DataType.NeedPacket:
                return "NeedPacket";
            case DataType.Copy:
                return "Copy";
            default:
                return "UNKNOW";
        }
    }
}
