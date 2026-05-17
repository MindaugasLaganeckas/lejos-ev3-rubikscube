package esp32client.events;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

public class EventBusWrapper {
    private final EventBus eventBus;
    private final Set<Object> subscribers = new HashSet<>();

    public EventBusWrapper() {
        this.eventBus = EventBus.getDefault();
    }

    public void register(final Object subscriber) {
        this.subscribers.add(subscriber);
        this.eventBus.register(subscriber);
    }

    public void unsubscribeAll() {
        for (final Object subscriber : this.subscribers) {
            this.eventBus.unregister(subscriber);
        }
    }

    public void post(final Object event) {
        this.eventBus.post(event);
    }

}
