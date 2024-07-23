package ev3.rubikscube.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.RubiksCuberSolverClient;
import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameGrabber;
import ev3.rubikscube.controller.frameprocessor.FrameObserver;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorFrameDecorator;
import ev3.rubikscube.controller.frameprocessor.decorator.ProcessedFrameDecorator;
import ev3.rubikscube.controller.frameprocessor.decorator.SquareFrameDecorator;
import ev3.rubikscube.controller.readcubecolors.ColorReadControllerForAllSides;
import ev3.rubikscube.controller.readcubecolors.CubeColorsReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

public class RubiksCubeAppController implements Closeable, PropertyChangeListener {

	private static final int VIDEO_DEVICE_INDEX = 0;
	public static final int RED_COLLOR_LOWER_RANGE2 = 120;
	public static final int RED_COLLOR_UPPER_RANGE2 = 180;
	
    private int rows = 9;
    private int columns = 12;
    
    private final RubiksCubeAppController me = this;
    private ColorReadControllerForAllSides colorReadController = null;
    
    private final Map<Integer, String> plateMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
	{
        put( 3, "U");
        put(36, "L");
        
        put(39, "F");
        put(42, "R");
        
        put(45, "B");
        put(75, "D");
    }};
	
	private static final int SERVER_PORT = 3333;
	
	private Map<String, RubiksCubePlate> kubeColors;
	
    private final RubiksCuberSolverClient solverClient = new RubiksCuberSolverClient();
	private MindstormRubiksCubeClient client;
	
	public void setRectangles() {
		this.kubeColors = drawCubeMap();
	}
	
	private String solutionStr;
	
	private final ColorFrameDecorator colorFrameDecorator = new ColorFrameDecorator();
	
	@FXML
	private TilePane cubeMap;
	@FXML
	private Group cubeMapGroup;
	
	@FXML
	private Slider redHigh;
	@FXML
	private Slider orangeHigh;
	@FXML
	private Slider yellowHigh;
	@FXML
	private Slider greenHigh;
	@FXML
	private Slider blueHigh;
	
	@FXML
	private Slider redLow;
	@FXML
	private Slider orangeLow;
	@FXML
	private Slider yellowLow;
	@FXML
	private Slider greenLow;
	@FXML
	private Slider blueLow;
	
	@FXML
	private Slider saturation;
	@FXML
	private Slider value;
	
	@FXML
	private Button connectButton;
	@FXML
	private Button cameraButton;
	@FXML
	private Button readColorsButton;
	@FXML
	private Button solveItButton;
	@FXML
	private Button turnRubiksCubeButton;
	@FXML
	private Button sendSolutionToRobot;
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView processedFrame;
	@FXML
	private ImageView colorFrame;
	@FXML
	private Label solution;
	@FXML
	private Label connectionStatus;
	@FXML
	private TextField robotIp;
	
	@FXML TextField turnToMake;
	@FXML Button turnToMakeButton;
	@FXML CheckBox debugMode;
	@FXML CheckBox showRedFilter;
	@FXML CheckBox showOrangeFilter;
	@FXML CheckBox showYellowFilter;
	@FXML CheckBox showGreenFilter;
	@FXML CheckBox showBlueFilter;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	private Runnable frameGrabber;
	
	private CubeColorsReader colorsReader = null;
	
	private final int[] colorLookup = new int[255];
	private final AtomicIntegerArray lowerRanges = new AtomicIntegerArray(5);
	private final AtomicIntegerArray upperRanges = new AtomicIntegerArray(5);
	private final AtomicBoolean[] showFilters = new AtomicBoolean[5];
	
	private final AtomicInteger saturationValue = new AtomicInteger();
	private final AtomicInteger valueValue = new AtomicInteger();
	
	private boolean solveItModeEnabled = false;
	
	public void initRanges() {
		lowerRanges.set(CubeColors.RED.ordinal(), (int)redLow.getValue());
		lowerRanges.set(CubeColors.ORANGE.ordinal(), (int)orangeLow.getValue());
		lowerRanges.set(CubeColors.YELLOW.ordinal(), (int)yellowLow.getValue());
		lowerRanges.set(CubeColors.GREEN.ordinal(), (int)greenLow.getValue());
		lowerRanges.set(CubeColors.BLUE.ordinal(), (int)blueLow.getValue());
		
		upperRanges.set(CubeColors.RED.ordinal(), (int)redHigh.getValue());
		upperRanges.set(CubeColors.ORANGE.ordinal(), (int)orangeHigh.getValue());
		upperRanges.set(CubeColors.YELLOW.ordinal(), (int)yellowHigh.getValue());
		upperRanges.set(CubeColors.GREEN.ordinal(), (int)greenHigh.getValue());
		upperRanges.set(CubeColors.BLUE.ordinal(), (int)blueHigh.getValue());
		
		saturationValue.set((int) saturation.getValue());
		valueValue.set((int) value.getValue());
		
		// white by default
		Arrays.fill(colorLookup, RubiksCubeColors.WHITE.ordinal());
		for (int i = 0; i < lowerRanges.length(); i++) {
			for (int j = lowerRanges.get(i); j <= upperRanges.get(i); j++) {
				colorLookup[j] = CubeColors.values()[i].ordinal();
			}
		}
		// extra for the second red color range
		for (int j = RED_COLLOR_LOWER_RANGE2; j <= RED_COLLOR_UPPER_RANGE2; j++) {
			colorLookup[j] = CubeColors.RED.ordinal();
		}
	}
	private final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
	@FXML public void turnToMakeButton() throws IOException {
		if (client != null) {
			newSingleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						for (final String command : turnToMake.getText().split("\\s+")) {
							client.sendCommand(command);	
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							client.sendCommand("COMPLETED");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	@FXML protected void debugMode() {
		if (client != null) {
			client.enableDebugMode(debugMode.isSelected());
		}
	}
	
	@FXML public void updateFilters() {
		showFilters[CubeColors.RED.ordinal()] = new AtomicBoolean(showRedFilter.isSelected());
		showFilters[CubeColors.ORANGE.ordinal()] = new AtomicBoolean(showOrangeFilter.isSelected());
		showFilters[CubeColors.YELLOW.ordinal()] = new AtomicBoolean(showYellowFilter.isSelected());
		showFilters[CubeColors.GREEN.ordinal()] = new AtomicBoolean(showGreenFilter.isSelected());
		showFilters[CubeColors.BLUE.ordinal()] = new AtomicBoolean(showBlueFilter.isSelected());
	}
	
	@FXML
	protected void turnRubiksCube() throws IOException {
		client.sendCommand("UP");
	}
	
	@FXML
	protected void connect() {
		try {
			this.client = new MindstormRubiksCubeClient(robotIp.getText(), SERVER_PORT);
			connectionStatus.setText("Connection status: connected.");
			readColorsButton.setDisable(false);
			turnRubiksCubeButton.setDisable(false);
			sendSolutionToRobot.setDisable(false);
			turnToMakeButton.setDisable(false);
		} catch (Exception e) {
			connectionStatus.setText("Connection status: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@FXML
	protected void calculateSolution() throws IOException {
		// order is defined here ev3.rubikscube.controller.RubiksCuberSolverClient.solve(String)
		final String[] facesInOrder = new String[] {"U", "R", "F", "D", "L", "B"};
		final StringBuilder scrambledCube = new StringBuilder();
		final int[] faceletCheck = new int[RubiksCubeColors.values().length];
		for (final String face : facesInOrder) {
			for (int j = 1; j <= 9; j++) {
				final RubiksCubeColors color = kubeColors.get(face + j).getColor();
				scrambledCube.append(color.toString().charAt(0));
				faceletCheck[color.ordinal()]++;
			}
		}
		System.out.println(scrambledCube);
		System.out.println();
		for (final RubiksCubeColors color : RubiksCubeColors.values()) {
			System.out.println(color + " " + faceletCheck[color.ordinal()]);
		}
		
		final long start = System.currentTimeMillis();
		solutionStr = solverClient.solve(scrambledCube.toString());
		System.out.println("Time taken to find a solution: " + formatDuration(System.currentTimeMillis() - start));
		
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	solution.setText("Solution: " + solutionStr);
		    }
		});
	}
	
	@FXML
	protected void sendSolutionToRobot() throws IOException {
		final String[] commands = solutionStr.split("\\s+");
		int currentTurn = 0;
		for (final String turn : commands) {
			if (!"".equals(turn)) {
				client.sendCommand(turn);
				currentTurn++;
				System.out.println(String.format("Completed %.2f", currentTurn * 100/commands.length * 1.0));
			}
		}
	}
	
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
		
		// set a fixed width for the frame
		colorFrame.setFitWidth(380);
		// preserve image ratio
		colorFrame.setPreserveRatio(true);
				
		this.colorsReader = new CubeColorsReader(colorLookup, saturationValue, valueValue);
		this.colorsReader.subscribe(colorFrameDecorator);
		
		// grab a frame every 33 ms (30 frames/sec)
		this.frameGrabber = new FrameGrabber( 
				new IFrameObserver[] {
						new FrameObserver(originalFrame, new SquareFrameDecorator()),
						new FrameObserver(processedFrame, new ProcessedFrameDecorator(lowerRanges, upperRanges, showFilters, saturationValue, valueValue)),
						new FrameObserver(colorFrame, colorFrameDecorator),
						this.colorsReader,
			}, VIDEO_DEVICE_INDEX
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
				e.printStackTrace();
			}
		}
	}

	/**
	 * On application close, stop the acquisition from the camera
	 * @throws IOException 
	 */
	protected void setClosed() throws IOException {
		this.stopAcquisition();
		try {
			if (client != null) {
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			setClosed();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	protected void readCurrentSide() {
		this.colorsReader.startColorRead();
	}
	
	@FXML
	protected void readColors() {
		if (this.client != null) {
			this.startTime = System.currentTimeMillis();
			this.colorReadController = new ColorReadControllerForAllSides(this.client, me, this.colorsReader, kubeColors, 
					new IColorReadStartedObserver[] {colorFrameDecorator});
			this.colorsReader.subscribe(colorReadController);
			this.readColorsButton.setDisable(true);
			this.solveItButton.setDisable(true);
			this.colorReadController.startRead();
			
			try {
				client.sendCommand("COMPLETED");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getSource() == me) {
			System.out.println("Time taken to read cube colors: " + formatDuration(System.currentTimeMillis() - startTime));
			if (solveItModeEnabled) {
				try {
					calculateSolution();
					sendSolutionToRobot();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.readColorsButton.setDisable(false);
			this.colorsReader.unsubscribe(colorReadController);
			this.solveItButton.setDisable(false);
			
			System.out.println("Time taken to solve the cube: " + formatDuration(System.currentTimeMillis() - startTime));
			
			try {
				client.sendCommand("COMPLETED");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String formatDuration(long durationMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
                       TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%d minutes, %d seconds", minutes, seconds);
    }
	
	private Map<String, RubiksCubePlate> drawCubeMap() {
		final Map<String, RubiksCubePlate> kubeColors = new HashMap<>();
		int index = 0;
		for (int row = 0; row < rows; row++) {
		    for (int col = 0; col < columns; col++) {
		    	if (plateMap.containsKey(index)) {
		    		int littleIndex = 1;
		    		for (int i = 0; i < 3; i++) {
		    			for (int j = 0; j < 3; j++) {
		    				final int x = index / columns + i;
		    				final int y = index % columns + j;
		    				final RubiksCubePlate plate = new RubiksCubePlate(
		    		        		cubeMap.tileWidthProperty().intValue(), 
		    		        		cubeMap.tileHeightProperty().intValue(), 
		    		        		y, x, plateMap.get(index) + littleIndex);
		    				kubeColors.put(plateMap.get(index) + littleIndex, plate);
		    				cubeMapGroup.getChildren().add(plate);
		    				littleIndex++;
		    			}
		    		}
		    	}
		    	index++;
		    }
		}
		
		kubeColors.get("R5").setColor(RubiksCubeColors.YELLOW);
		kubeColors.get("L5").setColor(RubiksCubeColors.WHITE);
		return kubeColors;
	}

	private long startTime = 0;
	
	@FXML public void solveIt() {
		solveItModeEnabled = true;
		this.startTime = System.currentTimeMillis();
		readColors();
	}

	@FXML public void rangesChangedRed() {
		orangeLow.setValue(redHigh.getValue());
		initRanges();
	}

	@FXML public void rangesChangedOrange() {
		yellowLow.setValue(orangeHigh.getValue());
		initRanges();
	}

	@FXML public void rangesChangedYellow() {
		greenLow.setValue(yellowHigh.getValue());
		initRanges();
	}

	@FXML public void rangesChangedGreen() {
		blueLow.setValue(greenHigh.getValue());
		initRanges();
	}

	@FXML public void rangesChangedBlue() {
		initRanges();
	}

	@FXML public void saturationChanged() {
		initRanges();
	}

	@FXML public void valueChanged() {
		initRanges();
	}
}
