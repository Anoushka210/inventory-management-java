package com.inventory.controller;

import com.inventory.core.InventoryManager;
import com.inventory.exceptions.OutOfStockException;
import com.inventory.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products") // All URLs in this file will start with /api/products
@CrossOrigin(origins = "*")      //  allows frontend HTML to talk to this backend
public class ProductController {

    private final InventoryManager manager;

    @Autowired
    public ProductController(InventoryManager manager) {
        this.manager = manager;
    }

    // 1. GET ALL PRODUCTS
    // Test by visiting: http://localhost:8080/api/products
    @GetMapping
    public List<Product> getAllProducts() {
        // Spring Boot will automatically convert this Java List into JSON!
        return manager.getProductList(); 
    }

    // 2. SEARCH PRODUCTS
    // Test by visiting: http://localhost:8080/api/products/search?keyword=milk
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        // We need a slight tweak here since your original search just printed to the console.
        // For now, let's filter and return it!
        return manager.getProductList().stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                             String.valueOf(p.getId()).contains(keyword))
                .toList();
    }

    // 3. SELL PRODUCT
    // This uses POST because it modifies data on the server
    @PostMapping("/sell")
    public ResponseEntity<String> sellProduct(@RequestParam int id, @RequestParam int quantity) {
        try {
            manager.sellProduct(id, quantity);
            return ResponseEntity.ok("Sale successful!");
        } catch (OutOfStockException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}