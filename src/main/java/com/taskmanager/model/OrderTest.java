package com.taskmanager.model;

import com.taskmanager.annotations.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Model Tests")
class OrderTest {
    
    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {
        
        @Test
        @DisplayName("Should create order with all fields populated")
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
        @DisplayName("Should create order with normal priority")
        void testOrderCreationWithNormalPriority() {
            Order order = new Order("Jane", "Phone", OrderType.Priority.NORMAL);
            
            assertEquals(OrderType.Priority.NORMAL, order.getPriority());
            assertEquals(Order.OrderStatus.CREATED, order.getStatus());
        }
        
        @Test
        @DisplayName("Should generate unique UUID for each order")
        void testUniqueIds() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product", OrderType.Priority.NORMAL);
            Order order3 = new Order("Bob", "Product", OrderType.Priority.NORMAL);
            
            assertNotEquals(order1.getId(), order2.getId());
            assertNotEquals(order2.getId(), order3.getId());
            assertNotEquals(order1.getId(), order3.getId());
        }
        
        @Test
        @DisplayName("Should set createdAt to current time")
        void testCreatedAtIsCurrentTime() {
            LocalDateTime before = LocalDateTime.now();
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            LocalDateTime after = LocalDateTime.now();
            
            assertFalse(order.getCreatedAt().isBefore(before.minusSeconds(1)));
            assertFalse(order.getCreatedAt().isAfter(after.plusSeconds(1)));
        }
    }
    
    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Same object should be equal")
        void testSameObject() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertEquals(order, order);
        }
        
        @Test
        @DisplayName("Different orders with different IDs should not be equal")
        void testDifferentOrders() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Product", OrderType.Priority.NORMAL);
            assertNotEquals(order1, order2);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void testNullComparison() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertNotEquals(null, order);
        }
        
        @Test
        @DisplayName("Should not be equal to different class")
        void testDifferentClass() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertNotEquals(order, "String object");
        }
        
        @Test
        @DisplayName("Same ID should be equal")
        void testSameId() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("Jane", "Other", OrderType.Priority.URGENT);
            order2.setId(order1.getId());
            
            assertEquals(order1, order2);
        }
        
        @Test
        @DisplayName("Equal orders should have same hash code")
        void testHashCodeConsistency() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            assertEquals(order.hashCode(), order.hashCode());
        }
        
        @Test
        @DisplayName("Different orders should have different hash codes")
        void testDifferentHashCodes() {
            Order order1 = new Order("John", "Product", OrderType.Priority.NORMAL);
            Order order2 = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            assertNotEquals(order1.hashCode(), order2.hashCode());
        }
    }
    
    @Nested
    @DisplayName("Setter and Getter Tests")
    class SetterGetterTests {
        
        @Test
        @DisplayName("Should set and get all statuses")
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
        @DisplayName("Should set and get customer name")
        void testCustomerName() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            order.setCustomerName("Jane Doe");
            assertEquals("Jane Doe", order.getCustomerName());
            
            order.setCustomerName("Bob Smith");
            assertEquals("Bob Smith", order.getCustomerName());
        }
        
        @Test
        @DisplayName("Should set and get product")
        void testProduct() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            order.setProduct("Laptop");
            assertEquals("Laptop", order.getProduct());
            
            order.setProduct("Smartphone");
            assertEquals("Smartphone", order.getProduct());
        }
        
        @Test
        @DisplayName("Should set and get priority")
        void testPriority() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            order.setPriority(OrderType.Priority.URGENT);
            assertEquals(OrderType.Priority.URGENT, order.getPriority());
            
            order.setPriority(OrderType.Priority.NORMAL);
            assertEquals(OrderType.Priority.NORMAL, order.getPriority());
        }
        
        @Test
        @DisplayName("Should set and get ID")
        void testId() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            String newId = "custom-id-123";
            
            order.setId(newId);
            assertEquals(newId, order.getId());
        }
        
        @Test
        @DisplayName("Should set and get createdAt")
        void testCreatedAt() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            LocalDateTime customTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            
            order.setCreatedAt(customTime);
            assertEquals(customTime, order.getCreatedAt());
        }
    }
    
    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {
        
        @Test
        @DisplayName("Should create order with URGENT priority")
        void testUrgentPriority() {
            Order order = new Order("John", "Product", OrderType.Priority.URGENT);
            assertEquals(OrderType.Priority.URGENT, order.getPriority());
        }
        
        @Test
        @DisplayName("Should create order with NORMAL priority")
        void testNormalPriority() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertEquals(OrderType.Priority.NORMAL, order.getPriority());
        }
        
        @Test
        @DisplayName("Should change priority after creation")
        void testChangePriority() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            
            order.setPriority(OrderType.Priority.URGENT);
            assertEquals(OrderType.Priority.URGENT, order.getPriority());
        }
    }
    
    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should contain customer name in toString")
        void testToStringContainsCustomer() {
            Order order = new Order("John Doe", "Laptop", OrderType.Priority.URGENT);
            assertTrue(order.toString().contains("John Doe"));
        }
        
        @Test
        @DisplayName("Should contain product in toString")
        void testToStringContainsProduct() {
            Order order = new Order("John", "Gaming Laptop", OrderType.Priority.URGENT);
            assertTrue(order.toString().contains("Gaming Laptop"));
        }
        
        @Test
        @DisplayName("Should contain priority in toString")
        void testToStringContainsPriority() {
            Order urgentOrder = new Order("John", "Product", OrderType.Priority.URGENT);
            Order normalOrder = new Order("Jane", "Product", OrderType.Priority.NORMAL);
            
            assertTrue(urgentOrder.toString().contains("URGENT"));
            assertTrue(normalOrder.toString().contains("NORMAL"));
        }
        
        @Test
        @DisplayName("Should contain ID in toString")
        void testToStringContainsId() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertTrue(order.toString().contains(order.getId()));
        }
        
        @Test
        @DisplayName("Should contain status in toString")
        void testToStringContainsStatus() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertTrue(order.toString().contains("CREATED"));
            
            order.setStatus(Order.OrderStatus.COMPLETED);
            assertTrue(order.toString().contains("COMPLETED"));
        }
        
        @Test
        @DisplayName("Should return non-null and non-empty toString")
        void testToStringNotNull() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            String result = order.toString();
            
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
