package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) {
        Gson gson = new Gson();
        try {
            Path path = Paths.get(args[0]);
            String absPath = path.toAbsolutePath().toString();
            FileReader fileReader = new FileReader(absPath);

            HashMap settings = gson.fromJson(fileReader, HashMap.class);
            Inventory inventory = initializeInventoryAndLoadBooks((ArrayList) settings.getOrDefault("initialInventory", null));
            ResourcesHolder resourcesHolder = initializeResourceHolder((ArrayList) settings.getOrDefault("initialResources", null));
            initializeServicesAndCustomers((LinkedTreeMap) settings.getOrDefault("services", null));

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

        ArrayList orderSchedule = (ArrayList)customerFromConfig.get("orderSchedule");
        OrderSchedule [] orderSchedules = new OrderSchedule[orderSchedule.size()];
        for (int i = 0; i < orderSchedule.size(); i++) {
            LinkedTreeMap bookOrder = (LinkedTreeMap)orderSchedule.get(i);
            OrderSchedule bookInfo = new OrderSchedule((String)bookOrder.get("bookTitle"),(int)(double)bookOrder.get("tick"));
            orderSchedules[i] = bookInfo;
        }
        Customer customer = new Customer(id, name, address, distance, creditCardAmount, creditCardNumber);
        return new Pair<>(customer, orderSchedules);
    }

    private static void startTask(Runnable task) {
        Thread n1 = new Thread(task);
        n1.start();
    }

    private static void initializeServicesAndCustomers(LinkedTreeMap servicesSettings) {
        int sellingServiceWorkers = (int) (double) servicesSettings.get("selling");
        int inventoryServiceWorkers = (int) (double) servicesSettings.get("inventoryService");
        int logisticsServiceWorkers = (int) (double) servicesSettings.get("logistics");
        int resourceServiceWorker = (int) (double) servicesSettings.get("resourcesService");
        ArrayList customers = (ArrayList) servicesSettings.get("customers");

        LinkedTreeMap timeService = (LinkedTreeMap) servicesSettings.get("time");


        /***********   Initialize SellingService   ***********/

        for (int i = 0; i < sellingServiceWorkers; i++) {
//            Runnable runnableSeller = new SellingService("SellerService" + i);
//            startTask(runnableSeller);
        }
        /***********   Initialize InventoryService   ***********/
        for (int i = 0; i < inventoryServiceWorkers; i++) {
//            Runnable runnableInventory = new InventoryService();
//            startTask(runnableInventory);
        }
        /***********   Initialize LogisticsService   ***********/
        for (int i = 0; i < logisticsServiceWorkers; i++) {
//            Runnable runnableLogistics = new LogisticsService();
//            startTask(runnableLogistics);
        }
        /***********   Initialize ResourceService   ***********/
        for (int i = 0; i < resourceServiceWorker; i++) {
//            Runnable runnableResource = new ResourceService();
//            startTask(runnableResource);
        }
        /***********   Initialize APIService   ***********/
        for (int i = 0; i < customers.size(); i++) {
            Pair <Customer, OrderSchedule []> pair = buildCustomerFromConfig((LinkedTreeMap) customers.get(i));
            String ohad = "ohad";
//            Runnable runnableSession = new APIService("ApiService" + i, buildCustomerFromConfig((LinkedTreeMap) customers.get(i)));
//            startTask(runnableSession);
        }

        /***********   Initialize TimeService   ***********/
//        Runnable runnableTime = new TimeService((int) timeService.get("speed"), (int) timeService.get("duration"));
//        startTask(runnableTime);

    }
}
