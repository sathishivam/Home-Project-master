package com.order.microservice.exception;

public class ServiceNotAvailableException extends RuntimeException {
    public ServiceNotAvailableException(String message) {
        super(message);
    }
}
