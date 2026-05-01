package org.nrg.dcm.id;

import java.util.List;

/**
 * RoutingExpressionProvider
 *
 * Provide a List of Strings that are expressions to be used in routing resources to project/subject/session on upload.
 *
 */
public interface RoutingExpressionProvider {

    /**
     * Provide expressions of given type.
     *
     * @param type
     * @return
     */
    List<String> provide(CompositeDicomObjectIdentifier.ExtractorType type);

    /**
     * Provide expressions of all types.
     * @return
     */
    List<String> provide();
}
