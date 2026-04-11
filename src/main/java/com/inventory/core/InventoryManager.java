package com.inventory.core;

import com.inventory.exceptions.OutOfStockException;
import com.inventory.models.NonPerishableProduct;
import com.inventory.models.PerishableProduct;
import com.inventory.models.Product;
import com.inventory.storage.FileManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryManager {
    private Map<Integer, Product> inventoryMap;
    private double totalSales;
    private FileManager fileManager;

    @Autowired
    public InventoryManager(FileManager fileManager) {
        this.fileManager = fileManager; 
        this.inventoryMap = new ConcurrentHashMap<>();
        this.totalSales = 0.0;
        
        loadInventoryOrInitialize();
    }

    public void loadInventoryOrInitialize() {
        fileManager.loadData();
        
        if (inventoryMap.isEmpty()) {
            addProduct(new PerishableProduct(101, "Milk", 45.0, 15, "20-10-2025"));
            addProduct(new NonPerishableProduct(102, "Sugar", 55.0, 4, "N/A"));
            addProduct(new NonPerishableProduct(103, "Tea Leaves", 120.0, 2, "N/A"));
            addProduct(new PerishableProduct(104, "Yogurt", 35.0, 25, "30-10-2025"));
            System.out.println(" Sample data loaded successfully!");
        }
    }

    public void addProduct(Product product) {
        inventoryMap.put(product.getId(), product);
    }

    public void addProductWithMessage(Product product) {
        if (inventoryMap.containsKey(product.getId())) {
            System.out.println(" Error: Product ID " + product.getId() + " already exists!");
            return;
        }
        inventoryMap.put(product.getId(), product);
        System.out.println(" Product added successfully!");
    }

    public void sellProduct(int productId, int quantity) throws OutOfStockException {
        Product product = findProductById(productId);
        if (product == null) {
            throw new OutOfStockException(" Product not found!");
        }
        
        synchronized (product) {
            if (product.getQuantity() < quantity) {
                throw new OutOfStockException(" Insufficient stock! Available: " + product.getQuantity());
            }
            product.updateStock(-quantity);
            double saleAmount = quantity * product.getPrice();
            totalSales += saleAmount;
            System.out.println(" Sale successful! Remaining quantity: " + product.getQuantity());

            // --- NEW LOGGING LOGIC ---
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("[%s] SOLD: %d units of %s (ID: %d) for ₹%.2f. Total Sale: ₹%.2f",
                    timestamp, quantity, product.getName(), product.getId(), product.getPrice(), saleAmount);
            
            fileManager.appendLog(logEntry); // Save it to the text file
        }
    }

    public void restockProduct(int productId, int quantity) {
        Product product = findProductById(productId);
        if (product == null) {
            System.out.println(" Product not found!");
            return;
        }
        synchronized (product) {
            product.updateStock(quantity);
            System.out.println(" Product restocked! New quantity: " + product.getQuantity());
        }
    }

    public void showAllProducts() {
        if (inventoryMap.isEmpty()) {
            System.out.println(" No products in inventory.");
            return;
        }
        System.out.println(" CURRENT INVENTORY");
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%-6s %-20s %-11s %-10s %-14s %s\n",
                "ID", "Name", "Price (₹)", "Quantity", "Type", "Extra Info");
        System.out.println("-------------------------------------------------------------");
        inventoryMap.values().forEach(Product::displayInfo);
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total Products: " + inventoryMap.size());
    }

    public void checkLowStock() {
        System.out.println("\n Low Stock Alert!");
        long lowStockCount = inventoryMap.values().stream()
                .filter(p -> p.getQuantity() < 10)
                .peek(p -> System.out.println(" - " + p.getName() + " (" + p.getQuantity() + " left)"))
                .count();

        if (lowStockCount == 0) {
            System.out.println(" All products are well stocked!");
        }
    }

    public void generateReport() {
        int totalItems = inventoryMap.values().stream().mapToInt(Product::getQuantity).sum();
        double totalValue = inventoryMap.values().stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();

        System.out.println("\n DAILY INVENTORY REPORT");
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total Products in Stock: " + inventoryMap.size());
        System.out.println("Total Items Remaining: " + totalItems);
        System.out.printf("Total Stock Value: ₹ %,.2f\n", totalValue);
        System.out.printf("Total Sales Today: ₹ %,.2f\n", totalSales);

        StringBuilder report = new StringBuilder();
        report.append(" DAILY INVENTORY REPORT\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("=".repeat(60)).append("\n");
        report.append("Total Products in Stock: ").append(inventoryMap.size()).append("\n");
        report.append("Total Items Remaining: ").append(totalItems).append("\n");
        report.append(String.format("Total Stock Value: ₹ %,.2f\n", totalValue));
        report.append(String.format("Total Sales Today: ₹ %,.2f\n", totalSales));
        report.append("=".repeat(60)).append("\n\n");
        report.append("PRODUCT DETAILS:\n");
        report.append("-------------------------------------------------------------\n");
        inventoryMap.values().forEach(p -> {
            report.append(String.format("ID: %d | %s | Price: ₹%.1f | Qty: %d\n",
                    p.getId(), p.getName(), p.getPrice(), p.getQuantity()));
        });

        fileManager.saveReport(report.toString());
    }

    public void saveInventory() { fileManager.saveData(); }
    public void loadInventory() { fileManager.loadData(); }

    private Product findProductById(int id) { return inventoryMap.get(id); }

    // Helper for your Thread
    public List<Product> getProductList() {
        return new ArrayList<>(inventoryMap.values());
    }

    public void searchProducts(String keyword) {
        System.out.println("\n--- Search Results for: '" + keyword + "' ---");
        
        // Convert to lowercase so searching for "milk" finds "Milk"
        String searchTarget = keyword.toLowerCase();

        List<Product> results = inventoryMap.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(searchTarget) || 
                             String.valueOf(p.getId()).contains(searchTarget))
                .toList();

        if (results.isEmpty()) {
            System.out.println(" No products found matching that keyword.");
        } else {
            System.out.printf("%-6s %-20s %-11s %-10s %-14s %s\n",
                    "ID", "Name", "Price (₹)", "Quantity", "Type", "Extra Info");
            System.out.println("-------------------------------------------------------------");
            results.forEach(Product::displayInfo);
        }
    }
}