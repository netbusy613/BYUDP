package com.netbusy.udo.byudp.entity;

import java.io.Serializable;

public class SendObjectInfo implements Serializable{
    private String uuid;
    private long id;
    private int type;
    private boolean[] packetStatus;

    public SendObjectInfo() {
    }

    public SendObjectInfo(String uuid, long id, int type, boolean[] packetStatus) {
        this.uuid = uuid;
        this.id = id;
        this.type = type;
        this.packetStatus = packetStatus;
    }

    public SendObjectInfo(String uuid , long id, int type) {
        this.uuid = uuid;
        this.id = id;
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean[] getPacketStatus() {
        return packetStatus;
    }

    public void setPacketStatus(boolean[] packetStatus) {
        this.packetStatus = packetStatus;
    }

    public BasePacketInfo[] grPacketsInfo(){
        BasePacketInfo[] basePacketInfos = new BasePacketInfo[this.getPacketStatus().length];
        for(int i=0;i<basePacketInfos.length;i++){
            basePacketInfos[i]= new BasePacketInfo(uuid,id,basePacketInfos.length,i,type);
        }
        return basePacketInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendObjectInfo)) return false;

        SendObjectInfo info = (SendObjectInfo) o;

        if (getId() != info.getId()) return false;
        if (getType() != info.getType()) return false;
        return getUuid().equals(info.getUuid());
    }

    @Override
    public int hashCode() {
        int result = getUuid().hashCode();
        result = 31 * result + (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getType();
        return result;
    }

    @Override
    public String toString() {
        return "[uuid="+uuid+"] "+"[id="+id+"] "+"type=["+DataType.toString(type)+"]";
    }
}
