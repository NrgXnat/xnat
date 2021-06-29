package org.nrg.xnat.archive.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.xapi.DirectArchiveSessionPaginatedRequest;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface DirectArchiveSessionService {
    void delete(SessionData session);

    void touch(SessionData session) throws NotFoundException;

    SessionData findByProjectTagName(UserI sessionUser, String project, String tag, String name) throws InvalidPermissionException;

    SessionData getOrCreate(SessionData initialize, AtomicBoolean isNew) throws ArchivingException;

    void build(long id) throws NotFoundException, ArchivingException;
    void archive(long id) throws NotFoundException, ArchivingException;

    void triggerArchive();

    List<SessionData> getPaginated(UserI user, DirectArchiveSessionPaginatedRequest request);
}
