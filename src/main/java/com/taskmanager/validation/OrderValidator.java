package com.taskmanager.validation;

import com.taskmanager.annotations.Validate;

import java.lang.reflect.Field;

public class OrderValidator {

    public static void validate(Object object) throws IllegalAccessException {
        if (object == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }

        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Validate.class)) {
                field.setAccessible(true);
                Validate annotation = field.getAnnotation(Validate.class);
                Object value = field.get(object);

                if (annotation.notNull() && value == null) {
                    throw new IllegalArgumentException(annotation.message());
                }

                if (annotation.notEmpty() && value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.isEmpty()) {
                        throw new IllegalArgumentException(annotation.message());
                    }
                }
            }
        }
    }
}