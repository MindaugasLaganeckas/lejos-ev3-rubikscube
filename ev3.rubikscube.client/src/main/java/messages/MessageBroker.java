package messages;

import java.util.concurrent.CopyOnWriteArrayList;

public class MessageBroker<T> {
	private final CopyOnWriteArrayList<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();

	public void subscribe(Subscriber<T> subscriber) {
		subscribers.add(subscriber);
	}

	public void unsubscribe(Subscriber<T> subscriber) {
		subscribers.remove(subscriber);
	}

	public void publish(final IMessage<T> message) {
		for (final Subscriber<T> subscriber : subscribers) {
			try {
				subscriber.getQueue().put(message);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
