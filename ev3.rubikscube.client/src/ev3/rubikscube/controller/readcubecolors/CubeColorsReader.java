package ev3.rubikscube.controller.readcubecolors;

import static ev3.rubikscube.controller.frameprocessor.decorator.ImageProcessingUtils.histogramEqualization;
import static ev3.rubikscube.controller.frameprocessor.decorator.ImageProcessingUtils.illuminationCompensation;
import static ev3.rubikscube.ui.RubiksCubeAppController.RED_COLLOR_UPPER_RANGE2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.ui.IColorReadCompletedObserver;
import ev3.rubikscube.ui.IFrameObserver;
import ev3.rubikscube.ui.RubiksCubeColors;

public class CubeColorsReader implements IFrameObserver {
	
	private static final int EDGE_LENGTH = 130;
	public static final int NUMBER_OF_FACETS = 9;
	private static final int TIMES_TO_READ_BEFORE_NOTIFY = 5; // read every facet x times
	
	private final int[] colorLookup;
	private final Set<IColorReadCompletedObserver> observers = new LinkedHashSet<IColorReadCompletedObserver>();
	
	private int currentRead = 0;
	private boolean colorReadStarted = false;
	private boolean colorReadFinished = false;
	private RubiksCubeColors[] colors = null;
	
	public synchronized void startColorRead() {
		this.currentRead = 0;
		this.colorReadStarted = true;
		this.colorReadFinished = false;
		this.colors = new RubiksCubeColors[NUMBER_OF_FACETS];
	}
	
	public CubeColorsReader(final int[] colorLookup) {
		this.colorLookup = colorLookup;
	}
	
	@Override
	public void update(final Mat input) {
		if (!colorReadStarted || colorReadFinished) return;
		
		if (currentRead < TIMES_TO_READ_BEFORE_NOTIFY) {
			currentRead++;
			
			final Mat illuminationCompensation = illuminationCompensation(input);
			final Mat histogramEqualizationProcessedHsvImage = histogramEqualization(illuminationCompensation);
			final List<Rect> areasForColorTest = calcAreasOfInterest(input.width(), input.height());
			for (int faceIndex = 0; faceIndex < areasForColorTest.size(); faceIndex++) {
				final Rect rect = areasForColorTest.get(faceIndex);
				final Mat croppedImage = new Mat(histogramEqualizationProcessedHsvImage, rect);
		        final List<Mat> channels = new ArrayList<>();
		        Core.split(croppedImage, channels);
		        
		        // Create a mask for pixels with Saturation and Value between 50 and 255
		        final Mat mask = new Mat();
		        Core.inRange(croppedImage, new Scalar(0, 50, 50), new Scalar(RED_COLLOR_UPPER_RANGE2, 255, 255), mask);
		        
		        // Compute histograms for each channel (Hue, Saturation, Value)
		        final MatOfInt histSize = new MatOfInt(256);
		        final MatOfFloat histRange = new MatOfFloat(0f, 256f);
		        final boolean accumulate = false;

		        final Mat histHue = new Mat();
		        
		        Imgproc.calcHist(Arrays.asList(channels.get(0)), new MatOfInt(0), mask, histHue, histSize, histRange, accumulate);
		        
		        final float[] histHueArray = new float[(int) histHue.total()];
		        histHue.get(0, 0, histHueArray);
		        
		        this.colors[faceIndex] = getDominantColor(histHueArray, colorLookup, croppedImage.rows() * croppedImage.cols());
			}
			System.out.println();
		} else {
			for (final IColorReadCompletedObserver observer : observers) {
				observer.colorReadCompleted(this.colors);
			}
			colorReadFinished = true;
		}
	}
	
	private static RubiksCubeColors getDominantColor(final float[] histHueArray, final int[] colorLookup, final int totalPixels) {
		final int[] colorFrequency = new int[RubiksCubeColors.values().length];
		for (int hueValue = 0; hueValue < histHueArray.length; hueValue++) {
        	final int hueValueFrequencey = (int) histHueArray[hueValue];
        	if (hueValueFrequencey > 0) {
        		colorFrequency[colorLookup[hueValue]] += hueValueFrequencey;
        	}
        }
		RubiksCubeColors mostFrequentColor = null;
		int mostFrequentColorCount = 0;
		for (int colorIndex = 0; colorIndex < colorFrequency.length; colorIndex++) {
			if (mostFrequentColorCount < colorFrequency[colorIndex]) {
				mostFrequentColorCount = colorFrequency[colorIndex];
				mostFrequentColor = RubiksCubeColors.values()[colorIndex];
			}
		}
		
		final double mostFrequentColorCountPercentage = mostFrequentColorCount * 1.0 / totalPixels;
		System.out.print(mostFrequentColor + ": " + (mostFrequentColorCountPercentage * 100) + "%   ");
		
		if (Double.compare(mostFrequentColorCountPercentage, 0.3) > 0) {
			return mostFrequentColor;
		}
		return RubiksCubeColors.WHITE;
	}
	
	public static List<Rect> calcAreasOfInterest(final int frameWidth, final int frameHeight) {
		final List<Rect> pointsOfInterest = new LinkedList<>();
		final int x = frameWidth / 2;
		final int y = frameHeight / 2;
		
		pointsOfInterest.add(generatePoints(x - EDGE_LENGTH, y - EDGE_LENGTH));
		pointsOfInterest.add(generatePoints(x, y - EDGE_LENGTH));
		pointsOfInterest.add(generatePoints(x + EDGE_LENGTH, y - EDGE_LENGTH));
		
		pointsOfInterest.add(generatePoints(x - EDGE_LENGTH, y));
		pointsOfInterest.add(generatePoints(x, y));
		pointsOfInterest.add(generatePoints(x + EDGE_LENGTH, y));
		
		pointsOfInterest.add(generatePoints(x - EDGE_LENGTH, y + EDGE_LENGTH));
		pointsOfInterest.add(generatePoints(x, y + EDGE_LENGTH));
		pointsOfInterest.add(generatePoints(x + EDGE_LENGTH, y + EDGE_LENGTH));
		
		return pointsOfInterest;
	}
	
	private static Rect generatePoints(final int x, final int y) {
		final int edgeLength = 50;
		return new Rect(x, y, edgeLength, edgeLength);
	}

	public void subscribe(final IColorReadCompletedObserver observer) {
		this.observers.add(observer);
	}
	
	public void unsubscribe(final IColorReadCompletedObserver observer) {
		this.observers.remove(observer);
	}
}
