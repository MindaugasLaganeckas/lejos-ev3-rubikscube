package it.polito.elite.teaching.cv;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ev3.rubikscube.controller.Client;
import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameGrabber;
import ev3.rubikscube.controller.frameprocessor.FrameObserver;
import ev3.rubikscube.controller.frameprocessor.decorator.DottedFrameDecorator;
import ev3.rubikscube.controller.frameprocessor.decorator.InterprettedFrameDecorator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class RubiksCubeAppController implements Closeable {

	private static final int VIDEO_DEVICE_INDEX = 0;
	
	private static final int SERVER_PORT = 3333;
	private static final String SERVER_IP = "192.168.1.130";
	
	private List<Rectangle> rectangles;
	
	//private final Client client;
	
	public void setRectangles(List<Rectangle> rectangles) {
		this.rectangles = rectangles;
	}

	public RubiksCubeAppController() throws Exception {
		//client = new Client(SERVER_IP, SERVER_PORT);
	}
	
	@FXML
	private Button connectButton;
	@FXML
	private Button cameraButton;
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView processedFrame;
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	
	/*@FXML
	private Slider redUpper;
	@FXML
	private Slider redLower;*/
	
	private Runnable frameGrabber;

	private InterprettedFrameDecorator decorator = new InterprettedFrameDecorator();
	
	@FXML
	protected void connect() throws IOException {
		System.out.println("Clicked");
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 * @throws IOException 
	 */
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
		
		/*redUpper.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				decorator.setUpper(CubeColors.RED, newValue.intValue());
				
			}
		});
		redLower.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				decorator.setLower(CubeColors.RED, newValue.intValue());
			}
		});*/

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
	}

	@Override
	public void close() throws IOException {
		try {
			setClosed();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//client.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
