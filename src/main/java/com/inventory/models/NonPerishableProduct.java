package com.inventory.models;

public class NonPerishableProduct extends Product {
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