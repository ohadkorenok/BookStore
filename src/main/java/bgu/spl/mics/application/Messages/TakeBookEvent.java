package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;

public class TakeBookEvent implements Event {
    String bookName;

    public TakeBookEvent(String name) {
        this.bookName = name;
    }

    public String getName() {
        return bookName;
    }
}
