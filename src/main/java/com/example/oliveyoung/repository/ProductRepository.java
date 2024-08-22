package com.example.oliveyoung.repository;

import com.example.oliveyoung.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}