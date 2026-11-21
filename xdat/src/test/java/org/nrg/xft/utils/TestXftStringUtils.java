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
     * XNAT-6374: the column a subquery display field (a custom variable or a pipeline's workflow status) gets in the generated
     * SQL. Distinct values must get distinct columns, whether they differ past PostgreSQL's 63-character identifier limit or
     * only in characters that cleaning or lower-casing merges.
     */
    @Test
    public void testCreateSubqueryFieldAlias() {
        // Custom variables that differ only past the identifier limit.
        final String alpha = XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "radiotherapy_planning_dose_constraint_alpha");
        final String beta  = XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "radiotherapy_planning_dose_constraint_beta");
        assertThat(alpha).hasSize(63);
        assertThat(beta).hasSize(63).isNotEqualToIgnoringCase(alpha);

        // Pipeline names that differ only past the limit, in characters that cleaning also changes.
        final String scan    = XftStringUtils.createSubqueryFieldAlias("WRK_STATUS_CID", "example_segmentation_pipeline_with_a_long_name_v3-scan");
        final String session = XftStringUtils.createSubqueryFieldAlias("WRK_STATUS_CID", "example_segmentation_pipeline_with_a_long_name_v3-session");
        assertThat(scan).hasSizeLessThanOrEqualTo(63);
        assertThat(session).hasSizeLessThanOrEqualTo(63).isNotEqualToIgnoringCase(scan);

        // Values that clean to the same string, well under the limit.
        assertThat(XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "dose-alpha")).isNotEqualToIgnoringCase(XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "dose_alpha"));
        assertThat(XftStringUtils.createSubqueryFieldAlias("WRK_STATUS", "model_v2.4-scan")).isNotEqualToIgnoringCase(XftStringUtils.createSubqueryFieldAlias("WRK_STATUS", "model_v2_4-scan"));
        assertThat(XftStringUtils.createSubqueryFieldAlias("WRK_STATUS", "batch process-session")).isNotEqualToIgnoringCase(XftStringUtils.createSubqueryFieldAlias("WRK_STATUS", "batch_process-session"));

        // Values that differ only in case.
        assertThat(XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "Segment")).isNotEqualToIgnoringCase(XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "segment"));

        // A value that cleaning and lower-casing leave alone keeps the column name it always had.
        assertThat(XftStringUtils.createSubqueryFieldAlias("XNAT_MRSESSIONDATA_FIELD_MAP", "dose_alpha")).isEqualTo("XNAT_MRSESSIONDATA_FIELD_MAP_dose_alpha");
    }
}
