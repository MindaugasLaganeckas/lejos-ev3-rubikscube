package ev3.rubikscube.asyncmessaging;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class FrameBuffer {
    private final BufferedImage image;
    private final AtomicInteger leases = new AtomicInteger(0);

    FrameBuffer(final int width, final int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    BufferedImage image() {
        return this.image;
    }

    void acquire() {
        this.leases.incrementAndGet();
    }

    void release() {
        final int remaining = this.leases.decrementAndGet();
        if (remaining < 0)
            System.err.println("FrameBuffer released too many times");
    }

    boolean isFree() {
        return this.leases.get() <= 0;
    }
}