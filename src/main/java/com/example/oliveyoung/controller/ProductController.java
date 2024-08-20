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

    // 상품 구매
    @PostMapping("/purchase/{id}")
    public void purchase(@PathVariable Long id) {
        productService.purchase(id);  // 쓰기 작업은 라이터에서 처리됨
    }

    // 모든 상품 정보 조회
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();  // 읽기 작업은 리더에서 처리됨
    }

    // 특정 상품 조회
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);  // 캐시를 먼저 조회하고, 없으면 리더로 조회
    }
}