package com.netbusy.udo.byudp.entity;

import java.io.Serializable;

public class ReplyControl implements Serializable{
    private Object control;
    private BasePacketInfo info;
    private boolean sendOver;

    public ReplyControl() {
    }

    public ReplyControl(BasePacketInfo info, Object control) {
        this.control = control;
        this.info = info;
        this.sendOver = false;
    }

    public Object getControl() {
        return control;
    }

    public void setControl(Object control) {
        this.control = control;
    }

    public boolean isSendOver() {
        return sendOver;
    }

    public void setSendOver(boolean sendOver) {
        this.sendOver = sendOver;
    }

    public BasePacketInfo getInfo() {
        return info;
    }

    public void setInfo(BasePacketInfo info) {
        this.info = info;
    }
}
