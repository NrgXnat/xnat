package org.nrg.framework.utilities;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
    private StreamUtils() {
        // No extension or construction permitted
    }

    /**
     * Converts the submitted enumeration to a stream. This calls the {@link #asStream(Enumeration, boolean)} method
     * with  <b>parallel</b> set to <b>false</b>.
     *
     * @param enumeration The enumeration to convert
     * @param <T>         The type of objects in the enumeration
     *
     * @return A stream of objects from the enumeration.
     */
    public static <T> Stream<T> asStream(final Enumeration<T> enumeration) {
        return asStream(enumeration, false);
    }

    /**
     * Converts the submitted enumeration to a stream.
     *
     * @param enumeration The enumeration to convert
     * @param parallel    Indicates whether the stream should be parallel.
     * @param <T>         The type of objects in the enumeration
     *
     * @return A stream of objects from the enumeration.
     */
    public static <T> Stream<T> asStream(final Enumeration<T> enumeration, final boolean parallel) {
        return asStream(new EnumerationIterator<>(enumeration), parallel);
    }

    /**
     * Converts the submitted enumeration to a stream. This calls the {@link #asStream(Iterator, boolean)} method with
     * <b>parallel</b> set to <b>false</b>.
     *
     * @param iterator The enumeration to convert
     * @param <T>      The type of objects in the enumeration
     *
     * @return A stream of objects from the enumeration.
     */
    public static <T> Stream<T> asStream(final Iterator<T> iterator) {
        return asStream(iterator, false);
    }

    /**
     * Converts the submitted enumeration to a stream.
     *
     * @param iterator The enumeration to convert
     * @param parallel Indicates whether the stream should be parallel.
     * @param <T>      The type of objects in the enumeration
     *
     * @return A stream of objects from the enumeration.
     */
    public static <T> Stream<T> asStream(final Iterator<T> iterator, final boolean parallel) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), parallel);
    }

    /**
     * Creates a predicate that tests whether the string representation of the tested object matches any of the regular
     * expressions in the submitted list.
     *
     * @param regexes The list of regular expressions to match against
     * @param <T>     The type of the objects to be tested
     *
     * @return A predicate object that tests whether objects match any of the submitted regular expressions.
     */
    public static <T> Predicate<T> predicateFromRegexes(final List<String> regexes) {
        return predicateFromPatterns(regexes.stream().map(Pattern::compile).collect(Collectors.toList()));
    }

    /**
     * Creates a predicate that tests whether the string representation of the tested object matches any of the patterns
     * in the submitted list.
     *
     * @param patterns The list of patterns to match against
     * @param <T>      The type of the objects to be tested
     *
     * @return A predicate object that tests whether objects match any of the submitted patterns.
     */
    public static <T> Predicate<T> predicateFromPatterns(final List<Pattern> patterns) {
        return object -> patterns.stream().anyMatch(pattern -> pattern.matcher(object.toString()).matches());
    }

    /**
     * Filters out entries in the map where the key appears in the array of keys.
     *
     * @param map  The map to filter
     * @param keys The keys to remove from the map
     * @param <K>  The key type
     * @param <V>  The value type
     *
     * @return A map with all keys from the array removed
     */
    @SafeVarargs
    public static <K, V> Map<K, V> filterByKeys(final Map<K, V> map, final K... keys) {
        return reduceMapByKey(map, entry -> !ArrayUtils.contains(keys, entry.getKey()));
    }

    /**
     * Filters out entries in the map where the key <i>does not</i> appear in the array of keys.
     *
     * @param map  The map to filter
     * @param keys The keys to retain in the map
     * @param <K>  The key type
     * @param <V>  The value type
     *
     * @return A map with all keys not in the array removed
     */
    @SafeVarargs
    public static <K, V> Map<K, V> retainByKeys(final Map<K, V> map, final K... keys) {
        return reduceMapByKey(map, entry -> ArrayUtils.contains(keys, entry.getKey()));
    }

    /**
     * Filters out entries in the map where the string value of the key matches at least one of the regular expression
     * patterns.
     *
     * @param map      The map to filter
     * @param patterns The regex patterns to match against
     * @param <K>      The key type
     * @param <V>      The value type
     *
     * @return A map with all keys matching any of the regular expression patterns removed
     */
    public static <K, V> Map<K, V> filterByPattern(final Map<K, V> map, final String... patterns) {
        final Predicate<String> predicate = predicateFromRegexes(Arrays.asList(patterns));
        return reduceMapByKey(map, entry -> !predicate.test(entry.getKey().toString()));
    }

    /**
     * Filters out entries in the map where the string value of the key doesn't match any of the regular expression
     * patterns.
     *
     * @param map      The map to filter
     * @param patterns The regex patterns to match against
     * @param <K>      The key type
     * @param <V>      The value type
     *
     * @return A map with all keys not matching any of the regular expression patterns removed
     */
    public static <K, V> Map<K, V> retainByPattern(final Map<K, V> map, final String... patterns) {
        final Predicate<String> predicate = predicateFromRegexes(Arrays.asList(patterns));
        return reduceMapByKey(map, entry -> predicate.test(entry.getKey().toString()));
    }

    /**
     * Partitions the given list into lists with the indicated page size. If the size of the incoming list
     * doesn't divide evenly by the page size, the last list contains whatever number is left over.
     *
     * @param list     The list to be partitioned
     * @param pageSize The size of the page for each partition
     * @param <T>      The type of the list to be partitioned
     *
     * @return A list of lists containing the partitioned items
     */
    public static <T> List<List<T>> partition(final List<T> list, final int pageSize) {
        // TODO: This would be awesome to have as a Collector<T, ?, List<List<T>>>, but that's not straightforward.
        if (pageSize >= list.size()) {
            return Collections.singletonList(list);
        }

        final List<List<T>> partitioned = new ArrayList<>();
        final int           remainder   = list.size() % pageSize;
        final int           limit       = list.size() - remainder;
        final AtomicInteger index       = new AtomicInteger();
        while (index.get() < limit) {
            partitioned.add(list.subList(index.getAndAdd(pageSize), index.get()));
        }
        if (remainder > 0) {
            partitioned.add(list.subList(index.get(), list.size()));
        }
        return partitioned;
    }

    private static <K, V> Map<K, V> reduceMapByKey(final Map<K, V> map, final Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream().filter(predicate).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static class EnumerationIterator<T> implements Iterator<T> {
        private final Enumeration<T> _enumeration;

        EnumerationIterator(final Enumeration<T> enumeration) {
            _enumeration = enumeration;
        }

        public T next() {
            return _enumeration.nextElement();
        }

        public boolean hasNext() {
            return _enumeration.hasMoreElements();
        }

        public void forEachRemaining(final Consumer<? super T> action) {
            while (_enumeration.hasMoreElements()) {
                action.accept(_enumeration.nextElement());
            }
        }
    }
}
