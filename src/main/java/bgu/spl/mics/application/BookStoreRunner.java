package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {

    private static HashMap<Integer, Customer> customersById = new HashMap<>();
    private static LinkedList<Thread> Threads = new LinkedList<>();

    public static void main(String[] args) {
        Gson gson = new Gson();
        try {
            Path path = Paths.get(args[0]);
            String absPath = path.toAbsolutePath().toString();
            FileReader fileReader = new FileReader(absPath);
////            FileReader fileReader = new FileReader(args[0]);
//            BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));
            HashMap settings = gson.fromJson(fileReader, HashMap.class);
            Inventory inventory = initializeInventoryAndLoadBooks((ArrayList) settings.getOrDefault("initialInventory", null));
            ResourcesHolder resourcesHolder = initializeResourceHolder((ArrayList) settings.getOrDefault("initialResources", null));
            initializeServicesAndCustomers((LinkedTreeMap) settings.getOrDefault("services", null));
            System.out.println("WELCOME TO NITZAN AND OHAD BOOKSTORE. ENJOY YOUR STAY");
            for (Thread thread :
                    Threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("The thread was interrupted. ");
                }
            }
            writeAllFiles(args[1], args[2], args[3], args[4]);

//            int numOfTest = Integer.parseInt(args[0].replace(new File(args[0]).getParent(), "").replace("/", "").replace(".json", ""));
//            String dir = new File(args[1]).getParent() + "/" + numOfTest + " - ";
//            Customer[] customers1 = customersById.values().toArray(new Customer[0]);
//            Arrays.sort(customers1, Comparator.comparing(Customer::getName));
//            String str_custs = Arrays.toString(customers1);
//            str_custs = str_custs.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
//            Print(str_custs, dir + "Customers");
//
//            HashMap books=Inventory.getInstance().getBookCollection();
//
//
//            String str_books = Arrays.toString(books.keySet().toArray());
//            str_books = str_books.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
//            Print(str_books, dir + "Books");
//
//            List<OrderReceipt> receipts_lst = MoneyRegister.getInstance().getOrderReceipts();
//            receipts_lst.sort(Comparator.comparing(OrderReceipt::getOrderId));
//            receipts_lst.sort(Comparator.comparing(OrderReceipt::getOrderTick));
//            OrderReceipt[] receipts = receipts_lst.toArray(new OrderReceipt[0]);
//            String str_receipts = Arrays.toString(receipts);
//            str_receipts = str_receipts.replaceAll(", ", "\n---------------------------\n").replace("[", "").replace("]", "");
//            Print(str_receipts, dir + "Receipts");
//
//            Print(MoneyRegister.getInstance().getTotalEarnings() + "", dir + "Total");
//
//
//


        } catch (FileNotFoundException e) {
            System.out.println("file not found!");
        }
    }

    private static Inventory initializeInventoryAndLoadBooks(ArrayList<LinkedTreeMap> inventorySettings) {
        Inventory inventory = null;
        if (inventorySettings == null) {
            System.out.println("No field 'initial inventory' in the config file!");
        } else {
            inventory = Inventory.getInstance();
            BookInventoryInfo[] booksToLoad = new BookInventoryInfo[inventorySettings.size()];
            for (int i = 0; i < booksToLoad.length; i++) {
                LinkedTreeMap bookInfoItem = inventorySettings.get(i);
                BookInventoryInfo bookToPush = new BookInventoryInfo((String) (bookInfoItem.get("bookTitle")), (int) (double) (bookInfoItem.get("price")), (int) (double) bookInfoItem.get("amount"));
                booksToLoad[i] = bookToPush;
            }
            inventory.load(booksToLoad);
        }
        return inventory;
    }

    private static ResourcesHolder initializeResourceHolder(ArrayList<LinkedTreeMap> resourceSettings) {
        ResourcesHolder resourcesHolder = null;
        if (resourceSettings == null) {
            System.out.println("No field 'initial Resources' in the config file!");
        } else {
//            ArrayList veichles = resourceSettings.get(0);
            ArrayList resourceTahles = (ArrayList) resourceSettings.get(0).get("vehicles");
            resourcesHolder = ResourcesHolder.getInstance();
            DeliveryVehicle[] bikesToLoad = new DeliveryVehicle[resourceTahles.size()];
            for (int i = 0; i < bikesToLoad.length; i++) {
                LinkedTreeMap bikeItem = (LinkedTreeMap) resourceTahles.get(i);
                DeliveryVehicle bikeToPush = new DeliveryVehicle((int) (double) (bikeItem.get("license")), (int) (double) bikeItem.get("speed"));
                bikesToLoad[i] = bikeToPush;
            }
            resourcesHolder.load(bikesToLoad);
        }
        return resourcesHolder;
    }

    private static Pair<Customer, OrderSchedule[]> buildCustomerFromConfig(LinkedTreeMap customerFromConfig) {
        int id = (int) (double) customerFromConfig.get("id");
        String name = (String) customerFromConfig.get("name");
        String address = (String) customerFromConfig.get("address");
        int distance = (int) (double) customerFromConfig.get("distance");
        LinkedTreeMap creditCard = (LinkedTreeMap) customerFromConfig.get("creditCard");
        int creditCardNumber = (int) (double) creditCard.get("number");
        int creditCardAmount = (int) (double) creditCard.get("amount");

        ArrayList orderSchedule = (ArrayList) customerFromConfig.get("orderSchedule");
        OrderSchedule[] orderSchedules = new OrderSchedule[orderSchedule.size()];
        for (int i = 0; i < orderSchedule.size(); i++) {
            LinkedTreeMap bookOrder = (LinkedTreeMap) orderSchedule.get(i);
            OrderSchedule bookInfo = new OrderSchedule((String) bookOrder.get("bookTitle"), (int) (double) bookOrder.get("tick"));
            orderSchedules[i] = bookInfo;
        }
        Customer customer = new Customer(id, name, address, distance, creditCardAmount, creditCardNumber);
        customersById.put(customer.getId(), customer);
        return new Pair<>(customer, orderSchedules);
    }

    private static Thread startTask(Runnable task) {
        Thread n1 = new Thread(task);
        n1.start();
        return n1;
    }

    private static void initializeServicesAndCustomers(LinkedTreeMap servicesSettings) {
        int sellingServiceWorkers = (int) (double) servicesSettings.get("selling");
        int inventoryServiceWorkers = (int) (double) servicesSettings.get("inventoryService");
        int logisticsServiceWorkers = (int) (double) servicesSettings.get("logistics");
        int resourceServiceWorker = (int) (double) servicesSettings.get("resourcesService");
        ArrayList customers = (ArrayList) servicesSettings.get("customers");

        LinkedTreeMap timeService = (LinkedTreeMap) servicesSettings.get("time");


        /***********   Initialize SellingService   ***********/
//        for (int i = 0; i < 1; i++) {

        for (int i = 0; i < sellingServiceWorkers; i++) {
            Runnable runnableSeller = new SellingService("SellerService" + i);

            Threads.add(startTask(runnableSeller));
        }
        /***********   Initialize InventoryService   ***********/
        for (int i = 0; i < inventoryServiceWorkers; i++) {
//        for (int i = 0; i < 1; i++) {

            Runnable runnableInventory = new InventoryService("InventoryService " + i);
            Threads.add(startTask(runnableInventory));
        }
        /***********   Initialize LogisticsService   ***********/
        for (int i = 0; i < logisticsServiceWorkers; i++) {

//        for (int i = 0; i < 1; i++) {
            Runnable runnableLogistics = new LogisticsService("LogisticsService " + i);
            Threads.add(startTask(runnableLogistics));
        }
        /***********   Initialize ResourceService   ***********/
        for (int i = 0; i < resourceServiceWorker; i++) {
//        for (int i = 0; i < 1; i++) {

            Runnable runnableResource = new ResourceService("ResourceService " + i);
            Threads.add(startTask(runnableResource));
        }
        /***********   Initialize APIService   ***********/
        for (int i = 0; i < customers.size(); i++) {

//        for (int i = 0; i < 1; i++) {
            Pair<Customer, OrderSchedule[]> pair = buildCustomerFromConfig((LinkedTreeMap) customers.get(i));
            Runnable runnableSession = new APIService("APISerivce " + i, pair.getKey(), pair.getValue());
            Threads.add(startTask(runnableSession));
        }

        /***********   Initialize TimeService   ***********/

        Runnable runnableTime = new TimeService((int) (double) timeService.get("speed"), (int) (double) timeService.get("duration"));
        Threads.add(startTask(runnableTime));
    }

    private static void writeAllFiles(String customersFileName, String inventoryFileName, String recieptsFileName, String moneyRegisterFileName) {

        /* custoemrs!!!! */
        writeObjectToFileName(customersFileName, customersById);


        /* Receipts */
        MoneyRegister moneyRegister = MoneyRegister.getInstance();
        moneyRegister.printOrderReceipts(recieptsFileName);

        /* InventoryFileName */
        Inventory inventory = Inventory.getInstance();
        inventory.printInventoryToFile(inventoryFileName);

        /* Money register */
        writeObjectToFileName(moneyRegisterFileName, moneyRegister);
    }

    public static void writeObjectToFileName(String filename, Object object){

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File not found");
        }
        catch (IOException e ) {
            e.printStackTrace();
        }

    }

    public static String customers2string(Customer[] customers) {
        String str = "";
        for (Customer customer : customers)
            str += customer2string(customer) + "\n---------------------------\n";
        return str;
    }

    public static String customer2string(Customer customer) {
        String str = "id    : " + customer.getId() + "\n";
        str += "name  : " + customer.getName() + "\n";
        str += "addr  : " + customer.getAddress() + "\n";
        str += "dist  : " + customer.getDistance() + "\n";
        str += "card  : " + customer.getCreditNumber() + "\n";
        str += "money : " + customer.getAvailableCreditAmount();
        return str;
    }

    public static String books2string(BookInventoryInfo[] books) {
        String str = "";
        for (BookInventoryInfo book : books)
            str += book2string(book) + "\n---------------------------\n";
        return str;
    }

    public static String book2string(BookInventoryInfo book) {
        String str = "";
        str += "title  : " + book.getBookTitle() + "\n";
        str += "amount : " + book.getAmountInInventory() + "\n";
        str += "price  : " + book.getPrice();
        return str;
    }


    public static String receipts2string(OrderReceipt[] receipts) {
        String str = "";
        for (OrderReceipt receipt : receipts)
            str += receipt2string(receipt) + "\n---------------------------\n";
        return str;
    }
    public static String receipt2string(OrderReceipt receipt) {
        String str = "";
        str += "customer   : " + receipt.getCustomerId() + "\n";
        str += "order tick : " + receipt.getOrderTick() + "\n";
        str += "id         : " + receipt.getOrderId() + "\n";
        str += "price      : " + receipt.getPrice() + "\n";
        str += "seller     : " + receipt.getSeller();
        return str;
    }

    public static void Print(String str, String filename) {
        try {
            try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
                out.print(str);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
        }
    }

}
