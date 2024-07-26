package messages.cameraframes;

import org.opencv.core.Mat;

import messages.IMessage;

public class FrameMessage implements IMessage<Mat>{

	private final Mat frame;
	private final int videoDeviceIndex;
	
	public FrameMessage(final Mat frame, final int videoDeviceIndex) {
		this.frame = frame;
		this.videoDeviceIndex = videoDeviceIndex;
	}

	@Override
	public Mat getContent() {
		return frame;
	}

	public int getVideoDeviceIndex() {
		return videoDeviceIndex;
	}
}
