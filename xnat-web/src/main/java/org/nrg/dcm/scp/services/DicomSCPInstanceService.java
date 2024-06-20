package org.nrg.dcm.scp.services;


import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DicomSCPInstanceService extends BaseHibernateService<DicomSCPInstance> {
    List<DicomSCPInstance> findByAllByPort(int port);

    Optional<DicomSCPInstance> findByAETitleAndPort(String ae, int port);

    Set<Integer> getPortsWithEnabledInstances();

    @Nullable
    String validate(@Nullable DicomSCPInstance instance);
}
