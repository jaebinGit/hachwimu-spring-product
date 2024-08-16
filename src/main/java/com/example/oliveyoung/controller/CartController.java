package com.example.oliveyoung.controller;

import com.example.oliveyoung.model.Cart;
import com.example.oliveyoung.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestBody Cart cart) {
        return cartService.addToCart(cart);
    }

    @PostMapping("/purchase/{id}")
    public void purchase(@PathVariable Long id) {
        cartService.purchase(id);
    }
}