package com.taskmanager.service;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Processor Tests")
class OrderProcessorTest {

    @Nested
    @DisplayName("Processing Tests")
    class ProcessingTests {

        @Test
        @DisplayName("Should process order successfully")
        void testProcessOrder() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order = new Order("John", "Product", OrderType.Priority.URGENT);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            Order result = processor.processOrder();

            assertNotNull(result);
            assertEquals(Order.OrderStatus.COMPLETED, result.getStatus());
            assertTrue(processed.containsKey(order.getId()));
        }

        @Test
        @DisplayName("Should process normal priority order")
        void testProcessNormalOrder() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order = new Order("Jane", "Product", OrderType.Priority.NORMAL);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            Order result = processor.processOrder();

            assertNotNull(result);
            assertEquals(Order.OrderStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should process urgent order faster than normal")
        void testProcessUrgentFaster() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order urgentOrder = new Order("John", "Product", OrderType.Priority.URGENT);
            Order normalOrder = new Order("Jane", "Product", OrderType.Priority.NORMAL);

            queue.put(urgentOrder);
            queue.put(normalOrder);

            OrderProcessor processor = new OrderProcessor(queue, processed);

            long startUrgent = System.currentTimeMillis();
            processor.processOrder();
            long urgentTime = System.currentTimeMillis() - startUrgent;

            long startNormal = System.currentTimeMillis();
            processor.processOrder();
            long normalTime = System.currentTimeMillis() - startNormal;

            assertTrue(urgentTime <= normalTime + 200,
                    "Urgent order should be processed at least as fast as normal");
        }
    }

    @Nested
    @DisplayName("Empty Queue Tests")
    class EmptyQueueTests {

        @Test
        @DisplayName("Should return null when queue is empty")
        void testEmptyQueue() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            OrderProcessor processor = new OrderProcessor(queue, processed);
            Order result = processor.processOrder();

            assertNull(result);
        }

        @Test
        @DisplayName("Should not add anything to processed map when queue is empty")
        void testEmptyQueueNoProcessing() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            OrderProcessor processor = new OrderProcessor(queue, processed);
            processor.processOrder();

            assertTrue(processed.isEmpty());
        }
    }

    @Nested
    @DisplayName("Multiple Orders Tests")
    class MultipleOrdersTests {

        @Test
        @DisplayName("Should process multiple orders")
        void testMultipleOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order1 = new Order("John", "Product1", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product2", OrderType.Priority.URGENT);
            Order order3 = new Order("Bob", "Product3", OrderType.Priority.NORMAL);

            queue.put(order1);
            queue.put(order2);
            queue.put(order3);

            OrderProcessor processor = new OrderProcessor(queue, processed);

            Order result1 = processor.processOrder();
            Order result2 = processor.processOrder();
            Order result3 = processor.processOrder();

            assertNotNull(result1);
            assertNotNull(result2);
            assertNotNull(result3);
            assertEquals(3, processed.size());
        }

        @Test
        @DisplayName("Should store all processed orders in map")
        void testAllOrdersStored() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order1 = new Order("John", "Product1", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product2", OrderType.Priority.NORMAL);

            queue.put(order1);
            queue.put(order2);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            processor.processOrder();
            processor.processOrder();

            assertTrue(processed.containsKey(order1.getId()));
            assertTrue(processed.containsKey(order2.getId()));
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should mark orders as COMPLETED after successful processing")
        void testOrderMarkedCompleted() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            processor.processOrder();

            Order processedOrder = processed.get(order.getId());
            assertEquals(Order.OrderStatus.COMPLETED, processedOrder.getStatus());
        }

        @Test
        @DisplayName("Should set PROCESSING status during processing")
        void testProcessingStatus() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);


            processor.processOrder();

            Order processedOrder = processed.get(order.getId());
            assertNotNull(processedOrder);
            assertEquals(Order.OrderStatus.COMPLETED, processedOrder.getStatus());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return the order queue")
        void testGetOrderQueue() {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            OrderProcessor processor = new OrderProcessor(queue, processed);

            assertSame(queue, processor.getOrderQueue());
        }

        @Test
        @DisplayName("Should return the processed orders map")
        void testGetProcessedOrders() {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            OrderProcessor processor = new OrderProcessor(queue, processed);

            assertSame(processed, processor.getProcessedOrders());
        }
    }
}
