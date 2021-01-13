package org.nrg.xnat.snapshot.services.impl;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SnapshotProviderPool extends GenericKeyedObjectPool<String, SnapshotProvider> {
    @Autowired
    public SnapshotProviderPool(final @Lazy SnapshotProviderObjectFactory factory) {
        super(factory);
        setBlockWhenExhausted(true);
        setMaxTotalPerKey(1);
        setMaxTotal(100);
        setMaxWaitMillis(2 * ONE_MIN_MILLIS);
    }

    private static final int ONE_MIN_MILLIS = 60 * 1000;
}
