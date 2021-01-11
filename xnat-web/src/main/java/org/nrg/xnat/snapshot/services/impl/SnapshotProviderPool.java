package org.nrg.xnat.snapshot.services.impl;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.springframework.stereotype.Component;

@Component
public class SnapshotProviderPool extends GenericKeyedObjectPool<String, SnapshotProvider> {
    private static final int ONE_MIN_MILLIS = 60 *1000;
    public SnapshotProviderPool( SnapshotProviderObjectFactory factory) {
        super( factory);
        setBlockWhenExhausted( true);
        setMaxTotalPerKey(1);
        setMaxTotal(100);
        setMaxWaitMillis( 2 * ONE_MIN_MILLIS);
    }
}
