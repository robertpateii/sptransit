package sptransit;

import java.io.Serializable;

public class CausalPacket extends TPacket{
    int N;
    int W[][];

    public CausalPacket(Serializable message, TAddress address,int matrix[][]) {
        super(message, address);
        this.W = matrix;
    }
}
