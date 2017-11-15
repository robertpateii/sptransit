package Samples.InventoryManagement;

import java.io.IOException;

public class server {

        public static void main(String[] args) throws IOException
        {
            InventoryManager.Initialize();
            
            // open both tcp and udp sockets - clients may be either
            TCPServerRunner tcpRunner = new TCPServerRunner(3007);
            Thread t1 = new Thread(tcpRunner,"TCP");
            t1.start();
            
            UDPServerRunner udpRunner = new UDPServerRunner(3008);
            Thread t2 = new Thread(udpRunner,"UDP");
            t2.start();
	}
}