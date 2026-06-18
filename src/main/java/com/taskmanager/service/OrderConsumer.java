package com.taskmanager.service;

import com.taskmanager.model.Order;

import java.util.concurrent.atomic.AtomicBoolean;

public class OrderConsumer implements Runnable {
    private final OrderProcessor processor;
    private final AtomicBoolean running;
    private int processedCount = 0;

    public OrderConsumer(OrderProcessor processor, AtomicBoolean running) {
        this.processor = processor;
        this.running = running;
    }

    @Override
    public void run() {
        while (running.get() || !processor.getOrderQueue().isEmpty()) {
            try {
                Order order = processor.processOrder();
                if (order != null) {
                    processedCount++;
                } else if (!running.get() && processor.getOrderQueue().isEmpty()) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " finished. Processed: " + processedCount);
    }

    public int getProcessedCount() {
        return processedCount;
    }
}