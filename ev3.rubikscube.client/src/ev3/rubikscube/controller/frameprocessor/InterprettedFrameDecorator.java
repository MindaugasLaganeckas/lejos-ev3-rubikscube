package ev3.rubikscube.controller.frameprocessor;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class InterprettedFrameDecorator extends AbstractFrameDecorator {

	@Override
	public Mat decorate(Mat input) {
		final List<Point> pointsOfInterest = calcPointsOfInterest(input.width(), input.height());
		
		Scalar defaultWhite = new Scalar(255, 255, 255);
		Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);
		
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

				Mat hsv = input.clone();
				Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);

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
		return dest;
	}
	
	private void drawRect(Mat dest, Scalar color, final Point p) {
		int delta = 60;
		Imgproc.rectangle(dest, new Point(p.x + delta, p.y + delta), new Point(p.x - delta, p.y - delta) , color, -1);
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
}
