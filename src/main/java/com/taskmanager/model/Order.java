package com.taskmanager.model;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.annotations.Validate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Order {
    @Validate(notNull = true, message = "Order ID cannot be null")
    private String id;

    @Validate(notNull = true, notEmpty = true, message = "Customer name cannot be null or empty")
    private String customerName;

    @Validate(notNull = true, notEmpty = true, message = "Product cannot be null or empty")
    private String product;

    @OrderType
    private OrderType.Priority priority;

    private LocalDateTime createdAt;
    private OrderStatus status;

    public enum OrderStatus {
        CREATED, PROCESSING, COMPLETED, FAILED
    }

    public Order(String customerName, String product, OrderType.Priority priority) {
        this.id = UUID.randomUUID().toString();
        this.customerName = customerName;
        this.product = product;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }

    public String getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProduct() {
        return product;
    }

    public OrderType.Priority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setPriority(OrderType.Priority priority) {
        this.priority = priority;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', product='%s', priority=%s, status=%s}",
                id, customerName, product, priority, status);
    }
}