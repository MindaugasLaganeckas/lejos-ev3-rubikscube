package messages;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Subscriber<T> implements Runnable, Closeable {
	private final BlockingQueue<IMessage<T>> queue = new LinkedBlockingQueue<>();
	private boolean keepProcessing = true;
	
	public BlockingQueue<IMessage<T>> getQueue() {
		return queue;
	}

	@Override
	public void run() {
		try {
			while (keepProcessing) {
				final IMessage<T> message = queue.take();
				process(message);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void close() throws IOException {
		keepProcessing = false;
	}
	
	public abstract void process(final IMessage<T> message);
	
	
}
