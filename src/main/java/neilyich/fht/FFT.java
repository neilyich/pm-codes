package neilyich.fht;

import java.util.BitSet;

public interface FFT {
    BitSet transform(BitSet c, int nbits);
}
