package org.nrg.xnat.archive.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: the delete path claims a session by moving it to DELETING, which is only allowed from the two resting
 * statuses. Directory ownership is decided by whether any other session at the same location is still alive.
 */
public class DirectArchiveSessionHibernateServiceImplTest {
    private static final long   SESSION_ID = 42L;
    private static final String LOCATION   = "/data/xnat/archive/PROJ/arc001/SESSION_01";

    private DirectArchiveSessionDao                 dao;
    private DirectArchiveSessionHibernateServiceImpl service;

    @Before
    public void setUp() {
        dao     = mock(DirectArchiveSessionDao.class);
        service = new DirectArchiveSessionHibernateServiceImpl();
        service.setDao(dao);
    }

    private static DirectArchiveSession sessionIn(final long id, final PrearcStatus status) {
        final DirectArchiveSession session = new DirectArchiveSession();
        session.setId(id);
        session.setStatus(status);
        session.setLocation(LOCATION);
        return session;
    }

    @Test
    public void aRestingSessionCanBeClaimedForDeletion() throws Exception {
        for (final PrearcStatus status : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR)) {
            final DirectArchiveSession session = sessionIn(SESSION_ID, status);
            when(dao.retrieve(SESSION_ID)).thenReturn(session);

            assertThat(service.setStatusToDeletingAndReturn(SESSION_ID).getStatus()).as("from %s", status).isEqualTo(PrearcStatus.DELETING);

            assertThat(session.getStatus()).isEqualTo(PrearcStatus.DELETING);
        }
        verify(dao, times(2)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void aSessionInAnyOtherStatusCannotBeClaimed() {
        final EnumSet<PrearcStatus> notDeletable = EnumSet.complementOf(EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR));
        for (final PrearcStatus status : notDeletable) {
            final DirectArchiveSession session = sessionIn(SESSION_ID, status);
            when(dao.retrieve(SESSION_ID)).thenReturn(session);

            assertThatThrownBy(() -> service.setStatusToDeletingAndReturn(SESSION_ID)).as("from %s", status).isInstanceOf(ArchivingException.class);

            assertThat(session.getStatus()).isEqualTo(status);
        }
        verify(dao, never()).update(any());
    }

    @Test
    public void locationIsActiveWhenAnotherSessionThereIsNotInError() {
        when(dao.findByLocation(LOCATION)).thenReturn(Arrays.asList(sessionIn(SESSION_ID, PrearcStatus.ERROR),
                                                                     sessionIn(43L, PrearcStatus.RECEIVING)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isTrue();
    }

    @Test
    public void locationIsNotActiveWhenEveryOtherSessionThereIsInError() {
        when(dao.findByLocation(LOCATION)).thenReturn(Arrays.asList(sessionIn(SESSION_ID, PrearcStatus.RECEIVING),
                                                                     sessionIn(43L, PrearcStatus.ERROR)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isFalse();
    }

    @Test
    public void withoutAnExclusionEverySessionAtTheLocationCounts() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.singletonList(sessionIn(SESSION_ID, PrearcStatus.RECEIVING)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, null)).isTrue();
        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isFalse();
    }

    @Test
    public void anEmptyLocationIsNotActive() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.emptyList());

        assertThat(service.hasActiveSessionAtLocation(LOCATION, null)).isFalse();
    }
}
