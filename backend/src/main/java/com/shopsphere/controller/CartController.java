package com.shopsphere.controller;

import com.shopsphere.dto.Dtos.CartDto;
import com.shopsphere.dto.Dtos.CartItemRequest;
import com.shopsphere.dto.Dtos.QuantityRequest;
import com.shopsphere.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto get(Authentication auth) {
        return cartService.get(auth.getName());
    }

    @PostMapping("/items")
    public CartDto add(Authentication auth, @Valid @RequestBody CartItemRequest request) {
        return cartService.add(auth.getName(), request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartDto setQuantity(Authentication auth, @PathVariable Long productId,
                               @Valid @RequestBody QuantityRequest request) {
        return cartService.setQuantity(auth.getName(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartDto remove(Authentication auth, @PathVariable Long productId) {
        return cartService.remove(auth.getName(), productId);
    }
}
