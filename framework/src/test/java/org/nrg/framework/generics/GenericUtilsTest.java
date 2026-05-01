package org.nrg.framework.generics;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericUtilsTest {
    @Test
    public void testConvertToTypedList() {
        final AnnoyingClass        annoyingClass   = new AnnoyingClass();
        final List<String>         untypedItemList = GenericUtils.convertToTypedList(annoyingClass.getUntypedItemList(), String.class);
        final Set<String>          untypedItemSet  = GenericUtils.convertToTypedSet(annoyingClass.getUntypedItemList(), String.class);
        final List<String>         typedItemList   = GenericUtils.convertToTypedList(annoyingClass.getTypedItemList());
        final Set<String>          typedItemSet    = GenericUtils.convertToTypedSet(annoyingClass.getTypedItemList());
        final Map<String, Integer> itemMap         = GenericUtils.convertToTypedMap(annoyingClass.getUntypedItemMap(), String.class, Integer.class);
        assertThat(untypedItemList).isNotNull().isNotEmpty().containsExactly("one", "two", "three");
        assertThat(untypedItemSet).isNotNull().isNotEmpty().containsExactly("one", "two", "three");
        assertThat(typedItemList).isNotNull().isNotEmpty().containsExactly("one", "two", "three");
        assertThat(typedItemSet).isNotNull().isNotEmpty().containsExactly("one", "two", "three");
        assertThat(itemMap).isNotNull().isNotEmpty().containsEntry("one", 1).containsEntry("two", 2).containsEntry("three", 3);
    }

    @SuppressWarnings("rawtypes")
    private static class AnnoyingClass {
        public AnnoyingClass() {
            _itemList = Arrays.asList("one", "two", "three");
            _itemMap  = ImmutableMap.<String, Integer>builder().put("one", 1).put("two", 2).put("three", 3).build();
        }

        public List getUntypedItemList() {
            return _itemList;
        }

        public Map getUntypedItemMap() {
            return _itemMap;
        }

        public List<String> getTypedItemList() {
            return _itemList;
        }

        private final List<String>         _itemList;
        private final Map<String, Integer> _itemMap;
    }
}
