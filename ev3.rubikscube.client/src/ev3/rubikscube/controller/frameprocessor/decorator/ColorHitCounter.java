package ev3.rubikscube.controller.frameprocessor.decorator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;

import org.opencv.core.Point;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.ui.RubiksCubeColors;

public class ColorHitCounter {

	private static final int EDGE_LENGTH = 150;
	private static final int NUMBER_OF_POINTS_IN_FACET = 9;
	public static final int NUMBER_OF_FACETS = 9;
	private static final int NUMBER_OF_POINTS = NUMBER_OF_FACETS * NUMBER_OF_POINTS_IN_FACET;
	private static final int TIMES_TO_READ_BEFORE_NOTIFY = NUMBER_OF_POINTS * 30; // read every facet x times
	private static final int ERROR_THRESHOLD = TIMES_TO_READ_BEFORE_NOTIFY / 10;
	private final ColorHitCounter me = this;
	
	private final AtomicInteger readCounter = new AtomicInteger();
	private final AtomicIntegerArray counters = new AtomicIntegerArray(CubeColors.values().length * NUMBER_OF_FACETS);
	private boolean read = true;
	
	private final PropertyChangeListener colorReadListener;
	
	public ColorHitCounter(final PropertyChangeListener colorReadListener) {
		this.colorReadListener = colorReadListener;
	}

	public static List<List<Point>> calcPointsOfInterest(final int frameWidth, final int frameHeight) {
		return calcPointsOfInterest(frameWidth, frameHeight, EDGE_LENGTH);
	}
	
	public static List<Point> calcPointsOfInterestFlat(final int frameWidth, final int frameHeight) {
		return calcPointsOfInterest(frameWidth, frameHeight, EDGE_LENGTH).stream()
		        .flatMap(List::stream)
		        .collect(Collectors.toList());
	}
	
	private static List<List<Point>> calcPointsOfInterest(final int frameWidth, final int frameHeight, final int edgeLength) {
		final List<List<Point>> pointsOfInterest = new LinkedList<>();
		final Point center = new Point(frameWidth / 2, frameHeight / 2);
		
		pointsOfInterest.add(generatePoints(center.x - edgeLength, center.y - edgeLength));
		pointsOfInterest.add(generatePoints(center.x, center.y - edgeLength));
		pointsOfInterest.add(generatePoints(center.x + edgeLength, center.y - edgeLength));
		
		pointsOfInterest.add(generatePoints(center.x - edgeLength, center.y));
		pointsOfInterest.add(generatePoints(center.x, center.y));
		pointsOfInterest.add(generatePoints(center.x + edgeLength, center.y));
		
		pointsOfInterest.add(generatePoints(center.x - edgeLength, center.y + edgeLength));
		pointsOfInterest.add(generatePoints(center.x, center.y + edgeLength));
		pointsOfInterest.add(generatePoints(center.x + edgeLength, center.y + edgeLength));
		
		return pointsOfInterest;
	}
	
	private static List<Point> generatePoints(final double x, final double y) {
		final List<Point> pointsOfInterest = new LinkedList<>();
		final double edgeLength = 20;
		
		final Point center = new Point(x, y);
		
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
	
	public int getReadCount() {
		return readCounter.get();
	}
	
	public void inc(final CubeColors color, final int faceId) {
		if (read) {
			final int reads = readCounter.incrementAndGet();
			counters.getAndIncrement(faceId * CubeColors.values().length + color.ordinal());
			if (reads == TIMES_TO_READ_BEFORE_NOTIFY) {
				colorReadListener.propertyChange(new PropertyChangeEvent(me, "", new Object(), new Object()));
				read = false;
			}
		}
	}
	
	public RubiksCubeColors get(final int faceId) {
		int max = 0;
		int colorIndex = 0;
		final int length = CubeColors.values().length;
		for (int i = 0; i < length; i++) {
			final int value = counters.get(faceId * length + i);
			if (max < value) {
				max = value;
				colorIndex = i;
			}
		}
		if (max < ERROR_THRESHOLD) {
			return RubiksCubeColors.WHITE;
		}
		return RubiksCubeColors.values()[colorIndex];
	}
}
