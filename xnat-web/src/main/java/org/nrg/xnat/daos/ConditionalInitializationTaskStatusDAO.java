package org.nrg.xnat.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.ConditionalInitializationTaskStatus;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ConditionalInitializationTaskStatusDAO extends AbstractHibernateDAO<ConditionalInitializationTaskStatus> {
    public static final String TASK_NAME = "taskName";

    /**
     * Gets the ConditionalInitializationTaskStatus for a given Initialization TaskName
     *
     * @param taskName - name of the Initialization Task
     * @return ConditionalInitializationTaskStatus or null
     */
    public ConditionalInitializationTaskStatus getTaskInitializationStatus(String taskName) {
        return findByUniqueProperty(TASK_NAME, taskName);
    }
}
