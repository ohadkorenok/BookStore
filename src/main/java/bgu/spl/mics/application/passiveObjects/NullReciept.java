package bgu.spl.mics.application.passiveObjects;

public class NullReciept extends OrderReceipt {
    public NullReciept(String seller, int customerId, String bookTitle, int price, int issuedTick, int proccessesTick, int orderTick) {
        super(seller, customerId, bookTitle, price, issuedTick, proccessesTick, orderTick);
    }
}
