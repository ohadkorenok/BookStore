package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;

public class CheckAvailabilityandReduceEvent implements Event {
    String bookName;
    public CheckAvailabilityandReduceEvent(String name){bookName=name;}
    public String getName(){return bookName;}
}
