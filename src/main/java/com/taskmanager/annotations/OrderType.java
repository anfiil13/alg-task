package com.taskmanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderType {
    enum Priority {
        URGENT, NORMAL
    }

    Priority value() default Priority.NORMAL;
}