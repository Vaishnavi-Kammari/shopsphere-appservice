package com.shopsphere.service;

import com.shopsphere.dto.Dtos.CartDto;
import com.shopsphere.dto.Dtos.CartItemDto;
import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.CartItem;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CartItemRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final UserRepository users;

    public CartService(CartItemRepository cartItems, ProductRepository products, UserRepository users) {
        this.cartItems = cartItems;
        this.products = products;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public CartDto get(String email) {
        return toDto(cartItems.findByUserOrderById(user(email)));
    }

    @Transactional
    public CartDto add(String email, Long productId, int quantity) {
        AppUser user = user(email);
        Product product = product(productId);
        CartItem item = cartItems.findByUserAndProduct(user, product).orElse(null);
        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;
        checkStock(product, newQuantity);
        if (item == null) {
            cartItems.save(new CartItem(user, product, newQuantity));
        } else {
            item.setQuantity(newQuantity);
        }
        return toDto(cartItems.findByUserOrderById(user));
    }

    @Transactional
    public CartDto setQuantity(String email, Long productId, int quantity) {
        AppUser user = user(email);
        Product product = product(productId);
        CartItem item = cartItems.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item is not in your cart"));
        checkStock(product, quantity);
        item.setQuantity(quantity);
        return toDto(cartItems.findByUserOrderById(user));
    }

    @Transactional
    public CartDto remove(String email, Long productId) {
        AppUser user = user(email);
        Product product = product(productId);
        cartItems.findByUserAndProduct(user, product).ifPresent(cartItems::delete);
        return toDto(cartItems.findByUserOrderById(user));
    }

    private void checkStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only " + product.getStock() + " of \"" + product.getName() + "\" in stock");
        }
    }

    private AppUser user(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Product product(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private static CartDto toDto(List<CartItem> items) {
        List<CartItemDto> dtos = items.stream().map(ci -> {
            Product p = ci.getProduct();
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemDto(p.getId(), p.getName(), p.getImageUrl(), p.getPrice(),
                    ci.getQuantity(), p.getStock(), line);
        }).toList();
        BigDecimal total = dtos.stream().map(CartItemDto::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = dtos.stream().mapToInt(CartItemDto::quantity).sum();
        return new CartDto(dtos, total, count);
    }
}
