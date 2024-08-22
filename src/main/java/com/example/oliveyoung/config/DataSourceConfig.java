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

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.oliveyoung.repository",
        entityManagerFactoryRef = "readerEntityManagerFactory",
        transactionManagerRef = "readerTransactionManager"
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
    @Qualifier("readerDataSource")
    public DataSource readerDataSource() {
        return DataSourceBuilder.create()
                .url(readerDbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Bean
    @Qualifier("writerDataSource")
    public DataSource writerDataSource() {
        return DataSourceBuilder.create()
                .url(writerDbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    @Bean
    @Qualifier("readerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean readerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("readerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.oliveyoung.model")
                .persistenceUnit("reader")
                .build();
    }

    @Bean
    @Qualifier("writerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("writerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.oliveyoung.model")
                .persistenceUnit("writer")
                .build();
    }

    @Bean
    @Qualifier("readerTransactionManager")
    public PlatformTransactionManager readerTransactionManager(
            @Qualifier("readerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Qualifier("writerTransactionManager")
    public PlatformTransactionManager writerTransactionManager(
            @Qualifier("writerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}