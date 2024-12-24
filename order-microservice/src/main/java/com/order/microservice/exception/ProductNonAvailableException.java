package com.order.microservice.exception;

public class ProductNonAvailableException extends RuntimeException {
    public ProductNonAvailableException(String message) {
        super(message);
    }
}
