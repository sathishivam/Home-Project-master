package com.order.microservice.client;

import com.order.microservice.Entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-microservice", url = "http://localhost:8500/products")
public interface ProductClient {

    @GetMapping(value = "/getProduct", produces = "application/json")
    ResponseEntity<Product> getProductById(@RequestParam("id") String id);

    @PutMapping(value = "/updateProduct", produces = "application/json")
    ResponseEntity<String> updateProduct(@RequestParam("id") String id,
                                         @RequestParam("quantity") Integer quantity);
}
