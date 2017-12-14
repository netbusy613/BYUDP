import com.netbusy.udo.byudp.ByUdpSocket;
import com.netbusy.udo.byudp.entity.DataType;
import com.netbusy.util.threadutil.ThreadUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;

public class Client {
    public static void main(String[] args) {
        ByUdpSocket socket = new ByUdpSocket(9527);
        try {
            socket.run();
            ThreadUtil.waits(3000,new Date());
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName("192.168.100.103");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            InetSocketAddress address = new InetSocketAddress(addr,5665);
            FileInputStream inputStream = new FileInputStream(new File("E://pyg.pdf"));
            BufferedInputStream  bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[2000];
            int pos = 0;
            int re = 3000;
            re = bufferedInputStream.read(bytes);
            while (re>0){
                re = bufferedInputStream.read(bytes);
                pos = pos+re;
                socket.send(bytes,address);
//                ThreadUtil.waits(1,new Date());
            }
//            String bhy ="上帝发誓打死了看见分厘卡时间浪费就打算离开酱豆腐立刻机阿里山的回复了哈老师就好的飞机哈四大皆空发哈伦裤还是独立开发哈咯看是否加啊四了货到付款就好阿拉山口地方皇家卡森货到付款撒谎发来看哈时空复活啊阿斯顿发贺卡是的接口煞费了好大就开始打飞机啊是快点放假哈上了飞机哈是开放阿三";
//            for(int i = 0;i<200;i++){
//                byte [] sed = (i+bhy).getBytes(Charset.forName("UTF-8"));
//                socket.send(sed,address);
////                ThreadUtil.waits(100,new Date());
//            }
        } catch (SocketException e) {
            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
