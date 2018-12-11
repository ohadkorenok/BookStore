package bgu.spl.mics.application.passiveObjects;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer {

    private String name;
    private int id;
    private String address;
    private int distance;
    private volatile int amount;
    private int creditCardNumber;
    private Semaphore locker;

    /**
     * Retrieves the name of the customer.
     */
    public Customer(int id, String name, String address, int distance, int amount, int creditCardNumber) {
        id = id;
        name = name;
        address = address;
        distance = distance;
        amount = amount;
        creditCardNumber = creditCardNumber;
        locker=new Semaphore(1);
    }

    public Semaphore getSema(){return locker;}

    public String getName() {
        return name;
    }

    /**
     * Retrieves the ID of the customer  .
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the address of the customer.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Retrieves the distance of the customer from the store.
     */
    public int getDistance() {
        return distance;
    }


    /**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     *
     * @return A list of receipts.
     */
    public List<OrderReceipt> getCustomerReceiptList() {
        // TODO Implement this
        return null;
    }

    /**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     *
     * @return Amount of money left.
     */
    public int getAvailableCreditAmount() {
        return amount;
    }

    /**
     * Retrieves this customers credit card serial number.
     */
    public int getCreditNumber() {
        return creditCardNumber;
    }

}
