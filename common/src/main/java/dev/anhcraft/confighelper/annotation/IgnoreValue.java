package dev.anhcraft.confighelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreValue {
    boolean ifNull() default false;
    boolean ifEmptyArray() default false;
    boolean ifEmptyList() default false;
    boolean ifEmptyString() default false;
}
