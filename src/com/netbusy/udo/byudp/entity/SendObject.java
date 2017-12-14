package com.netbusy.udo.byudp.entity;

import com.netbusy.udo.byudp.factory.ByFactory;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import java.net.InetSocketAddress;

public class SendObject {
    private String uuid;
    private long id;
    private int type;
    private byte[] data;
    private InetSocketAddress address;
    private BasePacket[] packets;
    private BasePacketInfo[] objinfo;
    private int count = 1;
    private int totlen = 0;

    public SendObject() {
    }

    public SendObject(int type, byte[] data, InetSocketAddress address) {
        uuid = ByFactory.getByUdp().clientID();
        this.type = type;
        this.data = data;
        this.address = address;
        packets = PacketUtil.SO2BPS(uuid,type, address, data).toArray(new BasePacket[0]);
        this.id = packets[0].getBasePacketData().getId();

        objinfo = new BasePacketInfo[packets.length];
        for(int i =0;i<objinfo.length;i++){
            objinfo[i] = packets[i].getInfo();
        }
    }

    public boolean pushPacket(BasePacket basePacket){
        BasePacketData info = basePacket.getBasePacketData();

        //firsh pack init;
        if(packets==null){
            packets = new BasePacket[info.getTot()];
            uuid = info.getClientId();
            id = info.getId();
            type = info.getType();
            address = basePacket.getAddress();
            totlen = info.getTot()*Statics.PacketDataLen;
            objinfo = new BasePacketInfo[info.getTot()];
            for(int i =0;i<objinfo.length;i++){
                objinfo[i] = null;
            }
        }
        // 记录已收到包;
        objinfo[info.getNum()]= basePacket.getInfo();
        packets[info.getNum()]= basePacket;

        //last pack gr data;
        if (count==info.getTot()) {
            totlen = totlen-Statics.PacketDataLen+info.getData().length;
            data = new byte[totlen];
            int i = 0;
            for (BasePacket bp:packets){
                BasePacketData basePacketData = bp.getBasePacketData();
                System.arraycopy(basePacketData.getData(),0,data, basePacketData.getNum()* Statics.PacketDataLen, basePacketData.getData().length);
                i++;
            }
            return true;
        }
        count++;
        return false;
    }

    public String getUuid() {
        return uuid;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public int getType() {
        return type;
    }

    public BasePacket[] getPackets() {
        return packets;
    }

    public BasePacketInfo[] getObjinfo() {
        return objinfo;
    }
    public SendObjectInfo getInfo(){
        return new SendObjectInfo(uuid,id,type,objinfo);
    }
}
