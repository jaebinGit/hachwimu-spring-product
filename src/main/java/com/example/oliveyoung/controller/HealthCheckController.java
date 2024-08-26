package com.example.oliveyoung.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/products")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    // /products/health 요청을 처리하는 메서드
    @GetMapping("/health")
    public String productsHealthCheck() {
        logger.info("Products에서 health check를 받았습니다.");
        return "Products service is healthy!";
    }
}
