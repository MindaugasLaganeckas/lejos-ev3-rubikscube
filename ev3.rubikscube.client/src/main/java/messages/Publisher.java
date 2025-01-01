package messages;

public abstract class Publisher<T> implements Runnable {
	private final MessageBroker<T> broker;
	public Publisher(final MessageBroker<T> broker) {
		this.broker = broker;
	}

	@Override
	public void run() {
		try {
			final IMessage<T> message = createMessage();
			broker.publish("queue", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract IMessage<T> createMessage();
}
