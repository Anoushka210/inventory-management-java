package storage;

import models.Product;
import java.io.*;
import java.util.Map;

public class FileManager implements FileHandler {
    private Map<Integer, Product> inventoryMap;
    private static final String DATA_FILE = "inventory.dat";
    private static final String REPORT_FILE = "report.txt";

    public FileManager(Map<Integer, Product> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    @Override
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(inventoryMap);
            System.out.println(" Inventory saved successfully!");
        } catch (IOException e) {
            System.out.println(" Error saving data: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Map<Integer, Product> loadedMap = (Map<Integer, Product>) ois.readObject();
            inventoryMap.clear();
            inventoryMap.putAll(loadedMap);
            System.out.println(" Inventory loaded successfully!");
        } catch (FileNotFoundException e) {
            System.out.println(" No previous data found. Starting with sample data.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(" Error loading data: " + e.getMessage());
        }
    }

    public void saveReport(String content) {
        try (FileWriter writer = new FileWriter(REPORT_FILE)) {
            writer.write(content);
            System.out.println(" Report generated and saved to '" + REPORT_FILE + "'");
        } catch (IOException e) {
            System.out.println(" Error saving report: " + e.getMessage());
        }
    }
}