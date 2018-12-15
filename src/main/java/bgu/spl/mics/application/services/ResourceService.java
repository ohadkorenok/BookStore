package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Messages.FindDriverEvent;
import bgu.spl.mics.application.Messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.Messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService {
    private ResourcesHolder rH;
    private static AtomicInteger instanceCounter = new AtomicInteger(0);
    private static ConcurrentLinkedQueue<Future> futurePool = new ConcurrentLinkedQueue<>();

    public ResourceService(String name) {
        super(name);
        rH = ResourcesHolder.getInstance();
        instanceCounter.getAndIncrement();
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, finallCall -> {
            synchronized (instanceCounter) {
                System.out.println("The resource service instance counter is : " + instanceCounter.get());
                if (instanceCounter.get() != 1) {
                    this.terminate();
                    instanceCounter.getAndDecrement();
                } else {
                    System.out.println("Hi instanceCounter is 1");
                    for (Future futuro :
                            futurePool) {
                        if (!futuro.isDone()) {
                            System.out.println("Resoling to NULL" + futuro);
                            futuro.resolve(null);
                        }
                    }
                    terminate();
                }
            }
        });

        subscribeEvent(FindDriverEvent.class, event -> {
            System.out.println("Find driver event got into " + this.getName());
            Future f1 = rH.acquireVehicle();
            if(!f1.isDone()) {
                System.out.println(f1.toString()+" Added to waiting poll");
                futurePool.add(f1);
            }
            complete(event, f1);
        });
        subscribeEvent(ReleaseVehicleEvent.class, event -> {
            System.out.println("Release driver event got into " + this.getName());
            rH.releaseVehicle(event.getVehicle());
        });
    }


}
