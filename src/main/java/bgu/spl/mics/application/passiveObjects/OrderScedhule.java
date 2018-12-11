package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.application.Messages.TickBroadcast;

public class OrderScedhule implements Comparable{

    private BookInventoryInfo bookInventoryInfo;
    private int tick;

    public OrderScedhule(BookInventoryInfo bookInventoryInfo, int tick) {
        this.bookInventoryInfo = bookInventoryInfo;
        this.tick = tick;
    }

    public BookInventoryInfo getBookInventoryInfo() {
        return bookInventoryInfo;
    }

    public void setBookInventoryInfo(BookInventoryInfo bookInventoryInfo) {
        this.bookInventoryInfo = bookInventoryInfo;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    @Override
    public int compareTo(Object o) {
        int answer=0;
        if(o instanceof OrderScedhule){
            answer = tick - ((OrderScedhule) o).getTick();
        }
        else{
            throw new ClassCastException("Not OrderScedhule Object");
        }
        return answer;
    }
}