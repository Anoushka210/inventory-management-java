package com.inventory.exceptions;

public class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
        super(message);
    }
}