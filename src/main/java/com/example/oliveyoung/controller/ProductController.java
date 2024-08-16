package com.example.oliveyoung.controller;

import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/purchase/{id}")
    public void purchase(@PathVariable Long id) {
        productService.purchase(id);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
}