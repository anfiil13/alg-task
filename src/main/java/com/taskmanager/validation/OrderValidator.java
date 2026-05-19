package com.taskmanager.validation;

import com.taskmanager.annotations.NotNull;
import com.taskmanager.annotations.NotEmpty;

import java.lang.reflect.Field;

public class OrderValidator {

    public static void validate(Object object) throws IllegalAccessException {
        if (object == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }

        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(object);

            if (field.isAnnotationPresent(NotNull.class)) {
                NotNull annotation = field.getAnnotation(NotNull.class);
                if (value == null) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }

            if (field.isAnnotationPresent(NotEmpty.class)) {
                NotEmpty annotation = field.getAnnotation(NotEmpty.class);
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue != null && strValue.isEmpty()) {
                        throw new IllegalArgumentException(annotation.message());
                    }
                }
            }
        }
    }
}
