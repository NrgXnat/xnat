package org.nrg.xdat.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.StudyRoutingDAO;
import org.nrg.xdat.entities.StudyRouting;
import org.nrg.xdat.services.StudyRoutingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class HibernateStudyRoutingService extends AbstractHibernateEntityService<StudyRouting, StudyRoutingDAO> implements StudyRoutingService {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assign(String studyInstanceUid, String projectId, String userId) {
        return assign(studyInstanceUid, projectId, null, null, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assign(String studyInstanceUid, String projectId, String subjectId, String userId) {
        return assign(studyInstanceUid, projectId, subjectId, null, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assign(String studyInstanceUid, String projectId, String subjectId, String label, String userId) {
        if (StringUtils.isAnyBlank(studyInstanceUid, userId)) {
            throw new RuntimeException("You must specify a study instance UID and user ID to create a new routing configuration.");
        }
        if (StringUtils.isAllBlank(projectId, subjectId, label)) {
            throw new RuntimeException("You must specify at least one of a project ID, subject ID, or label to create a new routing configuration.");
        }

        if (getDao().exists(StudyRouting.STUDY_INSTANCE_UID, studyInstanceUid)) {
            log.warn("Attempt to assign new routing configuration for study instance UID: {} (configuration already exists!)", studyInstanceUid);
            return false;
        }

        getDao().create(new StudyRouting(studyInstanceUid, projectId, subjectId, label, userId));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StudyRouting getStudyRouting(String studyInstanceUid) {
        return getDao().findByStudyInstanceUid(studyInstanceUid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StudyRouting> getProjectRoutings(String projectId) {
        return getDao().findByProjectId(projectId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<StudyRouting> getUserRoutings(String userId) {
        return getDao().findByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> findStudyRouting(String studyInstanceUid) {
        StudyRouting routing = getStudyRouting(studyInstanceUid);
        return routing != null ? routing.toMap() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, String>> findProjectRoutings(String projectId) {
        return toMapOfMaps(getProjectRoutings(projectId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, String>> findUserRoutings(String userId) {
        return toMapOfMaps(getUserRoutings(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, String>> findAllRoutings() {
        return toMapOfMaps(getAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close(String studyInstanceUid) {
        if (!getDao().exists(StudyRouting.STUDY_INSTANCE_UID, studyInstanceUid)) {
            return false;
        }
        return getDao().delete(studyInstanceUid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeAll() {
        List<StudyRouting> all = getDao().findAll();
        log.info("Got asked to close all study routings, found {} to close", all.size());
        all.forEach(entity -> getDao().delete(entity));
    }

    private Map<String, Map<String, String>> toMapOfMaps(List<StudyRouting> routings) {
        if (routings == null || routings.isEmpty()) {
            return Collections.emptyMap();
        }
        return routings.stream().collect(Collectors.toMap(StudyRouting::getStudyInstanceUid, StudyRouting::toMap));
    }
}
