package com.product.microservice.controller;

import com.product.microservice.Entity.Product;
import com.product.microservice.exception.ProductNotFoundException;
import com.product.microservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController
{

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create New Product")
    @PostMapping(value = "saveProduct", consumes = "application/json")
    public ResponseEntity<String > saveProduct(@RequestBody Product product)
    {
        try
        {
            productService.saveProduct(product);
            return new ResponseEntity<>("Product added Successfully", HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("Unable to add the item",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get Products By ID")
    @GetMapping(value = "/getProduct", produces = {"application/json"})
    public ResponseEntity<Product> getProductById(@RequestParam("id") String id) throws Exception {
        Product product = productService.getProducts(id);
        if(product.getId()==null){
            throw new ProductNotFoundException("Product not found with this ID :" + id);
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }


    @Operation(summary = "GET All Product")
    @GetMapping(value = "/getProducts")
    public ResponseEntity<List<Product>> getProducts()
    {
        try
        {
            return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update a product's quantity")
    @PutMapping(value = "/updateProduct")
    public ResponseEntity<String> updateProduct(
            @RequestParam("id") String id,
            @RequestParam("quantity") Integer quantity
    )
    {
        try
        {
            productService.updateProduct(id,quantity);
            return new ResponseEntity<>("Successfully Update with ID:" + id,HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
