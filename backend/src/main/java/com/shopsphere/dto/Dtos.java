package com.shopsphere.dto;

import com.shopsphere.entity.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** All request/response shapes of the API, grouped in one file to keep the project small. */
public final class Dtos {
    private Dtos() {}

    // ----- auth -----
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record UserDto(Long id, String name, String email, String role) {}

    public record AuthResponse(String token, UserDto user) {}

    // ----- catalog -----
    public record CategoryDto(Long id, String name) {}

    public record ProductRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            @Size(max = 1000) String imageUrl,
            @NotNull Long categoryId) {}

    public record ProductDto(Long id, String name, String description, BigDecimal price, Integer stock,
                             String imageUrl, Long categoryId, String categoryName) {}

    // ----- cart -----
    public record CartItemRequest(@NotNull Long productId, @NotNull @Min(1) Integer quantity) {}

    public record QuantityRequest(@NotNull @Min(1) Integer quantity) {}

    public record CartItemDto(Long productId, String name, String imageUrl, BigDecimal unitPrice,
                              Integer quantity, Integer stock, BigDecimal lineTotal) {}

    public record CartDto(List<CartItemDto> items, BigDecimal total, int itemCount) {}

    // ----- orders -----
    public record CheckoutRequest(@NotBlank @Size(max = 500) String shippingAddress) {}

    public record StatusRequest(@NotNull OrderStatus status) {}

    public record OrderItemDto(Long productId, String productName, BigDecimal unitPrice,
                               Integer quantity, BigDecimal lineTotal) {}

    public record OrderDto(Long id, String status, BigDecimal total, String shippingAddress, Instant createdAt,
                           String customerName, String customerEmail, List<OrderItemDto> items) {}
}
