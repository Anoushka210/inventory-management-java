package storage;

import models.NonPerishableProduct;
import models.PerishableProduct;
import models.Product;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class FileManager implements FileHandler {
    private Map<Integer, Product> inventoryMap;
    // Changed from .dat to .csv
    private static final String DATA_FILE = "inventory.csv"; 
    private static final String REPORT_FILE = "report.txt";
    private static final String LOG_FILE = "transaction_log.txt";
    public FileManager(Map<Integer, Product> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    @Override
    public void saveData() {

        //temp debug to check where the file is being saved
        System.out.println("DEBUG: Saving to -> " + new File(DATA_FILE).getAbsolutePath());
        // PrintWriter makes it easy to write formatted text strings
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            // Write the CSV Header
            writer.println("Type,ID,Name,Price,Quantity,ExtraInfo");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (Product p : inventoryMap.values()) {
                if (p instanceof PerishableProduct pp) {
                    writer.printf("Perishable,%d,%s,%.2f,%d,%s\n",
                            pp.getId(), pp.getName(), pp.getPrice(), pp.getQuantity(), pp.getExpiryDate().format(formatter));
                } else if (p instanceof NonPerishableProduct np) {
                    writer.printf("NonPerishable,%d,%s,%.2f,%d,%s\n",
                            np.getId(), np.getName(), np.getPrice(), np.getQuantity(), np.getWarranty());
                }
            }
            System.out.println(" Inventory saved successfully to CSV!");
        } catch (IOException e) {
            System.out.println(" Error saving data: " + e.getMessage());
        }
    }

    @Override
    public void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println(" No previous data found. Starting with sample data.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            inventoryMap.clear();
            String line = reader.readLine(); // Read and ignore the header row

            // Read line by line until the end of the file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // Split the CSV by commas
                
                // Safety check: ensure the line has all 6 pieces of data
                if (data.length < 6) continue;

                String type = data[0];
                int id = Integer.parseInt(data[1]);
                String name = data[2];
                double price = Double.parseDouble(data[3]);
                int quantity = Integer.parseInt(data[4]);
                String extra = data[5];

                // Reconstruct the objects based on the Type column
                if (type.equals("Perishable")) {
                    inventoryMap.put(id, new PerishableProduct(id, name, price, quantity, extra));
                } else if (type.equals("NonPerishable")) {
                    inventoryMap.put(id, new NonPerishableProduct(id, name, price, quantity, extra));
                }
            }
            System.out.println(" Inventory loaded successfully from CSV!");
        } catch (IOException | NumberFormatException e) {
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

    public void appendLog(String logMessage) {
        // The "true" parameter here is the magic! It tells Java to APPEND, not overwrite.
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            System.out.println(" Error saving log: " + e.getMessage());
        }
    }
}