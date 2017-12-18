package com.netbusy.udo.byudp.entity;

import com.netbusy.udo.byudp.factory.ByFactory;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.util.PacketUtil;
import java.net.InetSocketAddress;

public class SendObject {
    private SendObjectInfo info;
    private byte[] data;
    private InetSocketAddress address;
    private BasePacket[] packets;
    private int count = 1;
    private int totlen = 0;

    public SendObject() {
    }

    public SendObject(int type, byte[] data, InetSocketAddress address) {
        info = new SendObjectInfo();
        info.setUuid(ByFactory.getByUdp().clientID());
        info.setType(type);
        this.data = data;
        this.address = address;
        packets = PacketUtil.SO2BPS(info.getUuid(),type, address, data).toArray(new BasePacket[0]);
        info.setId(packets[0].getInfo().getId());
        boolean[] basepacketstatus = new boolean[packets.length];
        for(int i=0;i<basepacketstatus.length;i++){
            basepacketstatus[i] = false;
        }
        info.setPacketStatus(basepacketstatus);
    }

    public boolean pushPacket(BasePacket basePacket){
        BasePacketInfo basePacketInfo = basePacket.getInfo();

        //firsh pack init;
        if(packets==null){
            packets = new BasePacket[basePacketInfo.getTot()];
            info = new SendObjectInfo();
            info.setUuid(basePacketInfo.getClientId());
            info.setId(basePacketInfo.getId());
            info.setType(basePacketInfo.getType());
            address = basePacket.getAddress();
            totlen = basePacketInfo.getTot()*Statics.PacketDataLen;
            boolean []packetStatus = new boolean[basePacketInfo.getTot()];
            for(int i =0;i<packetStatus.length;i++){
                packetStatus[i] = false;
            }
            info.setPacketStatus(packetStatus);
        }
        // 记录已收到包;
        packets[basePacketInfo.getNum()]= basePacket;
        info.getPacketStatus()[basePacketInfo.getNum()] = true;

        //last pack gr data;
        if (count==basePacketInfo.getTot()) {
            totlen = totlen-Statics.PacketDataLen+basePacket.getBasePacketData().length;
            data = new byte[totlen];
            int i = 0;
            for (BasePacket bp:packets){
                BasePacketInfo info1 = bp.getInfo();
                byte[] basePacketData = bp.getBasePacketData();
                System.arraycopy(basePacketData,0,data, info1.getNum()* Statics.PacketDataLen, basePacketData.length);
                i++;
            }
            return true;
        }
        count++;
        return false;
    }


    public byte[] getData() {
        return data;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public BasePacket[] getPackets() {
        return packets;
    }


    public SendObjectInfo getInfo() {
        return info;
    }

    public void setInfo(SendObjectInfo info) {
        this.info = info;
    }
}
