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
public class RoutingExpressionFromInstanceProvider implements RoutingExpressionProvider {
    private final DicomSCPInstance _dicomScpInstance;

    public RoutingExpressionFromInstanceProvider(final DicomSCPInstance dicomScpInstance) {
        _dicomScpInstance = dicomScpInstance;
    }

    @Override
    public List<String> provide(CompositeDicomObjectIdentifier.ExtractorType type) {
        List<String> rules = new ArrayList<>();
        if (_dicomScpInstance.isRoutingExpressionsEnabled()) {
            String routingExpression;
            switch (type) {
                case PROJECT:
                    routingExpression = _dicomScpInstance.getProjectRoutingExpression();
                    break;
                case SUBJECT:
                    routingExpression = _dicomScpInstance.getSubjectRoutingExpression();
                    break;
                case SESSION:
                    routingExpression = _dicomScpInstance.getSessionRoutingExpression();
                    break;
                case AA:
                default:
                    routingExpression = null;
                    break;
            }
            if (routingExpression != null) {
                rules.addAll(parseConfig(routingExpression));
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
}
