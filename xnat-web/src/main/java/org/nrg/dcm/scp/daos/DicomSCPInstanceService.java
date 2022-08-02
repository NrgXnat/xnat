package org.nrg.dcm.scp.daos;

import org.nrg.dcm.id.ExtractorFromRuleProvider;
import org.nrg.dcm.id.RoutingExpressionFromMultilineStringProvider;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DicomSCPInstanceService extends AbstractHibernateEntityService<DicomSCPInstance, DicomSCPInstanceDAO> {

    /**
     * findByAETitleAndPort
     **
     * @param ae
     * @param port
     * @return Optional DicomSCPInstance for specified aeTitle and port.
     */
    @Transactional
    public Optional<DicomSCPInstance> findByAETitleAndPort(String ae, int port) {
        Map<String, Object> map = Stream.of(new Object[][] {
                { "aeTitle", ae },
                { "port", port },
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));
        // org.xnat.framework findByProperties turns an empty list to null, which is surprising.
        List<DicomSCPInstance> list = getDao().findByProperties( map);
        return (list == null)? Optional.ofNullable( null):
                              // Optional containing either the sole element, or nothing (empty) if there are zero or multiple elements
                              list.stream().collect(Collectors.reducing((a, b) -> null));
    }

    @Transactional
    public List<DicomSCPInstance> findAll() {
        return getDao().findAll();
    }

    @Transactional
    public void saveOrUpdate(DicomSCPInstance instance) {
        if( instance.getCreated() == null)   instance.setCreated( new Date());
        instance.setTimestamp( new Date());
        getDao().saveOrUpdate( instance);
    }

    @Transactional
    public DicomSCPInstance findById(long instanceId) {
        return getDao().findById( instanceId);
    }

    @Transactional
    public List<DicomSCPInstance> findAllEnabled() {
        return getDao().findAllEnabled();
    }

    @Transactional
    public Set<Integer> getPortsWithEnabledInstances() {
        return findAllEnabled().stream().map( DicomSCPInstance::getPort).collect(Collectors.toSet());
    }

    public String validate( DicomSCPInstance instance) {
        List<String> expressions = Arrays.asList( instance.getProjectRoutingExpression(),
                instance.getSubjectRoutingExpression(),
                instance.getSessionRoutingExpression());
        RoutingExpressionFromMultilineStringProvider expressionProvider = new RoutingExpressionFromMultilineStringProvider( expressions);
        ExtractorFromRuleProvider extratorProvider = new ExtractorFromRuleProvider( expressionProvider);
        return extratorProvider.validate().stream().collect(Collectors.joining(":"));
    }
}
