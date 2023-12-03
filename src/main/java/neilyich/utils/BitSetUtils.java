package neilyich.utils;

import java.util.BitSet;
import java.util.stream.IntStream;

public class BitSetUtils {
    public static BitSet fromString(String s) {
        BitSet result = new BitSet(s.length());
        for (int i = 0; i < s.length(); i++) {
            result.set(i, s.charAt(i) == '1');
        }
        return result;
    }

    public static String toBinaryString(BitSet c, int nbits) {
        StringBuilder builder = new StringBuilder(nbits);
        IntStream.range(0, nbits).mapToObj(i -> c.get(i) ? '1' : '0').forEach(builder::append);
        return builder.toString();
    }

}
