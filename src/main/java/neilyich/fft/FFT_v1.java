package neilyich.fft;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FFT_v1 implements FFT {
    private int[] c;
    private final ExecutorService executor;

    public FFT_v1(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public int[] transform(int[] c) throws InterruptedException {
        this.completedCount.set(0);
        this.c = c;
        int[] buffer = new int[c.length];
        int blockSize = c.length >> 1;
        executor.execute(new FourierIterationTask(c, 0, blockSize, buffer));
        int depth = (int) (Math.log(c.length) / Math.log(2));
        int[] result = depth % 2 == 0 ? c : buffer;
        boolean completed = executor.awaitTermination(10, TimeUnit.HOURS);
        if (!completed) {
            throw new RuntimeException("Could not complete calculation");
        }
        return result;
    }

    private final AtomicInteger completedCount = new AtomicInteger(0);

    private class FourierIterationTask implements Runnable {
        public FourierIterationTask(int[] source, int start, int blockSize, int[] target) {
            this.source = source;
            this.start = start;
            this.blockSize = blockSize;
            this.target = target;
        }

        private final int[] source;
        private final int start;
        private final int blockSize;
        private final int[] target;

        @Override
        public void run() {
            System.out.println(Arrays.toString(source));
            System.out.println(String.join(", ", Stream.of(start, blockSize).map(String::valueOf).toList()));
            fourierIteration();
            System.out.println(Arrays.toString(target));
            System.out.println();
            int newBlockSize = blockSize >> 1;
            if (newBlockSize == 0) {
                if (completedCount.addAndGet(2) == c.length) {
                    executor.shutdown();
                }
                return;
            }
            executor.execute(new FourierIterationTask(target, start, newBlockSize, source));
            executor.execute(new FourierIterationTask(target, start + blockSize, newBlockSize, source));
        }

        private void fourierIteration() {
            int mid = start + blockSize;
            for (int offset = 0; offset < blockSize; offset++) {
                int startPlusOffset = start + offset;
                int midPlusOffset = mid + offset;
                target[startPlusOffset] = source[startPlusOffset] + source[midPlusOffset];
                target[midPlusOffset] = source[midPlusOffset] - source[startPlusOffset];
            }
        }
    }
}
