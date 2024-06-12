package org.nrg.framework.constants;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestPrearchiveCode {
    @Test
    public void testGetByCode() {
        assertThat(PrearchiveCode.Manual).hasFieldOrPropertyWithValue("code", 0);
        assertThat(PrearchiveCode.AutoArchive).hasFieldOrPropertyWithValue("code", 4);
        assertThat(PrearchiveCode.AutoArchiveOverwrite).hasFieldOrPropertyWithValue("code", 5);
        assertThat(PrearchiveCode.code(0)).isEqualTo(PrearchiveCode.Manual);
        assertThat(PrearchiveCode.code(4)).isEqualTo(PrearchiveCode.AutoArchive);
        assertThat(PrearchiveCode.code(5)).isEqualTo(PrearchiveCode.AutoArchiveOverwrite);
        assertThat(PrearchiveCode.code("0")).isEqualTo(PrearchiveCode.Manual);
        assertThat(PrearchiveCode.code("4")).isEqualTo(PrearchiveCode.AutoArchive);
        assertThat(PrearchiveCode.code("5")).isEqualTo(PrearchiveCode.AutoArchiveOverwrite);
        assertThat(PrearchiveCode.normalize("Manual")).isEqualTo(PrearchiveCode.Manual);
        assertThat(PrearchiveCode.normalize("AutoArchive")).isEqualTo(PrearchiveCode.AutoArchive);
        assertThat(PrearchiveCode.normalize("AutoArchiveOverwrite")).isEqualTo(PrearchiveCode.AutoArchiveOverwrite);
        assertThat(PrearchiveCode.normalize("manual")).isEqualTo(PrearchiveCode.Manual);
        assertThat(PrearchiveCode.normalize("autoarchive")).isEqualTo(PrearchiveCode.AutoArchive);
        assertThat(PrearchiveCode.normalize("autoarchiveoverwrite")).isEqualTo(PrearchiveCode.AutoArchiveOverwrite);
        assertThat(PrearchiveCode.normalize("MANUAL")).isEqualTo(PrearchiveCode.Manual);
        assertThat(PrearchiveCode.normalize("AUTOARCHIVE")).isEqualTo(PrearchiveCode.AutoArchive);
        assertThat(PrearchiveCode.normalize("AUTOARCHIVEOVERWRITE")).isEqualTo(PrearchiveCode.AutoArchiveOverwrite);
    }
}
