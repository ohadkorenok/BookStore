import bgu.spl.mics.*;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private MessageBus magicBus;
    private ExampleEvent exampleEvent;
    private ExampleBroadcast exampleBroadcast;
    private ExampleEventHandlerService ms1;
    private ExampleMessageSenderService ms2;
    private ExampleMessageSenderService ms3;


    @Before
    public void setUp() throws Exception {
        magicBus = MessageBusImpl.getInstance();
        String[] a1 = new String[3];
        MicroService ms1 = new ExampleEventHandlerService("Worker1", a1);
        MicroService ms2 = new ExampleMessageSenderService("Worker2", a1);
        MicroService ms3 = new ExampleMessageSenderService("Worker3", a1);
        magicBus.register(ms1);
        magicBus.register(ms2);
        ExampleEvent exampleEvent = new ExampleEvent("ohad");
        exampleBroadcast = new ExampleBroadcast("123456");
    }

    @Test
    public void subscribeEvent() {
        magicBus.subscribeEvent(exampleEvent.getClass(), ms3);
        assertNull(magicBus.sendEvent(exampleEvent));

        magicBus.subscribeEvent(exampleEvent.getClass(), ms1);
        assertNotNull(magicBus.sendEvent(exampleEvent));
    }

    @Test
    public void subscribeBroadcast() {
    }

    @Test
    public void complete() {
        magicBus.subscribeEvent(exampleEvent.getClass(), ms1);
        Future o1 = magicBus.sendEvent(exampleEvent);
        assertFalse(o1.isDone());
        magicBus.complete(exampleEvent, "YES");
        assertTrue(o1.isDone());
        assertEquals(o1.get(1, TimeUnit.SECONDS), "YES");
    }

    @Test
    public void sendBroadcast() {
    }

    @Test
    public void sendEvent() {
        magicBus.subscribeEvent(exampleEvent.getClass(), ms1);

    }

    @Test
    public void register() {

    }

    @Test
    public void unregister() {
        magicBus.unregister(ms1);
        assertNull(magicBus.sendEvent(exampleEvent));
    }

    @Test(expected = IllegalStateException.class)
    public void awaitMessage() {
        try {
            magicBus.awaitMessage(ms3);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
        }
    }
}