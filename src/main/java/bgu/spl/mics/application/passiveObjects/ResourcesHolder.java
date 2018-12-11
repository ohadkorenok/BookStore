package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

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
	private DeliveryVehicle[] deliveryVehicles;
	
	/**
     * Retrieves the single instance of this class.
     */
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
		//TODO: Implement this
		return null;
	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		//TODO: Implement this
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		deliveryVehicles = new DeliveryVehicle[vehicles.length];
		for (int i = 0; i < deliveryVehicles.length; i++) {
			deliveryVehicles[i] = vehicles[i];
		}
	}

}
