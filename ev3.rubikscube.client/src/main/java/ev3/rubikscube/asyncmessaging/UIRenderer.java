package ev3.rubikscube.asyncmessaging;

import ev3.rubikscube.asyncmessaging.events.FrameCapturedEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.swing.*;

public final class UIRenderer {
    private final JLabel label;
    private final FrameBufferPool pool;

    UIRenderer(final JLabel label, final FrameBufferPool pool) {
        this.label = label;
        this.pool = pool;
    }

    /**
     * Lease is acquired by the camera @CameraReader#captureLoop,
     * this one must release it after rendering
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFrame(final FrameCapturedEvent event) {


        // Display image directly (no snapshot)
        this.label.setIcon(new ImageIcon(event.frame().image()));
        // Release lease AFTER Swing event loop renders (using invokeLater)
        SwingUtilities.invokeLater(() -> this.pool.releaseBuffer(event.frame()));
    }
}