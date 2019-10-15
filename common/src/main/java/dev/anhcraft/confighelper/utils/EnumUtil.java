package dev.anhcraft.confighelper.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class EnumUtil {
    private static final Map<Class<? extends Enum>, List<String>> ENUM_MAP = new WeakHashMap<>();

    public static <T extends Enum> Object findEnum(Class<T> clazz, String name){
        List<String> list = ENUM_MAP.get(clazz);
        if(list == null) {
            if(clazz.isEnum()){
                list = Arrays.stream(clazz.getEnumConstants()).map(Enum::toString).collect(Collectors.toList());
                ENUM_MAP.put(clazz, list);
            } else {
                return null;
            }
        }

        name = name.toUpperCase();
        if(list.contains(name)) {
            return Enum.valueOf(clazz, name);
        } else {
            return null;
        }
    }
}
