package neilyich.rm;

import neilyich.bf.BFVectorCalculator;
import neilyich.fft.FHT;
import neilyich.fht.FFT;
import neilyich.real.RealAnalogTransformer;
import neilyich.utils.LogUtils;

import java.util.Arrays;
import java.util.BitSet;

import static neilyich.utils.BitSetUtils.toBinaryString;

public class RMCode {
    private final FHT fht;
    private final FFT fft;
    private final BFVectorCalculator bfVectorCalculator;
    private final RealAnalogTransformer realAnalogTransformer;

    public RMCode(FHT fht, FFT fft, BFVectorCalculator bfVectorCalculator, RealAnalogTransformer realAnalogTransformer) {
        this.fht = fht;
        this.fft = fft;
        this.bfVectorCalculator = bfVectorCalculator;
        this.realAnalogTransformer = realAnalogTransformer;
    }

    public BitSet encode(BitSet a, int nbits) {
        return fft.transform(expand(a, nbits), nbits);
    }

    public BitSet decode(BitSet c, int nbits) {
        LogUtils.println("Decoding: " + toBinaryString(c, nbits));
        int[] realAnalog = realAnalogTransformer.transform(c, nbits);
        int[] statAnalogs = fht.transform(realAnalog);
        LogUtils.println("Stat analogs: " + Arrays.toString(statAnalogs));
        int max = 0;
        int maxABS = 0;
        int statAnalogCoefs = -1;
        int notZeroCount = -1;
        int sum = 0;
        for (int i = 0; i < nbits; i++) {
            int abs = Math.abs(statAnalogs[i]);
            if (maxABS < abs) {
                maxABS = abs;
                max = statAnalogs[i];
                statAnalogCoefs = i;
            }
            if (abs != 0) {
                notZeroCount++;
                sum += abs * abs;
            }
        }
        if (notZeroCount > 0) {
            System.out.println("ERRORS DETECTED: " + notZeroCount);
        }
        int xCount = (int) (Math.log(nbits) / Math.log(2));
        if (sum != (int) Math.pow(2, 2 * xCount)) {
            LogUtils.println("Stat analogs calculated not correctly: sum=" + sum + ", != " + (int) Math.pow(2, 2 * xCount));
        }
        BitSet xCoefs = toBitSet(statAnalogCoefs, xCount);
        //LogUtils.println("xCoefs: " + toBinaryString(xCoefs, xCount));
        BitSet bfReversed = bfVectorCalculator.reversedFromLinearCoefs(max < 0, xCoefs, xCount, nbits);
        //LogUtils.println("Reversed BF vector: " + toBinaryString(bfReversed, nbits));
        BitSet allCoefs = fft.transform(bfReversed, nbits);
        //LogUtils.println("Transformed coefs: " + toBinaryString(allCoefs, nbits));
        return linearCoefs(allCoefs, nbits, xCount);
    }

    private BitSet toBitSet(int n, int xCount) {
        BitSet result = new BitSet(xCount);
        int index = 0;
        while (n > 0) {
            result.set(xCount - 1 - index++, n % 2 == 1);
            n /= 2;
        }
        return result;
    }

    private BitSet linearCoefs(BitSet allCoefs, int nbits, int xCount) {
        BitSet result = new BitSet(xCount + 1);
        result.set(0, allCoefs.get(0));
        int resultIndex = 1;
        for (int i = 1; i < nbits; i = i << 1) {
            result.set(resultIndex++, allCoefs.get(i));
        }
        return result;
    }

    private BitSet expand(BitSet a, int nbits) {
        BitSet expanded = new BitSet(nbits);
        expanded.set(0, a.get(0));
        int aIndex = 1;
        for (int i = 1; i < nbits; i = i << 1) {
            expanded.set(i, a.get(aIndex++));
        }
        return expanded;
    }
}
