package org.nrg.dcm.scp.daos;

import org.hibernate.StaleStateException;
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

    /**
     * Overridden to apply updates with {@code Session.merge()} rather than {@code Session.update()}. Receiver updates
     * arrive from {@link org.nrg.dcm.scp.DicomSCPManager} as detached, JSON-deserialized instances, and this entity's
     * whitelist element collection is audited: reattaching a detached instance whose collection was replaced
     * wholesale recreates the collection without its prior state, so Envers records additions but never removals, and
     * every revision reconstructs removed whitelist entries as though they were still present. Merging applies the
     * incoming state to the managed instance and its persistent collection, so Hibernate — and therefore Envers —
     * sees the true delta. Note that {@link #saveOrUpdate} routes existing entities through this method as well.
     *
     * <p>The existence check preserves update semantics for missing rows: merge treats a row deleted between the
     * caller's read and this write as transient state and would quietly re-insert it under a new ID — resurrecting
     * deleted receiver configuration — where {@code Session.update()} failed at flush. Missing rows fail with
     * {@link StaleStateException} instead.
     */
    @Override
    public void update(final DicomSCPInstance entity) {
        if (!exists("id", entity.getId())) {
            throw new StaleStateException("Cannot update DicomSCPInstance " + entity.getId() + ": no row with that ID exists. It may have been deleted concurrently.");
        }
        getSession().merge(entity);
    }
}