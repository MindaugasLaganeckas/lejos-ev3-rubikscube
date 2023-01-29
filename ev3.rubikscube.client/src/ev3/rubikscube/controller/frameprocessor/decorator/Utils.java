package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Point;

public class Utils {
	
	private static final int EDGE_LENGTH = 150;
	
	public static final int NUMBER_OF_POINTS = 9;
	
	public static List<Point> calcPointsOfInterest(final int frameWidth, final int frameHeight) {
		return calcPointsOfInterest(frameWidth, frameHeight, EDGE_LENGTH);
	}
	
	public static List<Point> calcPointsOfInterest(final int frameWidth, final int frameHeight, final int edgeLength) {
		final List<Point> pointsOfInterest = new LinkedList<>();
		final Point center = new Point(frameWidth / 2, frameHeight / 2);
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y - edgeLength));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y));
		pointsOfInterest.add(new Point(center.x, center.y));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y + edgeLength));
		
		if (pointsOfInterest.size() != NUMBER_OF_POINTS) {
			throw new IllegalArgumentException();
		}
		
		return pointsOfInterest;
	}
}
