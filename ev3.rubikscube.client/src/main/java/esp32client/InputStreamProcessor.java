package esp32client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.EventBus;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class InputStreamProcessor {
    private final RubiksColorDetector rubiksColorDetector = new RubiksColorDetector();
    private final int cameraIndex;
    private final boolean isSideCamera;

    public void processStream(final InputStream in) {
        // ImageIO.read is blocking; happens on the OkHttp Dispatcher thread
        // Meaning, we will not call Wifi camera again, until we are done processing here
        Mat destination = null;
        try {
            destination = ImageUtils.inputStreamToMat(in);
            ImageUtils.flipImage(destination, destination, true);
            //ImageUtils.adjustContrast(destination, destination, 1.0d, 0.0d);
            final RubiksColorDetector.CubeColor[][] dominantColorsInGrid = this.rubiksColorDetector.getDominantColorsInGrid(destination, Constants.SIDE_LENGTH);
            ImageUtils.overlayDetectedColors(destination, dominantColorsInGrid, Constants.SIDE_LENGTH, this.isSideCamera);
            EventBus.getDefault().post(new Frame(this.cameraIndex, ImageUtils.matToBufferedImage(destination)));
        } catch (final IOException e) {
            if (destination != null) {
                destination.release();
            }
            log.error("Camera {} decode error: {}", +this.cameraIndex, e.getMessage());
        }
    }
}
