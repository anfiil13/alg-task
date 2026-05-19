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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskManagerApp Integration Tests")
class TaskManagerAppTest {

    @Nested
    @DisplayName("TaskManagerApp main method")
    class MainMethodTests {

        @Test
        @DisplayName("Should run main method without exceptions")
        void testMainMethodRuns() {
            assertDoesNotThrow(() -> {
                TaskManagerApp.main(new String[]{});
            });
        }

        @Test
        @DisplayName("Should output expected messages")
        void testMainMethodOutput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                TaskManagerApp.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }

            String output = outContent.toString();
            assertTrue(output.contains("Task Manager Application Started"));
            assertTrue(output.contains("Available order priorities"));
            assertTrue(output.contains("URGENT"));
            assertTrue(output.contains("NORMAL"));
            assertTrue(output.contains("Processing Results"));
            assertTrue(output.contains("Total processed orders:"));
            assertTrue(output.contains("Validation Demo"));
            assertTrue(output.contains("Application Finished"));
        }

        @Test
        @DisplayName("Should process orders when main runs")
        void testMainMethodProcessesOrders() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                TaskManagerApp.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }

            String output = outContent.toString();
            assertTrue(output.contains("Produced:"));
            assertTrue(output.contains("processing order:"));
            assertTrue(output.contains("completed order:"));
            assertTrue(output.contains("Total processed orders: 10"));
        }

        @Test
        @DisplayName("Should show @OrderType statistics in output")
        void testMainMethodShowsOrderTypeStats() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                TaskManagerApp.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }

            String output = outContent.toString();
            assertTrue(output.contains("Urgent orders processed:"));
            assertTrue(output.contains("Normal orders processed:"));
        }

        @Test
        @DisplayName("Should demonstrate validation in main method")
        void testMainMethodShowsValidation() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                TaskManagerApp.main(new String[]{});
            } finally {
                System.setOut(originalOut);
            }

            String output = outContent.toString();
            assertTrue(output.contains("Validation error caught:"));
        }
    }

    @Nested
    @DisplayName("Full workflow")
    class FullWorkflowTests {

        @Test
        @DisplayName("Should complete full producer-consumer workflow with 10 orders")
        void testFullWorkflow() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderProducer producer = new OrderProducer(queue, running, 10);
            OrderConsumer consumer1 = new OrderConsumer(processor, running);
            OrderConsumer consumer2 = new OrderConsumer(processor, running);

            Thread producerThread = new Thread(producer, "Producer");
            Thread c1 = new Thread(consumer1, "Consumer-1");
            Thread c2 = new Thread(consumer2, "Consumer-2");

            producerThread.start();
            c1.start();
            c2.start();

            producerThread.join(10000);
            Thread.sleep(3000);
            running.set(false);
            c1.join(5000);
            c2.join(5000);

            assertEquals(10, processed.size());
            processed.values().forEach(o ->
                    assertEquals(Order.OrderStatus.COMPLETED, o.getStatus()));
            assertEquals(10, consumer1.getProcessedCount() + consumer2.getProcessedCount());
        }

        @Test
        @DisplayName("Should handle @OrderType priorities in workflow")
        void testPriorityHandling() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            queue.put(new Order("John", "P1", OrderType.Priority.URGENT));
            queue.put(new Order("Jane", "P2", OrderType.Priority.NORMAL));
            queue.put(new Order("Bob", "P3", OrderType.Priority.URGENT));
            queue.put(new Order("Alice", "P4", OrderType.Priority.NORMAL));

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread t = new Thread(consumer);
            t.start();

            Thread.sleep(4000);
            running.set(false);
            t.join(3000);

            assertEquals(4, processed.size());
            long urgent = processed.values().stream()
                    .filter(o -> o.getPriority() == OrderType.Priority.URGENT).count();
            assertEquals(2, urgent);
        }
    }

    @Nested
    @DisplayName("Concurrent processing")
    class ConcurrentTests {

        @Test
        @DisplayName("Should handle multiple consumers without data loss")
        void testMultipleConsumers() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            int orderCount = 20;
            for (int i = 0; i < orderCount; i++) {
                queue.put(new Order("C" + i, "P" + i,
                        i % 2 == 0 ? OrderType.Priority.NORMAL : OrderType.Priority.URGENT));
            }

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer[] consumers = new OrderConsumer[4];
            Thread[] threads = new Thread[4];

            for (int i = 0; i < 4; i++) {
                consumers[i] = new OrderConsumer(processor, running);
                threads[i] = new Thread(consumers[i]);
                threads[i].start();
            }

            Thread.sleep(8000);
            running.set(false);
            for (Thread t : threads) t.join(3000);

            assertEquals(orderCount, processed.size());
            int total = 0;
            for (OrderConsumer c : consumers) total += c.getProcessedCount();
            assertEquals(orderCount, total);
        }
    }

    @Nested
    @DisplayName("Validation in workflow")
    class ValidationTests {

        @Test
        @DisplayName("Should validate orders before processing")
        void testValidationInWorkflow() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            Order validOrder = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertDoesNotThrow(() -> OrderValidator.validate(validOrder));
            queue.put(validOrder);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderConsumer consumer = new OrderConsumer(processor, running);

            Thread t = new Thread(consumer);
            t.start();

            Thread.sleep(2000);
            running.set(false);
            t.join(2000);

            assertTrue(processed.containsKey(validOrder.getId()));

            Order invalid = new Order(null, "", OrderType.Priority.NORMAL);
            assertThrows(IllegalArgumentException.class,
                    () -> OrderValidator.validate(invalid));
        }
    }

    @Nested
    @DisplayName("@OrderType usage in workflow")
    class OrderTypeUsageTests {

        @Test
        @DisplayName("Should use @OrderType for processing priority")
        void testOrderTypeAffectsProcessing() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order urgent = new Order("John", "UP", OrderType.Priority.URGENT);
            Order normal = new Order("Jane", "NP", OrderType.Priority.NORMAL);

            assertEquals(OrderType.Priority.URGENT, urgent.getPriority());
            assertEquals(OrderType.Priority.NORMAL, normal.getPriority());

            queue.put(urgent);
            queue.put(normal);

            OrderProcessor processor = new OrderProcessor(queue, processed);

            long startU = System.currentTimeMillis();
            processor.processOrder();
            long timeU = System.currentTimeMillis() - startU;

            long startN = System.currentTimeMillis();
            processor.processOrder();
            long timeN = System.currentTimeMillis() - startN;

            assertTrue(timeU <= timeN + 300);
            assertEquals(2, processed.size());
        }
    }

    @Nested
    @DisplayName("ExecutorService usage (like in main)")
    class ExecutorServiceTests {

        @Test
        @DisplayName("Should work with ExecutorService like main method")
        void testExecutorServiceWorkflow() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            OrderProducer producer = new OrderProducer(queue, running, 5);
            OrderConsumer consumer1 = new OrderConsumer(processor, running);
            OrderConsumer consumer2 = new OrderConsumer(processor, running);

            java.util.concurrent.ExecutorService executor =
                    java.util.concurrent.Executors.newFixedThreadPool(3);

            executor.submit(producer);
            executor.submit(consumer1);
            executor.submit(consumer2);

            executor.shutdown();
            boolean finished = executor.awaitTermination(15, java.util.concurrent.TimeUnit.SECONDS);

            assertTrue(finished || processed.size() > 0);
        }
    }
}
