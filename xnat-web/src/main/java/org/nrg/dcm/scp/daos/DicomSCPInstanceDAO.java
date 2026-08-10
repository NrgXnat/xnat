package org.nrg.dcm.scp.daos;

import org.hibernate.query.Query;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class DicomSCPInstanceDAO extends AbstractHibernateDAO<DicomSCPInstance> {
    public Set<Integer> getPortsWithEnabledInstances() {
        final Query<Integer> getPortsWithEnabledInstances = createNamedQuery("getPortsWithEnabledInstances", Integer.class);
        return getPortsWithEnabledInstances.getResultStream().collect(Collectors.toSet());
    }
}