package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;

public class CheckBookInfo implements Event {
    String bookName;
    public CheckBookInfo(String name){bookName=name;}
    public String getName(){return bookName;}
}
