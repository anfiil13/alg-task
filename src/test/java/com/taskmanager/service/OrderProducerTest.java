package com.taskmanager.service;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Producer Tests")
class OrderProducerTest {

    @Nested
    @DisplayName("Order Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create exact number of orders")
        void testCreatesExactNumberOfOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);
            int orderCount = 5;

            OrderProducer producer = new OrderProducer(queue, running, orderCount);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(5000);

            assertEquals(orderCount, queue.size());
        }

        @Test
        @DisplayName("Should create zero orders when orderCount is zero")
        void testCreatesZeroOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 0);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(3000);

            assertEquals(0, queue.size());
        }

        @Test
        @DisplayName("Should create valid orders with all required fields")
        void testCreatesValidOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 3);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(3000);

            assertEquals(3, queue.size());

            for (Order order : queue) {
                assertNotNull(order.getId());
                assertNotNull(order.getCustomerName());
                assertTrue(order.getCustomerName().startsWith("Customer"));
                assertNotNull(order.getProduct());
                assertTrue(order.getProduct().startsWith("Product"));
                assertNotNull(order.getPriority());
                assertEquals(Order.OrderStatus.CREATED, order.getStatus());
                assertNotNull(order.getCreatedAt());
            }
        }
    }

    @Nested
    @DisplayName("Priority Distribution Tests")
    class PriorityTests {

        @Test
        @DisplayName("Should create orders with both URGENT and NORMAL priorities")
        void testCreatesVariousPriorities() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 6);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(3000);

            boolean hasUrgent = false;
            boolean hasNormal = false;

            for (Order order : queue) {
                if (order.getPriority() == OrderType.Priority.URGENT) hasUrgent = true;
                if (order.getPriority() == OrderType.Priority.NORMAL) hasNormal = true;
            }

            assertTrue(hasUrgent, "Should contain urgent orders");
            assertTrue(hasNormal, "Should contain normal orders");
        }

        @Test
        @DisplayName("Every third order should be URGENT")
        void testEveryThirdOrderIsUrgent() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 9);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(5000);

            Order[] orders = queue.toArray(new Order[0]);
            assertEquals(OrderType.Priority.URGENT, orders[2].getPriority());  // 3rd
            assertEquals(OrderType.Priority.URGENT, orders[5].getPriority());  // 6th
            assertEquals(OrderType.Priority.URGENT, orders[8].getPriority());  // 9th
        }
    }

    @Nested
    @DisplayName("Stop Mechanism Tests")
    class StopTests {

        @Test
        @DisplayName("Should stop production when running flag is set to false")
        void testStopsWhenFlagIsFalse() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 1000);
            Thread producerThread = new Thread(producer);
            producerThread.start();

            Thread.sleep(100);
            running.set(false);
            producerThread.join(2000);

            assertTrue(queue.size() < 1000);
            assertTrue(queue.size() > 0);
        }

        @Test
        @DisplayName("Should set running to false when finished normally")
        void testSetsRunningFalseWhenFinished() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 3);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(3000);

            assertFalse(running.get());
        }
    }

    @Nested
    @DisplayName("Queue Tests")
    class QueueTests {

        @Test
        @DisplayName("Should add orders to the queue")
        void testAddsOrdersToQueue() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 1);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(2000);

            assertEquals(1, queue.size());
            Order order = queue.peek();
            assertNotNull(order);
            assertEquals("Customer1", order.getCustomerName());
            assertEquals("Product1", order.getProduct());
        }
    }
}
