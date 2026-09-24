package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DirectArchiveSessionHibernateServiceImpl
        extends AbstractHibernateEntityService<DirectArchiveSession, DirectArchiveSessionDao>
        implements DirectArchiveSessionHibernateService {

    /**
     * The guarded edges of the direct archive state machine: the statuses a session must be in to move to the key
     * status. ERROR and QUEUED_ARCHIVING are reachable from any status and are not listed. QUEUED_BUILDING accepts
     * ERROR so a user can retry a failed archive; DELETING is re-claimable so a delete that died after claiming the
     * session can be retried; RECEIVING is only reachable by undoing a queued build, so a session a delete has
     * claimed is never revived. Each guarded transition is applied by {@link DirectArchiveSessionDao#transitionStatus}
     * as one conditional update, never as a read-check-write on the entity, so two callers racing on a row cannot
     * both win.
     */
    private static final Map<PrearcStatus, Set<PrearcStatus>> GUARDED_TRANSITIONS = Map.of(
            PrearcStatus.QUEUED_BUILDING, EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR),
            PrearcStatus.BUILDING, EnumSet.of(PrearcStatus.QUEUED_BUILDING),
            PrearcStatus.ARCHIVING, EnumSet.of(PrearcStatus.QUEUED_ARCHIVING),
            PrearcStatus.RECEIVING, EnumSet.of(PrearcStatus.QUEUED_BUILDING),
            PrearcStatus.DELETING, EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING));

    @Override
    public void touch(long id) throws NotFoundException {
        if (getDao().touch(id) == 0) {
            throw new NotFoundException("Could not find DirectArchiveSession with ID " + id);
        }
    }

    @Override
    public SessionData findBySessionData(SessionData incoming) {
        DirectArchiveSession das = getDao().findBySessionData(incoming);
        return das == null ? null : das.toSessionData();
    }

    @Override
    public SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException {
        DirectArchiveSession das = getDao().findByProjectTagName(project, tag, name);
        if (das == null) {
            throw new NotFoundException("No matching direct archive session");
        }
        return das.toSessionData();
    }

    @Override
    public boolean hasActiveSessionAtLocation(String location, @Nullable Long excludingId) {
        final List<DirectArchiveSession> sessions = getDao().findByLocation(location);
        return sessions != null && sessions.stream().anyMatch(session -> session.getStatus() != PrearcStatus.ERROR
                                                                        && !Objects.equals(session.getId(), excludingId));
    }

    @Override
    public SessionData create(SessionData initialize) throws ArchivingException {
        String location = initialize.getUrl();
        // Direct archive sessions are removed from db after successful archive, so only in-progress or error cases remain
        // We allow re-archive if a prior attempt errored out
        if (hasActiveSessionAtLocation(location, null)) {
            throw new ArchivingException("Cannot direct archive " + initialize + " due to one or more " +
                    "direct archive sessions with location=\"" + location + "\" in a non-ERROR status");
        }

        return create(new DirectArchiveSession(initialize.getProject(), initialize.getSubject(), initialize.getName(),
                initialize.getTimestamp(), initialize.getFolderName(), initialize.getTag(), initialize.getVisit(),
                initialize.getProtocol(), initialize.getTimeZone(), location, initialize.getSource(),
                initialize.getUploadDate(), initialize.getLastBuiltDate(), initialize.getStatus(),
                initialize.getScan_date(), initialize.getScan_time(), initialize.getPreventAnon())).toSessionData();
    }

    @Override
    public void setOverwriteMode(long id, String overwriteMode) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setOverwriteMode(overwriteMode);
        update(das);
    }

    @Override
    public String getOverwriteMode(long id) throws NotFoundException {
        return get(id).getOverwriteMode();
    }

    @Override
    public SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException {
        return transition(id, PrearcStatus.BUILDING).toSessionData();
    }

    @Override
    public SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException {
        return transition(id, PrearcStatus.ARCHIVING).toSessionData();
    }

    @Override
    public void setStatusToDeleting(long id, boolean force) throws NotFoundException, ArchivingException {
        transition(id, PrearcStatus.DELETING, force ? EnumSet.allOf(PrearcStatus.class) : GUARDED_TRANSITIONS.get(PrearcStatus.DELETING));
    }

    @Override
    public void delete(long id) {
        // Idempotent: the row may already have been removed by a concurrent delete or by the importer
        final DirectArchiveSession das = retrieve(id);
        if (das != null) {
            delete(das);
        }
    }

    @Override
    public void setStatusToError(long id, Exception e) throws NotFoundException {
        setStatus(get(id), PrearcStatus.ERROR, e.getMessage());
    }

    @Override
    public boolean setStatusToQueuedBuilding(long id) throws NotFoundException {
        return setStatusToQueuedBuilding(id, false);
    }

    @Override
    public boolean setStatusToQueuedBuilding(long id, boolean force) throws NotFoundException {
        return transitionIfAllowed(id, PrearcStatus.QUEUED_BUILDING,
                                   force ? EnumSet.complementOf(EnumSet.of(PrearcStatus.DELETING)) : GUARDED_TRANSITIONS.get(PrearcStatus.QUEUED_BUILDING));
    }

    @Override
    public void setStatusToQueuedArchiving(long id) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setUploadDate(new Date());
        setStatus(das, PrearcStatus.QUEUED_ARCHIVING, null);
    }

    @Override
    public void setStatusBackToReceiving(long id) {
        try {
            transitionIfAllowed(id, PrearcStatus.RECEIVING);
        } catch (NotFoundException e) {
            log.error("Unable to reset status for DirectArchiveSession id={}", id, e);
        }
    }

    @Override
    public List<SessionData> findReadyForArchive() {
        List<DirectArchiveSession> sessions = getDao().findReadyForArchive();
        return sessions == null ? null :
                sessions.stream().map(DirectArchiveSession::toSessionData).collect(Collectors.toList());
    }

    @Override
    public SessionData getSessionData(long id) throws NotFoundException {
        return get(id).toSessionData();
    }

    private DirectArchiveSession transition(long id, PrearcStatus target) throws NotFoundException, ArchivingException {
        return transition(id, target, GUARDED_TRANSITIONS.get(target));
    }

    /** Applies the transition atomically; the row is read afterwards, to return it on success or to name its status on refusal. */
    private DirectArchiveSession transition(long id, PrearcStatus target, Set<PrearcStatus> allowed) throws NotFoundException, ArchivingException {
        if (getDao().transitionStatus(id, target, allowed) == 0) {
            throw new ArchivingException("DirectArchiveSession id=" + id + " has status " + get(id).getStatus() +
                    ", from which it cannot move to " + target + ".");
        }
        return get(id);
    }

    /**
     * For transitions the archive trigger makes on its own schedule: when the session is no longer in a status the
     * transition applies to, typically because a delete has claimed it, leave it alone and say so.
     */
    private boolean transitionIfAllowed(long id, PrearcStatus target) throws NotFoundException {
        return transitionIfAllowed(id, target, GUARDED_TRANSITIONS.get(target));
    }

    private boolean transitionIfAllowed(long id, PrearcStatus target, Set<PrearcStatus> allowed) throws NotFoundException {
        if (getDao().transitionStatus(id, target, allowed) == 1) {
            return true;
        }
        final PrearcStatus current = get(id).getStatus();
        log.warn("Leaving DirectArchiveSession id={} in status {}: it cannot move to {}", id, current, target);
        return false;
    }

    private void setStatus(DirectArchiveSession das, PrearcStatus status, @Nullable String message) {
        das.setStatus(status);
        if (StringUtils.isNotBlank(message)) {
            das.setMessage(message);
        }
        update(das);
    }
}
