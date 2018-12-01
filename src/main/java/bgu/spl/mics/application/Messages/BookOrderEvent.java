package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;

public class BookOrderEvent implements Event {
    private Customer c1;
    private int tick;
    private int price;
    public BookOrderEvent(Customer c1,int startOfCareTick,int price){this.c1=c1;this.tick=startOfCareTick;this.price=price;}

    public Customer getCustomer(){return c1;}
    public int getissuedTick(){return tick;}
    public int getPrice(){return price;}
}
