package org.nrg.xft.utils;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;

public class TestXftStringUtils {
    @Test
    public void testCleanColumnName() {
        final String cleanColumnName = XftStringUtils.cleanColumnName(",:.-\\;'\"?!~`#$%^&*()+=|{}<>/@[]");
        assertEquals("_com__col__________________________", cleanColumnName);
    }

    @Test
    public void testCreateAlias() {
        final String alias1 = XftStringUtils.CreateAlias("this_is_a_long_table_name_that_could_possibly_break_things_itself", "here's a long column name that isn't even really a column name!");
        final String alias2 = XftStringUtils.CreateAlias("this_is_a_long_table_name_that_could_possibly_break_things_itself", "here's a long column name that isn't even really a column name but it's different from the other one!");
        final String alias3 = XftStringUtils.CreateAlias("very_tidy", "not longer than 63 chars");
        assertThat(alias1).hasSize(63).isEqualTo("this_is_a_long_table_name_that_could_possibly_break_th_1b4d87bd");
        assertThat(alias2).hasSize(63).isEqualTo("this_is_a_long_table_name_that_could_possibly_break_th_6a3849e0").isNotEqualTo(alias1);
        assertThat(alias3).hasSizeLessThan(63).isEqualTo("very_tidy_not_longer_than_63_chars").isNotEqualTo(alias1).isNotEqualTo(alias2);
    }
}
