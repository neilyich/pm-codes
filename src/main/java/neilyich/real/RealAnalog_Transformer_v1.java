package neilyich.real;

import java.util.BitSet;

public class RealAnalog_Transformer_v1 implements RealAnalogTransformer {
    @Override
    public int[] transform(BitSet c, int nbits) {
        var result = new int[nbits];
        for (int i = 0; i < nbits; i++) {
            result[i] = c.get(i) ? -1 : 1;
        }
        return result;
    }
}
