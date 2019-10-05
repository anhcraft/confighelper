package dev.anhcraft.confighelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validation {
    boolean notNull() default false;
    boolean notEmptyArray() default false;
    boolean notEmptyList() default false;
    boolean notEmptyString() default false;
}
