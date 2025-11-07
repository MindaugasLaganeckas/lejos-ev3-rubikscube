package ev3.rubikscube.asyncmessaging;

import ev3.rubikscube.asyncmessaging.events.FrameCapturedEvent;
import org.greenrobot.eventbus.EventBus;

import java.awt.image.BufferedImage;
import java.util.random.RandomGenerator;

public final class CameraReader implements Runnable {
    private final FrameBufferPool pool;
    private final RandomGenerator rng = RandomGenerator.getDefault();
    private volatile boolean running = true;

    CameraReader(final FrameBufferPool pool, final EventBus bus) {
        this.pool = pool;
    }

    /**
     * Lease will be released by @{@link UIRenderer#onFrame(FrameCapturedEvent)}
     */
    @Override
    public void run() {
        while (this.running && !Thread.currentThread().isInterrupted()) {
            final FrameBuffer fb = this.pool.acquireFreeBuffer();
            captureInto(fb.image());
            EventBus.getDefault().post(new FrameCapturedEvent(fb));
            // there is always a camera view, that needs to show the frame
            // so the camera view will release the lease
            try {
                Thread.sleep(33L);
            } catch (final InterruptedException e) {
                break;
            }
        }
    }

    void stop() {
        this.running = false;
    }

    private void captureInto(final BufferedImage img) {
        final int color = this.rng.nextInt(0xFFFFFF);
        final int w = img.getWidth();
        final int h = img.getHeight();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                img.setRGB(x, y, color);
    }
}