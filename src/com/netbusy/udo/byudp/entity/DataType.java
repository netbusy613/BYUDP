package com.netbusy.udo.byudp.entity;

public class DataType {
    public static final int Data=0,SendOver=1,NeedPacket=2,Copy=3,ReceivedAll=4;
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
            case DataType.ReceivedAll:
                return "ReceivedAll";
            default:
                return "UNKNOW";
        }
    }
}
