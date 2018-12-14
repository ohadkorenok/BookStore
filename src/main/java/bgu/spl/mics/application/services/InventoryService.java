package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.CheckBookInfo;
import bgu.spl.mics.application.Messages.TakeBookEvent;
import bgu.spl.mics.application.Messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;

import static bgu.spl.mics.application.passiveObjects.OrderResult.SUCCESSFULLY_TAKEN;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	private Inventory inv;

	public InventoryService(String name) {
		super(name);
		inv=Inventory.getInstance();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, finallCall->{
			this.terminate();
		});
		subscribeEvent(CheckBookInfo.class, ev->{
			int price=inv.checkAvailabiltyAndGetPrice(ev.getName());
		        complete(ev,price);
		});
		subscribeEvent(TakeBookEvent.class, ev->{
			OrderResult answer=inv.take(ev.getName());
		    if(answer==SUCCESSFULLY_TAKEN)
		        complete(ev,true);
		    else
		        complete(ev,false);
        });
	}

}
