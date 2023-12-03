package neilyich.fht;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FHT_v1 implements FHT {
    private BitSet c;
    private int nbits;
    private final ExecutorService executor;

    public FHT_v1(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }
    
    @Override
    public BitSet transform(BitSet c, int nbits) throws InterruptedException {
        this.completedCount.set(0);
        this.nbits = nbits;
        this.c = c;
        BitSet buffer = new BitSet(this.nbits);
        int blockSize = this.nbits >> 1;
        executor.execute(new HadamardIterationTask(c, 0, blockSize, buffer));
        int depth = (int) (Math.log(this.nbits) / Math.log(2));
        BitSet result = depth % 2 == 0 ? c : buffer;
        boolean completed = executor.awaitTermination(10, TimeUnit.HOURS);
        if (!completed) {
            throw new RuntimeException("Could not complete calculation");
        }
        return result;
    }

    private final AtomicInteger completedCount = new AtomicInteger(0);

    private class HadamardIterationTask implements Runnable {
        public HadamardIterationTask(BitSet source, int start, int blockSize, BitSet target) {
            this.source = source;
            this.start = start;
            this.blockSize = blockSize;
            this.target = target;
        }

        private final BitSet source;
        private final int start;
        private final int blockSize;
        private final BitSet target;

        @Override
        public void run() {
            System.out.println(source.toString());
            System.out.println(String.join(", ", Stream.of(start, blockSize).map(String::valueOf).toList()));
            fourierIteration();
            System.out.println(target.toString());
            System.out.println();
            int newBlockSize = blockSize >> 1;
            if (newBlockSize == 0) {
                if (completedCount.addAndGet(2) == nbits) {
                    executor.shutdown();
                }
                return;
            }
            executor.execute(new HadamardIterationTask(target, start, newBlockSize, source));
            executor.execute(new HadamardIterationTask(target, start + blockSize, newBlockSize, source));
        }

        private void fourierIteration() {
            int mid = start + blockSize;
            for (int offset = 0; offset < blockSize; offset++) {
                int startPlusOffset = start + offset;
                int midPlusOffset = mid + offset;
                target.set(startPlusOffset, source.get(startPlusOffset));
                target.set(midPlusOffset, source.get(startPlusOffset) ^ source.get(midPlusOffset));
            }
        }
    }
}
