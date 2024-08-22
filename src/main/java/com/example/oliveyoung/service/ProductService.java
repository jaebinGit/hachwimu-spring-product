//package com.example.oliveyoung.service;
//
//import com.example.oliveyoung.model.Product;
//import com.example.oliveyoung.repository.ProductRepository;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class ProductService {
//    private final ProductRepository productRepository;
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
//        this.productRepository = productRepository;
//        this.redisTemplate = redisTemplate;
//    }
//
//    // 상품 구매 처리 (쓰기 작업은 라이터에서 처리)
//    @Transactional(transactionManager = "writerTransactionManager")
//    public void purchase(Long id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
//        product.purchase();
//        productRepository.save(product);
//
//        // 구매 후 Redis 캐시 무효화
//        redisTemplate.delete("product:" + id);
//    }
//
//    // 모든 상품 조회 (읽기 작업은 리더에서 처리)
//    @Transactional(transactionManager = "readerTransactionManager", readOnly = true)
//    public List<Product> getAllProducts() {
//        return productRepository.findAll();
//    }
//
//    // 특정 상품 조회 (캐시 -> Aurora 리더)
//    @Transactional(transactionManager = "readerTransactionManager", readOnly = true)
//    public Product getProductById(Long id) {
//        String cacheKey = "product:" + id;
//
//        // Redis Hash에서 먼저 조회
//        Product cachedProduct = getCachedProduct(cacheKey);
//        if (cachedProduct != null) {
//            return cachedProduct;
//        }
//
//        // 캐시에 없으면 DB 리더 인스턴스에서 조회
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
//
//        // 조회된 상품을 Redis Hash에 캐시하고 1시간 TTL 설정
//        cacheProduct(cacheKey, product, 1, TimeUnit.HOURS);
//        return product;
//    }
//
//    private Product getCachedProduct(String cacheKey) {
//        Map<Object, Object> productMap = redisTemplate.opsForHash().entries(cacheKey);
//        if (productMap == null || productMap.isEmpty()) {
//            return null;
//        }
//
//        Product product = new Product();
//        product.setId(Long.valueOf((String) productMap.get("id")));
//        product.setName((String) productMap.get("name"));
//        product.setImageUrl((String) productMap.get("imageUrl"));
//        product.setPrice(Double.valueOf((String) productMap.get("price")));
//        product.setBrand((String) productMap.get("brand"));
//        product.setBest(Boolean.parseBoolean((String) productMap.get("isBest")));
//        product.setDeliveryInfo((String) productMap.get("deliveryInfo"));
//        product.setSaleStatus(Boolean.parseBoolean((String) productMap.get("saleStatus")));
//        product.setCouponStatus(Boolean.parseBoolean((String) productMap.get("couponStatus")));
//        product.setGiftStatus(Boolean.parseBoolean((String) productMap.get("giftStatus")));
//        product.setTodayDreamStatus(Boolean.parseBoolean((String) productMap.get("todayDreamStatus")));
//        product.setStock(Integer.parseInt((String) productMap.get("stock")));
//        product.setDiscountPrice(Double.parseDouble((String) productMap.get("discountPrice")));
//        product.setOtherDiscount(Boolean.parseBoolean((String) productMap.get("otherDiscount")));
//
//        return product;
//    }
//
//    private void cacheProduct(String cacheKey, Product product, long timeout, TimeUnit unit) {
//        redisTemplate.opsForHash().put(cacheKey, "id", String.valueOf(product.getId()));
//        redisTemplate.opsForHash().put(cacheKey, "name", product.getName());
//        redisTemplate.opsForHash().put(cacheKey, "imageUrl", product.getImageUrl());
//        redisTemplate.opsForHash().put(cacheKey, "price", String.valueOf(product.getPrice()));
//        redisTemplate.opsForHash().put(cacheKey, "brand", product.getBrand());
//        redisTemplate.opsForHash().put(cacheKey, "isBest", String.valueOf(product.isBest()));
//        redisTemplate.opsForHash().put(cacheKey, "deliveryInfo", product.getDeliveryInfo());
//        redisTemplate.opsForHash().put(cacheKey, "saleStatus", String.valueOf(product.isSaleStatus()));
//        redisTemplate.opsForHash().put(cacheKey, "couponStatus", String.valueOf(product.isCouponStatus()));
//        redisTemplate.opsForHash().put(cacheKey, "giftStatus", String.valueOf(product.isGiftStatus()));
//        redisTemplate.opsForHash().put(cacheKey, "todayDreamStatus", String.valueOf(product.isTodayDreamStatus()));
//        redisTemplate.opsForHash().put(cacheKey, "stock", String.valueOf(product.getStock()));
//        redisTemplate.opsForHash().put(cacheKey, "discountPrice", String.valueOf(product.getDiscountPrice()));
//        redisTemplate.opsForHash().put(cacheKey, "otherDiscount", String.valueOf(product.isOtherDiscount()));
//        redisTemplate.expire(cacheKey, timeout, unit);
//    }
//
//    // Backoffice에서 상품 업데이트 (쓰기 작업은 라이터에서 처리)
//    @Transactional(transactionManager = "writerTransactionManager")
//    public Product updateProduct(Long id, Product updatedProduct) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
//
//        product.setName(updatedProduct.getName());
//        product.setImageUrl(updatedProduct.getImageUrl());
//        product.setPrice(updatedProduct.getPrice());
//        product.setBrand(updatedProduct.getBrand());
//        product.setBest(updatedProduct.isBest());
//        product.setDeliveryInfo(updatedProduct.getDeliveryInfo());
//        product.setSaleStatus(updatedProduct.isSaleStatus());
//        product.setCouponStatus(updatedProduct.isCouponStatus());
//        product.setGiftStatus(updatedProduct.isGiftStatus());
//        product.setTodayDreamStatus(updatedProduct.isTodayDreamStatus());
//        product.setStock(updatedProduct.getStock());
//        product.setDiscountPrice(updatedProduct.getDiscountPrice());
//        product.setOtherDiscount(updatedProduct.isOtherDiscount());
//
//        productRepository.save(product);
//
//        // 업데이트 후 Redis 캐시 무효화
//        redisTemplate.delete("product:" + id);
//
//        return product;
//    }
//}