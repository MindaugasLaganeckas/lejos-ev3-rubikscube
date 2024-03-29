package ev3.rubikscube.controller.frameprocessor;

import org.opencv.core.Mat;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FrameObserver {
	
	private final ImageView imageView;
	private final FrameDecorator decorator;
	
	public FrameObserver(final ImageView imageView, final FrameDecorator decorator) {
		this.imageView = imageView;
		this.decorator = decorator;
	}
	
	void update(final Mat frame) {
		final Mat decoratedFrame = decorator.decorate(frame);
		updateImageView(Utils.mat2Image(decoratedFrame));	
	}
	
	private void updateImageView(Image image) {
		Utils.onFXThread(imageView.imageProperty(), image);
	}
}
