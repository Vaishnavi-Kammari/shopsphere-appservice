package com.shopsphere.service;

import com.shopsphere.dto.Dtos.OrderDto;
import com.shopsphere.dto.Dtos.OrderItemDto;
import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.CartItem;
import com.shopsphere.entity.CustomerOrder;
import com.shopsphere.entity.OrderItem;
import com.shopsphere.entity.OrderStatus;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CartItemRepository;
import com.shopsphere.repository.OrderRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final UserRepository users;

    @PersistenceContext
    private EntityManager em;

    public OrderService(OrderRepository orders, CartItemRepository cartItems,
                        ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.products = products;
        this.users = users;
    }

    /** Turns the user's cart into an order. Totals and stock are always computed/checked here, never trusted from the client. */
    @Transactional
    public OrderDto place(String email, String shippingAddress) {
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        List<CartItem> items = new ArrayList<>(cartItems.findByUserOrderById(user));
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }
        // lock products in a fixed order so concurrent checkouts cannot deadlock or oversell
        items.sort((a, b) -> a.getProduct().getId().compareTo(b.getProduct().getId()));

        CustomerOrder order = new CustomerOrder(user, shippingAddress.trim());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = item.getProduct();
            try {
                em.refresh(product, LockModeType.PESSIMISTIC_WRITE); // re-read latest stock and lock the row
            } catch (EntityNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A product in your cart is no longer available");
            }
            if (item.getQuantity() > product.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough stock for \"" + product.getName() + "\" (available: " + product.getStock() + ")");
            }
            product.setStock(product.getStock() - item.getQuantity());
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), item.getQuantity()));
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotal(total);
        orders.save(order);
        cartItems.deleteAll(items);
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listForUser(String email) {
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return orders.findByUserOrderByCreatedAtDesc(user).stream().map(OrderService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getForUser(String email, Long id) {
        CustomerOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listAll() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderService::toDto).toList();
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        OrderStatus current = order.getStatus();
        if (current == OrderStatus.CANCELLED && status != OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A cancelled order cannot be reopened");
        }
        if (status == OrderStatus.CANCELLED && current != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {      // put the stock back
                products.findById(item.getProductId())
                        .ifPresent(p -> p.setStock(p.getStock() + item.getQuantity()));
            }
        }
        order.setStatus(status);
        return toDto(order);
    }

    private static OrderDto toDto(CustomerOrder o) {
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new OrderDto(o.getId(), o.getStatus().name(), o.getTotal(), o.getShippingAddress(), o.getCreatedAt(),
                o.getUser().getName(), o.getUser().getEmail(), items);
    }
}
