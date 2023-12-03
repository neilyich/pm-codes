package neilyich.bf;

import java.util.BitSet;

public class BFVectorCalculator_v1 implements BFVectorCalculator {
    @Override
    public BitSet reversedFromLinearCoefs(boolean a0, BitSet xCoefs, int xCount, int nbits) {
        BitSet result = new BitSet(nbits);
        for (int i = 0; i < nbits; i++) {
            int tmpI = i;
            boolean value = a0;
            for (int x = xCount - 1; x >= 0; x--) {
                value ^= (tmpI % 2 == 1) && xCoefs.get(x);
                tmpI /= 2;
            }
            result.set(nbits - 1 - i, value);
        }
        return result;
    }
}
