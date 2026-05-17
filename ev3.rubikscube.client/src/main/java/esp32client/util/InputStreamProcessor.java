package esp32client.util;

import esp32client.RubiksColorDetector;
import esp32client.commands.ReadColorsCommand;
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

@Slf4j
@RequiredArgsConstructor
public class InputStreamProcessor {
    private static final int SIDE_LENGTH = 50;
    private static final int MAX_READ_COUNT = 10;
    private final ColorAnalyzer colorAnalyzer;
    private final EventBusWrapper eventBus;
    private final RubiksColorDetector rubiksColorDetector;
    private final CameraId cameraId;
    private final Object lock = new Object();
    private boolean processColors = false;
    private int readCount = 0;
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
    public void process(final RobotStatusChanged event) {
        synchronized (this.lock) {
            if (event.status() == RobotStatus.IN_MOTION) {
                this.processColors = false;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void process(final ReadColorsCommand command) {
        synchronized (this.lock) {
            this.processColors = true;
            this.readCount = 0;
            this.colorReads = new ArrayList<>(MAX_READ_COUNT);
            this.mostFrequentPerCell = null;
        }
    }

    /**
     * Caller is responsible to close @param destination
     */
    public void processMat(final Mat destination) {
        CubeColor[][] overlayColors = null;
        boolean shouldAnalyze = false;
        synchronized (this.lock) {
            if (this.processColors) {
                if (this.readCount < MAX_READ_COUNT) {
                    shouldAnalyze = true;
                    this.readCount++;
                }
                overlayColors = this.mostFrequentPerCell;
            }
        }
        if (shouldAnalyze) {
            final CubeColor[][] dominant = this.rubiksColorDetector.getDominantColorsInGrid(destination, SIDE_LENGTH);
            CubeColor[][] completedResult = null;
            synchronized (this.lock) {
                this.colorReads.add(dominant);
                if (this.colorReads.size() == MAX_READ_COUNT && this.mostFrequentPerCell == null) {
                    this.mostFrequentPerCell = this.colorAnalyzer.mostFrequentPerCell(this.colorReads);
                    completedResult = this.mostFrequentPerCell;
                }
                overlayColors = this.mostFrequentPerCell;
            }
            if (completedResult != null) {
                this.eventBus.post(new ColorReadCompleted(this.cameraId, completedResult)
                );
            }
        }
        if (overlayColors != null) {
            ImageUtils.overlayDetectedColors(destination, overlayColors, SIDE_LENGTH, this.cameraId.isSideCamera());
        }
        this.eventBus.post(new FrameCreated(this.cameraId, ImageUtils.matToBufferedImage(destination)));
    }
}
