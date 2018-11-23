import bgu.spl.mics.*;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private MessageBus magicBus;

    @Before
    public void setUp() throws Exception {
        magicBus = new MessageBusImpl();


    }

    @Test
    public void subscribeEvent() {
        String[] a1 = new String[3];
        MicroService exampleEventHandlerService = new ExampleEventHandlerService("Worker1", a1);
        magicBus.register(exampleEventHandlerService);
    }

    @Test
    public void subscribeBroadcast() {
        String[] a1 = new String[3];
        MicroService exampleEventHandlerService = new ExampleEventHandlerService("Worker1", a1);
        magicBus.register(exampleEventHandlerService);
//        ExampleBroadcast broadcast = new ExampleBroadcast("ohad");
//        magicBus.subscribeBroadcast(broadcast, exampleEventHandlerService);
    }

    @Test
    public void complete() {
    }

    @Test
    public void sendBroadcast() {
    }

    @Test
    public void sendEvent() {
    }

    @Test
    public void register() {
    }

    @Test
    public void unregister() {
    }

    @Test
    public void awaitMessage() {
    }
}