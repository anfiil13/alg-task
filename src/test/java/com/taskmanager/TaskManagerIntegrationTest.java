package com.taskmanager;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import com.taskmanager.service.OrderConsumer;
import com.taskmanager.service.OrderProcessor;
import com.taskmanager.service.OrderProducer;
import com.taskmanager.validation.OrderValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests")
class TaskManagerIntegrationTest {

    @Nested
    @DisplayName("Full Workflow")
    class FullWorkflowTests {

        @Test
        @DisplayName("Should complete full producer-consumer workflow")
        void testFullWorkflow() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderProducer producer = new OrderProducer(queue, running, 10);
            OrderConsumer consumer1 = new OrderConsumer(processor, running);
            OrderConsumer consumer2 = new OrderConsumer(processor, running);

            Thread producerThread = new Thread(producer, "Producer");
            Thread consumerThread1 = new Thread(consumer1, "Consumer-1");
            Thread consumerThread2 = new Thread(consumer2, "Consumer-2");

            producerThread.start();
            consumerThread1.start();
            consumerThread2.start();

            producerThread.join(5000);
            Thread.sleep(3000);
            running.set(false);
            consumerThread1.join(3000);
            consumerThread2.join(3000);

            assertTrue(processed.size() > 0);
            int totalProcessed = consumer1.getProcessedCount() + consumer2.getProcessedCount();
            assertEquals(10, totalProcessed);

            processed.values().forEach(order ->
                    assertEquals(Order.OrderStatus.COMPLETED, order.getStatus())
            );
        }

        @Test
        @DisplayName("Should handle concurrent processing without data loss")
        void testConcurrentProcessing() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            int orderCount = 20;
            for (int i = 0; i < orderCount; i++) {
                queue.put(new Order("Customer" + i, "Product" + i,
                        i % 2 == 0 ? OrderType.Priority.NORMAL : OrderType.Priority.URGENT));
            }

            OrderProcessor processor = new OrderProcessor(queue, processed);

            int consumerCount = 4;
            Thread[] threads = new Thread[consumerCount];
            OrderConsumer[] consumers = new OrderConsumer[consumerCount];

            for (int i = 0; i < consumerCount; i++) {
                consumers[i] = new OrderConsumer(processor, running);
                threads[i] = new Thread(consumers[i], "Consumer-" + i);
                threads[i].start();
            }

            Thread.sleep(5000);
            running.set(false);

            for (Thread thread : threads) {
                thread.join(2000);
            }

            assertEquals(orderCount, processed.size());

            int totalProcessed = 0;
            for (OrderConsumer consumer : consumers) {
                totalProcessed += consumer.getProcessedCount();
            }
            assertEquals(orderCount, totalProcessed);
        }
    }

    @Nested
    @DisplayName("Validation Integration")
    class ValidationIntegrationTests {

        @Test
        @DisplayName("Should validate orders before processing")
        void testValidationInWorkflow() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            Order validOrder = new Order("John", "Product", OrderType.Priority.NORMAL);
            queue.put(validOrder);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread consumerThread = new Thread(consumer);
            consumerThread.start();

            Thread.sleep(2000);
            running.set(false);
            consumerThread.join(2000);

            assertTrue(processed.containsKey(validOrder.getId()));

            Order invalidOrder = new Order(null, "", OrderType.Priority.NORMAL);
            assertThrows(IllegalArgumentException.class,
                    () -> OrderValidator.validate(invalidOrder));
        }
    }
}
