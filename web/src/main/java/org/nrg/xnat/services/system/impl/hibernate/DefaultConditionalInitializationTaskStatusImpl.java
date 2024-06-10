package org.nrg.xnat.services.system.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.daos.ConditionalInitializationTaskStatusDAO;
import org.nrg.xnat.entities.ConditionalInitializationTaskStatus;
import org.nrg.xnat.services.system.ConditionalInitializationTaskStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultConditionalInitializationTaskStatusImpl extends AbstractHibernateEntityService<ConditionalInitializationTaskStatus, ConditionalInitializationTaskStatusDAO> implements ConditionalInitializationTaskStatusService {

    public ConditionalInitializationTaskStatus getTaskInitializationStatus(String taskName) {
      return getDao().getTaskInitializationStatus(taskName);
    }
}
