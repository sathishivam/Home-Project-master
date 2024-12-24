package com.order.microservice.controller;

import com.order.microservice.Entity.Order;
import com.order.microservice.exception.OrderNotFoundException;
import com.order.microservice.exception.ProductNonAvailableException;
import com.order.microservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController
{

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Operation(summary = "Place New Order")
    @PostMapping(value = "/newOrder", consumes = "application/json")
    @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackProductValidation")
    public ResponseEntity<Object> placeOrder(@RequestBody Order order) {
        logger.info("Request received for new order");
        String orderId = orderService.placeOrder(order);
        return new ResponseEntity<>("Order Placed Successfully with OrderID" + orderId, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> fallbackProductValidation(Exception e) {
        if(e instanceof ProductNonAvailableException){
            throw new ProductNonAvailableException("Product not available or insufficient quantity.");
        }
        // Fallback logic when Product Service is not available
        return new ResponseEntity<>("Product Service is unavailable. Please try again later.",HttpStatus.FORBIDDEN);
    }

    @Operation(summary = "Get Order By ID")
    @GetMapping(value = "/getOrder", produces = {"application/json"})
    public ResponseEntity<Order> getOrderByID(@RequestParam("id") String id) throws Exception {
        Order product = orderService.getOrders(id);
        if(product.getId()==null){
            throw new OrderNotFoundException("Product not found with this ID :" + id);
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }


    @Operation(summary = "Get All Orders")
    @GetMapping(value = "/getOrders")
    public ResponseEntity<List<Order>> getAllOrders()
    {
        try
        {
            return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
