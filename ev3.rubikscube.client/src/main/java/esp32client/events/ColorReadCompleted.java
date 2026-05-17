package esp32client.events;

import esp32client.enums.CameraId;
import esp32client.enums.CubeColor;

public record ColorReadCompleted(CameraId cameraId, CubeColor[][] cubeColors) {
}
