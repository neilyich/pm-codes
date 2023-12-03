package neilyich.bf;

import java.util.BitSet;

public interface BFVectorCalculator {
    BitSet reversedFromLinearCoefs(boolean a0, BitSet xCoefs, int xCount, int nbits);
}
