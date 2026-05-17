package esp32client.enums;

public enum CameraId {
    LEFT,
    MAIN,
    RIGHT;

    public boolean isSideCamera() {
        return this != MAIN;
    }
}
