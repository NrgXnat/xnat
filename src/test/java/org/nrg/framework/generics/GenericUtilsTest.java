package org.nrg.framework.generics;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenericUtilsTest {
    @Test
    public void testConvertToTypedList() {
        final AnnoyingClass        annoyingClass = new AnnoyingClass();
        final List<String>         itemList      = GenericUtils.convertToTypedList(annoyingClass.getItemList(), String.class);
        final Map<String, Integer> itemMap       = GenericUtils.convertToTypedMap(annoyingClass.getItemMap(), String.class, Integer.class);
        assertThat(itemList).isNotNull().isNotEmpty().containsExactly("one", "two", "three");
        assertThat(itemMap).isNotNull().isNotEmpty().containsEntry("one", 1).containsEntry("two", 2).containsEntry("three", 3);
    }

    @SuppressWarnings("rawtypes")
    private static class AnnoyingClass {
        public AnnoyingClass() {
            _itemList = Arrays.asList("one", "two", "three");
            _itemMap = ImmutableMap.builder().put("one", 1).put("two", 2).put("three", 3).build();
        }

        public List getItemList() {
            return _itemList;
        }

        public Map getItemMap() {
            return _itemMap;
        }

        private final List _itemList;
        private final Map  _itemMap;
    }
}
