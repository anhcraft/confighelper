package dev.anhcraft.confighelper.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExampleList.class)
public @interface Example {
    String[] value();
}
