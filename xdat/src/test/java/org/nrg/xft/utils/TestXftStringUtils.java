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

    /**
     * XNAT-6374: SubQueryField aliases are built as {@code <FIELD_ID>_<value>}, where the value is a
     * custom variable name or a workflow pipeline name. Two values that differ only after PostgreSQL's
     * 63-byte identifier limit produced the same column name, and the query then failed with
     * "column reference ... is ambiguous". The lengths and shared prefixes here match the cases that were reported.
     */
    @Test
    public void testSubQueryFieldAliasesStayDistinctPastTheIdentifierLimit() {
        // Custom variables on a data type — the case XNAT-6374 was filed for.
        final String customVar1 = XftStringUtils.formatPostgreSQLIdentifier(XftStringUtils.cleanColumnName("XNAT_MRSESSIONDATA_FIELD_MAP_radiotherapy_planning_dose_constraint_alpha"));
        final String customVar2 = XftStringUtils.formatPostgreSQLIdentifier(XftStringUtils.cleanColumnName("XNAT_MRSESSIONDATA_FIELD_MAP_radiotherapy_planning_dose_constraint_beta"));
        assertThat(customVar1).hasSize(63);
        assertThat(customVar2).hasSize(63).isNotEqualTo(customVar1);

        // Container-service pipeline names on the Processing Dashboard — same alias, same collision.
        // Two wrappers over one pipeline, differing only in the "-scan" / "-session" suffix.
        final String pipeline1 = XftStringUtils.formatPostgreSQLIdentifier(XftStringUtils.cleanColumnName("WRK_STATUS_CID_example_segmentation_pipeline_with_a_long_name_v3-scan"));
        final String pipeline2 = XftStringUtils.formatPostgreSQLIdentifier(XftStringUtils.cleanColumnName("WRK_STATUS_CID_example_segmentation_pipeline_with_a_long_name_v3-session"));
        assertThat(pipeline1).hasSize(63);
        assertThat(pipeline2).hasSize(63).isNotEqualTo(pipeline1);

        // Truncation alone is what collided: without the hash suffix both sides are identical.
        assertThat(pipeline1.substring(0, 54)).isEqualTo(pipeline2.substring(0, 54));

        // Values inside the limit must pass through untouched, so existing column names do not move.
        assertThat(XftStringUtils.formatPostgreSQLIdentifier(XftStringUtils.cleanColumnName("WRK_STATUS_CID_short_pipeline-session"))).isEqualTo("WRK_STATUS_CID_short_pipeline_session");
    }
}
