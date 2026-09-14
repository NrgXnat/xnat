package org.nrg.xnat.archive.services;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.xapi.DirectArchiveSessionPaginatedRequest;
import org.nrg.xnat.helpers.prearchive.SessionData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface DirectArchiveSessionService {
    void delete(SessionData session);

    /**
     * Deletes a direct archive session on behalf of a user: the tracking row and, when the session directory belongs
     * to this session alone, the files received into it along with the session XML. Sessions the archiver is
     * building or archiving cannot be deleted.
     *
     * @param id          The direct archive session id
     * @param sessionUser The user requesting the delete; must be able to delete from the session's project
     *
     * @throws InvalidPermissionException If the user cannot delete from the project
     * @throws NotFoundException          If no session has the given id
     * @throws ClientException            If the session is currently being built or archived (409)
     * @throws ServerException            If the session files could not be removed; the row is left in place
     */
    void delete(long id, UserI sessionUser) throws InvalidPermissionException, NotFoundException, ClientException, ServerException;

    void touch(SessionData session) throws NotFoundException;

    SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException;

    SessionData getOrCreate(SessionData initialize, AtomicBoolean isNew, String overwriteMode) throws ArchivingException;

    void build(long id) throws NotFoundException, ArchivingException;
    void archive(long id) throws NotFoundException, ArchivingException;

    void triggerArchive();
    void triggerArchive(@Nonnull SessionData session) throws ClientException, ServerException;

    List<SessionData> getPaginated(UserI user, DirectArchiveSessionPaginatedRequest request);
}
