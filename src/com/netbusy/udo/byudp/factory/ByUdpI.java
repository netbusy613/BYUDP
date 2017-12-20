package com.netbusy.udo.byudp.factory;

import com.netbusy.udo.byudp.entity.*;

import java.net.DatagramSocket;
import java.net.SocketException;

public interface ByUdpI {
    String clientID();
    void init() throws SocketException;
    void setParam(String key,Object value);
    Object getParam(String key);
    DatagramSocket getSocket();

    void cacheSendObjects(SendObject sendObject);
    SendObject findSendCache(SendObjectInfo sendObjectInfo);
    void cleanSendCache(SendObjectInfo sendObjectInfo);

    SendObject pullSendObject();
    void pushSendObject(SendObject sendObject);
    SendObject pullReSendObject();
    void pushReSendObject(SendObject sendObject);

    BasePacket pullCmd();
    void pushCmd(BasePacket basePacket);

    void  pushReplayControl(ReplyControl value);
    boolean checkReplayControl(BasePacketInfo key);
    void setAndNofifyReplayControl(BasePacketInfo info,boolean sendOver);
    void releaseReplyControl(BasePacketInfo key);

    void releaseSendCache();

    boolean ifReceived(BasePacket basePacket);//cache received and judge if received
    SendObject receiveData(BasePacket basePacket);
    SendObject getReceivedObject(SendObjectInfo info);

    void pushReceivePacket(BasePacket basePacket);
    BasePacket pullReceivePacket();


    void sendCmd(BasePacket basePacket);
    void sendObject(SendObject sendObject);
}
