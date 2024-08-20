package com.example.oliveyoung.service;

import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    // 상품 구매 처리 (쓰기 작업은 라이터에서 처리)
    @Transactional(transactionManager = "writerTransactionManager")
    public void purchase(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
        product.purchase();
        productRepository.save(product);

        // 구매 후 Redis 캐시 무효화
        redisTemplate.delete("product:" + id);
    }

    // 모든 상품 조회 (읽기 작업은 리더에서 처리)
    @Transactional(transactionManager = "readerTransactionManager", readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 특정 상품 조회 (캐시 -> Aurora 리더)
    @Transactional(transactionManager = "readerTransactionManager", readOnly = true)
    public Product getProductById(Long id) {
        String cacheKey = "product:" + id;

        // 캐시에서 먼저 조회
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        // 캐시에 없으면 DB 리더 인스턴스에서 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

        // 조회된 상품을 Redis에 캐시하고 1시간 TTL 설정
        redisTemplate.opsForValue().set(cacheKey, product, 1, TimeUnit.HOURS);
        return product;
    }
}