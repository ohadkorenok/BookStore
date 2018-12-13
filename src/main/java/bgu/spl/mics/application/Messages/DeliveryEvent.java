package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;

public class DeliveryEvent implements Event {

    private int distance;
    private String address;
    public DeliveryEvent(String address1 ,int distance1) {
        distance = distance1;
        address = address1;
    }

    public int getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }
}
