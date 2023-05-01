package org.nrg.xnat.services.system;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.daos.ConditionalInitializationTaskStatusDAO;
import org.nrg.xnat.daos.HostInfoDAO;
import org.nrg.xnat.entities.ConditionalInitializationTaskStatus;
import org.nrg.xnat.entities.HostInfo;

public interface ConditionalInitializationTaskStatusService extends BaseHibernateService<ConditionalInitializationTaskStatus> {

    ConditionalInitializationTaskStatus getTaskInitializationStatus(String taskName);
}
