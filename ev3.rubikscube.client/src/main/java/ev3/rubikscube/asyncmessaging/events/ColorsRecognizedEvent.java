package ev3.rubikscube.asyncmessaging.events;

import java.util.Map;

public record ColorsRecognizedEvent(Map<String, String> colors) {}
