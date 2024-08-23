package com.example.oliveyoung.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // API 경로를 지정
                .allowedOrigins("https://www.hachwimu.com", "http://www.hachwimu.com:3000", "http://hachwimu.com")  // React 애플리케이션이 배포된 도메인 (프론트엔드 도메인)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // allowCredentials가 true일 경우, '*' 대신 구체적인 도메인을 지정해야 함
    }
}