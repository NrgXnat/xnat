package org.nrg.framework.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final List<String>        STRING_LIST        = Arrays.asList("A", "B", "C", "D", "E");
    private static final Iterator<String>    STRING_ITERATOR    = STRING_LIST.iterator();
    private static final Enumeration<String> STRING_ENUMERATION = Collections.enumeration(STRING_LIST);
    private static final String              JOINED_STRINGS     = String.join(", ", STRING_LIST);
    private static final Map<String, String> STRING_STRING_MAP  = IntStream.range(0, STRING_LIST.size()).boxed().collect(Collectors.toMap(STRING_LIST::get, index -> Integer.toString(index)));
}
