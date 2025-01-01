package messages;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import messages.cameraframes.FrameMessage;

public class CustomCodec implements MessageCodec<FrameMessage, FrameMessage> {
    @Override
    public FrameMessage transform(FrameMessage obj) {
        return obj; // Directly return the object for in-JVM optimization
    }

    @Override
    public void encodeToWire(Buffer buffer, FrameMessage obj) {
        throw new IllegalStateException();
    }

    @Override
    public FrameMessage decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException();
    }

    @Override
    public String name() {
        return "FrameMessageCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1; // User codec
    }
}
