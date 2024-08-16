package com.example.oliveyoung.service;

import com.example.oliveyoung.model.Cart;
import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.repository.CartRepository;
import com.example.oliveyoung.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart addToCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void purchase(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null) {
            Product product = cart.getProduct();
            product.setStock(product.getStock() - cart.getQuantity());
            productRepository.save(product);
            cartRepository.delete(cart);
        }
    }

    public List<Cart> getCartItems(Long userId) {
        // userId에 해당하는 장바구니 아이템들을 반환
        return cartRepository.findByUserId(userId);
    }
}