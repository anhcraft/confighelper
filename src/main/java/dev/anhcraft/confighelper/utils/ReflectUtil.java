package dev.anhcraft.confighelper.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectUtil {
    public static Field[] getAllFields(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);
        Field[] fields = clazz.getDeclaredFields();
        while(!Objects.equals(clazz = clazz.getSuperclass(), Object.class)){
            fields = (Field[]) ArrayUtils.addAll(fields, clazz.getDeclaredFields());
        }
        return fields;
    }
}
