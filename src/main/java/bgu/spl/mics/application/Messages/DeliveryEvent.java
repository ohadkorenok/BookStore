package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Event;

public class DeliveryEvent implements Event {

    private int distance;
    private String address;
    public DeliveryEvent(String address ,int distance) {
        distance = distance;
        address = address;
    }

    public int getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }
}
