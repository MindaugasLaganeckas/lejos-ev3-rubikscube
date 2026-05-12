package esp32client;

import java.awt.image.BufferedImage;

public record Frame(int cameraIndex, BufferedImage bufferedImage) {
}
