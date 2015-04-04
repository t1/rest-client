package com.github.t1.rest;

import static java.util.Collections.*;

import java.util.*;

public class MethodExtensions {
    public static <T> T head(List<T> list) {
        return list.get(0);
    }

    public static <T> List<T> tail(List<T> list) {
        if (list.size() < 2)
            return emptyList();
        return list.subList(1, list.size());
    }

    public static <T> T or(T object, T ifNull) {
        return object != null ? object : ifNull;
    }

    /** return a new list containing all elements plus one more without adding it to the list itself */
    public static <T> List<T> with(List<T> in, T element) {
        List<T> out = new ArrayList<>(in);
        out.add(element);
        return out;
    }
}
