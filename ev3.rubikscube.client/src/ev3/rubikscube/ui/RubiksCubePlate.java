package ev3.rubikscube.ui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RubiksCubePlate extends StackPane {

	private final Rectangle rectangle;
	
	private int currentColor = 0;
	private boolean colorIsLocked = false;
	
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
    			if (!colorIsLocked) {
    				setNextColor();
        			updateColor();	
    			}
    		}
        });
	}
	private void updateColor() {
		rectangle.setStyle("-fx-fill: " + getColor() + "; -fx-stroke: black; -fx-stroke-width: 5;");
	}
	private void setNextColor() {
		currentColor = (currentColor + 1) % RubiksCubeColors.values().length;
	}
	public RubiksCubeColors getColor() {
		return RubiksCubeColors.values()[currentColor];
	}
	public void setAndLockColor(final RubiksCubeColors color) {
		setColor(color);
		colorIsLocked = true;
	}
	public void setColor(final RubiksCubeColors color) {
		currentColor = color.ordinal();
		updateColor();
	}
}
