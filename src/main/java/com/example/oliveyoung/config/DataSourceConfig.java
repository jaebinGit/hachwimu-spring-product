package com.example.oliveyoung.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.oliveyoung.repository")
public class DataSourceConfig {

    @Bean
    @Qualifier("readerDataSource")
    public DataSource readerDataSource() {
        // Aurora 리더 데이터소스 설정
        return DataSourceBuilder.create()
                .url("jdbc:mysql://aurora-reader-url:3306/hachwimu")
                .username("admin")
                .password("your-password")
                .build();
    }

    @Bean
    @Qualifier("writerDataSource")
    public DataSource writerDataSource() {
        // Aurora 라이터 데이터소스 설정
        return DataSourceBuilder.create()
                .url("jdbc:mysql://aurora-writer-url:3306/hachwimu")
                .username("admin")
                .password("your-password")
                .build();
    }

    @Bean
    @Qualifier("readerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean readerEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                             @Qualifier("readerDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .packages("com.example.oliveyoung.model")
                .persistenceUnit("reader")
                .build();
    }

    @Bean
    @Qualifier("writerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writerEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                             @Qualifier("writerDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
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