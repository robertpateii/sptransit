package inventory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerRunner implements Runnable {

    private int _port;

    public TCPServerRunner(int port) {
        _port = port;
    }

    public void run() {
        try {
            System.out.println("TCP server starting");
            int port = _port;
            ServerSocket listener = new ServerSocket(port);
            Socket s;
            while ((s = listener.accept()) != null) {
                Thread t = new TCPServerThread(s);
                t.start();
            }
        } catch (IOException e) {
            System.err.print(e);
        }
    }
}
