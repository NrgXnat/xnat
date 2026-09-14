package org.nrg.xnat.archive.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.SessionData;

import javax.annotation.Nullable;
import java.util.List;

public interface DirectArchiveSessionHibernateService extends BaseHibernateService<DirectArchiveSession> {
    void touch(long id) throws NotFoundException;

    SessionData findBySessionData(SessionData incoming);

    /**
     * Whether a session other than {@code excludingId} is tracking the given archive directory in a status other than
     * ERROR. More than one session can share a location: a new session may be created at a location whose earlier
     * sessions all ended in ERROR, in which case the directory belongs to the newer session.
     *
     * @param location    The session directory as stored in {@link SessionData#getUrl()}
     * @param excludingId A session id to leave out of the check, or null to consider every session at the location
     * @return true if another session is still using the directory
     */
    boolean hasActiveSessionAtLocation(String location, @Nullable Long excludingId);

    SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException;

    SessionData create(SessionData initialize) throws ArchivingException;

    void setOverwriteMode(long id, String overwriteMode) throws NotFoundException;

    String getOverwriteMode(long id) throws NotFoundException;

    SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException;
    SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException;

    /**
     * Claims a session for deletion by moving it from a resting status (RECEIVING or ERROR) to DELETING. A session
     * that is queued, building or archiving cannot be claimed. Once claimed, the importer no longer appends files to
     * the session and the archive trigger no longer picks it up.
     *
     * @param id The session id
     * @return The claimed session
     * @throws ArchivingException If the session is not in a status from which it can be deleted
     */
    SessionData setStatusToDeletingAndReturn(long id) throws NotFoundException, ArchivingException;

    void setStatusToError(long id, Exception e) throws NotFoundException;
    void setStatusToQueuedBuilding(long id) throws NotFoundException;
    void setStatusToQueuedArchiving(long id) throws NotFoundException;
    void setStatusBackToReceiving(long id);

    List<SessionData> findReadyForArchive();

    SessionData getSessionData(long id) throws NotFoundException;
}
