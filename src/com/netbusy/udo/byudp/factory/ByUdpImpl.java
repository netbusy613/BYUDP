package com.netbusy.udo.byudp.factory;

import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.worker.ObjectSender;
import com.netbusy.udo.byudp.worker.Receiver;
import com.netbusy.util.threadutil.ThreadUtil;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class ByUdpImpl implements ByUdpI{

    private String clientUUDI;

    private HashMap<String,Object> params = new HashMap<String, Object>();
    private ArrayList<SendObject> sendObjects = new ArrayList<SendObject>();
    private HashMap<SendObjectInfo,ReplyControl> replyConytols = new HashMap<SendObjectInfo, ReplyControl>();


    private HashMap<SendObjectInfo,SendObject> sendcache = new HashMap<SendObjectInfo,SendObject>();
    private HashMap<BasePacketInfo,BasePacket> receivedcache = new HashMap<BasePacketInfo,BasePacket>();

    private HashMap<SendObjectInfo,SendObject> receivedObj = new HashMap<SendObjectInfo, SendObject>();

    private DatagramSocket socket;


    @Override
    public String clientID() {
        if(clientUUDI == null){
            UUID uuid = UUID.randomUUID();
            clientUUDI = uuid.toString();
        }
        return clientUUDI;
    }

    @Override
    public void init() throws SocketException {
        Integer port =(Integer) getParam("port");
        if(port==null){
            setParam("port",9527);
        }
        this.socket = new DatagramSocket((Integer) getParam("port"));
        ByLog.log("ByUdpSocket listenning at "+getParam("port")+".....");
        setParam("socket",socket);

        Object receiveControl = new Date();
        Object senderObjectControl = new Date();
        Object replyConytol = new Date();

        setParam("receiveControl",receiveControl);
        setParam("senderObjectControl",senderObjectControl);
        setParam("replyConytol",replyConytol);

        ThreadUtil.CreatThread(new Receiver(getParam("receiveControl"),this)).start();
        ThreadUtil.CreatThread(new ObjectSender(getParam("senderObjectControl"),this)).start();
    }

    @Override
    public void setParam(String key, Object value) {
        params.put(key,value);
    }

    @Override
    public Object getParam(String key) {
        if(params.containsKey(key))
            return params.get(key);
        return null;
    }

    @Override
    public DatagramSocket getSocket() {
        return this.socket;
    }

    @Override
    public void cacheSendObjects(SendObject sendObject) {
        Object control = getParam("senderObjectControl");
        synchronized (control){
            ByLog.log("cache send OBJ num="+sendObject.getPackets().length);
            sendcache.put(sendObject.getInfo(),sendObject);
        }
    }

    @Override
    public SendObject pullSendObject() {
        SendObject sendObject = null;
        synchronized (getParam("senderObjectControl")){
            if(!sendObjects.isEmpty()) {
                sendObject = sendObjects.remove(0);
            }
        }
        return sendObject;
    }

    @Override
    public void pushSendObject(SendObject sendObject) {
        Object control = getParam("senderObjectControl");
        synchronized (control){
            sendObjects.add(sendObject);
            control.notify();
        }
    }

    @Override
    public void pushReplayControl( ReplyControl value) {
        synchronized (getParam("replyConytol")) {
            replyConytols.put(value.getSendObjectInfo(), value);
        }
    }

    @Override
    public int checkReplayControl(SendObjectInfo key) {
        synchronized (getParam("replyConytol")) {
            ReplyControl control = replyConytols.get(key);
            if(control!=null){
                if(control.getSendOver()== PacketStatu.OVER){
                    replyConytols.remove(key);
                    return PacketStatu.OVER;
                }
                return control.getSendOver();
            }
            return PacketStatu.UNKNOW;
        }
    }

    private void sendPacket(BasePacket basePacket){
        try {
            socket.send(basePacket.getDatagramPacket());
            BasePacketInfo packet = basePacket.getInfo();
            ByLog.log("send need packets [id:" + packet.getId() + "] [tot:" + packet.getTot() + "] [num:" + packet.getNum() + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNeedPackates(SendObjectInfo key) {
        SendObject sendObject;
        Object control = getParam("senderObjectControl");
        synchronized (control){
            sendObject = sendcache.get(key);
            BasePacket[]  basePackets = sendObject.getPackets();
            ByLog.log("find send OBJ cache num="+basePackets.length);

            if(sendObject==null){
                ByLog.err("DO NOT FIND SENDOBJECTS [uuid="+key.getUuid()+"] [id="+key.getId()+"] [type="+key.getType()+"]");
                return;
            }
            if(key.getPacketInfos()==null){
                ByLog.log("do sendNeedPackets() all num="+basePackets.length);
                for(int i=0;i<basePackets.length;i++){
                    sendPacket(basePackets[i]);
                }

            }else {
                BasePacketInfo[] infos = key.getPacketInfos();
                int j = 0;
                for(int i=0;i<infos.length;i++){
                    if(infos==null)
                    {
                        sendPacket(basePackets[i]);
                        j++;
                    }
                }
                ByLog.log("do sendNeedPackets() part num="+j);
            }
        }
    }

    @Override
    public void setAndNofifyReplayControl(SendObjectInfo info,int statu) {
        synchronized (getParam("replyConytol")) {
            ReplyControl control = replyConytols.get(info);
            if(control!=null){
                control.setSendOver(statu);
                control.setSendObjectInfo(info);
                synchronized (control.getControl()) {
                    control.getControl().notify();
                }
            }

        }
    }

    @Override
    public void releaseReplyControl(SendObjectInfo key) {
        synchronized (getParam("replyConytol")) {
            ReplyControl control = replyConytols.remove(key);
        }
    }

    @Override
    public void releaseSendCachePacks(SendObjectInfo key) {
            Object control = getParam("senderObjectControl");
            synchronized (control){
                sendcache.remove(key);
            }
    }

    @Override
    public void releaseSendCache() {
        Object control = getParam("senderObjectControl");
        synchronized (control) {
            Iterator iter = sendcache.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                SendObject sendObject = (SendObject) entry.getValue();
                if(sendObject!=null) {
                    long sendTime = sendObject.getObjinfo()[0].getSendTime().getTime();
                    if ((sendTime + 600000) < (new Date().getTime())) {
                        iter.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean ifReceived(BasePacket basePacket) {
        Object control = getParam("receiveControl");
        BasePacket bp = receivedcache.get(basePacket.getInfo());
        synchronized (control){
            if(bp!=null){
                ByLog.log("重复 receivedpacket hashcode="+basePacket.getBasePacketData().hashCode());
                return true;
            }
            receivedcache.put(basePacket.getInfo(),basePacket);
            ByLog.log("cache receivedpacket hashcode="+basePacket.getBasePacketData().hashCode());
            return false;
        }
    }

    @Override
    public SendObject receiveData(BasePacket basePacket) {
        SendObject reObject = null;
        boolean ifok = false;
        synchronized (getParam("receiveControl")){
            SendObjectInfo info= basePacket.getInfo().getSendObjectInfo();
            SendObject sendObject =  receivedObj.get(info);
            if(sendObject==null){
                sendObject = new SendObject();
                ByLog.log("create SendObject id="+info.getId());
                receivedObj.put(info,sendObject);
            }
            ifok = sendObject.pushPacket(basePacket);
            if(ifok){
                reObject = sendObject;
                ByLog.log("receivedObject ready! id="+sendObject.getPackets()[0].getBasePacketData().getId() );
            }
        }
        return reObject;
    }

    @Override
    public SendObject getReceivedObject(SendObjectInfo info) {
        synchronized (getParam("receiveControl")){
            ByLog.log("getReceivedObject id="+info.getId());
            SendObject sendObject =  receivedObj.get(info);
            return sendObject;
        }
    }

}
