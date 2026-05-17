package esp32client.physical;

import esp32client.util.InputStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A robust MJPEG-like stream client that fetches snapshots sequentially.
 * This opens the actual Windows Driver Settings window
 * //this.capture.set(Videoio.CAP_PROP_SETTINGS, 1);
 */
@Slf4j
public class RobustLogitechC920Client implements Closeable {
    private final InputStreamProcessor inputStreamProcessor;
    private final VideoCapture capture;

    // Each instance gets its own executor to manage its specific loop
    private final ScheduledExecutorService executor;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private volatile boolean isRunning = true;

    public RobustLogitechC920Client(final InputStreamProcessor inputStreamProcessor) {
        this.capture = new VideoCapture(0, Videoio.CAP_DSHOW);

        if (!this.capture.isOpened()) {
            throw new RuntimeException("Camera failed to open");
        }

        this.capture.set(Videoio.CAP_PROP_FOURCC, VideoWriter.fourcc('M', 'J', 'P', 'G'));

        this.capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 320);
        this.capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 240);

        // --- Video Processing Amplifier Settings ---

        // Brightness: 140
        this.capture.set(Videoio.CAP_PROP_BRIGHTNESS, 140);

        // Contrast: 0
        this.capture.set(Videoio.CAP_PROP_CONTRAST, 0);

        // Saturation (Mætning): 160 (This is quite high, good for Rubik's cubes!)
        this.capture.set(Videoio.CAP_PROP_SATURATION, 160);

        // Sharpness (Skarphed): 0 (Turning this down helps K-Means find smooth colors)
        this.capture.set(Videoio.CAP_PROP_SHARPNESS, 0);

        // Backlight Compensation (Kompensation for baggr.lys): 1
        this.capture.set(Videoio.CAP_PROP_BACKLIGHT, 1);

        // Gain (Forstærking): 0
        this.capture.set(Videoio.CAP_PROP_GAIN, 0);

        // --- Camera Control Settings ---
        // Disable Auto Focus first, then set manual value
        this.capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);
        this.capture.set(Videoio.CAP_PROP_FOCUS, 90);

        // Disable Auto Exposure (0.25 is often manual mode for DSHOW)
        this.capture.set(Videoio.CAP_PROP_AUTO_EXPOSURE, 0.25);
        this.capture.set(Videoio.CAP_PROP_EXPOSURE, -3);

        // White Balance (Hvidbalance): 2800
        this.capture.set(Videoio.CAP_PROP_AUTO_WB, 0);

        // This opens the actual Windows Driver Settings window
        this.capture.set(Videoio.CAP_PROP_SETTINGS, 1);

        this.inputStreamProcessor = inputStreamProcessor;
        // Single thread ensures we aren't decoding two frames from the SAME camera at once
        this.executor = Executors.newSingleThreadScheduledExecutor();
        // Kick off the loop
        startStreaming();
    }

    private void startStreaming() {
        if (this.isRunning) {
            fetchSnapshot();
        }
    }

    private void fetchSnapshot() {
        // Safety gate: prevent overlapping requests for this specific camera
        if (!this.isRunning || !this.isProcessing.compareAndSet(false, true)) {
            return;
        }

        final Mat img = new Mat();
        try {
            final boolean readSuccessful = this.capture.read(img);
            if (readSuccessful) {
                RobustLogitechC920Client.this.inputStreamProcessor.processMat(img);
            }
        } finally {
            img.release();
            RobustLogitechC920Client.this.isProcessing.set(false);
            if (RobustLogitechC920Client.this.isRunning) {
                // Immediately schedule the next frame
                RobustLogitechC920Client.this.executor.execute(this::fetchSnapshot);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
        this.isRunning = false;
        this.executor.shutdownNow();
    }
}