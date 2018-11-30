package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.RoundRobinLinkedListSemaphore;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl messageBus;
    private ConcurrentHashMap<Event, Future> eventToFuture = new ConcurrentHashMap<>(); //send
    private ConcurrentHashMap<Class<? extends Event>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> eventClassToRoundRobinQueues = new ConcurrentHashMap<>(); //subscribe
    private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Event>>> microServiceInstancetoEventClass = new ConcurrentHashMap<>(); //subscribe event
    private ConcurrentHashMap<MicroService, SpecificBlockingQueue<Message>> microServiceToQueue = new ConcurrentHashMap<>(); // register

    public static MessageBusImpl getInstance() {
        if (messageBus == null) {
            messageBus = new MessageBusImpl();
        }
        return messageBus;
    }

    /**
     * Subscribes the given event class to the microservice instance.
     *
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @param <T>
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (!microServiceInstancetoEventClass.containsKey(m)) {
            LinkedList<Class<? extends Event>> eventList = new LinkedList<>();
            eventList.add(type);
            microServiceInstancetoEventClass.put(m, eventList);
        } else {
            LinkedList<Class<? extends Event>> eventList = microServiceInstancetoEventClass.get(m);
            synchronized (eventList) {
                eventList.add(type);
            }
            microServiceInstancetoEventClass.put(m, eventList);
        }
        if(!eventClassToRoundRobinQueues.containsKey(type)){
            eventClassToRoundRobinQueues.put(type, new RoundRobinLinkedListSemaphore<>());
            fetchNPushQueue(m , type);
        }
        else{
            fetchNPushQueue(m, type);
        }

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

    }

    /**
     * Calls resolve to the specific future.
     *
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @param <T>
     */
    @Override
    public <T> void complete(Event<T> e, T result) {
        Future futoro = eventToFuture.get(e);
        futoro.resolve(result);
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
        SpecificBlockingQueue <Message> queueToPush = new SpecificBlockingQueue<>();
        queueToPush.setNameAndClassOfQueue(m.getName(), m.getClass());
        microServiceToQueue.putIfAbsent(m, queueToPush);
    }

    /**
     * pushing new queues to the Linked-List, acquiring lock on Data-Structure inside value of HashMap.
     *
     * @param m          instance of Micro-Service
     * @param eventClass type of Event
     */
    private void fetchNPushQueue(MicroService m, Class<? extends Event> eventClass) {
        try {
            eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
            SpecificBlockingQueue<Message> queuetoPush = microServiceToQueue.get(m);
            eventClassToRoundRobinQueues.get(eventClass).add(queuetoPush);
        } catch (InterruptedException ex) {
        } finally {
            eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);
        }
    }

    @Override
    public void unregister(MicroService m) {
        SpecificBlockingQueue<Message> qtoRemove = microServiceToQueue.get(m);
            //*******************//
            //clear data in queue.

        synchronized (qtoRemove) {
            for (Message i : qtoRemove) {
                if (i instanceof Event)
                    complete((Event) i, null);
            }
            qtoRemove.clear();
        }
        microServiceToQueue.remove(m);
            //*******************//

        for (Class<? extends Event> eventClass : microServiceInstancetoEventClass.get(m)) {
            qtoRemove = searchnGet(m, eventClass);
            try {
                eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
                eventClassToRoundRobinQueues.remove(qtoRemove);
            } catch (InterruptedException e) {
            } finally {
                eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);
                microServiceInstancetoEventClass.remove(m);

            }
        }
    }

    /**
     * Searching the specificblockingqueue assigned to the micro-service in order to down-grade the lock inside
     * unregister.
     *
     * @param m          MicroService
     * @param eventClass Event
     * @return
     */
    private SpecificBlockingQueue<Message> searchnGet(MicroService m, Class<? extends Event> eventClass) {
        SpecificBlockingQueue<Message> toRet = null;
        try {
            eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
            for (SpecificBlockingQueue<Message> i : eventClassToRoundRobinQueues.get(eventClass)) {
                if (i.getName().equals(m.getName())) {
                    toRet = i;
                    break;
                }
            }
        } catch (InterruptedException ex) {
        } finally {
            eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);
        }
        return toRet;
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        SpecificBlockingQueue<Message> queue = microServiceToQueue.get(m);
        return queue.take();
    }


}
