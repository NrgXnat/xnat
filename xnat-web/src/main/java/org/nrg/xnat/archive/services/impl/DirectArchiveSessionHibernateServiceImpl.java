package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DirectArchiveSessionHibernateServiceImpl
        extends AbstractHibernateEntityService<DirectArchiveSession, DirectArchiveSessionDao>
        implements DirectArchiveSessionHibernateService {

    @Override
    public void touch(long id) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setLastBuiltDate(Calendar.getInstance().getTime());
        update(das);
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
        List<DirectArchiveSession> sessions = getDao().findByLocation(location);
        return sessions != null && sessions.stream()
                .filter(session -> excludingId == null || session.getId() != excludingId)
                .anyMatch(session -> session.getStatus() != PrearcUtils.PrearcStatus.ERROR);
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
        return setStatusAndReturn(id, EnumSet.of(PrearcUtils.PrearcStatus.QUEUED_BUILDING),
                PrearcUtils.PrearcStatus.BUILDING, "buildable");
    }

    @Override
    public SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException {
        return setStatusAndReturn(id, EnumSet.of(PrearcUtils.PrearcStatus.QUEUED_ARCHIVING),
                PrearcUtils.PrearcStatus.ARCHIVING, "archivable");
    }

    @Override
    public SessionData setStatusToDeletingAndReturn(long id) throws NotFoundException, ArchivingException {
        // DELETING is re-claimable so a delete that died after claiming the session can be retried
        return setStatusAndReturn(id, EnumSet.of(PrearcUtils.PrearcStatus.RECEIVING, PrearcUtils.PrearcStatus.ERROR,
                        PrearcUtils.PrearcStatus.DELETING),
                PrearcUtils.PrearcStatus.DELETING, "deletable");
    }

    @Override
    public void delete(long id) {
        // Idempotent: the row may already have been removed by a concurrent delete or by the importer
        DirectArchiveSession das = retrieve(id);
        if (das == null) {
            log.debug("DirectArchiveSession id={} already deleted", id);
            return;
        }
        delete(das);
    }

    @Override
    public void setStatusToError(long id, Exception e) throws NotFoundException {
        setStatus(id, PrearcUtils.PrearcStatus.ERROR, e.getMessage());
    }

    @Override
    public void setStatusToQueuedBuilding(long id) throws NotFoundException {
        // Only a RECEIVING session can be queued: this must not overwrite a session that was claimed for deletion
        try {
            setStatusAndReturn(id, EnumSet.of(PrearcUtils.PrearcStatus.RECEIVING), PrearcUtils.PrearcStatus.QUEUED_BUILDING,
                    "queueable for building");
        } catch (ArchivingException e) {
            log.warn("Not queueing DirectArchiveSession id={} for building: {}", id, e.getMessage());
        }
    }

    @Override
    public void setStatusToQueuedArchiving(long id) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setUploadDate(new Date());
        setStatus(das, PrearcUtils.PrearcStatus.QUEUED_ARCHIVING, null);
    }

    @Override
    public void setStatusBackToReceiving(long id) {
        // Only undo a queued-for-building transition; never revive a session that was claimed for deletion
        try {
            setStatusAndReturn(id, EnumSet.of(PrearcUtils.PrearcStatus.QUEUED_BUILDING), PrearcUtils.PrearcStatus.RECEIVING,
                    "queued for building");
        } catch (NotFoundException | ArchivingException e) {
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


    private void setStatus(long id, PrearcUtils.PrearcStatus status) throws NotFoundException {
        setStatus(id, status, null);
    }

    private void setStatus(long id, PrearcUtils.PrearcStatus status, @Nullable String message) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setStatus(status);
        if (StringUtils.isNotBlank(message)) {
            das.setMessage(message);
        }
        update(das);
    }

    private void setStatus(DirectArchiveSession das, PrearcUtils.PrearcStatus status, @Nullable String message) {
        das.setStatus(status);
        if (StringUtils.isNotBlank(message)) {
            das.setMessage(message);
        }
        update(das);
    }

    private SessionData setStatusAndReturn(long id, Set<PrearcUtils.PrearcStatus> initStatuses,
                                           PrearcUtils.PrearcStatus newStatus, String action)
            throws NotFoundException, ArchivingException {
        DirectArchiveSession das = get(id);
        if (!initStatuses.contains(das.getStatus())) {
            throw new ArchivingException("DirectArchiveSession id=" + id + " has status " + das.getStatus() +
                    ", which is not " + action + ".");
        }
        das.setStatus(newStatus);
        update(das);
        return das.toSessionData();
    }
}