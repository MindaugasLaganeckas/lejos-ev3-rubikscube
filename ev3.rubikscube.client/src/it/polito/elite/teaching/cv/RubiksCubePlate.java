package it.polito.elite.teaching.cv;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RubiksCubePlate extends StackPane {

	private final Rectangle rectangle;
	
	private final String[] colors = new String[] {
			"white", "orange", "blue", "yellow", "red", "green"
	};
	
	private int currentColor = 0;
	
	public RubiksCubePlate(final int width, final int height, final int positionX, final int positionY, final String label) {
		rectangle = new Rectangle(width, height);
        rectangle.setX(positionX * width);
        rectangle.setY(positionY * width);
        rectangle.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 5;");
        final Text text = new Text(label);
        this.getChildren().addAll(rectangle, text);
        this.setLayoutX(positionX * width);
        this.setLayoutY(positionY * width);
        
        this.setOnMouseClicked(new EventHandler<Event>() {
    		@Override
    		public void handle(Event event) {
    			currentColor = (currentColor + 1) % colors.length;
    			rectangle.setStyle("-fx-fill: " + colors[currentColor] + "; -fx-stroke: black; -fx-stroke-width: 5;");
    		}
        });
	}
}
