package org.nrg.xnat.archive.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.util.List;

public interface DirectArchiveSessionHibernateService extends BaseHibernateService<DirectArchiveSession> {
    void touch(long id) throws NotFoundException;

    SessionData findBySessionData(SessionData incoming);

    /**
     * Finds every direct archive session tracking the given archive directory. More than one can exist: a new
     * session may be created at a location whose earlier sessions all ended in ERROR.
     *
     * @param location The session directory as stored in {@link SessionData#getUrl()}
     * @return the matching sessions, empty if none
     */
    List<SessionData> findByLocation(String location);

    SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException;

    SessionData create(SessionData initialize) throws ArchivingException;

    void setOverwriteMode(long id, String overwriteMode) throws NotFoundException;

    String getOverwriteMode(long id) throws NotFoundException;

    SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException;
    SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException;

    void setStatusToError(long id, Exception e) throws NotFoundException;
    void setStatusToQueuedBuilding(long id) throws NotFoundException;
    void setStatusToQueuedArchiving(long id) throws NotFoundException;
    void setStatusBackToReceiving(long id);

    List<SessionData> findReadyForArchive();

    SessionData getSessionData(long id) throws NotFoundException;
}
