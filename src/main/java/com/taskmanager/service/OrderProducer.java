package com.taskmanager.service;

import com.taskmanager.model.Order;
import com.taskmanager.annotations.OrderType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrderProducer implements Runnable {
    private final BlockingQueue<Order> orderQueue;
    private final AtomicBoolean running;
    private final int orderCount;

    public OrderProducer(BlockingQueue<Order> orderQueue, AtomicBoolean running, int orderCount) {
        this.orderQueue = orderQueue;
        this.running = running;
        this.orderCount = orderCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= orderCount && running.get(); i++) {
                OrderType.Priority priority;
                if (i % 3 == 0) {
                    priority = OrderType.Priority.URGENT;
                } else {
                    priority = OrderType.Priority.NORMAL;
                }

                Order order = new Order("Customer" + i, "Product" + i, priority);

                if (order.getPriority() == OrderType.Priority.URGENT) {
                    System.out.println("Produced URGENT: " + order);
                } else {
                    System.out.println("Produced: " + order);
                }

                orderQueue.put(order);
                Thread.sleep(200);
            }

            System.out.println("Producer completed. Total orders produced: " + orderCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Producer interrupted");
        } finally {
            running.set(false);
        }
    }
}
