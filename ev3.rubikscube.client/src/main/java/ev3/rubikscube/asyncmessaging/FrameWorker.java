package ev3.rubikscube.asyncmessaging;


import ev3.rubikscube.asyncmessaging.events.ColorsRecognizedEvent;
import org.greenrobot.eventbus.EventBus;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.*;

public final class FrameWorker implements Runnable {
    private final BlockingQueue<FrameBuffer> queue;
    private final FrameBufferPool pool;
    private final EventBus bus;

    FrameWorker(final BlockingQueue<FrameBuffer> queue, final FrameBufferPool pool, final EventBus bus) {
        this.queue = queue;
        this.pool = pool;
        this.bus = bus;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                final FrameBuffer fb = this.queue.take();
                fb.acquire(); // worker lease
                try {
                    final Map<String, String> colors = recognizeColors(fb.image());
                    this.bus.post(new ColorsRecognizedEvent(colors));
                } finally {
                    this.pool.releaseBuffer(fb);
                }
            }
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, String> recognizeColors(final BufferedImage img) {
        return Map.of("U", "white", "R", "red");
    }
}