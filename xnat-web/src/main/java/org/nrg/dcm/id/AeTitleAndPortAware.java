package org.nrg.dcm.id;

/**
 * A Mixin interface to allow ExtractorProviders and RoutingExpressionProviders to be per-receiver aware.
 */
public interface AeTitleAndPortAware {
    void setAeTitle( String aeTitle);
    String getAeTitle();

    void setPort( int port);
    int getPort();
}
