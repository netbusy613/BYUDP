package com.netbusy.udo.byudp.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class BasePacketData implements Serializable{
    public BasePacketData(String clientId, long id, int tot, int num, Date sendTime, int type, byte[] data) {
        this.clientId = clientId;
        this.id = id;
        this.tot = tot;
        this.num = num;
        this.sendTime = sendTime;
        this.type = type;
        this.data = data;
    }
    public BasePacketData(BasePacketInfo info, byte[] data){
        this.clientId = info.getClientId();
        this.id = info.getId();
        this.tot = info.getTot();
        this.num = info.getNum();
        this.type = info.getType();
        this.data = data;

    }

    private String clientId;
    private long id;
    private int tot;
    private int num;
    private Date sendTime;
    private int type;
    private byte data[];

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTot() {
        return tot;
    }

    public void setTot(int tot) {
        this.tot = tot;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
