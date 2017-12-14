package com.netbusy.udo.byudp.factory;


public class ByFactory {
    public static ByUdpI getByUdp(){
        if (byUdpI==null)
            byUdpI = new ByUdpImpl();
        return byUdpI;
    }

    private static ByUdpI byUdpI;
}
