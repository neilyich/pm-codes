package neilyich;

import neilyich.bf.BFVectorCalculator_v1;
import neilyich.fft.FHT_v1;
import neilyich.fht.FFT_v1;
import neilyich.real.RealAnalog_Transformer_v1;
import neilyich.rm.RMCode;
import neilyich.utils.LogUtils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Stream;

import static neilyich.utils.BitSetUtils.fromString;
import static neilyich.utils.BitSetUtils.toBinaryString;

public class Main {
    public static void main(String[] args) {
        int threads = 130;
        var fft = new FHT_v1(threads);
        var fht = new FFT_v1(threads);
        var bfVectorCalculator = new BFVectorCalculator_v1();
        var realAnalogTransformer = new RealAnalog_Transformer_v1();
        RMCode code = new RMCode(fft, fht, bfVectorCalculator, realAnalogTransformer);

        var start = System.currentTimeMillis();
//        var i = 0;
//        while (testAll(code, 22)) {
//            System.out.println("outer I: " + i++);
//        }
//        while (test(code, "1111")) {
//            System.out.println("simple I: " + i++);
//        }
//        if (true) {
//            return;
//        }
        var a0 = "01010011110000101001";
        System.out.println(a0.length());
        var result = test(code, a0);
        var end = System.currentTimeMillis();
        //var result = testAll(code, 7);
        System.out.println("\n\n--------------------------");
        System.out.println("Test result: " + result);
        System.out.println("Duration: " + (end - start) + "ms");

        if (true) {
            return;
        }

        var a = "1010";
        int xCount = a.length() - 1;
        int nbits = (int) Math.pow(2, xCount);

        LogUtils.println("Encoding: " + a);
        var encoded = code.encode(fromString(a), nbits);
        //LogUtils.println("ENCODED!!!");
        LogUtils.println(toBinaryString(encoded, nbits));
        LogUtils.println();

        var decoded = code.decode(encoded, nbits);
        LogUtils.println();
        //LogUtils.println("DECODED!!!");
        LogUtils.println(toBinaryString(decoded, xCount + 1));
    }

    private static boolean testAll(RMCode code, int aLen) {
        var result = true;
        var aCount = (int) Math.pow(2, aLen);
        for (int i = 0; i < aCount; i++) {
            result &= test(code, fromInt(i, aLen));
            System.out.println("inner I: " + i + ", result=" + result + ", word=" + fromInt(i, aLen));
            if (!result) {
                System.out.println("inner I: " + i + ", result=" + result + ", word=" + fromInt(i, aLen));
                return result;
            }
        }
        return result;
    }

    private static boolean test(RMCode code, String a) {
        int xCount = a.length() - 1;
        int nbits = (int) Math.pow(2, xCount);

        LogUtils.println("Encoding: " + a);
        var encoded = code.encode(fromString(a), nbits);
        LogUtils.println("ENCODED!!!");
        //LogUtils.println(toBinaryString(encoded, nbits));
        LogUtils.println();

        var decoded = code.decode(encoded, nbits);
        LogUtils.println();
        LogUtils.println("DECODED!!!");
        var decodedA = toBinaryString(decoded, xCount + 1);
        //LogUtils.println(decodedA);

        LogUtils.println("Decoded equals: " + decodedA.equals(a));
        return decodedA.equals(a);
    }


    private static void test() {
        var fft = new FHT_v1(5);
        var fht = new FFT_v1(5);
        var bfVectorCalculator = new BFVectorCalculator_v1();
        var realAnalogTransformer = new RealAnalog_Transformer_v1();
        RMCode code = new RMCode(fft, fht, bfVectorCalculator, realAnalogTransformer);




        var c = Stream.of(-1,1,1,-1,1,-1,-1,1).mapToInt(i -> i).toArray();
        var result = fft.transform(c);
        LogUtils.println(String.join(",", Arrays.stream(result).mapToObj(String::valueOf).toList()));

        var nbits = 8;
        var c1 = new BitSet(nbits);
        c1.set(0);
        c1.set(1);
        c1.set(2);
        c1.set(4);
        var result1 = fht.transform(c1, nbits);
        var result2 = fht.transform(c1, nbits);
        final StringBuilder buffer = new StringBuilder(8);

        LogUtils.println(buffer);
    }

    private static String fromInt(int n, int nbits) {
        StringBuilder s = new StringBuilder(Integer.toBinaryString(n));
        while (s.length() < nbits) {
            s.insert(0, "0");
        }
        return s.toString();
    }
}