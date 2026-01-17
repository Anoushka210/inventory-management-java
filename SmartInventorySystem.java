// ==================== CUSTOM EXCEPTION ====================
class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
        super(message);
    }
}

// ==================== PRODUCT BASE CLASS ====================
abstract class Product implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    protected double price;
    protected int quantity;

    public Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public abstract void displayInfo();

    public void updateStock(int amount) {
        this.quantity += amount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// ==================== PERISHABLE PRODUCT ====================
class PerishableProduct extends Product {
    private String expiryDate;

    public PerishableProduct(int id, String name, double price, int quantity, String expiryDate) {
        super(id, name, price, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public void displayInfo() {
        System.out.printf("%-6d %-20s %-11.1f %-10d %-14s Expiry: %s\n",
                id, name, price, quantity, "Perishable", expiryDate);
    }

    public String getExpiryDate() { return expiryDate; }
}

// ==================== NON-PERISHABLE PRODUCT ====================
class NonPerishableProduct extends Product {
    private String warranty;

    public NonPerishableProduct(int id, String name, double price, int quantity, String warranty) {
        super(id, name, price, quantity);
        this.warranty = warranty;
    }

    @Override
    public void displayInfo() {
        System.out.printf("%-6d %-20s %-11.1f %-10d %-14s Warranty: %s\n",
                id, name, price, quantity, "Non-Perishable", warranty);
    }

    public String getWarranty() { return warranty; }
}

// ==================== FILE HANDLER INTERFACE ====================
interface FileHandler {
    void saveData();
    void loadData();
}

// ==================== FILE MANAGER ====================
class FileManager implements FileHandler {
    private java.util.List<Product> products;
    private static final String DATA_FILE = "inventory.dat";
    private static final String REPORT_FILE = "report.txt";

    public FileManager(java.util.List<Product> products) {
        this.products = products;
    }

    @Override
    public void saveData() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(DATA_FILE))) {
            oos.writeObject(products);
            System.out.println(" Inventory saved successfully!");
        } catch (java.io.IOException e) {
            System.out.println(" Error saving data: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(DATA_FILE))) {
            java.util.List<Product> loadedProducts = (java.util.List<Product>) ois.readObject();
            products.clear();
            products.addAll(loadedProducts);
            System.out.println(" Inventory loaded successfully!");
        } catch (java.io.FileNotFoundException e) {
            System.out.println(" No previous data found. Starting with sample data.");
        } catch (java.io.IOException | ClassNotFoundException e) {
            System.out.println(" Error loading data: " + e.getMessage());
        }
    }

    public void saveReport(String content) {
        try (java.io.FileWriter writer = new java.io.FileWriter(REPORT_FILE)) {
            writer.write(content);
            System.out.println("Report generated and saved to '" + REPORT_FILE + "'");
        } catch (java.io.IOException e) {
            System.out.println(" Error saving report: " + e.getMessage());
        }
    }
}

// ==================== INVENTORY MANAGER ====================
class InventoryManager {
    private java.util.List<Product> productList;
    private double totalSales;
    private FileManager fileManager;

    public InventoryManager() {
        this.productList = new java.util.ArrayList<>();
        this.totalSales = 0.0;
        this.fileManager = new FileManager(productList);
    }

    public void loadInventoryOrInitialize() {
        fileManager.loadData();
        
        // If no data loaded, add sample data
        if (productList.isEmpty()) {
            addProduct(new PerishableProduct(101, "Milk", 45.0, 15, "20-10-2025"));
            addProduct(new NonPerishableProduct(102, "Sugar", 55.0, 4, "N/A"));
            addProduct(new NonPerishableProduct(103, "Tea Leaves", 120.0, 2, "N/A"));
            addProduct(new PerishableProduct(104, "Yogurt", 35.0, 25, "30-10-2025"));
            System.out.println(" Sample data loaded successfully!");
        }
    }

    public void addProduct(Product product) {
        productList.add(product);
    }

    public void addProductWithMessage(Product product) {
        productList.add(product);
        System.out.println(" Product added successfully!");
    }

    public void sellProduct(int productId, int quantity) throws OutOfStockException {
        Product product = findProductById(productId);
        if (product == null) {
            throw new OutOfStockException(" Product not found!");
        }
        if (product.getQuantity() < quantity) {
            throw new OutOfStockException(" Insufficient stock! Available: " + product.getQuantity());
        }
        product.updateStock(-quantity);
        double saleAmount = quantity * product.getPrice();
        totalSales += saleAmount;
        System.out.println(" Sale successful! Remaining quantity: " + product.getQuantity());
    }

    public void restockProduct(int productId, int quantity) {
        Product product = findProductById(productId);
        if (product == null) {
            System.out.println(" Product not found!");
            return;
        }
        product.updateStock(quantity);
        System.out.println(" Product restocked! New quantity: " + product.getQuantity());
    }

    public void showAllProducts() {
        if (productList.isEmpty()) {
            System.out.println(" No products in inventory.");
            return;
        }

        System.out.println(" CURRENT INVENTORY");
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%-6s %-20s %-11s %-10s %-14s %s\n",
                "ID", "Name", "Price (₹)", "Quantity", "Type", "Extra Info");
        System.out.println("-------------------------------------------------------------");

        productList.forEach(Product::displayInfo);
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total Products: " + productList.size());
    }

    public void checkLowStock() {
        System.out.println("\n Low Stock Alert!");
        long lowStockCount = productList.stream()
                .filter(p -> p.getQuantity() < 10)
                .peek(p -> System.out.println(" - " + p.getName() + " (" + p.getQuantity() + " left)"))
                .count();

        if (lowStockCount == 0) {
            System.out.println(" All products are well stocked!");
        }
    }

    public void generateReport() {
        // Calculate total items remaining
        int totalItems = productList.stream()
                .mapToInt(Product::getQuantity)
                .sum();

        // Calculate total stock value
        double totalValue = productList.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();

        // Print to console
        System.out.println("\n DAILY INVENTORY REPORT");
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total Products in Stock: " + productList.size());
        System.out.println("Total Items Remaining: " + totalItems);
        System.out.printf("Total Stock Value: ₹ %,.2f\n", totalValue);
        System.out.printf("Total Sales Today: ₹ %,.2f\n", totalSales);

        // Save to file
        StringBuilder report = new StringBuilder();
        report.append(" DAILY INVENTORY REPORT\n");
        report.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("=".repeat(60)).append("\n");
        report.append("Total Products in Stock: ").append(productList.size()).append("\n");
        report.append("Total Items Remaining: ").append(totalItems).append("\n");
        report.append(String.format("Total Stock Value: ₹ %,.2f\n", totalValue));
        report.append(String.format("Total Sales Today: ₹ %,.2f\n", totalSales));
        report.append("=".repeat(60)).append("\n\n");
        report.append("PRODUCT DETAILS:\n");
        report.append("-------------------------------------------------------------\n");
        productList.forEach(p -> {
            report.append(String.format("ID: %d | %s | Price: ₹%.1f | Qty: %d\n",
                    p.getId(), p.getName(), p.getPrice(), p.getQuantity()));
        });

        fileManager.saveReport(report.toString());
    }

    public void saveInventory() {
        fileManager.saveData();
    }

    public void loadInventory() {
        fileManager.loadData();
    }

    private Product findProductById(int id) {
        return productList.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public java.util.List<Product> getProductList() {
        return productList;
    }
}

// ==================== STOCK MONITOR THREAD ====================
class StockMonitorThread extends Thread {
    private InventoryManager inventory;
    private volatile boolean running = true;

    public StockMonitorThread(InventoryManager inventory) {
        this.inventory = inventory;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(30000); // Check every 30 seconds
                long lowStockCount = inventory.getProductList().stream()
                        .filter(p -> p.getQuantity() < 10)
                        .count();

                if (lowStockCount > 0) {
                    System.out.println("\n[Background Alert] " + lowStockCount +
                                     " product(s) running low on stock!");
                }
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stopMonitoring() {
        running = false;
        interrupt();
    }
}

// ==================== MAIN APPLICATION ====================
public class SmartInventorySystem {
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        // Load existing inventory or initialize with sample data
        manager.loadInventoryOrInitialize();

        // Start background stock monitor
        StockMonitorThread monitor = new StockMonitorThread(manager);
        monitor.start();

        boolean exit = false;

        while (!exit) {
            System.out.println("----------------------------------------");
            System.out.println("What would you like to do next?");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Sell Product");
            System.out.println("4. Restock Product");
            System.out.println("5. View Low Stock Items");
            System.out.println("6. Generate Report");
            System.out.println("7. Save & Exit");
            System.out.println("----------------------------------------");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addProductMenu(scanner, manager);
                        break;
                    case 2:
                        manager.showAllProducts();
                        break;
                    case 3:
                        sellProductMenu(scanner, manager);
                        break;
                    case 4:
                        restockProductMenu(scanner, manager);
                        break;
                    case 5:
                        manager.checkLowStock();
                        break;
                    case 6:
                        manager.generateReport();
                        break;
                    case 7:
                        manager.saveInventory();
                        monitor.stopMonitoring();
                        exit = true;
                        System.out.println(" Thank you for using Smart Inventory System!");
                        break;
                    default:
                        System.out.println(" Invalid choice! Please try again.");
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println(" Invalid input! Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }

        scanner.close();
    }

    private static void addProductMenu(java.util.Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Price: ");
            String priceInput = scanner.nextLine().trim();
            // Remove ₹ symbol and any commas
            priceInput = priceInput.replace("₹", "").replace(",", "").trim();
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
        } catch (java.util.InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }

    private static void sellProductMenu(java.util.Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            System.out.print("Enter Quantity to sell: ");
            int quantity = scanner.nextInt();
            manager.sellProduct(id, quantity);
        } catch (OutOfStockException e) {
            System.out.println(e.getMessage());
        } catch (java.util.InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }

    private static void restockProductMenu(java.util.Scanner scanner, InventoryManager manager) {
        try {
            System.out.print("Enter Product ID: ");
            int id = scanner.nextInt();
            System.out.print("Enter Quantity to restock: ");
            int quantity = scanner.nextInt();
            manager.restockProduct(id, quantity);
        } catch (java.util.InputMismatchException e) {
            System.out.println(" Invalid input format!");
            scanner.nextLine();
        }
    }
}