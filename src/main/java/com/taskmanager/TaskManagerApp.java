package com.taskmanager;

import com.taskmanager.model.Order;
import com.taskmanager.service.OrderConsumer;
import com.taskmanager.service.OrderProcessor;
import com.taskmanager.service.OrderProducer;
import com.taskmanager.validation.OrderValidator;
import com.taskmanager.annotations.OrderType;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskManagerApp {

    public static void main(String[] args) {
        System.out.println("=== Task Manager Application Started ===\n");

        BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<String, Order> processedOrders = new ConcurrentHashMap<>();

        OrderProcessor processor = new OrderProcessor(orderQueue, processedOrders);
        AtomicBoolean running = new AtomicBoolean(true);

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        OrderProducer producer = new OrderProducer(orderQueue, running, 10);
        executorService.submit(producer);

        OrderConsumer consumer1 = new OrderConsumer(processor, running);
        OrderConsumer consumer2 = new OrderConsumer(processor, running);

        executorService.submit(consumer1);
        executorService.submit(consumer2);

        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== Processing Results ===");
        System.out.println("Total processed orders: " + processedOrders.size());
        System.out.println("\nProcessed Orders:");
        processedOrders.forEach((id, order) ->
                System.out.println("  " + order));

        System.out.println("\n=== Validation Demo ===");
        try {
            Order invalidOrder = new Order("", "", OrderType.Priority.NORMAL);
            invalidOrder.setId(null);
            OrderValidator.validate(invalidOrder);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            System.out.println("Validation error caught: " + e.getMessage());
        }

        System.out.println("\n=== Application Finished ===");
    }
}