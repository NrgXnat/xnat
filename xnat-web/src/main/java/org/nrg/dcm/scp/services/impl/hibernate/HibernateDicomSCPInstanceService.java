package org.nrg.dcm.scp.services.impl.hibernate;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.id.ExtractorFromRuleProvider;
import org.nrg.dcm.id.RoutingExpressionFromMultilineStringProvider;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.daos.DicomSCPInstanceDAO;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class HibernateDicomSCPInstanceService extends AbstractHibernateEntityService<DicomSCPInstance, DicomSCPInstanceDAO> implements DicomSCPInstanceService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DicomSCPInstance> findByAllByPort(final int port) {
        return getDao().findByProperty("port", port);
    }

    /**
     * findByAETitleAndPort
     * *
     *
     * @param ae
     * @param port
     *
     * @return Optional DicomSCPInstance for specified aeTitle and port.
     */
    @Override
    public Optional<DicomSCPInstance> findByAETitleAndPort(String ae, int port) {
        Map<String, Object> map = ImmutableMap.of("aeTitle", ae, "port", port);
        // org.xnat.framework findByProperties turns an empty list to null, which is surprising.
        List<DicomSCPInstance> list = getDao().findByProperties(map);
        return (list == null) ? Optional.empty() :
               // Optional containing either the sole element, or nothing (empty) if there are zero or multiple elements
               list.stream().collect(Collectors.reducing((a, b) -> null));
    }

    @Override
    public Set<Integer> getPortsWithEnabledInstances() {
        return getDao().getPortsWithEnabledInstances();
    }

    /**
     * validate
     *
     * @param instance to be validated
     *
     * @return A string of colon delimited error messages,
     * or empty string if valid,
     * or null if validation was not run because instance does not have routing expressions enabled.
     */
    @Override
    @Nullable
    public String validate(@Nullable DicomSCPInstance instance) {
        if (instance == null || !instance.isRoutingExpressionsEnabled()) {
            return null;
        }
        return String.join(":", new ExtractorFromRuleProvider(new RoutingExpressionFromMultilineStringProvider(Arrays.asList(instance.getProjectRoutingExpression(),
                                                                                                                             instance.getSubjectRoutingExpression(),
                                                                                                                             instance.getSessionRoutingExpression()))).validate());
    }
}
