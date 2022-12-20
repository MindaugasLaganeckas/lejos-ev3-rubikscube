package ev3.rubikscube.controller.frameprocessor;

import java.io.Closeable;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class FrameGrabber implements Runnable, Closeable {
	
	private final VideoCapture capture;
	private final FrameObserver[] observers;
	
	public FrameGrabber(final FrameObserver[] observers, final int videoDeviceIndex) {
		this.capture = new VideoCapture(videoDeviceIndex);
		// start the video capture
		this.capture.open(videoDeviceIndex);
		//this.capture.set(3, 1080);
		//this.capture.set(4, 720);
		this.capture.set(39, 1);
		this.observers = observers;
	}
	
	@Override
	public void run() {
		final Mat frame = grabFrame();
		for (final FrameObserver frameObserver : observers) {
			frameObserver.update(frame);
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
