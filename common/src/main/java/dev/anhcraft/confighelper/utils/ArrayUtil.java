package dev.anhcraft.confighelper.utils;

import java.lang.reflect.Array;

public class ArrayUtil {
    public static <T> T[] concat(T[] a, T[] b){
        if(a.length == 0) return b;
        if(b.length == 0) return a;
        int length = a.length + b.length;
        T[] na = (T[]) Array.newInstance(a.getClass().getComponentType(), length);
        int i = 0;
        for(T x : a){
            na[i++] = x;
        }
        for(T x : b){
            na[i++] = x;
        }
        return na;
    }
}
