import com.inventory.core.InventoryManager;
import com.inventory.core.StockMonitorThread;
import com.inventory.exceptions.OutOfStockException;
import com.inventory.models.NonPerishableProduct;
import com.inventory.models.PerishableProduct;

import java.util.InputMismatchException;
import java.util.Scanner;

public class SmartInventorySystem {
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        Scanner scanner = new Scanner(System.in);

        manager.loadInventoryOrInitialize();

        StockMonitorThread monitor = new StockMonitorThread(manager);
        monitor.start();

        boolean exit = false;

        while (!exit) {
            System.out.println("\n----------------------------------------");
            System.out.println("What would you like to do next?");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Products"); // <--- NEW
            System.out.println("4. Sell Product");
            System.out.println("5. Restock Product");
            System.out.println("6. View Low Stock Items");
            System.out.println("7. Generate Report");
            System.out.println("8. Save & Exit");     // <--- MOVED TO 8
            System.out.println("----------------------------------------");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> addProductMenu(scanner, manager);
                    case 2 -> manager.showAllProducts();
                    case 3 -> { // <--- NEW SEARCH CASE
                        System.out.print("Enter product name or ID to search: ");
                        String keyword = scanner.nextLine();
                        manager.searchProducts(keyword);
                    }
                    case 4 -> sellProductMenu(scanner, manager);
                    case 5 -> restockProductMenu(scanner, manager);
                    case 6 -> manager.checkLowStock();
                    case 7 -> manager.generateReport();
                    case 8 -> { // <--- UPDATED TO 8
                        manager.saveInventory();
                        monitor.stopMonitoring();
                        exit = true;
                        System.out.println(" Thank you for using Smart Inventory System!");
                    }
                    default -> System.out.println(" Invalid choice! Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println(" Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private static void addProductMenu(Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Price: ");
            String priceInput = scanner.nextLine().replace("₹", "").replace(",", "").trim();
            double price = Double.parseDouble(priceInput);

            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Is it perishable (y/n)? ");
            String type = scanner.nextLine();

            if (type.equalsIgnoreCase("y")) {
                System.out.print("Enter Expiry Date (DD-MM-YYYY): ");
                String expiry = scanner.nextLine();
                manager.addProductWithMessage(new PerishableProduct(id, name, price, quantity, expiry));
            } else {
                System.out.print("Enter Warranty (or N/A): ");
                String warranty = scanner.nextLine();
                manager.addProductWithMessage(new NonPerishableProduct(id, name, price, quantity, warranty));
            }
        } catch (NumberFormatException e) {
            System.out.println(" Invalid price format!");
        } catch (InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }

    private static void sellProductMenu(Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            System.out.print("Enter Quantity to sell: ");
            int quantity = scanner.nextInt();
            manager.sellProduct(id, quantity);
        } catch (OutOfStockException e) {
            System.out.println(e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }

    private static void restockProductMenu(Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            System.out.print("Enter Quantity to restock: ");
            int quantity = scanner.nextInt();
            manager.restockProduct(id, quantity);
        } catch (InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }
}