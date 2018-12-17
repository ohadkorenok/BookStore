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
    private static class SingleMessageBus {
        private static MessageBusImpl messageBus = new MessageBusImpl();
    }

    private ConcurrentHashMap<Event, Future> eventToFuture = new ConcurrentHashMap<>(); //send
    private ConcurrentHashMap<Class<? extends Event>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> eventClassToRoundRobinQueues = new ConcurrentHashMap<>(); //subscribe
    private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Message>>> microServiceInstancetoMessageClass = new ConcurrentHashMap<>(); //subscribe event
    private ConcurrentHashMap<MicroService, SpecificBlockingQueue<Message>> microServiceToQueue = new ConcurrentHashMap<>(); // register , unregister.
    private ConcurrentHashMap<Class<? extends Broadcast>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> broadcastToRoundRobinQueues = new ConcurrentHashMap<>();

    public static MessageBusImpl getInstance() {
        return SingleMessageBus.messageBus;
    }

    /**
     * This method puts into the microServiceInstance the desired messageClass that it is subscribed to .
     *
     * @param type Class of message
     * @param m    MicroService
     */
    private void putIntoMsInstanceToMessageClassHashMap(Class<? extends Message> type, MicroService m) {
        if (!microServiceInstancetoMessageClass.containsKey(m)) {
            LinkedList<Class<? extends Message>> messageList = new LinkedList<>();
            messageList.add(type);
            microServiceInstancetoMessageClass.put(m, messageList);
        } else {
            synchronized (microServiceInstancetoMessageClass.get(m)) {
                LinkedList<Class<? extends Message>> messageList = microServiceInstancetoMessageClass.get(m);
                messageList.add(type);
                microServiceInstancetoMessageClass.put(m, messageList);
            }
        }
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
        putIntoMsInstanceToMessageClassHashMap(type, m);
        eventClassToRoundRobinQueues.putIfAbsent(type, new RoundRobinLinkedListSemaphore<>());
        fetchNPushQueue(m, type);

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        putIntoMsInstanceToMessageClassHashMap(type, m);
        broadcastToRoundRobinQueues.putIfAbsent(type, new RoundRobinLinkedListSemaphore<>());
        fetchNPushQueue(m, type);
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
        eventToFuture.remove(e);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (broadcastToRoundRobinQueues.get(b.getClass()) != null) {
            synchronized (broadcastToRoundRobinQueues) {
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> roundRobinBlockingQueueOfBroadcasts = broadcastToRoundRobinQueues.get(b.getClass());
                if (roundRobinBlockingQueueOfBroadcasts != null) {
                    for (SpecificBlockingQueue<Message> it : roundRobinBlockingQueueOfBroadcasts) {
                        try {
                            it.put(b);
                        } catch (InterruptedException e) {
                            System.out.println("The Thread was interrupted");
                        }
                    }
                }
            }
        }
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
        synchronized (eventClassToRoundRobinQueues) {
            RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> roundRobinBlockingQueueOfEvents = eventClassToRoundRobinQueues.getOrDefault(e.getClass(), null);
            if (roundRobinBlockingQueueOfEvents != null) {
                try {
                    if (roundRobinBlockingQueueOfEvents.size() > 0) {
                        SpecificBlockingQueue<Message> currentQueue = roundRobinBlockingQueueOfEvents.getNext();
                        currentQueue.put(e);
                    } else {
                        future.resolve(null);
                    }

                } catch (InterruptedException ex) {
                    System.out.println("Thread was interrupted");
                }
            } else {
                future.resolve(null);
                return null;
            }
        }
        return future;
    }

    @Override
    public void register(MicroService m) {
        SpecificBlockingQueue<Message> queueToPush = new SpecificBlockingQueue<>();
        queueToPush.setNameAndClassOfQueue(m.getName(), m.getClass());
        microServiceToQueue.putIfAbsent(m, queueToPush);
    }

    /**
     * pushing the queue to the required Linked-List, acquiring lock on Data-Structure inside value of HashMap.
     *
     * @param m            instance of Micro-Service
     * @param messageClass type of Event
     */
    private void fetchNPushQueue(MicroService m, Class<? extends Message> messageClass) {
        if (Broadcast.class.isAssignableFrom(messageClass)) {
            synchronized (broadcastToRoundRobinQueues) {
                SpecificBlockingQueue<Message> queuetoPush = microServiceToQueue.get(m);
                broadcastToRoundRobinQueues.get(messageClass).add(queuetoPush);
            }
        } else {
            synchronized (eventClassToRoundRobinQueues) {
                SpecificBlockingQueue<Message> queuetoPush = microServiceToQueue.get(m);
                eventClassToRoundRobinQueues.get(messageClass).add(queuetoPush);
            }
        }
    }

    @Override
    public void unregister(MicroService m) {
        //*******************//
        //clear data in queue.

        SpecificBlockingQueue<Message> qtoRemove = microServiceToQueue.get(m);
        try {

            //*******************//
            //the list inside the for-loop is applied only to one-thread or MicroService, therefore it is not shared-resource.
            for (Class<? extends Message> messageClass : microServiceInstancetoMessageClass.getOrDefault(m, new LinkedList<>())) {
                seekNdestroy(m, messageClass);
            }
            microServiceInstancetoMessageClass.remove(m);

            qtoRemove.getSemaphore().acquire();
            for (Message i : qtoRemove) {
                if (i instanceof Event) {
                    complete((Event) i, null);
                }
            }
            microServiceToQueue.get(m).clear();

            microServiceToQueue.remove(m);

        } catch (InterruptedException e) {
        } finally {
            qtoRemove.getSemaphore().release();
        }
    }

    /**
     * This method seeks for a specific instance of the queue that belongs to the microservice, and deletes it from the
     * Compatible hashmap ( eventClassToRoundRobinQueues or broadcastToRoundRobinQueues )
     *
     * @param m            MicroService
     * @param messageClass Class <? extends Message>
     */
    private void seekNdestroy(MicroService m, Class<? extends Message> messageClass) {
        if (Event.class.isAssignableFrom(messageClass)) {
            synchronized (eventClassToRoundRobinQueues) {
                SpecificBlockingQueue<Message> toRemove = null;
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList = eventClassToRoundRobinQueues.get(messageClass);
                toRemove = searchnGet(m, queueList);
                if (toRemove != null)
                    eventClassToRoundRobinQueues.get(messageClass).remove(toRemove);
            }
        } else if (Broadcast.class.isAssignableFrom(messageClass)) {
            synchronized (broadcastToRoundRobinQueues) {
                SpecificBlockingQueue<Message> toRemove = null;
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList = broadcastToRoundRobinQueues.get(messageClass);
                toRemove = searchnGet(m, queueList);
                if (toRemove != null) {
                    broadcastToRoundRobinQueues.get(messageClass).remove(toRemove);
                }
            }

        }
    }

    /**
     * Searching the specificblockingqueue assigned to the micro-service in order to down-grade the lock inside
     * unregister.
     *
     * @param m         MicroService
     * @param queueList RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>
     * @return
     */
    private SpecificBlockingQueue<Message> searchnGet(MicroService m, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList) {
        SpecificBlockingQueue<Message> toRet = null;
        for (SpecificBlockingQueue<Message> i : queueList) {
            if (i.getName().equals(m.getName())) {
                toRet = i;
                break;
            }
        }
        return toRet;
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        SpecificBlockingQueue<Message> queue = microServiceToQueue.get(m);
        return queue.take();
    }


}
