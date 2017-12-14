package com.netbusy.udo.byudp.worker;

import com.netbusy.udo.byudp.factory.ByUdpI;

public class PacketsReSender implements Runnable{

    public PacketsReSender(Object control, ByUdpI byUdpI) {
        this.control = control;
        this.byUdpI = byUdpI;
    }

    private boolean runing = true;
    private Object control;
    private ByUdpI byUdpI;

    @Override
    public void run() {

    }
}
