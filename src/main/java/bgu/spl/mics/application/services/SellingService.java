package bgu.spl.mics.application.services;


import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.BookOrderEvent;
import bgu.spl.mics.application.Messages.CheckBookInfo;
import bgu.spl.mics.application.Messages.TakeBookEvent;
import bgu.spl.mics.application.Messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

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
public class SellingService extends MicroService {

	private MoneyRegister accountant;
	private int time;

	public SellingService(String name) {
		super(name);
		accountant=MoneyRegister.getInstance();
		time=0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, tickIncoming->{
			this.time=tickIncoming.getCurrentTick();
		} );
		subscribeEvent(BookOrderEvent.class, event ->{
			System.out.println("Book Order Event arrived to "+this.getName());
			int proccessTick=this.time;
			Future<Integer> f1=sendEvent(new CheckBookInfo(event.getName()));
			if(f1.get()>0) {
				synchronized (event.getCustomer()) {
					Future<Boolean> f2 = sendEvent(new TakeBookEvent(event.getName()));
					if (f2.get()) {
						if (event.getCustomer().getAvailableCreditAmount() > 0) {
							accountant.chargeCreditCard(event.getCustomer(), f1.get());
							OrderReceipt reciept = new OrderReceipt(getName(), event.getCustomer().getId(), event.getName(), f1.get(), event.getissuedTick(), proccessTick, time);
							accountant.file(reciept);
							complete(event, reciept);
						} else
							//Couldn't Charge CreditCard
							complete(event,new NullReciept("",0,"",0,0,0,0));
					}
				}
			}
			else
				//Book not available
				complete(event,new NullReciept("",0,"",0,0,0,0));
		});
	}

}
