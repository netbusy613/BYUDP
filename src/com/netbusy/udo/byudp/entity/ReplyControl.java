package com.netbusy.udo.byudp.entity;

import java.io.Serializable;

public class ReplyControl implements Serializable{
    private Object control;
    private SendObjectInfo sendObjectInfo;
    private int sendOver;

    public ReplyControl() {
    }

    public ReplyControl(SendObjectInfo sendObjectInfo, Object control, int sendOver) {
        this.control = control;
        this.sendObjectInfo = sendObjectInfo;
        this.sendOver = sendOver;
    }

    public Object getControl() {
        return control;
    }

    public void setControl(Object control) {
        this.control = control;
    }

    public int getSendOver() {
        return sendOver;
    }

    public void setSendOver(int sendOver) {
        this.sendOver = sendOver;
    }

    public SendObjectInfo getSendObjectInfo() {
        return sendObjectInfo;
    }

    public void setSendObjectInfo(SendObjectInfo sendObjectInfo) {
        this.sendObjectInfo = sendObjectInfo;
    }
}
