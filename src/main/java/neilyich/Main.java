package neilyich;

import neilyich.fft.FFT_v1;
import neilyich.fht.FHT_v1;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var fft = new FFT_v1(5);
        var c = Stream.of(-1,1,1,-1,1,-1,-1,1).mapToInt(i -> i).toArray();
        var result = fft.transform(c);
        System.out.println(String.join(",", Arrays.stream(result).mapToObj(String::valueOf).toList()));

        var nbits = 8;
        var fht = new FHT_v1(5);
        var c1 = new BitSet(nbits);
        c1.set(0);
        c1.set(1);
        c1.set(2);
        c1.set(4);
        var result1 = fht.transform(c1, nbits);
        final StringBuilder buffer = new StringBuilder(8);
        IntStream.range(0, nbits).mapToObj(i -> result1.get(i) ? '1' : '0').forEach(buffer::append);
        System.out.println(buffer);
    }
}