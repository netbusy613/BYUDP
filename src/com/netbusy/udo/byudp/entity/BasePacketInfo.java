package com.netbusy.udo.byudp.entity;

import java.io.Serializable;
import java.util.Date;

public class BasePacketInfo implements Serializable{
    private String clientId;
    private long id;
    private int tot;
    private int num;
    private int type;
    private Date sendTime;

    public BasePacketInfo() {
    }


    public BasePacketInfo(String clientId, long id, int tot, int num, int type) {
        this.clientId = clientId;
        this.id = id;
        this.tot = tot;
        this.num = num;
        this.type = type;
        this.sendTime = new Date();
    }

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public SendObjectInfo getSendObjectInfo(){
        return new SendObjectInfo(clientId,id,type,null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasePacketInfo)) return false;

        BasePacketInfo that = (BasePacketInfo) o;

        if (id != that.id) return false;
        if (tot != that.tot) return false;
        if (num != that.num) return false;
        if (!clientId.equals(that.clientId)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + tot;
        result = 31 * result + num;
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "[clientId="+clientId+"] "+"[id="+id+"] "+"type=["+DataType.toString(type)+"] " +"tot=["+tot+"] "+"num=["+num+"]";
    }
}
