import com.netbusy.udo.byudp.ByUdpSocket;

import java.net.SocketException;

public class Server {
    public static void main(String[] args) {
        ByUdpSocket socket = new ByUdpSocket(5665);
        try {
            socket.run();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
