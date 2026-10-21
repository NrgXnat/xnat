package org.nrg.xnat.helpers.prearchive;

import org.junit.Test;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * When XNAT checks the prearchive table against the disk at startup, a session in ERROR keeps that status. Other sessions
 * take the status worked out from their XML file.
 */
public class TestPrearcDatabaseRefresh {
    @Test
    public void aSessionInErrorStaysInError() {
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.ERROR, PrearcStatus.RECEIVING)).isNull();
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.ERROR, PrearcStatus.READY)).isNull();
    }

    @Test
    public void otherSessionsTakeTheStatusReadFromTheirFolder() {
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.READY, PrearcStatus.RECEIVING)).isEqualTo(PrearcStatus.RECEIVING);
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.RECEIVING, PrearcStatus.READY)).isEqualTo(PrearcStatus.READY);
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus._BUILDING, PrearcStatus.RECEIVING)).isEqualTo(PrearcStatus.RECEIVING);
    }

    @Test
    public void anUnchangedOrUnreadableStatusLeavesTheSessionAsItIs() {
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.READY, PrearcStatus.READY)).isNull();
        assertThat(PrearcDatabase.statusAfterRefresh(PrearcStatus.READY, null)).isNull();
    }
}
