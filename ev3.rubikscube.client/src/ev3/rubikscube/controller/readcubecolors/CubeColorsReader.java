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
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.ui.IColorReadCompletedObserver;
import ev3.rubikscube.ui.RubiksCubeColors;
import messages.IMessage;
import messages.Subscriber;

public class CubeColorsReader extends Subscriber<Mat> {
	
	public static final int NUMBER_OF_FACETS = 9;
	private static final int TIMES_TO_READ_BEFORE_NOTIFY = 3; // read every facet x times
	
	private final int[] colorLookup;
	private final Set<IColorReadCompletedObserver> observers = new LinkedHashSet<IColorReadCompletedObserver>();
	
	private int currentRead = 0;
	private boolean colorReadStarted = false;
	private boolean colorReadFinished = false;
	private RubiksCubeColors[] colors = null;
	private Double[] percentages = null;
	
	public synchronized void startColorRead() {
		this.currentRead = 0;
		this.colorReadStarted = true;
		this.colorReadFinished = false;
		this.colors = new RubiksCubeColors[NUMBER_OF_FACETS];
		this.percentages = new Double[NUMBER_OF_FACETS];
	}
	
	private final AtomicInteger saturationValue;
	private final AtomicInteger valueValue;
	
	public CubeColorsReader(final int[] colorLookup, final AtomicInteger saturationValue, final AtomicInteger valueValue) {
		this.colorLookup = colorLookup;
		this.valueValue = valueValue;
		this.saturationValue = saturationValue;
	}
	
	@Override
	public void process(final IMessage<Mat> message) {
		if (!colorReadStarted || colorReadFinished) return;
		
		if (currentRead < TIMES_TO_READ_BEFORE_NOTIFY) {
			currentRead++;
			
			final Mat input = message.getContent();
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
		        Core.inRange(croppedImage, new Scalar(0, saturationValue.get(), valueValue.get()), new Scalar(RED_COLLOR_UPPER_RANGE2, 255, 255), mask);
		        
		        // Compute histograms for each channel (Hue, Saturation, Value)
		        final MatOfInt histSize = new MatOfInt(256);
		        final MatOfFloat histRange = new MatOfFloat(0f, 256f);
		        final boolean accumulate = false;

		        final Mat histHue = new Mat();
		        
		        Imgproc.calcHist(Arrays.asList(channels.get(0)), new MatOfInt(0), mask, histHue, histSize, histRange, accumulate);
		        
		        final float[] histHueArray = new float[(int) histHue.total()];
		        histHue.get(0, 0, histHueArray);
		        
		        final Pair pair = getDominantColor(histHueArray, colorLookup, croppedImage.rows() * croppedImage.cols());
		        if (percentages[faceIndex] == null || Double.compare(pair.percentage, percentages[faceIndex]) > 0) {
		        	this.colors[faceIndex] = pair.color;
		        	this.percentages[faceIndex] = pair.percentage;
		        }
			}
		} else {
			for (final IColorReadCompletedObserver observer : observers) {
				observer.colorReadCompleted(this.colors);
			}
			colorReadFinished = true;
		}
	}
	
	private static Pair getDominantColor(final float[] histHueArray, final int[] colorLookup, final int totalPixels) {
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
		if (Double.compare(mostFrequentColorCountPercentage, 0.2) > 0) {
			return new Pair(mostFrequentColor, mostFrequentColorCountPercentage);
		}
		return new Pair(RubiksCubeColors.WHITE, 0d);
	}
	
	public static List<Rect> calcAreasOfInterest(final int frameWidth, final int frameHeight) {
		final List<Rect> pointsOfInterest = new LinkedList<>();
		
		final int edgeLength = (int)(frameHeight * 0.3);
		final int x = frameWidth / 2 - edgeLength / 2;
		final int y = frameHeight / 2 - edgeLength / 2;
		final int actualEdgeLength = (int)(edgeLength * 0.8);
		
		pointsOfInterest.add(generatePoints(x - edgeLength, y - edgeLength, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x, y - edgeLength, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x + edgeLength, y - edgeLength, actualEdgeLength));
		
		pointsOfInterest.add(generatePoints(x - edgeLength, y, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x, y, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x + edgeLength, y, actualEdgeLength));
		
		pointsOfInterest.add(generatePoints(x - edgeLength, y + edgeLength, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x, y + edgeLength, actualEdgeLength));
		pointsOfInterest.add(generatePoints(x + edgeLength, y + edgeLength, actualEdgeLength));
		
		return pointsOfInterest;
	}
	
	private static Rect generatePoints(final int x, final int y, final int edgeLength) {
		return new Rect(x, y, edgeLength, edgeLength);
	}

	public void subscribe(final IColorReadCompletedObserver observer) {
		this.observers.add(observer);
	}
	
	public void unsubscribe(final IColorReadCompletedObserver observer) {
		this.observers.remove(observer);
	}
	
	private static class Pair {
		private final RubiksCubeColors color;
		private final Double percentage;
		
		Pair(final RubiksCubeColors color, final Double percentage) {
			this.color = color;
			this.percentage = percentage;
		}
	}
}
