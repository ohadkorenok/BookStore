package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;

public class BookOrderEvent implements Event {
    private Customer c1;
    private int tick;
    public BookOrderEvent(Customer c1,int startOfCareTick){this.c1=c1;this.tick=startOfCareTick;}

    public Customer getCustomer(){return c1;}
    public int getissuedTick(){return tick;}
}
