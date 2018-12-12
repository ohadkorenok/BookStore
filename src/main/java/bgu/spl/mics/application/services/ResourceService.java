package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.FindDriverEvent;
import bgu.spl.mics.application.Messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	private ResourcesHolder rH;

	public ResourceService(String name) {
		super(name);
		rH=ResourcesHolder.getInstance();
	}

	@Override
	protected void initialize() {
		subscribeEvent(FindDriverEvent.class, event->{
			Future f1=rH.acquireVehicle();
			if(f1.isDone())
				complete(event,f1.get());
		});
		subscribeEvent(ReleaseVehicleEvent.class, event->{
			rH.releaseVehicle(event.getVehicle());
		});
	}

}
