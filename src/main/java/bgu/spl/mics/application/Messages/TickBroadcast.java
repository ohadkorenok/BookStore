package bgu.spl.mics.application.Messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    private int currentTick;

    public TickBroadcast(int currentTick1) {
        currentTick = currentTick1;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}
