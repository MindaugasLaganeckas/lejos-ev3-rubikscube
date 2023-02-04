package it.polito.elite.teaching.cv;
	
import java.io.IOException;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RubiksCubeApp extends Application
{    
	@Override
	public void start(final Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("RubiksCubeApp.fxml"));
			final BorderPane root = (BorderPane) loader.load();
			
			// set a whitesmoke background
			root.setStyle("-fx-background-color: whitesmoke;");
			// create and style a scene
			final Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Robot control center");
			primaryStage.setScene(scene);

			// get the controller
			final RubiksCubeAppController controller = loader.getController();
			controller.setRectangles();
			controller.initRanges();
			
			// show the GUI
			primaryStage.show();
			
			// set the proper behavior on closing the application
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					try {
						controller.setClosed();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
