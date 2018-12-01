package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Inventory;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import sun.plugin2.message.Message;

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
            initialize((ArrayList)settings.getOrDefault("initialInventory", null));
            String ohad = "oahad";
        } catch (FileNotFoundException e) {
            System.out.println("file not found!");
        }
    }

    private static void initialize(ArrayList<LinkedTreeMap> inventorySettings) {
        if(inventorySettings ==null){
            System.out.println("No field 'initial inventory' in the config file!");
        }
        else{
            Inventory inventory = new Inventory();
            BookInventoryInfo [] booksToLoad = new BookInventoryInfo[inventorySettings.size()];
            for (LinkedTreeMap item:
                 inventorySettings) {
                try {
//                    item = (LinkedTreeMap)item;
                    BookInventoryInfo bookInventoryInfoItem = new BookInventoryInfo((String)((item).get("bookInfo")),  (int)(item.get("price")), (int)item.get("amount"));

                }
                catch (RuntimeException e){
                    System.out.println("problem with runtime in opening book inventory info");
                }
            }

//            inventory.load();
        }
    }
}
