package com.example.oliveyoung.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageUrl;
    private double price;
    private String brand;
    private boolean isBest;
    private String deliveryInfo;
    private boolean saleStatus;
    private boolean couponStatus;
    private boolean giftStatus;
    private boolean todayDreamStatus;
    private int stock;
    private double discountPrice;
    private boolean otherDiscount;

    public void purchase() {
        if (this.stock > 0) {
            this.stock--;
        } else {
            throw new RuntimeException("Product is out of stock!");
        }
    }
}