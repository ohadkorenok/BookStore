package bgu.spl.mics;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class SpecificBlockingQueue <T> extends LinkedBlockingQueue <T> {

    private String name = null;
    private Class <? extends MicroService> classOfQueue = MicroService.class;
    private Semaphore locker=new Semaphore(1);

    public void setClassOfQueue(Class<? extends MicroService> classOfQueue) {
        this.classOfQueue = classOfQueue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameAndClassOfQueue(String name, Class <?extends MicroService> classOfQueue){
        this.classOfQueue = classOfQueue;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Class<? extends MicroService> getClassOfQueue() {
        return classOfQueue;
    }

    @Override
    public String toString() {
        return name;
    }
    public Semaphore getSemaphore(){return this.locker;}
}
