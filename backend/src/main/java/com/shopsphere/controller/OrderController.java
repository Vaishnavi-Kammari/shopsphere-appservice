package com.shopsphere.controller;

import com.shopsphere.dto.Dtos.CheckoutRequest;
import com.shopsphere.dto.Dtos.OrderDto;
import com.shopsphere.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto checkout(Authentication auth, @Valid @RequestBody CheckoutRequest request) {
        return orderService.place(auth.getName(), request.shippingAddress());
    }

    @GetMapping
    public List<OrderDto> myOrders(Authentication auth) {
        return orderService.listForUser(auth.getName());
    }

    @GetMapping("/{id}")
    public OrderDto myOrder(Authentication auth, @PathVariable Long id) {
        return orderService.getForUser(auth.getName(), id);
    }
}
