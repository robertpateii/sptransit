import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner implements Runnable {

    private int _port;

    public ServerRunner(int port) {
        _port = port;
    }

    public void run() {
        try {
            System.out.println("TCP server starting");
            int port = _port;
            ServerSocket listener = new ServerSocket(port);
            Socket s;
            while ((s = listener.accept()) != null) {
                Thread t = new ServerThread(s);
                t.start();
            }
        } catch (IOException e) {
            System.err.print(e);
        }
    }
}