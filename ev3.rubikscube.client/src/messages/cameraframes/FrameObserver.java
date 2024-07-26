package messages.cameraframes;

import org.opencv.core.Mat;

import ev3.rubikscube.controller.frameprocessor.FrameDecorator;
import ev3.rubikscube.controller.frameprocessor.Utils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import messages.IMessage;
import messages.Subscriber;

public class FrameObserver extends Subscriber<Mat> {
	
	private final ImageView imageView;
	private final FrameDecorator decorator;
	
	public FrameObserver(final ImageView imageView, final FrameDecorator decorator) {
		this.imageView = imageView;
		this.decorator = decorator;
	}

	private void updateImageView(final Image image) {
		Utils.onFXThread(imageView.imageProperty(), image);
	}

	@Override
	public void process(final IMessage<Mat> message) {
		final Mat decoratedFrame = decorator.decorate(message.getContent());
		updateImageView(Utils.mat2Image(decoratedFrame));
	}
}
