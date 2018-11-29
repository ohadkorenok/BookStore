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
    	Iterator it=serviceClasstoEventClass.get(m.getClass()).iterator();
    	while(it.hasNext()){
    		try{
    		eventClassToRoundRobinQueues.getSema().acquire();}
    		catch(InterruptedException e){};
    		if(eventClassToRoundRobinQueues.get())

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
        // TODO Auto-generated method stub
        return null;
    }

    private LinkedBlockingQueue pullCurrentQueue(LinkedList<LinkedBlockingQueue<Message>>) {

    }


}
