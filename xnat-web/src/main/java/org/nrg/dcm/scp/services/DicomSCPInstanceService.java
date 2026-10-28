package org.nrg.dcm.scp.services;


import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface DicomSCPInstanceService extends BaseHibernateService<DicomSCPInstance> {
    List<DicomSCPInstance> findAllByPort(int port);

    /**
     * The receivers on the port that are enabled, or disabled. Filters {@link #findAllByPort(int)} by default, so an
     * implementation written before this method was added still works.
     */
    default List<DicomSCPInstance> findAllByPort(final int port, final boolean enabled) {
        return findAllByPort(port).stream().filter(instance -> instance.isEnabled() == enabled).collect(Collectors.toList());
    }

    Optional<DicomSCPInstance> findByAETitleAndPort(String ae, int port);

    DicomSCPInstance findById(long id);

    Set<Integer> getPortsWithEnabledInstances();

    @Nullable
    String validate(@Nullable DicomSCPInstance instance);

    void update(final DicomSCPInstance instance);
}
