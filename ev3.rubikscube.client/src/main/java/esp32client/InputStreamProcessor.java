package esp32client;

import esp32client.enums.CameraId;
import esp32client.enums.CubeColor;
import esp32client.enums.RobotStatus;
import esp32client.events.EventBusWrapper;
import esp32client.events.FrameCreated;
import esp32client.events.RobotStatusChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class InputStreamProcessor {
    private final EventBusWrapper eventBus;
    private final RubiksColorDetector rubiksColorDetector;
    private final CameraId cameraId;

    private final AtomicBoolean processColors = new AtomicBoolean(false);

    public void processStream(final InputStream in) {
        // ImageIO.read is blocking; happens on the OkHttp Dispatcher thread
        // Meaning, we will not call Wifi camera again, until we are done processing here
        Mat destination = null;
        try {
            destination = ImageUtils.inputStreamToMat(in);
            ImageUtils.flipImage(destination, destination, true);
            processMat(destination);
        } catch (final IOException e) {
            if (destination != null) {
                destination.release();
            }
            log.error("Camera {} decode error: {}", this.cameraId, e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void process(final RobotStatusChanged status) {
        this.processColors.set(status.status() != RobotStatus.IN_MOTION);
    }

    /**
     * Caller is responsible to close @param destination
     */
    public void processMat(final Mat destination) {
        if (this.processColors.get()) {
            final CubeColor[][] dominantColorsInGrid = this.rubiksColorDetector.getDominantColorsInGrid(destination, Constants.SIDE_LENGTH);
            ImageUtils.overlayDetectedColors(destination, dominantColorsInGrid, Constants.SIDE_LENGTH, this.cameraId.isSideCamera());
        }
        this.eventBus.post(new FrameCreated(this.cameraId, ImageUtils.matToBufferedImage(destination)));
    }
}
