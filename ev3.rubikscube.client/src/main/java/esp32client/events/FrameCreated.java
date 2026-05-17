package esp32client.events;

import esp32client.enums.CameraId;

import java.awt.image.BufferedImage;

public record FrameCreated(CameraId cameraId, BufferedImage bufferedImage) {
}
