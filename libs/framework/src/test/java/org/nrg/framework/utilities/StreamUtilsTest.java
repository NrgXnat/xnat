package org.nrg.framework.utilities;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link StreamUtils} utility methods.
 */
public class StreamUtilsTest {
    @Test
    public void testEnumerationAsStream() {
        final String joined = StreamUtils.asStream(STRING_ENUMERATION).collect(Collectors.joining(", "));
        assertThat(joined).isNotBlank().isEqualTo(JOINED_STRINGS);
    }

    @Test
    public void testIteratorAsStream() {
        final String joined = StreamUtils.asStream(STRING_ITERATOR).collect(Collectors.joining(", "));
        assertThat(joined).isNotBlank().isEqualTo(JOINED_STRINGS);
    }

    @Test
    public void testFilterByKeyArrayMap() {
        final Map<String, String> filtered = StreamUtils.filterByKeys(STRING_STRING_MAP, "A", "B");
        assertThat(filtered).isNotNull().isNotEmpty().containsKeys("C", "D", "E");
    }

    @Test
    public void testRetainByKeyArrayMap() {
        final Map<String, String> filtered = StreamUtils.retainByKeys(STRING_STRING_MAP, "A", "B");
        assertThat(filtered).isNotNull().isNotEmpty().containsKeys("A", "B");
    }

    @Test
    public void testFilterByPatternMap() {
        final Map<String, String> filtered = StreamUtils.filterByPattern(STRING_STRING_MAP, "^(A|B)$", "^C$");
        assertThat(filtered).isNotNull().isNotEmpty().containsKeys("D", "E");
    }

    @Test
    public void testRetainByPatternMap() {
        final Map<String, String> filtered = StreamUtils.retainByPattern(STRING_STRING_MAP, "^(A|B)$", "^C$");
        assertThat(filtered).isNotNull().isNotEmpty().containsKeys("A", "B", "C");
    }

    @Test
    public void testPredicateFromRegexes() {
        final Predicate<String> predicate = StreamUtils.predicateFromRegexes(Arrays.asList("^(A|B)$", "^C$"));
        final List<String>      matching  = STRING_LIST.stream().filter(predicate).collect(Collectors.toList());
        assertThat(matching).isNotNull().isNotEmpty().contains("A", "B", "C");
    }

    @Test
    public void testCleanPartition() {
        final List<String>       collection  = IntStream.range(0, 10000).boxed().map(index -> RandomStringUtils.randomAscii(10)).collect(Collectors.toList());
        final List<List<String>> partitioned = StreamUtils.partition(collection, 1000);
        assertThat(partitioned).isNotNull().isNotEmpty().hasSize(10);
        final AtomicInteger index = new AtomicInteger();
        for (final List<String> partition : partitioned) {
            assertThat(partition).isNotNull().isNotEmpty().containsExactlyElementsOf(collection.subList(index.getAndAdd(1000), index.get()));
        }
    }

    @Test
    public void testOddPartition() {
        final List<String>       collection  = IntStream.range(0, 9457).boxed().map(index -> RandomStringUtils.randomAscii(10)).collect(Collectors.toList());
        final List<List<String>> partitioned = StreamUtils.partition(collection, 1000);
        assertThat(partitioned).isNotNull().isNotEmpty().hasSize(10);
        final AtomicInteger index = new AtomicInteger();
        for (final List<String> partition : partitioned) {
            assertThat(partition).isNotNull().isNotEmpty().containsExactlyElementsOf(collection.subList(index.getAndAdd(partition.size()), index.get()));
        }
    }

    private static final Random              RANDOM             = new Random();
    private static final List<String>        STRING_LIST        = Arrays.asList("A", "B", "C", "D", "E");
    private static final Iterator<String>    STRING_ITERATOR    = STRING_LIST.iterator();
    private static final Enumeration<String> STRING_ENUMERATION = Collections.enumeration(STRING_LIST);
    private static final String              JOINED_STRINGS     = String.join(", ", STRING_LIST);
    private static final Map<String, String> STRING_STRING_MAP  = IntStream.range(0, STRING_LIST.size()).boxed().collect(Collectors.toMap(STRING_LIST::get, index -> Integer.toString(index)));
}
