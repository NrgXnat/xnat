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
     * to this session alone, the files received into it along with the session XML. Refused with a 409
     * {@link ClientException} while the session is still receiving files or the archiver is working on it; if the
     * files cannot be removed the row is left in ERROR and a {@link ServerException} is thrown.
     */
    default void delete(long id, UserI sessionUser) throws InvalidPermissionException, NotFoundException, ClientException, ServerException {
        delete(id, sessionUser, false);
    }

    /**
     * As {@link #delete(long, UserI)}; with {@code force}, which only a site admin may set, the session is claimed
     * whatever its status. This is for rows left in a queued, building or archiving status by a failure or a restart,
     * which nothing else moves on. Nothing checks that the archiver has really given up: forcing a session that is
     * still being built or archived can leave a half-saved experiment or a build failing on missing files, so check
     * the row's timestamp and the logs first. Files still landing are refused whatever the flag says.
     */
    void delete(long id, UserI sessionUser, boolean force) throws InvalidPermissionException, NotFoundException, ClientException, ServerException;

    /**
     * For the importer, once it holds the file lock for a session: confirms the session is still RECEIVING. Its
     * earlier check in {@link #getOrCreate} runs before the lock is taken, and a delete claims the session and then
     * looks for locks, so only a check made under the lock can guarantee the file is not written into a directory
     * the delete is about to remove.
     *
     * @throws ClientException 409 when the session has been claimed for deletion, has moved on, or is gone
     */
    void requireReceiving(SessionData session) throws ClientException;

    void touch(SessionData session) throws NotFoundException;

    SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException;

    SessionData getOrCreate(SessionData initialize, AtomicBoolean isNew, String overwriteMode) throws ArchivingException;

    void build(long id) throws NotFoundException, ArchivingException;
    void archive(long id) throws NotFoundException, ArchivingException;

    void triggerArchive();
    void triggerArchive(@Nonnull SessionData session) throws ClientException, ServerException;

    /**
     * As {@link #triggerArchive(SessionData)}, on behalf of a user; with {@code force}, which only a site admin may
     * set, the session is re-queued for building from any status except a delete in progress, to retry a session a
     * dead worker left queued, building or archiving. Nothing verifies that no worker is still processing it.
     */
    void triggerArchive(@Nonnull SessionData session, UserI user, boolean force) throws InvalidPermissionException, ClientException, ServerException;

    List<SessionData> getPaginated(UserI user, DirectArchiveSessionPaginatedRequest request);
}
