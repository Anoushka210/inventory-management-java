package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PerishableProduct extends Product {
    private LocalDate expiryDate; // Upgraded to LocalDate

    public PerishableProduct(int id, String name, double price, int quantity, String expiryDateStr) {
        super(id, name, price, quantity);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            this.expiryDate = LocalDate.parse(expiryDateStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println(" Invalid date format. Storing as current date.");
            this.expiryDate = LocalDate.now();
        }
    }

    @Override
    public void displayInfo() {
        System.out.printf("%-6d %-20s %-11.1f %-10d %-14s Expiry: %s\n",
                id, name, price, quantity, "Perishable", expiryDate.toString());
    }

    public LocalDate getExpiryDate() { return expiryDate; }
}