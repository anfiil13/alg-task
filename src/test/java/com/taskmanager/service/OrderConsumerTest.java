package com.taskmanager.service;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Consumer Tests")
class OrderConsumerTest {

    @Nested
    @DisplayName("Processing Tests")
    class ProcessingTests {

        @Test
        @DisplayName("Should process single order from queue")
        void testProcessesSingleOrder() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(2000);
            running.set(false);
            consumerThread.join(2000);

            assertTrue(processed.containsKey(order.getId()));
            assertEquals(Order.OrderStatus.COMPLETED, processed.get(order.getId()).getStatus());
        }

        @Test
        @DisplayName("Should process multiple orders")
        void testProcessesMultipleOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            int orderCount = 5;
            for (int i = 0; i < orderCount; i++) {
                queue.put(new Order("Customer" + i, "Product" + i, OrderType.Priority.NORMAL));
            }

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(5000);
            running.set(false);
            consumerThread.join(3000);

            assertEquals(orderCount, consumer.getProcessedCount());
            assertEquals(orderCount, processed.size());
        }

        @Test
        @DisplayName("Should process both urgent and normal orders")
        void testProcessesBothPriorities() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            queue.put(new Order("John", "Product1", OrderType.Priority.URGENT));
            queue.put(new Order("Jane", "Product2", OrderType.Priority.NORMAL));

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(3000);
            running.set(false);
            consumerThread.join(2000);

            assertEquals(2, processed.size());
            processed.values().forEach(order ->
                    assertEquals(Order.OrderStatus.COMPLETED, order.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Stop Mechanism Tests")
    class StopTests {

        @Test
        @DisplayName("Should stop when queue is empty and running is false")
        void testStopsWhenDone() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(false);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            consumerThread.join(1000);

            assertFalse(consumerThread.isAlive());
            assertEquals(0, consumer.getProcessedCount());
        }

        @Test
        @DisplayName("Should continue processing while running is true")
        void testContinuesWhileRunning() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            queue.put(new Order("John", "Product1", OrderType.Priority.NORMAL));
            queue.put(new Order("Jane", "Product2", OrderType.Priority.NORMAL));

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(3000);
            running.set(false);
            consumerThread.join(2000);

            assertEquals(2, consumer.getProcessedCount());
        }
    }

    @Nested
    @DisplayName("Processed Count Tests")
    class ProcessedCountTests {

        @Test
        @DisplayName("Should track processed count correctly")
        void testProcessedCount() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            int orderCount = 3;
            for (int i = 0; i < orderCount; i++) {
                queue.put(new Order("Customer" + i, "Product" + i, OrderType.Priority.NORMAL));
            }

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(3000);
            running.set(false);
            consumerThread.join(2000);

            assertEquals(orderCount, consumer.getProcessedCount());
        }

        @Test
        @DisplayName("Should have zero processed count initially")
        void testInitialProcessedCount() {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            assertEquals(0, consumer.getProcessedCount());
        }
    }

    @Nested
    @DisplayName("Concurrent Tests")
    class ConcurrentTests {

        @Test
        @DisplayName("Should handle multiple consumers processing same queue")
        void testMultipleConsumers() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            int orderCount = 10;
            for (int i = 0; i < orderCount; i++) {
                queue.put(new Order("Customer" + i, "Product" + i, OrderType.Priority.NORMAL));
            }

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer1 = new OrderConsumer(processor, running);
            OrderConsumer consumer2 = new OrderConsumer(processor, running);

            Thread thread1 = new Thread(consumer1, "Consumer-1");
            Thread thread2 = new Thread(consumer2, "Consumer-2");

            thread1.start();
            thread2.start();

            Thread.sleep(5000);
            running.set(false);
            thread1.join(3000);
            thread2.join(3000);

            assertEquals(orderCount, processed.size());
            int totalProcessed = consumer1.getProcessedCount() + consumer2.getProcessedCount();
            assertEquals(orderCount, totalProcessed);
        }
    }
}
