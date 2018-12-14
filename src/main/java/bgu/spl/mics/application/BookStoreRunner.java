package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
            BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));
            HashMap settings = gson.fromJson(fileReader, HashMap.class);
            initializeInventoryAndLoadBooks((ArrayList) settings.getOrDefault("initialInventory", null));
            initializeResourceHolder((ArrayList) settings.getOrDefault("initialResources", null));
            initializeServicesAndCustomers((LinkedTreeMap) settings.getOrDefault("services", null));
            for (Thread thread :
                    Threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("The thread was interrupted. ");
                }
            }
            writeAllFiles(args[1], args[2], args[3], args[4]);

        } catch (FileNotFoundException e) {
            System.out.println("file not found!");
        }

    }

    /**
     * This method initializes the inventory and loads books from the config to the inventory
     *
     * @param inventorySettings ArrayList<LinkedTreeMap>
     * @return Inventory
     */
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

    /**
     * This method initlaizes the resource holder from the config.
     *
     * @param resourceSettings ArrayList <LinkedTreeMap>
     * @return ResourceHolder
     */
    private static ResourcesHolder initializeResourceHolder(ArrayList<LinkedTreeMap> resourceSettings) {
        ResourcesHolder resourcesHolder = null;
        if (resourceSettings == null) {
            System.out.println("No field 'initial Resources' in the config file!");
        } else {
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

    /**
     * Build customer from config.
     *
     * @param customerFromConfig LinkedTreeMap
     * @return Pair <Customer, OrderSchedule[]>
     */
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

    /**
     * Starts the desired runnable object.
     *
     * @param task Runnable
     * @return Thread
     */
    private static Thread startTask(Runnable task) {
        Thread n1 = new Thread(task);
        n1.start();
        return n1;
    }

    /**
     * This method iniaitlizes the services and customers from services settings.
     *
     * @param servicesSettings LinkedTreeMap
     */
    private static void initializeServicesAndCustomers(LinkedTreeMap servicesSettings) {
        int sellingServiceWorkers = (int) (double) servicesSettings.get("selling");
        int inventoryServiceWorkers = (int) (double) servicesSettings.get("inventoryService");
        int logisticsServiceWorkers = (int) (double) servicesSettings.get("logistics");
        int resourceServiceWorker = (int) (double) servicesSettings.get("resourcesService");
        ArrayList customers = (ArrayList) servicesSettings.get("customers");

        LinkedTreeMap timeService = (LinkedTreeMap) servicesSettings.get("time");


        /***********   Initialize SellingService   ***********/

        for (int i = 0; i < sellingServiceWorkers; i++) {
            Runnable runnableSeller = new SellingService("SellerService" + i);

            Threads.add(startTask(runnableSeller));
        }
        /***********   Initialize InventoryService   ***********/
        for (int i = 0; i < inventoryServiceWorkers; i++) {

            Runnable runnableInventory = new InventoryService("InventoryService " + i);
            Threads.add(startTask(runnableInventory));
        }
        /***********   Initialize LogisticsService   ***********/
        for (int i = 0; i < logisticsServiceWorkers; i++) {

            Runnable runnableLogistics = new LogisticsService("LogisticsSerivce " + i);
            Threads.add(startTask(runnableLogistics));
        }
        /***********   Initialize ResourceService   ***********/
        for (int i = 0; i < resourceServiceWorker; i++) {

            Runnable runnableResource = new ResourceService("ResourceService " + i);
            Threads.add(startTask(runnableResource));
        }
        /***********   Initialize APIService   ***********/
        for (int i = 0; i < customers.size(); i++) {

            Pair<Customer, OrderSchedule[]> pair = buildCustomerFromConfig((LinkedTreeMap) customers.get(i));
            Runnable runnableSession = new APIService("APISerivce " + i, pair.getKey(), pair.getValue());
            Threads.add(startTask(runnableSession));
        }

        /***********   Initialize TimeService   ***********/

        Runnable runnableTime = new TimeService((int) (double) timeService.get("speed"), (int) (double) timeService.get("duration"));
        Threads.add(startTask(runnableTime));
    }

    /**
     * This function writes all the desired output files for the application .
     *
     * @param customersFileName     String
     * @param inventoryFileName     String
     * @param recieptsFileName      String
     * @param moneyRegisterFileName String
     */
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

    /**
     * This method writes the object to the desired filename
     *
     * @param filename String
     * @param object   Object
     */
    public static void writeObjectToFileName(String filename, Object object) {

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
