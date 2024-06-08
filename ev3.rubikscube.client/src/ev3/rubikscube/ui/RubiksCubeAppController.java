package ev3.rubikscube.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.RubiksCuberSolverClient;
import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameGrabber;
import ev3.rubikscube.controller.frameprocessor.FrameObserver;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import ev3.rubikscube.controller.frameprocessor.decorator.DottedFrameDecorator;
import ev3.rubikscube.controller.frameprocessor.decorator.InterprettedFrameDecorator;
import ev3.rubikscube.controller.readcubecolors.ColorReadControllerForAllSides;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

public class RubiksCubeAppController implements Closeable, PropertyChangeListener {

	private static final int VIDEO_DEVICE_INDEX = 0;
	
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
	private Slider colorDepthLow;
	@FXML
	private Slider colorDepthHigh;
	
	@FXML
	private Button connectButton;
	@FXML
	private Button cameraButton;
	@FXML
	private Button readColorsButton;
	@FXML
	private Button turnRubiksCubeButton;
	@FXML
	private Button sendSolutionToRobot;
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView processedFrame;
	@FXML
	private Label solution;
	@FXML
	private Label connectionStatus;
	@FXML
	private TextField robotIp;
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	private Runnable frameGrabber;
	
	private final AtomicIntegerArray lowerRanges = new AtomicIntegerArray(5);
	private final AtomicIntegerArray upperRanges = new AtomicIntegerArray(5);
	
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
	}
	
	@FXML
	protected void rangesChanged() {
		initRanges();
	}
	
	@FXML
	protected void turnRubiksCube() throws IOException {
		client.sendCommand("UP");
	}
	
	@FXML
	protected void connect() {
		try {
			this.client = new MindstormRubiksCubeClient(robotIp.getText(), SERVER_PORT);
			this.colorReadController = new ColorReadControllerForAllSides(this.client);
			connectionStatus.setText("Connection status: connected.");
			readColorsButton.setDisable(false);
			turnRubiksCubeButton.setDisable(false);
			sendSolutionToRobot.setDisable(false);
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
		solutionStr = solverClient.solve(scrambledCube.toString());
		this.solution.setText("Solution: " + solutionStr);
	}
	
	@FXML
	protected void sendSolutionToRobot() throws IOException {
		final String[] commands = solutionStr.split("\\s+");
		int currentTurn = 0;
		for (final String turn : commands) {
			client.sendCommand(turn);
			currentTurn++;
			System.out.println(String.format("0.2f", currentTurn * 1.0/commands.length));
		}
	}
	
	/**
	 * The action triggered by pushing the button on the GUI
	 * @throws IOException 
	 */
	private InterprettedFrameDecorator decorator;
	
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
		
		decorator = new InterprettedFrameDecorator(lowerRanges, upperRanges);
		
		// grab a frame every 33 ms (30 frames/sec)
		this.frameGrabber = new FrameGrabber( 
				new FrameObserver[] {
						new FrameObserver(originalFrame, new DottedFrameDecorator()),
						new FrameObserver(processedFrame, decorator),
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
		System.out.println("Read current side");
	}
	
	/**
	 * When the read is done, {@link #propertyChange(PropertyChangeEvent)} will be called by {@link #colorHitCounter}
	 */
	@FXML
	protected void readColors() {
		final ColorHitCounter colorHitCounter = new ColorHitCounter(me);
		decorator.resetColorRead(colorHitCounter);
		this.readColorsButton.setDisable(true);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		colorReadController.colorReadCompleted(kubeColors, (ColorHitCounter) event.getSource());
		if (colorReadController.isReadSequenceCompleted()) {
			colorReadController.startReadSequence();
			this.readColorsButton.setDisable(false);
		} else {
			colorReadController.setNextFaceToRead();
			readColors();
		}
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
		return kubeColors;
	}
}
