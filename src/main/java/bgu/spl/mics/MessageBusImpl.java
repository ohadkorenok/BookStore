package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.ConcurrentHashMapSemaphore;
import bgu.spl.mics.application.passiveObjects.RoundRobinLinkedListSemaphore;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl messageBus;
    private ConcurrentHashMap<Event, Future> eventToFuture;
    private ConcurrentHashMapSemaphore<Class<? extends Event>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> eventClassToRoundRobinQueues;
    private ConcurrentHashMap<Class<? extends MicroService>, LinkedList<Class<? extends Event>>> serviceClasstoEventClass;
    private ConcurrentHashMapSemaphore <MicroService, SpecificBlockingQueue<Message>> microServiceToQueue;

    public static MessageBusImpl getInstance() {
        if (messageBus == null) {
            messageBus = new MessageBusImpl();
        }
        return messageBus;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendBroadcast(Broadcast b) {
        // TODO Auto-generated method stub

    }

    /**
     * This method creates a new future, stores it as the value of the given event in the eventToFuture concurrent hash
     * map, pushes it to the desired blockingQueue of messages  (using semaphore acquire and release) and returns the
     * future object to the desired micro service.
     */
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Future<T> future = new Future<>();
        eventToFuture.put(e, future);
        RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> roundRobinBlockingQueueOfEvents = eventClassToRoundRobinQueues.getOrDefault(e.getClass(), null);
        if (roundRobinBlockingQueueOfEvents != null) {
            try {
                roundRobinBlockingQueueOfEvents.getSema().acquire(1);
                SpecificBlockingQueue<Message> currentQueue = roundRobinBlockingQueueOfEvents.getNext();
                currentQueue.put(e);
            } catch (InterruptedException ex) {
                System.out.println("SendEvent Was interrupted! ");
            } finally {
                roundRobinBlockingQueueOfEvents.getSema().release();
            }
        }
        return future;
    }

    @Override
    public void register(MicroService m) {
        Iterator it = serviceClasstoEventClass.get(m.getClass()).iterator();
        while (it.hasNext()) {
            try {
                eventClassToRoundRobinQueues.getSema().acquire();
            } catch (InterruptedException e) {
            }
            ;
            if (eventClassToRoundRobinQueues.get())

        }


//        synchronized (msQ) {
//            if (!msQ.containsKey(m.getClass())) {
//                LinkedList<SpecificBlockingQueue<Message>> listToPush = new LinkedList<>();
//                listToPush.add(new SpecificBlockingQueue<>()); // Extend to work with name
//                q = new SpecificBlockingQueue<>(); // Extend to work with name
//                q.setMicroService(m.getClass());
//                msQ.put(m.getClass(), listToPush);
//            } else {
//                LinkedList<SpecificBlockingQueue<Message>> listToUpdate = msQ.get(m.getClass());
//                listToUpdate.add(new SpecificBlockingQueue<>()); // Extend to work with name
//                msQ.replace(m.getClass(), listToUpdate);
//            }
//        }
    }

    @Override
    public void unregister(MicroService m) {


    }


    private LinkedBlockingQueue getQueueByEvent(Event e) {
        LinkedList<LinkedBlockingQueue<Message>> blockingQueueLinkedList = microServiceToLinkedList.getOrDefault(eventToMs.getOrDefault(e, null), null); //TODO :: finish after it.

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        SpecificBlockingQueue <Message> queue = microServiceToQueue.get(m);
        return queue.take();
    }

    private LinkedBlockingQueue pullCurrentQueue(LinkedList<LinkedBlockingQueue<Message>>) {

    }


}
