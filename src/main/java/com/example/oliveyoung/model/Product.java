package com.example.oliveyoung.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price")
    private double price;

    @Column(name = "brand")
    private String brand;

    @Column(name = "is_best")
    private boolean isBest;

    @Column(name = "delivery_info")
    private String deliveryInfo;

    @Column(name = "sale_status")
    private boolean saleStatus;

    @Column(name = "coupon_status")
    private boolean couponStatus;

    @Column(name = "gift_status")
    private boolean giftStatus;

    @Column(name = "today_dream_status")
    private boolean todayDreamStatus;

    @Column(name = "stock")
    private int stock;

    @Column(name = "discount_price")
    private double discountPrice;

    @Column(name = "other_discount")
    private boolean otherDiscount;

    public void purchase() {
        if (this.stock > 0) {
            this.stock--;
        } else {
            throw new RuntimeException("Product is out of stock!");
        }
    }
}