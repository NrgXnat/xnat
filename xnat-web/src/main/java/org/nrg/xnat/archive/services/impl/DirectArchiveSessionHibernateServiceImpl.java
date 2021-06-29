package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xft.security.UserI;
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
import java.util.List;
import java.util.Optional;
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
    public SessionData findByProjectTagName(UserI user, String project, String tag, String name) {
        DirectArchiveSession das = getDao().findByProjectTagName(project, tag, name);
        return das == null ? null : das.toSessionData();
    }

    @Override
    public SessionData create(SessionData initialize) throws ArchivingException {
        String location = initialize.getUrl();
        List<DirectArchiveSession> das = getDao().findByLocation(location);
        // Direct archive sessions are removed from db after successful archive, so only in-progress or error cases remain
        // We allow re-archive if a prior attempt errored out
        if (das != null && das.stream().anyMatch(s -> s.getStatus() != PrearcUtils.PrearcStatus.ERROR)) {
            throw new ArchivingException("Cannot direct archive " + initialize + " due to one or more " +
                    "direct archive sessions with location=\"" + location + "\" in a non-ERROR status");
        }

        return create(new DirectArchiveSession(initialize.getProject(), initialize.getSubject(), initialize.getName(),
                initialize.getTimestamp(), initialize.getFolderName(), initialize.getTag(), initialize.getVisit(),
                initialize.getProtocol(), initialize.getTimeZone(), location, initialize.getSource(),
                initialize.getUploadDate(), initialize.getLastBuiltDate(), initialize.getStatus(),
                initialize.getScan_date(), initialize.getScan_time())).toSessionData();
    }

    @Override
    public SessionData setStatusToBuildingAndReturn(long id) throws NotFoundException, ArchivingException {
        return setStatusAndReturn(id, PrearcUtils.PrearcStatus.BUILDING, PrearcUtils.PrearcStatus._BUILDING,
                "buildable");
    }

    @Override
    public SessionData setStatusToArchivingAndReturn(long id) throws NotFoundException, ArchivingException {
        return setStatusAndReturn(id, PrearcUtils.PrearcStatus.ARCHIVING, PrearcUtils.PrearcStatus._ARCHIVING,
                "archivable");
    }

    @Override
    public void setStatusToError(long id, Exception e) throws NotFoundException {
        setStatus(id, PrearcUtils.PrearcStatus.ERROR, e.getMessage());
    }

    @Override
    public void setStatusToQueuedBuilding(long id) throws NotFoundException {
        setStatus(id, PrearcUtils.PrearcStatus.BUILDING);
    }

    @Override
    public void setStatusToQueuedArchiving(long id) throws NotFoundException {
        DirectArchiveSession das = get(id);
        das.setUploadDate(new Date());
        setStatus(das, PrearcUtils.PrearcStatus.ARCHIVING, null);
    }

    @Override
    public void setStatusBackToReceiving(long id) {
        try {
            setStatus(id, PrearcUtils.PrearcStatus.RECEIVING);
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

    private SessionData setStatusAndReturn(long id, PrearcUtils.PrearcStatus initStatus,
                                           PrearcUtils.PrearcStatus newStatus, String action)
            throws NotFoundException, ArchivingException {
        DirectArchiveSession das = get(id);
        if (das.getStatus() != initStatus) {
            throw new ArchivingException("DirectArchiveSession id=" + id + " has status " + das.getStatus() +
                    ", which is not " + action + ".");
        }
        das.setStatus(newStatus);
        update(das);
        return das.toSessionData();
    }
}