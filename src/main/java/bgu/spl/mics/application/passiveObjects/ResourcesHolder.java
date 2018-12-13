package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	private static class SingleResourcesHolder {
		private static ResourcesHolder resourceHolder = new ResourcesHolder();
	}
    private ConcurrentLinkedQueue<DeliveryVehicle> queueOfVehicles;
    private ConcurrentLinkedQueue<Future<DeliveryVehicle>> futuretoResolve;
    private Semaphore locker;
	/**
     * Retrieves the single instance of this class.
     */
	private ResourcesHolder(){
        queueOfVehicles = new ConcurrentLinkedQueue<>();
        futuretoResolve = new ConcurrentLinkedQueue<>();
    }

	public static ResourcesHolder getInstance() {
		return SingleResourcesHolder.resourceHolder;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> futuro=new Future();
		if(locker.tryAcquire())
			futuro.resolve(queueOfVehicles.poll());
		else
			futuretoResolve.add(futuro);
		return futuro;
	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		queueOfVehicles.add(vehicle);
		locker.release();
		assignWaitersToVehicle();
	}
	private void assignWaitersToVehicle(){
		synchronized (futuretoResolve){
			if(!futuretoResolve.isEmpty()) {
				if (locker.tryAcquire()) {
					DeliveryVehicle tustus = queueOfVehicles.poll();
					if (tustus != null) {
						futuretoResolve.poll().resolve(tustus);
					}
				}
			}
		}
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		queueOfVehicles.addAll(Arrays.asList(vehicles));
		locker=new Semaphore(vehicles.length);
	}

}
