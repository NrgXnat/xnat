package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.daos.DicomSCPInstanceService;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RoutingExpressionFromInstanceProvider is a RoutingExpressionProvider that gets routing expressions from per-receiver configuration.
 *
 */
@Slf4j
public class RoutingExpressionFromInstanceProvider implements RoutingExpressionProvider, AeTitleAndPortAware {
    private final DicomSCPInstanceService _dicomSCPInstanceService;
    private String _aeTitle;
    private int _port;

    public RoutingExpressionFromInstanceProvider(DicomSCPInstanceService dicomSCPInstanceService) {
        _dicomSCPInstanceService = dicomSCPInstanceService;
    }
    @Override
    public List<String> provide(CompositeDicomObjectIdentifier.ExtractorType type) {
        DicomSCPInstance instance = _dicomSCPInstanceService.findByAETitleAndPort( _aeTitle, _port)
                .orElseThrow(() -> new IllegalArgumentException(String.format("No configuration found for receiver at %s:%d.", _aeTitle, _port)));

        List<String> rules = new ArrayList<>();
        if (instance.isRoutingExpressionsEnabled()) {
            String routingExpression;
            switch (type) {
                case PROJECT:
                    routingExpression = instance.getProjectRoutingExpression();
                    break;
                case SUBJECT:
                    routingExpression = instance.getSubjectRoutingExpression();
                    break;
                case SESSION:
                    routingExpression = instance.getSessionRoutingExpression();
                    break;
                case AA:
                    routingExpression = null;
                    break;
                default:
                    routingExpression = null;
            }
            if( routingExpression != null) {
                rules.addAll( parseConfig( routingExpression));
            }
        }
        return rules;
    }

    /**
     * parseConfig
     * Convert the string from configuration to possibly multiple routing expressions.
     *
     * The configuration string can contain new-line separated routing expressions.
     *
     * @param s string from config
     * @return List of routing expressions.
     */
    @Nonnull
    protected List<String> parseConfig( String s) {
        return Arrays.asList( s.split("\\R"));
    }

    @Override
    public List<String> provide() {
        List<String> expressions = new ArrayList<>();
        expressions.addAll( provide( CompositeDicomObjectIdentifier.ExtractorType.PROJECT));
        expressions.addAll( provide( CompositeDicomObjectIdentifier.ExtractorType.SUBJECT));
        expressions.addAll( provide( CompositeDicomObjectIdentifier.ExtractorType.SESSION));
        expressions.addAll( provide( CompositeDicomObjectIdentifier.ExtractorType.AA));
        return expressions;
    }

    @Override
    public String getAeTitle() {
        return _aeTitle;
    }

    @Override
    public void setAeTitle(String aeTitle) {
        _aeTitle = aeTitle;
    }

    @Override
    public int getPort() {
        return _port;
    }

    @Override
    public void setPort(int port) {
        _port = port;
    }
}
