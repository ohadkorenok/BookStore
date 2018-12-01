package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

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

    private static Customer buildCustomerFromConfig(LinkedTreeMap customerFromConfig) {
        int id = (int) (double) customerFromConfig.get("id");
        String name = (String) customerFromConfig.get("name");
        String address = (String) customerFromConfig.get("address");
        int distance = (int) (double) customerFromConfig.get("distance");
        LinkedTreeMap creditCard = (LinkedTreeMap) customerFromConfig.get("creditCard");
        int creditCardNumber = (int) (double) creditCard.get("number");
        int creditCardAmount = (int) (double) creditCard.get("amount");

        return new Customer(id, name, address, distance, creditCardAmount, creditCardNumber);
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
        for (int i = 0; i < customers.size(); i++) {
            Runnable runnableSession = new APIService("ApiService" + i, buildCustomerFromConfig((LinkedTreeMap) customers.get(i)));
            startTask(runnableSession);
        }
        for (int i = 0; i < sellingServiceWorkers; i++) {
            Runnable runnableSeller = new SellingService();
            startTask(runnableSeller);
        }
        for (int i = 0; i < inventoryServiceWorkers; i++) {
            Runnable runnableInventory = new InventoryService();
            startTask(runnableInventory);
        }

        for (int i = 0; i < logisticsServiceWorkers; i++) {
            Runnable runnableLogistics = new LogisticsService();
            startTask(runnableLogistics);
        }
        for (int i = 0; i < resourceServiceWorker; i++) {
            Runnable runnableResource = new ResourceService();
            startTask(runnableResource);
        }
        Runnable runnableTime = new TimeService();
        startTask(runnableTime);


    }
}
