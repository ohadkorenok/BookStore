package bgu.spl.mics;

import bgu.spl.mics.application.Messages.TerminateBroadcast;
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
        private static MessageBusImpl messageBus=new MessageBusImpl();
    }
    private ConcurrentHashMap<Event, Future> eventToFuture = new ConcurrentHashMap<>(); //send
    private ConcurrentHashMap<Class<? extends Event>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> eventClassToRoundRobinQueues = new ConcurrentHashMap<>(); //subscribe
    private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Message>>> microServiceInstancetoMessageClass = new ConcurrentHashMap<>(); //subscribe event
    private ConcurrentHashMap<MicroService, SpecificBlockingQueue<Message>> microServiceToQueue = new ConcurrentHashMap<>(); // register , unregister.
    private ConcurrentHashMap<Class<? extends Broadcast>,RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> broadcastToRoundRobinQueues= new ConcurrentHashMap<>();

    public static MessageBusImpl getInstance() {
        return SingleMessageBus.messageBus;
    }

    private void putIntoMsInstanceToMessageClassHashMap(Class <?extends Message>  type, MicroService m){
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
        fetchNPushQueue(m ,type);

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        putIntoMsInstanceToMessageClassHashMap(type, m);
        broadcastToRoundRobinQueues.putIfAbsent(type,new RoundRobinLinkedListSemaphore<>());
        fetchNPushQueue(m,type);
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
            synchronized (broadcastToRoundRobinQueues.get(b.getClass())) {
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> roundRobinBlockingQueueOfBroadcasts = broadcastToRoundRobinQueues.get(b.getClass());
                if (roundRobinBlockingQueueOfBroadcasts != null) {
                    for (SpecificBlockingQueue<Message> it : roundRobinBlockingQueueOfBroadcasts) {
                        try {
                            it.put(b);
                        }
                        catch (InterruptedException e ){System.out.println("Hi I was interupted");}
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
        RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> roundRobinBlockingQueueOfEvents = eventClassToRoundRobinQueues.getOrDefault(e.getClass(), null);
        if (roundRobinBlockingQueueOfEvents != null) {
            try {
                roundRobinBlockingQueueOfEvents.getSema().acquire(1);
                SpecificBlockingQueue<Message> currentQueue = roundRobinBlockingQueueOfEvents.getNext();
                currentQueue.put(e);
            } catch (InterruptedException ex) {
                System.out.println("SendEvent Was interrupted! ");
            } finally {
                roundRobinBlockingQueueOfEvents.getSema().release(1);
            }
        }
        else {return null;}
        return future;
    }

    @Override
    public void register(MicroService m) {
        SpecificBlockingQueue <Message> queueToPush = new SpecificBlockingQueue<>();
        queueToPush.setNameAndClassOfQueue(m.getName(), m.getClass());
        microServiceToQueue.putIfAbsent(m, queueToPush);
    }

    /**
     * pushing the queue to the required Linked-List, acquiring lock on Data-Structure inside value of HashMap.
     *
     * @param m          instance of Micro-Service
     * @param messageClass type of Event
     */
    private void fetchNPushQueue(MicroService m, Class<? extends Message> messageClass) {
        if(Broadcast.class.isAssignableFrom(messageClass)) {
                synchronized (broadcastToRoundRobinQueues.get(messageClass)) {
                    SpecificBlockingQueue<Message> queuetoPush = microServiceToQueue.get(m);
                    if (!messageClass.toString().equals("class bgu.spl.mics.application.Messages.TickBroadcast")) {
                        System.out.println("Hi I am fetching and gonna push " + Thread.currentThread().getName());
                    }
                    broadcastToRoundRobinQueues.get(messageClass).add(queuetoPush);
                    if (!messageClass.toString().equals("class bgu.spl.mics.application.Messages.TickBroadcast")) {
                        System.out.println(broadcastToRoundRobinQueues.get(messageClass).toString());
                    }
                }
        }
        else {
            synchronized (eventClassToRoundRobinQueues.get(messageClass)) {
                SpecificBlockingQueue<Message> queuetoPush = microServiceToQueue.get(m);
                eventClassToRoundRobinQueues.get(messageClass).add(queuetoPush);
            }
        }
    }

    @Override
    public void unregister(MicroService m) {
            //*******************//
            //clear data in queue.

        synchronized (microServiceToQueue.get(m)) {
            SpecificBlockingQueue<Message> qtoRemove = microServiceToQueue.get(m);
            for (Message i : qtoRemove) {
                if (i instanceof Event) {
                    complete((Event) i, null);
                }
                System.out.println("Hi,removing this Message: "+i+" "+Thread.currentThread().getName());
            }
            microServiceToQueue.get(m).clear();
        }
        microServiceToQueue.remove(m);
            //*******************//
        //the list inside the for-loop is applied only to one-thread or MicroService, therefore it is not shared-resource.
        for (Class<? extends Message> messageClass : microServiceInstancetoMessageClass.getOrDefault(m, new LinkedList<Class<? extends Message>>())) {
                seekNdestroy(m,messageClass);
        }
        microServiceInstancetoMessageClass.remove(m);
        System.out.println("Finished Unregister " +Thread.currentThread().getName());
    }
    private void seekNdestroy(MicroService m,Class<? extends Message> messageClass){
        if(Event.class.isAssignableFrom(messageClass)){
            synchronized (eventClassToRoundRobinQueues.get(messageClass)){
                SpecificBlockingQueue<Message> toRemove=null;
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList=eventClassToRoundRobinQueues.get(messageClass);
                toRemove=searchnGet(m,queueList);
                if(toRemove!=null)
                    eventClassToRoundRobinQueues.get(messageClass).remove(toRemove);
            }
        }
        else if(Broadcast.class.isAssignableFrom(messageClass)){
            synchronized (broadcastToRoundRobinQueues.get(messageClass)){
                SpecificBlockingQueue<Message> toRemove=null;
                RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList=broadcastToRoundRobinQueues.get(messageClass);
                toRemove=searchnGet(m,queueList);
                if(toRemove!=null)
                    broadcastToRoundRobinQueues.get(messageClass).remove(toRemove);
            }

        }
    }



    private void deleteSpecificQueue(RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>> queueList,SpecificBlockingQueue<Message> queueToRemove){
        try {
            queueList.getSema().acquire(1);
            queueList.remove(queueToRemove);
        } catch (InterruptedException e) {
        } finally {
            queueList.getSema().release(1);
        }
    }

    /**
     * Searching the specificblockingqueue assigned to the micro-service in order to down-grade the lock inside
     * unregister.
     *
     * @param m          MicroService
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
