package com.taskmanager.validation;

import com.taskmanager.annotations.OrderType;
import com.taskmanager.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Validator Tests")
class OrderValidatorTest {

    @Nested
    @DisplayName("Valid Orders")
    class ValidOrderTests {

        @Test
        @DisplayName("Should validate order with all valid fields")
        void testValidOrder() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }

        @Test
        @DisplayName("Should validate order with urgent priority")
        void testValidUrgentOrder() {
            Order order = new Order("Jane", "Laptop", OrderType.Priority.URGENT);
            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }

        @Test
        @DisplayName("Should validate order with long customer name")
        void testValidOrderWithLongName() {
            Order order = new Order("John Michael Smith Jr.", "Product", OrderType.Priority.NORMAL);
            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }
    }

    @Nested
    @DisplayName("Null Object Tests")
    class NullObjectTests {

        @Test
        @DisplayName("Should throw exception when validating null object")
        void testNullObject() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(null)
            );
            assertTrue(exception.getMessage().contains("cannot be null"));
        }
    }

    @Nested
    @DisplayName("ID Validation Tests")
    class IdValidationTests {

        @Test
        @DisplayName("Should throw exception when ID is null")
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
        @DisplayName("Should validate order with empty ID")
        void testEmptyId() {
            Order order = new Order("John", "Product", OrderType.Priority.NORMAL);
            order.setId("");


            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }
    }

    @Nested
    @DisplayName("Customer Name Validation Tests")
    class CustomerNameTests {

        @Test
        @DisplayName("Should throw exception when customer name is null")
        void testNullCustomerName() {
            Order order = new Order(null, "Product", OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Customer name"));
        }

        @Test
        @DisplayName("Should throw exception when customer name is empty")
        void testEmptyCustomerName() {
            Order order = new Order("", "Product", OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Customer name"));
        }

        @Test
        @DisplayName("Should throw exception when customer name is only spaces")
        void testSpacesCustomerName() {
            Order order = new Order("   ", "Product", OrderType.Priority.NORMAL);


            assertDoesNotThrow(() -> OrderValidator.validate(order));
        }
    }

    @Nested
    @DisplayName("Product Validation Tests")
    class ProductTests {

        @Test
        @DisplayName("Should throw exception when product is null")
        void testNullProduct() {
            Order order = new Order("John", null, OrderType.Priority.NORMAL);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );
            assertTrue(exception.getMessage().contains("Product"));
        }

        @Test
        @DisplayName("Should throw exception when product is empty")
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
    @DisplayName("Multiple Field Validation Tests")
    class MultipleFieldTests {

        @Test
        @DisplayName("Should validate first field and throw appropriate message")
        void testMultipleInvalidFields() {
            Order order = new Order(null, null, OrderType.Priority.NORMAL);
            order.setId(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> OrderValidator.validate(order)
            );

            assertNotNull(exception.getMessage());
        }
    }
}
