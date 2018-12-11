package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.DeliveryEvent;
import bgu.spl.mics.application.Messages.FindDriverEvent;
import bgu.spl.mics.application.Messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	public LogisticsService(String name) {
		super(name);
	}
	@Override
	protected void initialize() {
		subscribeEvent(DeliveryEvent.class, incomingDelivery->{
			Future <DeliveryVehicle> futuro = sendEvent(new FindDriverEvent());
			DeliveryVehicle futuroVehicle = futuro.get();
			futuroVehicle.deliver(incomingDelivery.getAddress(), incomingDelivery.getDistance());
			sendEvent(new ReleaseVehicleEvent(futuroVehicle));
		});
	}

}
