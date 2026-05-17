package esp32client.events;

import esp32client.enums.CameraId;

public record ColorReadCompleted(CameraId cameraId, esp32client.enums.CubeColor[][] cubeColors) {
}
