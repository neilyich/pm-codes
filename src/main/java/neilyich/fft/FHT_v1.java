package neilyich.fft;

import neilyich.utils.LogUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Stream;

public class FHT_v1 implements FHT {
    private final int threads;

    public FHT_v1(int threads) {
        this.threads = threads;
    }

    private ExecutorService executor;

    @Override
    public int[] transform(int[] c) {
        executor = Executors.newFixedThreadPool(threads);
        this.completedCount.set(0);
        int[] buffer = new int[c.length];
        int blockSize = c.length >> 1;
        var cArray = new AtomicIntegerArray(c.length);
        for (int i = 0; i < c.length; i++) {
            cArray.set(i, c[i]);
        }
        var bufferArray = new AtomicIntegerArray(buffer);
        executor.execute(new HadamardIterationTask(cArray, 0, blockSize, bufferArray, c.length));
        int depth = (int) (Math.log(c.length) / Math.log(2));
        var resultArray = depth % 2 == 0 ? cArray : bufferArray;
        boolean completed;
        try {
            completed = executor.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!completed) {
            throw new RuntimeException("Could not complete calculation");
        }
        int[] result = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            result[i] = resultArray.get(i);
        }
        return result;
    }

    private final AtomicInteger completedCount = new AtomicInteger(0);

    private class HadamardIterationTask implements Runnable {
        public HadamardIterationTask(AtomicIntegerArray source, int start, int blockSize, AtomicIntegerArray target, int length) {
            this.source = source;
            this.start = start;
            this.blockSize = blockSize;
            this.target = target;
            this.length = length;
        }

        private final AtomicIntegerArray source;
        private final int start;
        private final int blockSize;
        private final AtomicIntegerArray target;
        private final int length;

        @Override
        public void run() {
//            var t = source;
//            if (t[0] == 0) {
//                source = t;
//            } else {
//                source = t;
//            }
//            t = target;
//            if (t[0] == 0) {
//                target = t;
//            } else {
//                target = t;
//            }
            //source = source;
            //target = target;
            //LogUtils.println(source.toString());
            //LogUtils.println(String.join(", ", Stream.of(start, blockSize).map(String::valueOf).toList()));
            hadamardIteration();
            //LogUtils.println(target);
            //LogUtils.println();
            int newBlockSize = blockSize >> 1;
            if (newBlockSize == 0) {
                if (completedCount.addAndGet(2) == length) {
                    executor.shutdown();
                }
                return;
            }
            executor.execute(new HadamardIterationTask(target, start, newBlockSize, source, length));
            executor.execute(new HadamardIterationTask(target, start + blockSize, newBlockSize, source, length));
        }

        private void hadamardIteration() {
            int mid = start + blockSize;
            for (int offset = 0; offset < blockSize; offset++) {
                int startPlusOffset = start + offset;
                int midPlusOffset = mid + offset;

                target.compareAndSet(startPlusOffset, target.get(startPlusOffset), source.get(startPlusOffset) + source.get(midPlusOffset));
                //target[startPlusOffset] = source[startPlusOffset] + source[midPlusOffset];

                target.compareAndSet(midPlusOffset, target.get(midPlusOffset), source.get(midPlusOffset) - source.get(startPlusOffset));
                //target[midPlusOffset] = source[midPlusOffset] - source[startPlusOffset];
            }
        }
    }
}
