package dev.anhcraft.confighelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Middleware {
    enum Direction {
        SCHEMA_TO_CONFIG,
        CONFIG_TO_SCHEMA,
        ALL
    }

    Direction value() default Direction.ALL;
}
