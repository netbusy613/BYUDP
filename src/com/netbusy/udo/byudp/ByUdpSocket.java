package com.netbusy.udo.byudp;

import com.netbusy.udo.byudp.entity.DataType;
import com.netbusy.udo.byudp.entity.SendObject;
import com.netbusy.udo.byudp.factory.ByFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ByUdpSocket { ;

    public ByUdpSocket(int port) {
        ByFactory.getByUdp().setParam("port",port);
    }
    public void run() throws SocketException {
        ByFactory.getByUdp().init();
    }

    public void send(byte[] bytes,InetSocketAddress address){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(address == null)
           address = new InetSocketAddress(addr,5665);
        SendObject sendObject = new SendObject(DataType.Data,bytes,address);
        ByFactory.getByUdp().pushSendObject(sendObject);
    }

    public SendObject receive(){
        return ByFactory.getByUdp().pullReceivedObject();
    }
}
