package org.nrg.dcm.id;

/**
 * ExtractorFromInstanceProvider is an ExtractorProvider that gets routing expressions from per-receiver configuration
 * in the syntax of the custom-routing rule regex.
 *
 */
public class ExtractorFromInstanceProvider extends ExtractorFromRuleProvider implements AeTitleAndPortAware {
    private final RoutingExpressionFromInstanceProvider _routingExpressionFromInstanceProvider;
    public ExtractorFromInstanceProvider( RoutingExpressionFromInstanceProvider routingExpressionFromInstanceProvider) {
        super(routingExpressionFromInstanceProvider);
        _routingExpressionFromInstanceProvider = routingExpressionFromInstanceProvider;
    }

    @Override
    public String getAeTitle() {
        return _routingExpressionFromInstanceProvider.getAeTitle();
    }

    @Override
    public void setAeTitle(String aeTitle) {
        _routingExpressionFromInstanceProvider.setAeTitle( aeTitle);
    }

    @Override
    public int getPort() {
        return _routingExpressionFromInstanceProvider.getPort();
    }

    @Override
    public void setPort(int port) {
        _routingExpressionFromInstanceProvider.setPort( port);
    }

}
