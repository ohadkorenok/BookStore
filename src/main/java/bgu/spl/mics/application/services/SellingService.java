package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.BookOrderEvent;
import bgu.spl.mics.application.Messages.CheckAvailabilityandReduceEvent;
import bgu.spl.mics.application.Messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import sun.plugin2.jvm.RemoteJVMLauncher;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{

	private MoneyRegister accountant;
	private int time;

	public SellingService(String name) {
		super(name);
		accountant=MoneyRegister.getInstance();
		time=0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class,tickIncoming->{
			this.time=tickIncoming.getCurrentTick();
		} );
		subscribeEvent(BookOrderEvent.class,event ->{
			int proccessTick=this.time;
			Future<Boolean> f1=sendEvent(new CheckAvailabilityandReduceEvent());
			if(f1.get()){
				if(event.getCustomer().getAvailableCreditAmount()>0){
					accountant.chargeCreditCard(event.getCustomer(),event.getPrice());
					OrderReceipt reciept=new OrderReceipt();
					complete(event,reciept);
				}
				else
					complete(event,null);
			}
		});
	}

}
