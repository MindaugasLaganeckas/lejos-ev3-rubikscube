package ev3.rubikscube.asyncmessaging;

import ev3.rubikscube.asyncmessaging.events.FrameCapturedEvent;
import lombok.AllArgsConstructor;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public final class FrameDispatcher {
    private final BlockingQueue<FrameBuffer> queue;

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onFrame(final FrameCapturedEvent event) {
        // queue for workers
        if (!this.queue.offer(event.frame())) {
            // drop frame and release camera lease immediately
            event.frame().release();
            System.out.println("FrameDispatcher released frame");
        }
    }
}