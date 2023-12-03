package neilyich.fht;

import java.util.BitSet;

public interface FHT {
    BitSet transform(BitSet c, int nbits) throws InterruptedException;
}
