import com.netbusy.log.ByLog;
import com.netbusy.udo.byudp.ByUdpSocket;
import com.netbusy.udo.byudp.entity.SendObject;

import java.net.SocketException;
import java.nio.charset.Charset;

public class Server {
    public static void main(String[] args) {
        ByUdpSocket socket = new ByUdpSocket(5665);
        try {
            socket.run();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (true){
            SendObject sendObject = socket.receive();
            String msg = new String(sendObject.getData(), Charset.forName("UTF-8"));
            ByLog.err(msg);
        }
    }
}
