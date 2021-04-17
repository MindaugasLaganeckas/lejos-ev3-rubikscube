package it.polito.elite.teaching.cv;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ev3.rubikscube.controller.frameprocessor.FrameGrabber;
import ev3.rubikscube.controller.frameprocessor.FrameObserver;
import ev3.rubikscube.controller.frameprocessor.decorator.DottedFrameDecorator;
import ev3.rubikscube.controller.frameprocessor.decorator.InterprettedFrameDecorator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ImageSegController {

	// FXML buttons
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView processedFrame;
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	private Runnable frameGrabber;

	/**
	 * The action triggered by pushing the button on the GUI
	 * @throws IOException 
	 */
	@FXML
	protected void startCamera() throws IOException {
		// set a fixed width for the frame
		originalFrame.setFitWidth(380);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);
		
		// set a fixed width for the frame
		processedFrame.setFitWidth(380);
		// preserve image ratio
		processedFrame.setPreserveRatio(true);

		// grab a frame every 33 ms (30 frames/sec)
		this.frameGrabber = new FrameGrabber( 
				new FrameObserver[] {
						new FrameObserver(originalFrame, new DottedFrameDecorator()),
						new FrameObserver(processedFrame, new InterprettedFrameDecorator()),
			}
		);

		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		// update the button content
		this.cameraButton.setDisable(true);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 * @throws IOException 
	 */
	private void stopAcquisition() throws IOException {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
				((Closeable)this.frameGrabber).close();
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
	}

	/**
	 * On application close, stop the acquisition from the camera
	 * @throws IOException 
	 */
	protected void setClosed() throws IOException {
		this.stopAcquisition();
	}
}
