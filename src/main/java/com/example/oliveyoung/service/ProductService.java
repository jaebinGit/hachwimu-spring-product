package com.example.oliveyoung.service;

import com.example.oliveyoung.config.DataSourceContextHolder;
import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    // 상품 등록 처리 (쓰기 작업)
    @Transactional
    public Product createProduct(Product product) {
        try {
            // 쓰기 작업이므로 writer 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("writer");

            // 데이터베이스에 상품 정보 저장 (쓰기 작업)
            Product savedProduct = productRepository.save(product);

            // 전체 목록 캐시 무효화
            redisTemplate.delete("products:all");

            // 개별 상품 캐시에 저장 (TTL 1시간 설정)
            cacheProduct("product:" + savedProduct.getId(), savedProduct, 1, TimeUnit.HOURS);

            return savedProduct;
        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    // 상품 구매 처리 (쓰기 작업)
    @Transactional
    public void purchase(Long id) {
        try {
            // 쓰기 작업이므로 writer 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("writer");

            // 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 상품 구매 처리 (재고 감소)
            product.purchase();

            // 데이터베이스에 상품 정보 저장 (쓰기 작업)
            productRepository.save(product);

        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    // 모든 상품 정보 조회 (읽기 작업)
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        String cacheKey = "products:all";
        try {
            // 읽기 작업이므로 reader 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("reader");

//             캐시에서 모든 상품 조회 시도
//            List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);

//            if (cachedProducts != null && !cachedProducts.isEmpty()) {
//                return cachedProducts;  // 캐시된 데이터가 존재하면 반환
//            }

            // 캐시에 없을 경우, 데이터베이스에서 모든 상품 조회
            List<Product> products = productRepository.findAll();

//            // 조회된 상품 목록을 캐시에 저장 (TTL 1시간 설정)
//            redisTemplate.opsForValue().set(cacheKey, products, 1, TimeUnit.HOURS);

            return products;
        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    // 특정 상품 조회 (캐시 -> Aurora 리더)
    @Transactional(readOnly = true)
    public Product getProductById(Long id, @RequestParam(defaultValue = "true") boolean useCache) {
        String cacheKey = "product:" + id;
        try {
            // 읽기 작업이므로 reader 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("reader");

            // 캐시를 사용하는 경우
            if (useCache) {
                // 캐시에서 상품 조회 시도
                Product cachedProduct = getCachedProduct(cacheKey);

                if (cachedProduct != null) {
                    return cachedProduct;  // 캐시된 상품이 있으면 반환
                }
            }

            // 캐시에 없거나 캐시를 사용하지 않는 경우, 데이터베이스에서 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 캐시 사용이 true일 경우, 조회된 상품을 캐시에 저장 (TTL 1시간 설정)
            if (useCache) {
                cacheProduct(cacheKey, product, 1, TimeUnit.HOURS);
            }

            return product;
        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    // 상품 삭제 처리 (쓰기 작업)
    @Transactional
    public void deleteProduct(Long id) {
        try {
            // 쓰기 작업이므로 writer 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("writer");

            // 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 상품 삭제 처리
            productRepository.delete(product);

            // 삭제된 상품의 캐시 무효화
            redisTemplate.delete("product:" + id);
            redisTemplate.delete("products:all");
        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }


    // 상품 업데이트 처리 (쓰기 작업)
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        try {
            // 쓰기 작업이므로 writer 데이터 소스를 설정
            DataSourceContextHolder.setDataSourceType("writer");

            // 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 상품 정보 업데이트
            product.setName(updatedProduct.getName());
            product.setImageUrl(updatedProduct.getImageUrl());
            product.setPrice(updatedProduct.getPrice());
            product.setBrand(updatedProduct.getBrand());
            product.setBest(updatedProduct.isBest());
            product.setDeliveryInfo(updatedProduct.getDeliveryInfo());
            product.setSaleStatus(updatedProduct.isSaleStatus());
            product.setCouponStatus(updatedProduct.isCouponStatus());
            product.setGiftStatus(updatedProduct.isGiftStatus());
            product.setTodayDreamStatus(updatedProduct.isTodayDreamStatus());
            product.setStock(updatedProduct.getStock());
            product.setDiscountPrice(updatedProduct.getDiscountPrice());
            product.setOtherDiscount(updatedProduct.isOtherDiscount());

            // 데이터베이스에 업데이트된 상품 정보 저장
            productRepository.save(product);

            // 업데이트 후 해당 상품과 전체 목록 캐시 무효화
            redisTemplate.delete("product:" + id);
            redisTemplate.delete("products:all");

            return product;
        } finally {
            // 작업 완료 후 데이터 소스 컨텍스트 초기화
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    // 캐시에서 상품 조회 메서드
    private Product getCachedProduct(String cacheKey) {
        Map<Object, Object> productMap = redisTemplate.opsForHash().entries(cacheKey);
        if (productMap == null || productMap.isEmpty()) {
            return null;
        }

        Product product = new Product();
        product.setId(Long.valueOf((String) productMap.get("id")));
        product.setName((String) productMap.get("name"));
        product.setImageUrl((String) productMap.get("imageUrl"));
        product.setPrice(Double.valueOf((String) productMap.get("price")));
        product.setBrand((String) productMap.get("brand"));
        product.setBest(Boolean.parseBoolean((String) productMap.get("isBest")));
        product.setDeliveryInfo((String) productMap.get("deliveryInfo"));
        product.setSaleStatus(Boolean.parseBoolean((String) productMap.get("saleStatus")));
        product.setCouponStatus(Boolean.parseBoolean((String) productMap.get("couponStatus")));
        product.setGiftStatus(Boolean.parseBoolean((String) productMap.get("giftStatus")));
        product.setTodayDreamStatus(Boolean.parseBoolean((String) productMap.get("todayDreamStatus")));
        product.setDiscountPrice(Double.parseDouble((String) productMap.get("discountPrice")));
        product.setOtherDiscount(Boolean.parseBoolean((String) productMap.get("otherDiscount")));

        return product;
    }

    // 캐시에 상품 저장 메서드
    private void cacheProduct(String cacheKey, Product product, long timeout, TimeUnit unit) {
        redisTemplate.opsForHash().put(cacheKey, "id", String.valueOf(product.getId()));
        redisTemplate.opsForHash().put(cacheKey, "name", product.getName());
        redisTemplate.opsForHash().put(cacheKey, "imageUrl", product.getImageUrl());
        redisTemplate.opsForHash().put(cacheKey, "price", String.valueOf(product.getPrice()));
        redisTemplate.opsForHash().put(cacheKey, "brand", product.getBrand());
        redisTemplate.opsForHash().put(cacheKey, "isBest", String.valueOf(product.isBest()));
        redisTemplate.opsForHash().put(cacheKey, "deliveryInfo", product.getDeliveryInfo());
        redisTemplate.opsForHash().put(cacheKey, "saleStatus", String.valueOf(product.isSaleStatus()));
        redisTemplate.opsForHash().put(cacheKey, "couponStatus", String.valueOf(product.isCouponStatus()));
        redisTemplate.opsForHash().put(cacheKey, "giftStatus", String.valueOf(product.isGiftStatus()));
        redisTemplate.opsForHash().put(cacheKey, "todayDreamStatus", String.valueOf(product.isTodayDreamStatus()));
        redisTemplate.opsForHash().put(cacheKey, "discountPrice", String.valueOf(product.getDiscountPrice()));
        redisTemplate.opsForHash().put(cacheKey, "otherDiscount", String.valueOf(product.isOtherDiscount()));
        redisTemplate.expire(cacheKey, timeout, unit);
    }

    public void clearAllProductsCache() {
        redisTemplate.delete("products:all");
    }
}