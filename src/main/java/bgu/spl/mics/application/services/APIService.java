package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.BookOrderEvent;
import bgu.spl.mics.application.Messages.DeliveryEvent;
import bgu.spl.mics.application.Messages.TerminateBroadcast;
import bgu.spl.mics.application.Messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.Arrays;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService {
    private int time;
    private OrderSchedule[] orderSchedules;
    private int currentIndex = 0;
    private Customer customer;

    public APIService(String name, Customer customer1, OrderSchedule[] orderSchedule) {
        super(name);
        customer = customer1;
        Arrays.sort(orderSchedule);
        orderSchedules = new OrderSchedule[orderSchedule.length];
        for (int i = 0; i < orderSchedule.length; i++) {
            orderSchedules[i] = orderSchedule[i];
        }
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, finallCall->{
            this.terminate();
        });
        subscribeBroadcast(TickBroadcast.class, tickIncoming -> {
            this.time = tickIncoming.getCurrentTick();
            while (currentIndex <= orderSchedules.length - 1  && time == orderSchedules[currentIndex].getTick() ) {
                Future<OrderReceipt> futuro = sendEvent(new BookOrderEvent(customer, time, orderSchedules[currentIndex].getBookTitle()));
                OrderReceipt futuroReciept = futuro.get();
                if (!(futuroReciept instanceof NullReciept)) {
                    sendEvent(new DeliveryEvent(customer.getAddress(), customer.getDistance()));
                    customer.getCustomerReceiptList().add(futuro.get()); // add the issued tick.
                }
                currentIndex++;
            }
    });
}

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
