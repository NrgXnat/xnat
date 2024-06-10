package org.nrg.xnat.daos;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.ConditionalInitializationTaskStatus;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ConditionalInitializationTaskStatusDAO extends AbstractHibernateDAO<ConditionalInitializationTaskStatus> {

    /**
     * Gets the ConditionalInitializationTaskStatus for a given Initialization TaskName
     * @param taskName  - name of the Initialization Task
     * @return ConditionalInitializationTaskStatus or null
     */
    public ConditionalInitializationTaskStatus getTaskInitializationStatus(String taskName) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("taskName", taskName));
        return (ConditionalInitializationTaskStatus)criteria.uniqueResult();
    }
}
