package com.example.oliveyoung.controller;

import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> purchase(@PathVariable Long id) {
        try {
            productService.purchase(id);  // 쓰기 작업은 라이터에서 처리됨
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // 성공적으로 처리됨
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 상품을 찾을 수 없음
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 서버 오류 발생
        }
    }

    // 모든 상품 정보 조회
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();  // 읽기 작업은 리더에서 처리됨
            return ResponseEntity.ok(products);  // 성공적으로 조회됨
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 서버 오류 발생
        }
    }

    // 특정 상품 조회
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean useCache) {
        try {
            Product product = productService.getProductById(id, useCache);  // 캐시 사용 여부에 따라 조회
            if (product != null) {
                return ResponseEntity.ok(product);  // 성공적으로 조회됨
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 상품을 찾을 수 없음
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 상품을 찾을 수 없음
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 서버 오류 발생
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Void> clearAllProductsCache() {
        productService.clearAllProductsCache();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}