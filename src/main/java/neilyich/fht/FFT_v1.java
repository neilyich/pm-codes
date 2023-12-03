package neilyich.fht;

import neilyich.utils.AtomicBitSet;
import neilyich.utils.LogUtils;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static neilyich.utils.BitSetUtils.toBinaryString;

public class FFT_v1 implements FFT {
    private final int threads;

    public FFT_v1(int threads) {
        this.threads = threads;
    }

    private ExecutorService executor;
    
    @Override
    public BitSet transform(BitSet c, int nbits) {
        //LogUtils.println("FFT: " + toBinaryString(c, nbits));
        this.executor = Executors.newFixedThreadPool(threads);
        this.completedCount.set(0);
        var cAtomic = new AtomicBitSet(nbits);
        for (int i = 0; i < nbits; i++) {
            cAtomic.set(i, c.get(i));
        }
        BitSet buffer = new AtomicBitSet(nbits);
        int blockSize = nbits >> 1;
        executor.execute(new FourierIterationTask(cAtomic, 0, blockSize, buffer, nbits));
        int depth = (int) (Math.log(nbits) / Math.log(2));
        BitSet result = depth % 2 == 0 ? cAtomic : buffer;
        boolean completed;
        try {
            completed = executor.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!completed) {
            throw new RuntimeException("Could not complete calculation");
        }
        //LogUtils.println("FFT result: " + toBinaryString(result, nbits));
        var res = new BitSet(nbits);
        for (int i = 0; i < nbits; i++) {
            res.set(i, result.get(i));
        }
        return res;
    }

    private final AtomicInteger completedCount = new AtomicInteger(0);

    private class FourierIterationTask implements Runnable {
        public FourierIterationTask(BitSet source, int start, int blockSize, BitSet target, int nbits) {
            this.source = source;
            this.start = start;
            this.blockSize = blockSize;
            this.target = target;
            this.nbits = nbits;
        }

        private volatile BitSet source;
        private final int start;
        private final int blockSize;
        private volatile BitSet target;
        private final int nbits;

        @Override
        public void run() {
            //Thread.currentThread().getName();
//            LogUtils.println(name + ": " + toBinaryString(source, nbits));
//            LogUtils.println(name + ": " + String.join(", ", Stream.of(start, blockSize).map(String::valueOf).toList()));
//            fourierIteration();
//            LogUtils.println(name + ": " + toBinaryString(target, nbits));
            //LogUtils.println(toBinaryString(source, nbits));
            //LogUtils.println(String.join(", ", Stream.of(start, blockSize).map(String::valueOf).toList()));
            fourierIteration();
            //.println(toBinaryString(target, nbits));
            //LogUtils.println();
            int newBlockSize = blockSize >> 1;
            if (newBlockSize == 0) {
                if (completedCount.addAndGet(2) == nbits) {
                    executor.shutdown();
                }
                return;
            }
            executor.execute(new FourierIterationTask(target, start, newBlockSize, source, nbits));
            executor.execute(new FourierIterationTask(target, start + blockSize, newBlockSize, source, nbits));
        }

        private void fourierIteration() {
//            try {
//                //Thread.sleep(5);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
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

// 11010
// [-1, 1, -1, 1, 1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, -1]
// [0, 0, 0, 0, 0, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
