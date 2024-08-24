package com.example.oliveyoung.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.oliveyoung.repository",
        entityManagerFactoryRef = "routingEntityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class DataSourceConfig {

    @Value("${spring.datasource.reader.url}")
    private String readerDbUrl;

    @Value("${spring.datasource.writer.url}")
    private String writerDbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public DataSource readerDataSource() {
        return DataSourceBuilder.create()
                .url(readerDbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Bean
    public DataSource writerDataSource() {
        return DataSourceBuilder.create()
                .url(writerDbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("reader", readerDataSource());
        dataSourceMap.put("writer", writerDataSource());
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writerDataSource());
        return routingDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean routingEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(routingDataSource())
                .packages("com.example.oliveyoung.model")
                .persistenceUnit("default")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("routingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}