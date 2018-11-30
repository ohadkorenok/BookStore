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
    private ConcurrentHashMap<Class<? extends Event>, RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>> eventClassToRoundRobinQueues;
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
		for (Class<? extends Event> eventClass : serviceClasstoEventClass.get(m.getClass())) {

    		if(!eventClassToRoundRobinQueues.containsKey(eventClass)){
    			eventClassToRoundRobinQueues.put(eventClass,new RoundRobinLinkedListSemaphore<SpecificBlockingQueue<Message>>());
    			pushingQueue(m,eventClass);
			}
    		else {
    		    pushingQueue(m,eventClass);
            }
			}
		}

    /** pushing new queues to the Linked-List, acquiring lock on Data-Structure inside value of HashMap.
     *
     * @param m instance of Micro-Service
     * @param eventClass type of Event
     */
		private void pushingQueue(MicroService m,Class<? extends Event> eventClass){
		    try{
		        eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
                SpecificBlockingQueue<Message> queuetoPush= new SpecificBlockingQueue<>();
                queuetoPush.setNameAndClassOfQueue(m.getName(), m.getClass());
                eventClassToRoundRobinQueues.get(eventClass).add(queuetoPush);}
		    catch(InterruptedException ex){}
		    finally{eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);}
        }

    @Override
    public void unregister(MicroService m) {
        for (Class<? extends Event> eventClass : serviceClasstoEventClass.get(m.getClass())){
            SpecificBlockingQueue<Message> qtoRemove=searchnGet(m,eventClass);
            //Down-grading the lock in order to resolve the results to null and clear from Messages.
            synchronized (qtoRemove){
                for (Message i: qtoRemove) {
                    if(i instanceof Event)
                        complete((Event)i,null); }
                qtoRemove.clear();
            }
            //Up-grading the lock in order to remove the queue from the LL
            try{eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
                eventClassToRoundRobinQueues.remove(qtoRemove);}
            catch(InterruptedException e){}
            finally{eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);}
        }
    }

    /**Searching the specificblockingqueue assigned to the micro-service in order to down-grade the lock inside
     * unregister.
     * @param m
     * @param eventClass
     * @return
     */
    private SpecificBlockingQueue<Message> searchnGet(MicroService m,Class<? extends Event> eventClass){
        SpecificBlockingQueue<Message> toRet=null;
        try{
            eventClassToRoundRobinQueues.get(eventClass).getSema().acquire(1);
            for (SpecificBlockingQueue<Message> i: eventClassToRoundRobinQueues.get(eventClass)) {
                if(i.getName()==m.getName()) {
                    toRet=i;
                    break;
                }
            }
        }
        catch(InterruptedException ex){}
        finally{eventClassToRoundRobinQueues.get(eventClass).getSema().release(1);}
        return toRet;
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
