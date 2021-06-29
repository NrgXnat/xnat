package org.nrg.xnat.archive.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.util.List;

public interface DirectArchiveSessionHibernateService extends BaseHibernateService<DirectArchiveSession> {
    void touch(long id) throws NotFoundException;

    SessionData findBySessionData(SessionData incoming);
    SessionData findByProjectTagName(UserI user, String project, String tag, String name);

    SessionData create(SessionData initialize) throws ArchivingException;

    SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException;
    SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException;

    void setStatusToError(long id, Exception e) throws NotFoundException;
    void setStatusToQueuedBuilding(long id) throws NotFoundException;
    void setStatusToQueuedArchiving(long id) throws NotFoundException;
    void setStatusBackToReceiving(long id);

    List<SessionData> findReadyForArchive();

    SessionData getSessionData(long id) throws NotFoundException;
}
