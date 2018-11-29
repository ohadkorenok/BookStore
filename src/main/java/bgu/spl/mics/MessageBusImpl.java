package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.ConcurrentHashMapSemaphore;
import sun.security.provider.NativePRNG;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl messageBus;
    private ConcurrentHashMap<Class<? extends MicroService>, MicroService> classToWorker;
    private ConcurrentHashMap<Event, Future> eventToFuture;
    private ConcurrentHashMap<MicroService, LinkedList<LinkedBlockingQueue<Message>>> microServiceToLinkedList;
    private ConcurrentHashMap<Class<? extends Event>, Class<? extends MicroService>> eventToMs;
//    private Map<Event, Future> eventFutureMap;
//    private final Event<StubEvent> stubEvent;


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


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Future<T> future = new Future<>();
        eventToFuture.put(e, future);
        ///Computation result
        return future;
    }

    @Override
    public void register(MicroService m) {
		ConcurrentHashMapSemaphore<String,Integer> map=new ConcurrentHashMapSemaphore();
		ConcurrentHashMap<String,Integer> map2=new ConcurrentHashMap();

        synchronized (msQ) {
            if (!msQ.containsKey(m.getClass())) {
                LinkedList<LinkedBlockingQueue<Message>> listToPush = new LinkedList<>();
                listToPush.add(new LinkedBlockingQueue<>()); // Extend to work with name
                msQ.put(m.getClass(), listToPush);
            } else {
                LinkedList<LinkedBlockingQueue<Message>> listToUpdate = msQ.get(m.getClass());
                listToUpdate.add(new LinkedBlockingQueue<>()); // Extend to work with name
                msQ.replace(m.getClass(), listToUpdate);
            }
        }
    }

    @Override
    public void unregister(MicroService m) {


    }


    private LinkedBlockingQueue getQueueByEvent(Event e) {
        LinkedList<LinkedBlockingQueue<Message>> blockingQueueLinkedList = microServiceToLinkedList.getOrDefault(eventToMs.getOrDefault(e, null), null); //TODO :: finish after it.

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    private LinkedBlockingQueue pullCurrentQueue(LinkedList<LinkedBlockingQueue<Message>>){

    }


}
