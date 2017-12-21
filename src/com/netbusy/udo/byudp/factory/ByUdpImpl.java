package com.netbusy.udo.byudp.factory;

import com.netbusy.cache.CachedMap2;
import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.entity.*;
import com.netbusy.udo.byudp.statics.Statics;
import com.netbusy.udo.byudp.worker.*;
import com.netbusy.util.threadutil.ThreadUtil;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class ByUdpImpl implements ByUdpI{

    private String clientUUDI;

    private HashMap<String,Object> params = new HashMap<String, Object>();
    private ArrayList<SendObject> sendObjects = new ArrayList<SendObject>();
    private ArrayList<SendObject> resendObjects = new ArrayList<SendObject>();
    private ArrayList<BasePacket> receiveList = new ArrayList<BasePacket>();
    private ArrayList<BasePacket> cmdList = new ArrayList<BasePacket>();

    private HashMap<BasePacketInfo,ReplyControl> replyControls = new HashMap<BasePacketInfo, ReplyControl>();


    private HashMap<SendObjectInfo,SendObject> sendcache = new HashMap<SendObjectInfo,SendObject>();
    private CachedMap2<BasePacketInfo,BasePacket> receivedcache = new CachedMap2<BasePacketInfo,BasePacket>();

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
        Object reSenderControl = new Date();
        Object cmdControl = new Date();
        Object dataControl = new Date();
        Object sendCacheControl = new Date();
        Object replyControl = new Date();

        setParam("receiveControl",receiveControl);
        setParam("senderObjectControl",senderObjectControl);
        setParam("reSenderControl",reSenderControl);
        setParam("cmdControl",cmdControl);
        setParam("dataControl",dataControl);
        setParam("sendCacheControl",sendCacheControl);
        setParam("replyControl",replyControl);

        ThreadUtil.CreatThread(new Receiver(this)).start();
        ThreadUtil.CreatThread(new ObjectSender(getParam("senderObjectControl"),this)).start();
        ThreadUtil.CreatThread(new ObjectReSender(getParam("reSenderControl"),this)).start();
        ThreadUtil.CreatThread(new CmdProcessor(getParam("cmdControl"),this)).start();
        ThreadUtil.CreatThread(new ReceivePacketProcessor(getParam("dataControl"),this)).start();

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
        Object control = getParam("sendCacheControl");
        synchronized (control){
            ByLog.log("cache send OBJ num="+sendObject.getPackets().length);
            sendcache.put(sendObject.getInfo(),sendObject);
        }
    }

    @Override
    public SendObject findSendCache(SendObjectInfo sendObjectInfo) {
        Object control = getParam("sendCacheControl");
        synchronized (control){
            return sendcache.get(sendObjectInfo);
        }
    }

    @Override
    public void cleanSendCache(SendObjectInfo sendObjectInfo) {
        Object control = getParam("sendCacheControl");
        synchronized (control){
            sendcache.remove(sendObjectInfo);
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
    public SendObject pullReSendObject() {
        SendObject sendObject = null;
        synchronized (getParam("reSenderControl")){
            if(!resendObjects.isEmpty()) {
                sendObject = resendObjects.remove(0);
            }
        }
        return sendObject;
    }

    @Override
    public void pushReSendObject(SendObject sendObject) {
        Object control = getParam("reSenderControl");
        synchronized (control){
            resendObjects.add(sendObject);
            control.notify();
        }
    }

    @Override
    public BasePacket pullCmd() {
        BasePacket basePacket = null;
        synchronized (getParam("cmdControl")){
            if(!cmdList.isEmpty()) {
                basePacket = cmdList.remove(0);
            }
        }
        return basePacket;
    }

    @Override
    public void pushCmd(BasePacket basePacket) {
        Object control = getParam("cmdControl");
        synchronized (control){
            cmdList.add(basePacket);
            control.notify();
        }
    }


    @Override
    public void pushReplayControl( ReplyControl value) {
        synchronized (getParam("replyControl")) {
            replyControls.put(value.getInfo(), value);
        }
    }

    @Override
    public boolean checkReplayControl(BasePacketInfo key) {
        synchronized (getParam("replyControl")) {
            ReplyControl control = replyControls.get(key);
            if(control!=null){
                if(control.isSendOver()){
                    return true;
                }
            }
            return false;
        }
    }


    @Override
    public void setAndNofifyReplayControl(BasePacketInfo info,boolean sendOver) {
        synchronized (getParam("replyControl")) {
            ReplyControl control = replyControls.get(info);
            if(control!=null){
                control.setSendOver(true);
                synchronized (control.getControl()) {
                    control.getControl().notify();
                }
            }

        }
    }

    @Override
    public void releaseReplyControl(BasePacketInfo key) {
        synchronized (getParam("replyControl")) {
            ReplyControl control = replyControls.remove(key);
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
                    long sendTime = sendObject.getPackets()[0].getInfo().getSendTime().getTime();
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
        synchronized (control){
            BasePacket bp = receivedcache.get(basePacket.getInfo());
            if(bp!=null){
                ByLog.log("重复 receivedpacket ="+basePacket.getInfo());
                return true;
            }
            receivedcache.put(basePacket.getInfo(),basePacket);
            ByLog.log("cache receivedpacket ="+basePacket.getInfo());
            return false;
        }
    }

    @Override
    public SendObject receiveData(BasePacket basePacket) {
        SendObject reObject = null;
        boolean ifok = false;
        Object control = getParam("receiveControl");
        synchronized (control){
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
                control.notify();
                ByLog.log("receivedObject ready! id="+sendObject.getInfo().getId());
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

    @Override
    public SendObject pullReceivedObject() {
        Object control = getParam("receiveControl");
        SendObject sendObject= null;
        while (true) {
            synchronized (control) {
                if (!receivedObj.isEmpty()) {
                    sendObject = receivedObj.remove(0);
                }
            }
            if (sendObject != null){
                return sendObject;
            }else {
                ThreadUtil.waits(control);
            }
        }
    }

    @Override
    public void pushReceivePacket(BasePacket basePacket) {
        Object control = getParam("dataControl");
        synchronized (control){
            receiveList.add(basePacket);
            control.notify();
        }
    }

    @Override
    public BasePacket pullReceivePacket() {
        synchronized (getParam("dataControl")){
            BasePacket basePacket = null;
            if(!receiveList.isEmpty()) {
                basePacket = receiveList.remove(0);
            }
            return basePacket;
        }
    }

    private boolean checkSendPacketStatu(BasePacketInfo basePacketInfo,int i){
        boolean ifok = checkReplayControl(basePacketInfo);
        if(ifok){
            releaseReplyControl(basePacketInfo);
            ByLog.log("Send OVER");
            return true;
        }else{
            ByLog.log("Send ERROR time="+i);
            return false;
        }
    }

    @Override
    public void sendCmd(BasePacket basePacket){
        Object control = new Date();
        ReplyControl replyControl = new ReplyControl(basePacket.getInfo(),control);
        pushReplayControl(replyControl);
        for(int i = 0; i< Statics.SendDefaultTimes; i++) {
            try {
                getSocket().send(basePacket.getDatagramPacket());
                ByLog.log("Sender a Cmd packet! " + i + " times! +type="+DataType.toString(basePacket.getInfo().getType()));
                ThreadUtil.waits(Statics.TimeSep, replyControl.getControl());
                if(checkSendPacketStatu(basePacket.getInfo(),i)){
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByLog.err("SendCmd ERROR, [type="+basePacket.getInfo().getType()+"]");
    }

    @Override
    public void sendObject(SendObject sendObject) {
        BasePacket[] packets = sendObject.getPackets();
        boolean[] packetstatus = sendObject.getInfo().getPacketStatus();
        try {
            if (packetstatus==null){
                packetstatus = new boolean[packets.length];
            }
            Object object = new Date();
            int j = 0;
            for (int i = 0; i < packets.length; i++) {
                if (!packetstatus[i]) {
                    getSocket().send(packets[i].getDatagramPacket());
                    BasePacketInfo info = packets[i].getInfo();
                    ByLog.log("ObjectSender a packet![id:" + info.getId() + "] [tot:" + info.getTot() + "] [num:" + info.getNum() + "]");
                    j++;
                    if(j>Statics.sendMaxs) {
                        ThreadUtil.wait(Statics.waitTime, object);
                        j=0;
                    }
                }
            }
            cacheSendObjects(sendObject);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
