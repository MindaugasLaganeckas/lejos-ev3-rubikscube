package ev3.rubikscube.asyncmessaging.events;

import ev3.rubikscube.asyncmessaging.FrameBuffer;

public record FrameCapturedEvent(FrameBuffer frame) {
}
