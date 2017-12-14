package com.netbusy.udo.byudp.entity;

import com.netbusy.coder.CoderFactory;
import com.netbusy.coder.CoderI;
import com.netbusy.udo.byudp.exception.CRC32CheckException;
import com.netbusy.udo.byudp.util.BasePacketInfoUtil;
import com.netbusy.util.datautil.DTC;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.zip.CRC32;


public class BasePacket{

    public BasePacket() {
    }

    public BasePacket(DatagramPacket datagramPacket) throws CRC32CheckException {
        this.datagramPacket = datagramPacket;
        this.address = new InetSocketAddress(datagramPacket.getAddress(),datagramPacket.getPort());

        this.crc32 = (Long) DTC.B2OCopy(datagramPacket.getData(),0,8,long.class);
        //check crc32
        CRC32 crc32 = new CRC32();
        crc32.update(datagramPacket.getData(),8,datagramPacket.getLength()-8);
        if(crc32.getValue()!=this.crc32) {
            throw new CRC32CheckException();
        }
        byte [] data = (byte[]) DTC.B2OCopy(datagramPacket.getData(),12,datagramPacket.getLength()-12,byte[].class);
        //decoding.
        int code = (Integer) DTC.B2OCopy(datagramPacket.getData(),8,4,int.class);
        data = CoderFactory.getCoderI().decoding(code,data);

        byte[] infobytes = (byte[]) DTC.B2OCopy(data,0,64,byte[].class);
        byte [] reldata =  (byte[]) DTC.B2OCopy(data,64,data.length-64,byte[].class);
        this.info = BasePacketInfoUtil.toObject(infobytes);
        this.basePacketData = new BasePacketData(info,reldata);

    }
    public BasePacket(InetSocketAddress address, BasePacketData basePacketData) {
        this.address = address;
        this.basePacketData = basePacketData;
        this.info = getInfo();
        byte [] infodata = BasePacketInfoUtil.toBytes(this.info);
        byte[] senddata = new byte[infodata.length+basePacketData.getData().length];
        int pos = DTC.O2BCopy(senddata,0,infodata);
        DTC.O2BCopy(senddata,pos,basePacketData.getData());

        CoderI coderI = CoderFactory.getCoderI();
        int code = (Integer) coderI.grKey();
        senddata = coderI.encoding(code,senddata);

        byte [] data = new byte[senddata.length+12];

        int destPos = 8;
        destPos = DTC.O2BCopy(data,destPos,code);
        DTC.O2BCopy(data,destPos,senddata);

        CRC32 crc32 = new CRC32();
        crc32.update(data,8,senddata.length+4);
        this.crc32 = crc32.getValue();
        DTC.O2BCopy(data,0,this.crc32);

        this.datagramPacket = new DatagramPacket(data,data.length,address.getAddress(),address.getPort());
    }
    private InetSocketAddress address;
    private long crc32;
    private BasePacketData basePacketData;
    private DatagramPacket datagramPacket;
    private BasePacketInfo info;
    private String uuid;

    public InetSocketAddress getAddress() {
        return address;
    }

    public long getCrc32() {
        return crc32;
    }

    public BasePacketData getBasePacketData() {
        return basePacketData;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    public BasePacketInfo getInfo(){
        if (info==null){
            info = new BasePacketInfo(this.basePacketData);
        }
        return info;
    }

    public void setInfo(BasePacketInfo info) {
        this.info = info;
    }
}
