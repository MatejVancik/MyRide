/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by matej on 24/07/16.
 */

public class CollectionUtils {

    /**
     * Filters items based on result of {@link IValidator#isValid(Object)}. If any of parameters is null
     * method returns null.
     */
    @Nullable
    public static <T> List<T> filter(@Nullable Collection<T> collection, @Nullable IValidator<T> validator) {
        if (collection == null || validator == null) return null;
        List<T> result = new ArrayList<>();
        for (T element : collection) {
            if (validator.isValid(element)) {
                result.add(element);
            }
        }
        return result;
    }

    public interface IValidator<T> {
        boolean isValid(T object);
    }

    public interface IAction<T> {
        void apply(T object);
    }

    public interface IMapAction<K, V> {
        void apply(K key, V value);
    }

    public static <K, V> void iterateMap(Map<K, V> map, IMapAction<K, V> action) {
        if (map == null || action == null) return;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            action.apply(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Iterates over {@code iterable} and performs {@link IAction#apply(Object)}
     * action defined by {@code action} parameter on each element.
     * If any of parameters is null method does not perform iteration.
     */
    public static <T> void forEach(@Nullable Iterable<T> iterable, @Nullable IAction<T> action) {
        if (iterable == null || action == null) return;
        for (T element : iterable) {
            action.apply(element);
        }
    }

    /**
     * Performs optimized look over {@code list} and performs {@link IAction#apply(Object)}
     * action defined by {@code action} parameter on each element.
     * If any of parameters is null method does not perform iteration.
     */
    public static <T> void forEachOptimized(List<T> list, IAction<T> action) {
        if (list == null || action == null) return;
        for (int i = 0, size = list.size(); i < size; i++) {
            final T t = list.get(i);
            action.apply(t);
        }
    }
}

