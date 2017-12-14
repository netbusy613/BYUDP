package com.netbusy.udo.byudp.util;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.exception.CRC32CheckException;
import com.netbusy.udo.byudp.factory.ByFactory;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.util.datautil.DTC;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

public class PacketUtil {

    private static long id = 0;
    private static Date ocontrol = new Date();

    private static long getId(){
        synchronized (ocontrol){
        long re = id;
        id++;
        return re;
        }
    }
    public static BasePacket dp2bp(DatagramPacket datagramPacket) throws CRC32CheckException {
        return new BasePacket(datagramPacket);
    }
    public static DatagramPacket bp2dp(BasePacket bp ){
        return bp.getDatagramPacket();
    }

    public boolean checkCrc32(DatagramPacket datagramPacket){
        long crc1 = (Long) DTC.B2OCopy(datagramPacket.getData(),0,8,long.class);
        byte[] data = (byte[]) DTC.B2OCopy(datagramPacket.getData(),8,datagramPacket.getData().length-8,byte[].class);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        long crc2 = crc32.getValue();
        return crc1==crc2;
    }

    public static List<BasePacket> SO2BPS(String uuid,int type , InetSocketAddress address,byte[] data){
        ArrayList<BasePacket> ll = new ArrayList<BasePacket>();
        int tot = data.length/ Statics.PacketDataLen+1;
        long id = getId();
        for(int i=0;i<tot;i++){
            if(i==tot-1){
                ll.add(SO2BP(uuid,id,tot,i,type,address,(byte[])DTC.B2OCopy(data,i*Statics.PacketDataLen,data.length-Statics.PacketDataLen*i,byte[].class)));
            }else {
                ll.add(SO2BP(uuid,id,tot,i,type,address,(byte[])DTC.B2OCopy(data,i*Statics.PacketDataLen,Statics.PacketDataLen,byte[].class)));
            }
        }
        return ll;
    }

    public static BasePacket SO2BP(String uuid,long id,int tot, int num, int type , InetSocketAddress address,byte[] data){
        BasePacketData info = new BasePacketData(uuid,id,tot,num,new Date(),type,data);
        return new BasePacket(address,info);
    }
    public static BasePacket grSendOver(InetSocketAddress address,SendObjectInfo info){
        info.setPacketInfos(null);
        byte[] data = SendObjectInfoUtil.toBytes(info);
        ByLog.log("grSendOver datalen="+data.length);
        BasePacketData basePacketData = new BasePacketData(ByFactory.getByUdp().clientID(),getId(),1,0,new Date(),DataType.SendOver,data);
        return new BasePacket(address,basePacketData);
    }
    public static BasePacket grReceivedAll(InetSocketAddress address,SendObjectInfo info){
        info.setPacketInfos(null);
        byte[] data = SendObjectInfoUtil.toBytes(info);
        BasePacketData packetData = new BasePacketData(ByFactory.getByUdp().clientID(),getId(),1,0,new Date(),DataType.ReveiveAll,data);
        return new BasePacket(address,packetData);
    }
    public static BasePacket grNeedPackets(InetSocketAddress address,SendObjectInfo info){
        byte[] data = SendObjectInfoUtil.toBytes(info);
        BasePacketData packetData = new BasePacketData(ByFactory.getByUdp().clientID(),getId(),1,0,new Date(),DataType.NeedPacket,data);
        return new BasePacket(address,packetData);
    }

}
