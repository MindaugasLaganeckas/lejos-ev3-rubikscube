package ev3.rubikscube.controller;

import java.util.LinkedList;
import java.util.List;

public class ProducerConsumer<T> {
	// Create a list shared by producer and consumer
    final List<T> list = new LinkedList<>();
    final int maxCapacity = 5;
    final ValueProducer<T> valueProducer;
    final ValueConsumer<T> valueConsumer;
    
    public ProducerConsumer(final ValueProducer<T> valueProducer, final ValueConsumer<T> valueConsumer) {
    	this.valueProducer = valueProducer;
    	this.valueConsumer = valueConsumer;
    }
    
    // Function called by producer thread
    public void produce() throws InterruptedException
    {
        while (true) {
            synchronized (this)
            {
                // producer thread waits while list is full
                while (list.size() == maxCapacity)
                    wait();

                // to insert the jobs in the list
                list.add(valueProducer.produce());

                // notifies the consumer thread that
                // now it can start consuming
                notify();
            }
        }
    }

    // Function called by consumer thread
    public void consume() throws InterruptedException
    {
        while (true) {
            synchronized (this)
            {
                // consumer thread waits while list
                // is empty
                while (list.size() == 0)
                    wait();

                final T val = list.remove(0);
                valueConsumer.consume(val);
                // Wake up producer thread
                notify();
            }
        }
    }
}
