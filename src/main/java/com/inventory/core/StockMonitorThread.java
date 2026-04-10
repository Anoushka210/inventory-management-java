package com.inventory.core;

public class StockMonitorThread extends Thread {
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
                Thread.sleep(30000); 
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