package neilyich;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Fourier {
    public Fourier(int[] c, ExecutorService executor) {
        this.c = c;
        this.executor = executor;
    }

    private final int[] c;
    private final ExecutorService executor;

    private int[] executeFourier() throws InterruptedException {
        int[] buffer = new int[c.length];
        int blockSize = c.length >> 1;
        executor.execute(new FourierIterationTask(c, 0, blockSize, buffer));
        executor.execute(new FourierIterationTask(c, blockSize, blockSize << 1, buffer));
        boolean completed = executor.awaitTermination(10, TimeUnit.HOURS);
        return null;
    }


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
            fourierIteration();
            int newBlockSize = blockSize >> 1;
            if (newBlockSize == 0 && start == source.length - 2 ) {

            }
            executor.execute(new FourierIterationTask(target, start, newBlockSize, source));
            executor.execute(new FourierIterationTask(target, start + newBlockSize, newBlockSize, source));
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
