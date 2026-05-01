package org.nrg.dcm.id;


/**
 * ExtractorFromConfigProvider
 *
 * is an ExtractorFromRuleProvider that gets expressions from the config service.
 */
public class ExtractorFromConfigProvider extends ExtractorFromRuleProvider {
    public ExtractorFromConfigProvider() {
        super( new RoutingExpressionFromConfigProvider());
    }
}
