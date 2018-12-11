package bgu.spl.mics.application.passiveObjects;
import java.util.Arrays;
import java.util.concurrent.*;
import java.io.*;


/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory implements java.io.Serializable{
	private BookInventoryInfo[] bookCollection;
	private static class SingleInventory {
		private static Inventory inventory=new Inventory();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return SingleInventory.inventory;
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	//@PRE: inventory !=null
	//@POST: inv initialized
	public void load (BookInventoryInfo[ ] inventory ) {
		Arrays.sort(inventory);
		bookCollection = new BookInventoryInfo[inventory.length];
		for (int i = 0; i < inventory.length; i++) {
			bookCollection[i] = inventory[i];
		}
	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	//@PRE: book != null
	//@POST: inv.get(book).getAmountInInventory()=inv.get(book).getAmountInInventory()-1
	public OrderResult take (String book) {
		int i = 0;
		if (book == null) {
			return OrderResult.NOT_IN_STOCK;
		} else {
			synchronized (bookCollection) {
				while (i < bookCollection.length && (bookCollection[i].getBookTitle().compareTo(book) < 1)) {
					if (bookCollection[i].getBookTitle().equals(book) && bookCollection[i].getAmountInInventory() > 0) {
						bookCollection[i].setAmountInInventory(bookCollection[i].getAmountInInventory() - 1);
						return OrderResult.SUCCESSFULLY_TAKEN;
					}
					i++;
				}
				return OrderResult.NOT_IN_STOCK;
			}
		}

	}
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	//@PRE: book != null && inv.get(book).getPrice()>=0
	//@POST: if(inv.get(book).getAmountInInventory()>0)return value==inv.get(book).getPrice()
	public int checkAvailabiltyAndGetPrice(String book) {
		for (int i = 0; i < bookCollection.length; i++) {
			if(bookCollection[i].getBookTitle() .equals(book)){
				return bookCollection[i].getPrice();
			}
		}
		return -1;
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	//@PRE: inv != null
	//@POST: data in file == inv.toString() + inv.elements(i).toString()
	public void printInventoryToFile(String filename){

	}
}
