package org.nrg.dcm.id;

/**
 * A Mixin interface to allow ExtractorProviders and RoutingExpressionProviders to be per-receiver aware.
 */
public interface ReceiverAwareProjectIdentifier<T extends DicomProjectIdentifier> {
    T withExtractor(ExtractorFromInstanceProvider extractor);
}
