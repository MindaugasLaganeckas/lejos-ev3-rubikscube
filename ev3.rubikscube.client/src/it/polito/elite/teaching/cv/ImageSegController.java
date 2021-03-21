package it.polito.elite.teaching.cv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import it.polito.elite.teaching.cv.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the image segmentation process.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="mailto:alberto.cannavo@polito.it">Alberto Cannav�</a>
 * @version 2.0 (2017-03-10)
 * @since 1.0 (2013-12-20)
 * 
 */

public class ImageSegController {

	// FXML buttons
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView processedFrame;
	// checkbox for enabling/disabling Canny

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture(1);
	// a flag to change the button behavior
	private boolean cameraActive;

	Point clickedPoint = new Point(0, 0);
	Mat oldFrame;

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		// set a fixed width for the frame
		originalFrame.setFitWidth(380);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);
		
		// set a fixed width for the frame
		processedFrame.setFitWidth(380);
		// preserve image ratio
		processedFrame.setPreserveRatio(true);

		// mouse listener
		originalFrame.setOnMouseClicked(e -> {
			System.out.println("[" + e.getX() + ", " + e.getY() + "]");
			clickedPoint.x = e.getX();
			clickedPoint.y = e.getY();
		});

		if (!this.cameraActive) {

			// start the video capture
			this.capture.open(0);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						// effectively grab and process a single frame
						Mat[] frames = grabFrame();
						updateImageView(originalFrame, Utils.mat2Image(frames[0]));
						updateImageView(processedFrame, Utils.mat2Image(frames[1]));
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");

			// stop the timer
			this.stopAcquisition();
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Mat[] grabFrame() {
		Mat originalFrame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {

			// read the current frame
			this.capture.read(originalFrame);
			
			final List<Point> pointsOfInterest = calcPointsOfInterest(originalFrame);
			
			Scalar defaultWhite = new Scalar(255, 255, 255);
			Mat dest = Mat.zeros(originalFrame.size(), CvType.CV_8UC3);
			
			int sensitivity = 5;

			int[] colors = new int[] 
					{
							5,  // red
							15, // orange 
							35, // yellow
							75, // green
							105 // blue
							};
			Scalar[] cls = new Scalar[] {
					new Scalar(0, 0, 255),
					new Scalar(0, 100, 255),
					new Scalar(0, 255, 255),
					new Scalar(0, 255, 0),
					new Scalar(255, 0, 0),
			};
			
			for (final Point p : pointsOfInterest) {
				boolean colorDetected = false;
				for (int i = 0; i < cls.length; i++) {
					
					// Change color with your actual color
					Scalar lower = new Scalar(colors[i] - sensitivity, 100, 20);
					Scalar upper = new Scalar(colors[i] + sensitivity, 255, 255);

					Mat hsv = originalFrame.clone();
					Imgproc.cvtColor(originalFrame, hsv, Imgproc.COLOR_BGR2HSV);

					Core.inRange(hsv, lower, upper, hsv); // hsv
					colorDetected = findAndDrawContours(hsv, dest, cls[i], p);
					if (colorDetected) {
						break;
					}
				}
				if (!colorDetected) {
					drawRect(dest, defaultWhite, p);
				}
			}
			
			final Mat frameWithDots = createFrameWithDots(originalFrame, pointsOfInterest);
			return new Mat[] {frameWithDots, dest};
		}

		throw new RuntimeException();
	}

	private List<Point> calcPointsOfInterest(Mat originalFrame) {
		final List<Point> pointsOfInterest = new LinkedList<>();
		final Point center = new Point(originalFrame.width() / 2, originalFrame.height() / 2);
		pointsOfInterest.add(center);
		int edgeLength = 140;
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y - edgeLength));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y));
		pointsOfInterest.add(new Point(center.x, center.y));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y + edgeLength));
		
		return pointsOfInterest;
	}

	private Mat createFrameWithDots(Mat originalFrame, List<Point> pointsOfInterest) {
		final Mat clone = originalFrame.clone();
		final Scalar blue = new Scalar(255, 0, 0);
		pointsOfInterest.forEach(p -> {
			Imgproc.circle(clone, p, 5, blue, -1);
		});
		return clone;
	}

	private boolean findAndDrawContours(Mat maskedImage, Mat dest, Scalar color, Point p) {
		// init
		List<MatOfPoint> contours = new ArrayList<>();
		
		// Find contours
		Imgproc.findContours(maskedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (final MatOfPoint contour : contours) {
			if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), p, false) > 0) {
				drawRect(dest, color, p);
				return true;
			}
		}
		
		return false;
	}

	private void drawRect(Mat dest, Scalar color, final Point p) {
		int delta = 60;
		Imgproc.rectangle(dest, new Point(p.x + delta, p.y + delta), new Point(p.x - delta, p.y - delta) , color, -1);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view  the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		this.stopAcquisition();
	}
}
