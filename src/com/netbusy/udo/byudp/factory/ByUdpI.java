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

    SendObject pullSendObject();
    void pushSendObject(SendObject sendObject);

    SendObject pullSendPackets();
    void pushSendPackets(SendObject sendObject);

    void  pushReplayControl(ReplyControl value);
    int checkReplayControl(SendObjectInfo key);
    void sendNeedPackates(SendObjectInfo key);
    void setAndNofifyReplayControl(SendObjectInfo info,int statu);
    void releaseReplyControl(SendObjectInfo key);

    void releaseSendCachePacks(SendObjectInfo key);
    void releaseSendCache();

    boolean ifReceived(BasePacket basePacket);//cache received and judge if received
    SendObject receiveData(BasePacket basePacket);
    SendObject getReceivedObject(SendObjectInfo info);


}
