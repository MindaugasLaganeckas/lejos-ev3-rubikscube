package ev3.rubikscube.asyncmessaging;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FrameBufferPool {
    private final FrameBuffer[] buffers;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = this.lock.newCondition();

    FrameBufferPool(final int poolSize, final int width, final int height) {
        this.buffers = new FrameBuffer[poolSize];
        for (int i = 0; i < poolSize; i++)
            this.buffers[i] = new FrameBuffer(width, height);
    }

    FrameBuffer acquireFreeBuffer() {
        this.lock.lock();
        try {
            while (true) {
                for (final FrameBuffer fb : this.buffers) {
                    if (fb.isFree()) {
                        fb.acquire();
                        return fb;
                    }
                }
                this.available.awaitNanos(5_000_000L); // wait ~5 ms
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
        }
    }

    void releaseBuffer(final FrameBuffer fb) {
        fb.release();
        if (fb.isFree()) { // only signal when it actually becomes free
            this.lock.lock();
            try {
                this.available.signal();
            } finally {
                this.lock.unlock();
            }
        }
    }
}