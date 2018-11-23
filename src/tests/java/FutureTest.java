import bgu.spl.mics.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private Future<String> futuro;

    @Before
    public void setUp() throws Exception {
        futuro = new Future<>();
    }


    @Test
    public void get() {
        assertNull(futuro.get());
        futuro.resolve("hi my name is ohad");
        assertEquals(futuro.get(), "hi my name is ohad");
    }

    @Test
    public void resolve() {
        futuro.resolve("hi my name is shaul");
        assertEquals("hi my name is shaul", futuro.get());

        futuro.resolve("hi my name is ernio");
        assertEquals("hi my name is ernio", futuro.get());

        futuro.resolve("hi my name is nitzan");
        assertEquals("hi my name is nitzan", futuro.get());

        futuro.resolve(null);
        assertNull(futuro.get());

    }

    @Test
    public void isDone() {
        assertFalse(futuro.isDone());
        futuro.resolve("pppppp");
        assertTrue(futuro.isDone());
        futuro.resolve(null);
        assertTrue(futuro.isDone());
        futuro = null;
        futuro = new Future<>();
        assertFalse(futuro.isDone());
        futuro.resolve("pppppp");
        assertTrue(futuro.isDone());
    }

    @Test
    public void get1() {
        assertNull(futuro.get(10, TimeUnit.SECONDS));
        futuro.resolve("hi my name is ohad");
        assertEquals(futuro.get(10 , TimeUnit.SECONDS), "hi my name is ohad");
    }

    @After
    public void tearDown() throws Exception {
        futuro = null;
    }
}