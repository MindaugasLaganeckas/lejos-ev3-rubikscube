package ev3.rubikscube.controller.frameprocessor;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Point;

public abstract class AbstractFrameDecorator implements FrameDecorator {

	protected int edgeLength = 140;
	
	protected List<Point> calcPointsOfInterest(final int frameWidth, final int frameHeight) {
		final List<Point> pointsOfInterest = new LinkedList<>();
		final Point center = new Point(frameWidth / 2, frameHeight / 2);
		pointsOfInterest.add(center);
		
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
}
