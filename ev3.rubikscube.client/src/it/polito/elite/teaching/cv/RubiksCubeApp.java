package it.polito.elite.teaching.cv;
	
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.fxml.FXMLLoader;

public class RubiksCubeApp extends Application
{
    private int rows = 9;
    private int columns = 12;
	
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
    
	@Override
	public void start(final Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("RubiksCubeApp.fxml"));
			final BorderPane root = (BorderPane) loader.load();
			
			final Group cubeMapGroup = 
					((Group)((HBox)((VBox)root.getCenter()).getChildren().get(1)).getChildren().get(0));
			final TilePane cubeMap = (TilePane) cubeMapGroup.getChildren().get(0);
			
			// set a whitesmoke background
			root.setStyle("-fx-background-color: whitesmoke;");
			// create and style a scene
			final Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Robot control center");
			primaryStage.setScene(scene);
			
			final List<Rectangle> list = drawCubeMap(cubeMap, cubeMapGroup);

			// show the GUI
			primaryStage.show();
			
			// get the controller
			final RubiksCubeAppController controller = loader.getController();
			controller.setRectangles(list);
			
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

	private List<Rectangle> drawCubeMap(final TilePane cubeMap, final Group cubeMapGroup) {
		final List<Rectangle> list = new LinkedList<>();
		int index = 0;
		for (int row = 0; row < rows; row++) {
		    for (int col = 0; col < columns; col++) {
		    	if (plateMap.containsKey(index)) {
		    		int littleIndex = 1;
		    		for (int i = 0; i < 3; i++) {
		    			for (int j = 0; j < 3; j++) {
		    				final int x = index / columns + i;
		    				final int y = index % columns + j;
		    				cubeMapGroup.getChildren().add(new RubiksCubePlate(
		    		        		cubeMap.tileWidthProperty().intValue(), 
		    		        		cubeMap.tileHeightProperty().intValue(), 
		    		        		y, x, plateMap.get(index) + littleIndex));
		    				littleIndex++;
		    				
		    			}
		    		}
		    	}
		    	index++;
		    }
		}
		return list;
	}

	public static void main(String[] args)
	{
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
