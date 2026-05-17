package esp32client.util;

import esp32client.RubiksColorDetector;
import esp32client.enums.CameraId;
import esp32client.enums.CubeColor;
import esp32client.enums.RobotStatus;
import esp32client.events.ColorReadCompleted;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public class InputStreamProcessor {
    private static final int SIDE_LENGTH = 50;
    private static final int MAX_READ_COUNT = 10;
    private final ColorAnalyzer colorAnalyzer;
    private final EventBusWrapper eventBus;
    private final RubiksColorDetector rubiksColorDetector;
    private final CameraId cameraId;
    private final AtomicBoolean processColors = new AtomicBoolean(false);
    private final AtomicInteger readCount = new AtomicInteger(0);
    private List<CubeColor[][]> colorReads = new ArrayList<>(MAX_READ_COUNT);
    private CubeColor[][] mostFrequentPerCell = null;

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
        this.readCount.set(0);
        this.colorReads = new ArrayList<>(MAX_READ_COUNT);
    }

    /**
     * Caller is responsible to close @param destination
     */
    public void processMat(final Mat destination) {
        if (this.processColors.get()) {
            final int currentIteration = this.readCount.getAndIncrement();
            if (MAX_READ_COUNT > currentIteration) {
                final CubeColor[][] dominantColorsInGrid = this.rubiksColorDetector.getDominantColorsInGrid(destination, SIDE_LENGTH);
                this.colorReads.add(dominantColorsInGrid);
            } else if (MAX_READ_COUNT == currentIteration) {
                this.mostFrequentPerCell = this.colorAnalyzer.mostFrequentPerCell(this.colorReads);
                this.eventBus.post(new ColorReadCompleted(this.cameraId, this.mostFrequentPerCell));
            }
            if (this.mostFrequentPerCell != null) {
                ImageUtils.overlayDetectedColors(destination, this.mostFrequentPerCell, SIDE_LENGTH, this.cameraId.isSideCamera());
            }
        }
        this.eventBus.post(new FrameCreated(this.cameraId, ImageUtils.matToBufferedImage(destination)));
    }
}
