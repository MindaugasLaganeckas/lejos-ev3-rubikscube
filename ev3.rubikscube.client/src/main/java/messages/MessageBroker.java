package messages;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.util.function.Consumer;

public class MessageBroker<T> {

	private final EventBus eventBus;
	public MessageBroker() {
		final Vertx vertx = Vertx.vertx();
		this.eventBus = vertx.eventBus();
		this.eventBus.registerCodec(new CustomCodec());
	}

	public void subscribe(final String address, final Consumer<IMessage<T>> consumer) {
		final Handler<Message<IMessage<T>>> handler = event -> consumer.accept(event.body());
		eventBus.localConsumer(address, handler);
	}

	public void publish(final String address, final IMessage<T> message) {
		final DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.setCodecName("FrameMessageCodec");
		eventBus.publish(address, message, deliveryOptions);
	}
}
