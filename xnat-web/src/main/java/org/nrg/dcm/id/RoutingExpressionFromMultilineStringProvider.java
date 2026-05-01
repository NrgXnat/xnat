package org.nrg.dcm.id;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoutingExpressionFromMultilineStringProvider implements RoutingExpressionProvider {
    private List<String> _expressions;

    public RoutingExpressionFromMultilineStringProvider() {
        this( new ArrayList<>());
    }

    public RoutingExpressionFromMultilineStringProvider(List<String> expressions) {
        setExpressions( expressions);
    }

    public List<String> getExpressions() {
        return _expressions;
    }

    public void setExpressions(List<String> expressions) {
        _expressions = expressions.stream()
                .filter( StringUtils::isNotBlank)
                .map( this::parseExpression)
                .flatMap( List::stream)
                .collect( Collectors.toList());
    }

    @Override
    public List<String> provide(CompositeDicomObjectIdentifier.ExtractorType type) {
        return _expressions;
    }

    @Override
    public List<String> provide() {
        return _expressions;
    }

    /**
     * parseExpression
     * Convert the string from configuration to possibly multiple routing expressions.
     *
     * The configuration string can contain new-line separated routing expressions.
     *
     * @param s string from config
     * @return List of routing expressions.
     */
    @Nonnull
    protected List<String> parseExpression( String s) {
        return Arrays.asList( s.split("\\R"));
    }

}
