package neilyich.utils;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicBitSet extends BitSet {
    private final AtomicIntegerArray array;

    public AtomicBitSet(int length) {
        int intLength = (length + 31) >>> 5; // unsigned / 32
        array = new AtomicIntegerArray(intLength);
    }

    @Override
    public void set(int n, boolean value) {
        int idx = (int) (n >>> 5);
        while (true) {
            int num = array.get(idx);
            int num2;
            int bit = 1 << n;
            if (value) {
                num2 = num | bit;
            } else {
                num2 = num & (~bit);
            }
            if (num == num2 || array.compareAndSet(idx, num, num2))
                return;
        }
    }

    @Override
    public void set(int bitIndex) {
        set(bitIndex, true);
    }

    @Override
    public boolean get(int n) {
        int bit = 1 << n;
        int idx = (int) (n >>> 5);
        int num = array.get(idx);
        return (num & bit) != 0;
    }
}
