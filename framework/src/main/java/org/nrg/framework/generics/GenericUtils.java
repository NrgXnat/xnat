/*
 * framework: org.nrg.framework.generics.GenericUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.generics;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides a number of utility methods for converting untyped iterables to typed iterables, as well as converting
 * between different iterable implementations, e.g. from set to list.
 */
public class GenericUtils {
    /**
     * Converts a typed iterable to another typed iterable.  This differs from {@link #convertToTypedIterable(Iterable,
     * Class, Collector)} in that the input iterable for this method is already typed. The type of iterable returned is
     * determined by the <pre>collector</pre> parameter.
     *
     * @param objects   The iterable collection of objects to convert
     * @param collector The collector for the return type
     * @param <T>       The type of objects in the iterable
     * @param <I>       The type of iterable
     *
     * @return A typed iterable of objects from the typed input iterable
     */
    public static <T, I extends Iterable<T>> I convertToTypedIterable(final @Nonnull Iterable<T> objects, final Collector<T, ?, I> collector) {
        return StreamSupport.stream(objects.spliterator(), false).collect(collector);
    }

    /**
     * Converts an untyped iterable to a typed iterable. This differs from {@link #convertToTypedIterable(Iterable,
     * Collector)} method in that the input iterable is <em>not</em> already typed: this method filters out any objects
     * that are not of the indicated class.
     *
     * @param objects   The iterable collection of objects to convert
     * @param clazz     The class on which to filter
     * @param collector The collector for the return type
     * @param <T>       The type of objects in the iterable
     * @param <I>       The type of iterable
     *
     * @return A typed iterable of objects from the untyped iterable.
     */
    public static <T, I extends Iterable<T>> I convertToTypedIterable(final @Nonnull Iterable<?> objects, final Class<T> clazz, final Collector<T, ?, I> collector) {
        return StreamSupport.stream(objects.spliterator(), false)
                            .filter(clazz::isInstance)
                            .map(clazz::cast)
                            .collect(collector);
    }

    /**
     * Converts a typed iterable to a typed set.  This differs from {@link #convertToTypedSet(Iterable, Class)} in that
     * the input iterable for this method is already typed.
     *
     * @param objects The iterable collection of objects to convert
     * @param <T>     The type of objects in the iterable
     *
     * @return A typed set of objects from the typed input iterable
     */
    public static <T> Set<T> convertToTypedSet(final @Nonnull Iterable<T> objects) {
        return convertToTypedIterable(objects, Collectors.toSet());
    }

    /**
     * Converts an untyped iterable to a typed set. This differs from {@link #convertToTypedSet(Iterable)} method in
     * that the input iterable is <em>not</em> already typed: this method filters out any objects that are not of the
     * indicated class.
     *
     * @param objects The iterable collection of objects to convert
     * @param clazz   The class on which to filter
     * @param <T>     The type of objects in the iterable
     *
     * @return A typed set of objects from the untyped iterable.
     */
    public static <T> Set<T> convertToTypedSet(final @Nonnull Iterable<?> objects, final Class<T> clazz) {
        return convertToTypedIterable(objects, clazz, Collectors.toSet());
    }

    /**
     * Converts a typed iterable to a typed list.  This differs from {@link #convertToTypedList(Iterable, Class)} in
     * that the input iterable for this method is already typed.
     *
     * @param objects The iterable collection of objects to convert
     * @param <T>     The type of objects in the iterable
     *
     * @return A typed list of objects from the typed input iterable
     */
    public static <T> List<T> convertToTypedList(final @Nonnull Iterable<T> objects) {
        return convertToTypedIterable(objects, Collectors.toList());
    }

    /**
     * Converts an untyped iterable to a typed list. This differs from {@link #convertToTypedList(Iterable)} method in
     * that the input iterable is <em>not</em> already typed: this method filters out any objects that are not of the
     * indicated class.
     *
     * @param objects The iterable collection of objects to convert
     * @param clazz   The class on which to filter
     * @param <T>     The type of objects in the iterable
     *
     * @return A typed list of objects from the untyped iterable.
     */
    public static <T> List<T> convertToTypedList(final @Nonnull Iterable<?> objects, final Class<T> clazz) {
        return convertToTypedIterable(objects, clazz, Collectors.toList());
    }

    /**
     * Converts an untyped map to a map typed with the specified classes. Note that this method filters the objects in the
     * map and omits any entries where the key and value are <em>not</em> instances of the specified types.
     *
     * @param objects The map of objects to convert
     * @param <K>     The type of objects used as the map keys
     * @param <V>     The type of objects used as the map values
     *
     * @return A typed map of objects from the source map
     */
    public static <K, V> Map<K, V> convertToTypedMap(final @Nonnull Map<?, ?> objects, final Class<K> keyClazz, final Class<V> valueClazz) {
        return objects.entrySet().stream()
                      .filter(entry -> keyClazz.isInstance(entry.getKey()) && valueClazz.isInstance(entry.getValue()))
                      .collect(Collectors.toMap(entry -> keyClazz.cast(entry.getKey()), entry -> valueClazz.cast(entry.getValue())));
    }
}
