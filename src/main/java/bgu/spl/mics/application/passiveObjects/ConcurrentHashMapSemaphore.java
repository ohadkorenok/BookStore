package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.*;

public class ConcurrentHashMapSemaphore <T,V> extends ConcurrentHashMap <T,V> {
    private Semaphore lockingmap=new Semaphore(1);
     public Semaphore getSema(){return lockingmap;}
}
