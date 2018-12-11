package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.BookOrderEvent;
import bgu.spl.mics.application.Messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.Arrays;
import java.util.HashMap;

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
    private OrderScedhule[] orderScedhules;
    private int currentIndex = 0;
    private Customer customer;

    public APIService(String name, Customer customer, OrderScedhule[] orderScedhule) {
        super(name);
        customer = customer;
        Arrays.sort(orderScedhule);
        orderScedhules = new OrderScedhule[orderScedhule.length];
        for (int i = 0; i < orderScedhule.length; i++) {
            orderScedhules[i] = orderScedhule[i];
        }
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tickIncoming -> {
            this.time = tickIncoming.getCurrentTick();
            while(time == orderScedhules[currentIndex].getTick() && currentIndex <= orderScedhules.length-1){
                Future <OrderReceipt> futuro = sendEvent(new BookOrderEvent(customer, time, orderScedhules[currentIndex].getBookInventoryInfo().getBookTitle()));
                OrderReceipt futuroReciept = futuro.get();
                if(!(futuroReciept instanceof NullReciept)){
                }
                //                customer.getCustomerReceiptList().add(futuro.get());
                currentIndex ++;
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
