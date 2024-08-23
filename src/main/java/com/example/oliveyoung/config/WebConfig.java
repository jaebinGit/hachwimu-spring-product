package com.example.oliveyoung.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 API 경로에 대한 CORS 설정
                .allowedOrigins(
                        "https://www.hachwimu.com",   // 프론트엔드 배포 도메인
                        "http://www.hachwimu.com",    // HTTP 프론트엔드 도메인
                        "https://api.hachwimu.com",   // API 서버 도메인
                        "http://api.hachwimu.com"     // HTTP로 API 요청
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);  // 자격 증명을 허용 (쿠키, 인증 헤더 전송)

        // 헬스 체크 경로에 대한 CORS 허용 - 구체적인 도메인으로 설정
        registry.addMapping("/health")  // 헬스 체크 경로
                .allowedOrigins(
                        "https://www.hachwimu.com",
                        "http://www.hachwimu.com",
                        "https://api.hachwimu.com",
                        "http://api.hachwimu.com"
                )
                .allowedMethods("GET")  // GET 요청만 허용
                .allowedHeaders("*");
    }
}