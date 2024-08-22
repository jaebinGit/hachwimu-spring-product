package com.example.oliveyoung;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariablesLogger implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariablesLogger.class);

    @Value("${spring.datasource.writer.url}")
    private String writerUrl;

    @Value("${spring.datasource.reader.url}")
    private String readerUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Environment Variables:");
        logger.info("Writer URL: {}", writerUrl);
        logger.info("Reader URL: {}", readerUrl);
        logger.info("DataSource Username: {}", username);
        logger.info("DataSource Password: {}", password);
        logger.info("Redis Host: {}", redisHost);
        logger.info("Redis Port: {}", redisPort);
    }
}