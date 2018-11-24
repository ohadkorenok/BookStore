
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static bgu.spl.mics.application.passiveObjects.OrderResult.NOT_IN_STOCK;
import static bgu.spl.mics.application.passiveObjects.OrderResult.SUCCESSFULLY_TAKEN;
import static org.junit.Assert.*;

public class InventoryTest {
    private Inventory invent;
    private BookInventoryInfo[] arrToLoad;
    @Before
    public void setUp() throws Exception {
        invent=new Inventory();
        arrToLoad = new BookInventoryInfo[10];
        for(int i=0;i<10;i++){
            arrToLoad[i]=new BookInventoryInfo("harry potter "+i, 20+i,1);
        }
        invent.load(arrToLoad);
    }

    @Test
    public void getInstance() {
    }

    @Test
    public void load() {
    }

    @Test
    public void take() {
        OrderResult successes=SUCCESSFULLY_TAKEN;
        OrderResult fail=NOT_IN_STOCK;
        for(int i=0;i<10;i++){
            assertEquals(invent.take("harry potter "+i),successes);
            assertEquals(invent.take("Hit-Man"),fail);
        }
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        for(int i=0;i<10;i++){
            assertEquals(invent.checkAvailabiltyAndGetPrice("harry potter "+10),-1);
            assertEquals(invent.checkAvailabiltyAndGetPrice("harry potter "+i),arrToLoad[i].getPrice());
        }
    }

    @Test
    public void printInventoryToFile() {
    }

    @After
    public void tearDown() throws Exception {
    }
}