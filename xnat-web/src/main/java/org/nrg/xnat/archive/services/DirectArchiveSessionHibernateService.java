package org.nrg.xnat.archive.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.SessionData;

import javax.annotation.Nullable;
import java.util.List;

public interface DirectArchiveSessionHibernateService extends BaseHibernateService<DirectArchiveSession> {
    void touch(long id) throws NotFoundException;

    /**
     * Deletes the tracking row for a session, after checking the user can delete the session's project. Only the row
     * is removed: the files are left in place and the session is not claimed first, so this can race the importer.
     *
     * @deprecated Kept so plugins built against earlier 1.10.x releases still link. Use
     *             {@link DirectArchiveSessionService#delete(long, UserI)}, which also removes the session's files and
     *             refuses a session that is still receiving or being archived.
     */
    @Deprecated
    void delete(long id, UserI user) throws InvalidPermissionException, NotFoundException;

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

    /**
     * Whether any other session, whatever its status, records the given location. Unlike
     * {@link #hasActiveSessionAtLocation}, an errored session counts: its files are still in the directory.
     */
    boolean hasOtherSessionAtLocation(String location, @Nullable Long excludingId);

    SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException;

    SessionData create(SessionData initialize) throws ArchivingException;

    void setOverwriteMode(long id, String overwriteMode) throws NotFoundException;

    String getOverwriteMode(long id) throws NotFoundException;

    SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException;
    SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException;

    /**
     * Claims a session for deletion: allowed from RECEIVING, ERROR and DELETING (so an interrupted delete can be
     * retried), refused with {@link ArchivingException} while the session is queued, building or archiving unless
     * {@code force} is set, which claims it from any status. Once claimed, the importer no longer appends files to it
     * and the archive trigger no longer queues it. The claim is a single conditional update, so a concurrent
     * transition on the same row cannot overwrite it.
     *
     * @param force Claim the session whatever its status; for rows the archiver left behind
     */
    void setStatusToDeleting(long id, boolean force) throws NotFoundException, ArchivingException;

    void setStatusToError(long id, Exception e) throws NotFoundException;

    /**
     * Moves a session that is still RECEIVING to ERROR, recording why, as one conditional update. A session that has
     * moved on, e.g. queued for building or claimed by a delete, is left as it is. Once moved, the importer no longer
     * appends files to it and the archive trigger no longer queues it.
     *
     * @return whether the session was moved
     */
    boolean setStatusToErrorIfReceiving(long id, Exception e) throws NotFoundException;

    /**
     * Queues a session for building, as {@link #setStatusToQueuedBuilding(long, boolean)} without {@code force}. Stays
     * {@code void}: plugins built against earlier 1.10.x releases link to this signature. Callers that need to know
     * whether the session was queued without catching an exception use the overload.
     *
     * @throws IllegalStateException when the session is in a status it cannot be queued from, e.g. already queued,
     *                               being built or claimed by a delete; the session is left untouched. Callers that
     *                               go on to build the session themselves must not do so.
     */
    void setStatusToQueuedBuilding(long id) throws NotFoundException;

    /**
     * Queues a session for building. Allowed from RECEIVING and, so a failed archive can be retried, from ERROR. With
     * {@code force} the session is re-queued from any status except a delete in progress, so that a session left
     * queued, building or archiving by a dead worker can be retried.
     *
     * @return false, with the session left untouched, when it is in a status the transition is not allowed from, e.g.
     *         claimed by a delete
     */
    boolean setStatusToQueuedBuilding(long id, boolean force) throws NotFoundException;

    void setStatusToQueuedArchiving(long id) throws NotFoundException;
    void setStatusBackToReceiving(long id);

    List<SessionData> findReadyForArchive();

    SessionData getSessionData(long id) throws NotFoundException;
}
