package com.taskmanager;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import com.taskmanager.service.OrderConsumer;
import com.taskmanager.service.OrderProcessor;
import com.taskmanager.service.OrderProducer;
import com.taskmanager.validation.OrderValidator;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("Task Manager Tests")
class TaskManagerTest {

    @Nested
    @DisplayName("Order Model")
    class OrderTests {

        @Test
        @DisplayName("Should create order with valid parameters")
        void testOrderCreation() {
            Order order = new Order("John Doe", "Laptop", OrderType.Priority.URGENT);

            assertNotNull(order.getId());
            assertEquals("John Doe", order.getCustomerName());
            assertEquals("Laptop", order.getProduct());
            assertEquals(OrderType.Priority.URGENT, order.getPriority());
            assertEquals(Order.OrderStatus.CREATED, order.getStatus());
            assertNotNull(order.getCreatedAt());
        }

        @Test
        @DisplayName("Should generate unique IDs for different orders")
        void testUniqueIds() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product", OrderType.Priority.NORMAL);

            assertNotEquals(order1.getId(), order2.getId());
        }

        @Test
        @DisplayName("Should correctly compare orders")
        void testOrderEquality() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product", OrderType.Priority.NORMAL);

            assertEquals(order1, order1);
            assertNotEquals(order1, order2);
            assertNotEquals(null, order1);
            assertNotEquals(order1, "String");
        }

        @Test
        @DisplayName("Should generate consistent hash codes")
        void testOrderHashCode() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("John", "Product", OrderType.Priority.NORMAL);

            assertEquals(order1.hashCode(), order1.hashCode());
            assertNotEquals(order1.hashCode(), order2.hashCode());
        }

        @Test
        @DisplayName("Should correctly set and get all properties")
        void testSettersAndGetters() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);

            order.setStatus(Order.OrderStatus.PROCESSING);
            assertEquals(Order.OrderStatus.PROCESSING, order.getStatus());

            order.setCustomerName("Jane");
            assertEquals("Jane", order.getCustomerName());

            order.setProduct("NewProduct");
            assertEquals("NewProduct", order.getProduct());

            order.setPriority(OrderType.Priority.URGENT);
            assertEquals(OrderType.Priority.URGENT, order.getPriority());

            String newId = "test-id-123";
            order.setId(newId);
            assertEquals(newId, order.getId());

            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            assertEquals(now, order.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle all order statuses")
        void testAllStatuses() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);

            assertEquals(Order.OrderStatus.CREATED, order.getStatus());

            order.setStatus(Order.OrderStatus.PROCESSING);
            assertEquals(Order.OrderStatus.PROCESSING, order.getStatus());

            order.setStatus(Order.OrderStatus.COMPLETED);
            assertEquals(Order.OrderStatus.COMPLETED, order.getStatus());

            order.setStatus(Order.OrderStatus.FAILED);
            assertEquals(Order.OrderStatus.FAILED, order.getStatus());
        }

        @Test
        @DisplayName("Should handle both priority types")
        void testPriorities() {
            Order normalOrder = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order urgentOrder = new Order("Jane", "Product", OrderType.Priority.URGENT);

            assertEquals(OrderType.Priority.NORMAL, normalOrder.getPriority());
            assertEquals(OrderType.Priority.URGENT, urgentOrder.getPriority());
        }

        @Test
        @DisplayName("Should produce meaningful toString output")
        void testToString() {
            Order order = new Order("John", "Laptop", OrderType.Priority.URGENT);
            String str = order.toString();

            assertTrue(str.contains("John"));
            assertTrue(str.contains("Laptop"));
            assertTrue(str.contains("URGENT"));
            assertTrue(str.contains(order.getId()));
            assertTrue(str.contains("CREATED"));
        }
    }

    @Nested
    @DisplayName("Order Validator")
    class ValidatorTests {

        @Test
        @DisplayName("Should pass validation for valid order")
        void testValidOrder() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }

        @Test
        @DisplayName("Should reject null object")
        void testNullObject() {
            assertThrows(IllegalArgumentException.class,
                    () -> OrderValidator.validate(null));
        }

        @Test
        @DisplayName("Should reject null ID")
        void testNullId() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            order.setId(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("ID"));
        }

        @Test
        @DisplayName("Should reject null customer name")
        void testNullCustomerName() {
            Order order = new Order(null, "Product", OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Customer name"));
        }

        @Test
        @DisplayName("Should reject empty customer name")
        void testEmptyCustomerName() {
            Order order = new Order("", "Product", OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Customer name"));
        }

        @Test
        @DisplayName("Should reject null product")
        void testNullProduct() {
            Order order = new Order("John", null, OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Product"));
        }

        @Test
        @DisplayName("Should reject empty product")
        void testEmptyProduct() {
            Order order = new Order("John", "", OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Product"));
        }
    }

    @Nested
    @DisplayName("Order Producer")
    class ProducerTests {

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
        @DisplayName("Should create valid orders")
        void testCreatesValidOrders() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            AtomicBoolean running = new AtomicBoolean(true);

            OrderProducer producer = new OrderProducer(queue, running, 3);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            producerThread.join(3000);

            for (Order order : queue) {
                assertNotNull(order.getId());
                assertNotNull(order.getCustomerName());
                assertNotNull(order.getProduct());
                assertNotNull(order.getPriority());
                assertEquals(Order.OrderStatus.CREATED, order.getStatus());
            }
        }

        @Test
        @DisplayName("Should stop when running flag becomes false")
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
        @DisplayName("Should create orders with different priorities")
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
    }

    @Nested
    @DisplayName("Order Processor")
    class ProcessorTests {

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
        @DisplayName("Should return null for empty queue")
        void testEmptyQueue() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            OrderProcessor processor = new OrderProcessor(queue, processed);
            Order result = processor.processOrder();

            assertNull(result);
            assertTrue(processed.isEmpty());
        }

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
        @DisplayName("Should mark orders as completed after processing")
        void testOrderCompletionStatus() throws InterruptedException {
            BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
            ConcurrentHashMap<String, Order> processed = new ConcurrentHashMap<>();

            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            queue.put(order);

            OrderProcessor processor = new OrderProcessor(queue, processed);
            processor.processOrder();

            assertEquals(Order.OrderStatus.COMPLETED, processed.get(order.getId()).getStatus());
        }
    }

    @Nested
    @DisplayName("Order Consumer")
    class ConsumerTests {

        @Test
        @DisplayName("Should process orders from queue")
        void testProcessesOrders() throws InterruptedException {
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
        @DisplayName("Should stop when queue is empty and not running")
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
        @DisplayName("Should track processed count correctly")
        void testProcessedCount() throws InterruptedException {
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

            Thread.sleep(3000);
            running.set(false);
            consumerThread.join(3000);

            assertEquals(orderCount, consumer.getProcessedCount());
            assertEquals(orderCount, processed.size());
        }
    }

    @Nested
    @DisplayName("Integration")
    class IntegrationTests {

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