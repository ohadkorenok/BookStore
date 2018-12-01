import bgu.spl.mics.RoundRobinLinkedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class RoundRobinLinkedListTest {

    private RoundRobinLinkedList <Integer> lista;
    private LinkedList <Integer> listo  ;


    @Before
    public void setUp() throws Exception {
        lista = new RoundRobinLinkedList<Integer>();
        listo = new LinkedList<Integer>();
    }

    @After
    public void tearDown() throws Exception {
        lista = null;
        listo = null;
    }

    @Test
    public void testGetRoundRobin() {
        assertEquals(lista.getFirst(), listo.getFirst());
        lista.getNext();

    }

    @Test
    public void testStep() {
    }

    @Test
    public void testGetNextIndex() {

    }
}