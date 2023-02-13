package ev3.rubikscube.controller.frameprocessor.decorator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.opencv.core.Point;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import it.polito.elite.teaching.cv.RubiksCubeColors;

public class ColorHitCounter {

	private static final int ERROR_THRESHOLD = 10;
	private static final int EDGE_LENGTH = 150;
	private static final int TIMES_TO_READ_BEFORE_NOTIFY = 100;
	
	public static final int NUMBER_OF_POINTS = 9;
	public static final PropertyChangeEvent COLOR_READ_FINISHED = new PropertyChangeEvent(new Object(), "", new Object(), new Object());
	
	private final AtomicInteger readCounter = new AtomicInteger();
	
	private final PropertyChangeListener colorReadListener;
	
	public ColorHitCounter(final PropertyChangeListener colorReadListener) {
		this.colorReadListener = colorReadListener;
	}
	
	private boolean read = true;
	
	private final AtomicIntegerArray counters = new AtomicIntegerArray(CubeColors.values().length * NUMBER_OF_POINTS);
	
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
	
	public int getReadCount() {
		return readCounter.get();
	}
	
	public void inc(final CubeColors color, final int faceId) {
		if (read) {
			final int reads = readCounter.incrementAndGet();
			counters.getAndIncrement(faceId * CubeColors.values().length + color.ordinal());
			if (reads == TIMES_TO_READ_BEFORE_NOTIFY) {
				colorReadListener.propertyChange(COLOR_READ_FINISHED);
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
