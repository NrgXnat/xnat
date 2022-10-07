package org.nrg.dcm.id;

/**
 * ExtractorFromInstanceProvider is an ExtractorProvider that gets routing expressions from per-receiver configuration
 * in the syntax of the custom-routing rule regex.
 *
 */
public class ExtractorFromInstanceProvider extends ExtractorFromRuleProvider {
    public ExtractorFromInstanceProvider(RoutingExpressionFromInstanceProvider routingExpressionFromInstanceProvider) {
        super(routingExpressionFromInstanceProvider);
    }
}
