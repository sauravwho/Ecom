package com.example.ecom.controller;

import com.example.ecom.data.CartItem;
import com.example.ecom.dto.cartItemRequest;
import com.example.ecom.security.UserPrincipal;
import com.example.ecom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody cartItemRequest request){
        String userId = String.valueOf(principal.getUser().getId());
        if(!cartService.addToCart(userId, request)){
            return ResponseEntity.badRequest().body("Product Out of Stock or User not found or Product not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId){
        String userId = String.valueOf(principal.getUser().getId());
        boolean deleted = cartService.deleteItemFromCart(userId, productId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@AuthenticationPrincipal UserPrincipal principal){
        String userId = String.valueOf(principal.getUser().getId());
        return ResponseEntity.ok(cartService.getCart(userId));
    }
}
