package org.nrg.xdat.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.StudyRouting;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class StudyRoutingDAO extends AbstractHibernateDAO<StudyRouting> {
    public StudyRouting findByStudyInstanceUid(final String studyInstanceUid) {
        return findByUniqueProperty(StudyRouting.STUDY_INSTANCE_UID, studyInstanceUid);
    }

    public List<StudyRouting> findByProjectId(final String projectId) {
        return findByProperty(StudyRouting.PROJECT_ID, projectId);
    }

    public List<StudyRouting> findByUserId(final String userId) {
        return findByProperty(StudyRouting.USER_ID, userId);
    }

    public boolean delete(final String studyInstanceUid) {
        StudyRouting entity = findByStudyInstanceUid(studyInstanceUid);
        if (entity == null) {
            log.warn("I was asked to delete the routing for study instance UID {}, but there is no routing configured for that value", studyInstanceUid);
            return false;
        }
        log.info("Deleting routing for study instance UID {}", studyInstanceUid);
        delete(entity);
        return true;
    }
}
