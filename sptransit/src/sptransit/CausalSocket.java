package sptransit;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class CausalSocket extends BaseSocket {
    int M[][];
    int N;
    LinkedList deliveryQ = new LinkedList();
    ConcurrentLinkedQueue<CausalPacket> orderMessages;

    public CausalSocket(Logger log, int n) {
        super(log);
        N = n;
        M = new int[N][N];
        setZero(M);
        orderMessages = new ConcurrentLinkedQueue<>();
    }

    //TODO : these are added from the server to keep both send/receive methods on the same socket, discuss this!!
    public void bind(String host, int port) {
        super.bind(host, port);
    }

    //TODO : these are added from the server to keep both send/receive methods on the same socket, discuss this!!
    public void connect(String host,int port){_connectEndPointAddress = new TAddress(host, port);}

    //the full definition had to be copied because we are introducing a logic to increment matrix values
    public void send(Serializable message) {
        try {
            Socket s = new Socket(_connectEndPointAddress.getIPAddress(), _connectEndPointAddress.getPort());

            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            M[getPid(_bindEndPointAddress)][getPid(_connectEndPointAddress)]++;
            CausalPacket cpacket = new CausalPacket(message, _bindEndPointAddress, M);

            pout.writeObject(cpacket);
            pout.flush();
            pout.close();

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    //causal algorithm method
    boolean okayToRecv(int w[][], int srcId) {
        int myId = getPid(_bindEndPointAddress);

        // incoming matrix should be exactly one ahead for me
        if (w[srcId][myId] > M[srcId][myId] + 1) {
            return false;
        }

        // do any other processes know about any messages sent to me that i do not?
        // their count of messages sent to me should be equal or lesser than mine
        for (int k = 0; k < N; k++) {
            // TODO: This is probably where the logic is failing to order causally
            if (k != srcId) { // skip my vector we already tested it
                int theirCount = w[k][myId];
                int myCount = M[k][myId];
                if (theirCount > myCount) {
                    return false;
                }
            }
        }

        return true;
    }

    //causal algorithm method
    void checkPendingQ() {
        // TODO: should we copy over the code for this?
        // TODO: also is it ok to loop on this syrchonized message queue?
        Iterator iter = messageQueue.iterator();
        while (iter.hasNext()) {
            CausalPacket cp = (CausalPacket) iter.next();
            if (okayToRecv(cp.W, getPid(cp.address))) {
                iter.remove();
                deliveryQ.add(cp);
            }
        }
    }

    //causal ordering specific implementation
    public Serializable receive() {
        while (deliveryQ.isEmpty()) {
            // TODO: just because deliveryQ was not empty above doesn't mean it's still not empty now because threads
            checkPendingQ();
        }
        CausalPacket packet = (CausalPacket) deliveryQ.pollFirst();
        setMax(M, packet.W);
        return packet.message;
    }

    //Matrix helper methods
    void setZero(int m[][]) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = 0;
            }
        }
    }

    void setMax(int m[][], int w[][]) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (w[i][j] > m[i][j])
                    m[i][j] = w[i][j];
            }
        }
    }

    //TODO think of a way that can be shared across processes where servers can have a designated index (I think like hw2)
    //at this time it just assumes that you are creating servers starting port# 6000, very bad just for testing!!!!
    int getPid(TAddress address) {
        return address.getPort() - 6000;
    }
}
