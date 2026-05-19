package com.taskmanager.service;

import com.taskmanager.model.Order;
import com.taskmanager.model.Order.OrderStatus;
import com.taskmanager.annotations.OrderType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OrderProcessor {
    private final BlockingQueue<Order> orderQueue;
    private final ConcurrentHashMap<String, Order> processedOrders;

    public OrderProcessor(BlockingQueue<Order> orderQueue, ConcurrentHashMap<String, Order> processedOrders) {
        this.orderQueue = orderQueue;
        this.processedOrders = processedOrders;
    }

    public Order processOrder() throws InterruptedException {
        Order order = orderQueue.poll(100, TimeUnit.MILLISECONDS);

        if (order != null) {
            try {
                System.out.println(Thread.currentThread().getName() + " processing order: " + order.getId());

                order.setStatus(OrderStatus.PROCESSING);

                if (order.getPriority() == OrderType.Priority.URGENT) {
                    System.out.println("  -> URGENT order, processing faster...");
                    Thread.sleep(500);
                } else {
                    System.out.println("  -> Normal order, processing...");
                    Thread.sleep(1000);
                }

                order.setStatus(OrderStatus.COMPLETED);
                processedOrders.put(order.getId(), order);
                System.out.println(Thread.currentThread().getName() + " completed order: " + order.getId());

            } catch (InterruptedException e) {
                order.setStatus(OrderStatus.FAILED);
                processedOrders.put(order.getId(), order);
                System.err.println("Order processing failed: " + order.getId());
                Thread.currentThread().interrupt();
            }
        }

        return order;
    }

    public BlockingQueue<Order> getOrderQueue() {
        return orderQueue;
    }

    public ConcurrentHashMap<String, Order> getProcessedOrders() {
        return processedOrders;
    }
}
