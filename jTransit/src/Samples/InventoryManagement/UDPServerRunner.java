/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Samples.InventoryManagement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author scherinet
 */
public class UDPServerRunner implements Runnable{
    private int _port;
    public UDPServerRunner(int port){
        _port = port;
    }
    public void run(){
		int port = _port;
		int len = 1024;
		try {
			DatagramSocket dataSocket = new DatagramSocket(port);
            System.out.println("UDP Server Starting ...");
			while (true) {
				try {
                    byte[] buf = new byte[len];
					DatagramPacket dataPacket = new DatagramPacket(buf, buf.length);
					dataSocket.receive(dataPacket);
                    // pass port and ip to udp server thread which does the rest including the return command.
                    Thread t = new UDPServerThread(dataPacket);
                    t.start();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		} catch (SocketException se) {
			System.err.println(se);
		}
    }
}
