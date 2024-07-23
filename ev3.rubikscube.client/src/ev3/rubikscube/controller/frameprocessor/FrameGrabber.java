package ev3.rubikscube.controller.frameprocessor;

import java.io.Closeable;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import ev3.rubikscube.ui.IFrameObserver;

public class FrameGrabber implements Runnable, Closeable {
	
	private final VideoCapture capture;
	private final IFrameObserver[] observers;
	
	public FrameGrabber(final IFrameObserver[] observers, final int videoDeviceIndex) {
		
		this.observers = observers;
		
		this.capture = new VideoCapture(videoDeviceIndex);
		// start the video capture
		this.capture.open(videoDeviceIndex);
		
        // Set camera properties
		this.capture.set(Videoio.CAP_PROP_FPS, 30);
		this.capture.set(Videoio.CAP_PROP_BRIGHTNESS, 0.5);
		this.capture.set(Videoio.CAP_PROP_CONTRAST, 0.5);
		this.capture.set(Videoio.CAP_PROP_EXPOSURE, -1); // Auto exposure
		this.capture.set(Videoio.CAP_PROP_AUTOFOCUS, 1);
		
        // Get the highest resolution supported by the camera
        Size frameSize = new Size(
        		this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH),
        		this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT)
        );

        System.out.println("Current resolution: " + frameSize.width + "x" + frameSize.height);

        // Increase resolution (example: 1920x1080 for Full HD)
        this.capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1920);
        this.capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 1080);
		
        // Verify the resolution setting
        frameSize = new Size(
        		this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH),
        		this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT)
        );

        System.out.println("Set resolution: " + frameSize.width + "x" + frameSize.height);

	}
	
	@Override
	public void run() {
		final Mat frame = grabFrame();
		for (final IFrameObserver frameObserver : observers) {
			try {
				frameObserver.update(frame);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Mat grabFrame() {
		final Mat originalFrame = new Mat();
		this.capture.read(originalFrame);
		return originalFrame;
	}

	@Override
	public void close() throws IOException {
		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}
}
