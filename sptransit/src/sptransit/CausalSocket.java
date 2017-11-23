package sptransit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class CausalSocket extends BaseSocket {
    int M[][];
    int N;
    LinkedList deliveryQ = new LinkedList();
    LinkedList pendingQ = new LinkedList();
    ConcurrentLinkedQueue<CausalPacket> orderMessages;

    public CausalSocket(Logger log, int n) {
        super(log);
        M = new int[N][N];
        setZero(M);
        N = n;

        orderMessages = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void send(Serializable message, TAddress address) {

        try {
            Socket s = new Socket(address.getIPAddress(), address.getPort());

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            M[getPid(_bindEndPointAddress)][getPid(_connectEndPointAddress)]++;
            CausalPacket cpacket = new CausalPacket(message, _bindEndPointAddress, M);

            pout.writeObject(cpacket);
            pout.flush();
            pout.close();

            // TODO: see if this works with no din since we don't do anything with it
            String retValue = din.readLine(); // scanner next time

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    boolean okayToRecv(int w[][], int srcId) {
        int myId = getPid(_bindEndPointAddress);

        if (w[srcId][myId] > M[srcId][myId] + 1) return false;

        for (int k = 0; k < N; k++) {
            if ((k != srcId) && (w[k][myId] > M[k][myId])) return false;
        }

        return true;
    }

    void checkPendingQ()
    {
        Iterator iter = messageQueue.iterator();
        while(iter.hasNext())
        {
            CausalPacket cp = (CausalPacket)iter.next();
            if(okayToRecv(cp.W,getPid(cp.address)))
            {
                iter.remove();
                deliveryQ.add(cp.message);
            }
        }
    }

    protected Object receive() {
        log.info("Attempting to receive message");
        while (deliveryQ.isEmpty()) {
            // waiting
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("received packet");
        return deliveryQ.pollFirst();
    }

    //Matrix helper methods
    void setZero(int m[][]) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = 0;
            }
        }
    }

    //gotta think of how this can be achieved
    int getPid(TAddress address) {
        return address.getPort() - 6000;
    }
}
