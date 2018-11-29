package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.RoundRobinLinkedList;
import java.util.concurrent.*;

public class RoundRobinLinkedListSemaphore<T> extends RoundRobinLinkedList<T>{
    private Semaphore lockingRR=new Semaphore(1);
    public Semaphore getSema(){return lockingRR;}
}
