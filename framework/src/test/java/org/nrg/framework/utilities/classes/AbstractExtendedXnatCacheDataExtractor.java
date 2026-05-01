package org.nrg.framework.utilities.classes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExtendedXnatCacheDataExtractor<K, V> extends AbstractDataExtractor<ExtendedXnatCache, K, V> {
    protected AbstractExtendedXnatCacheDataExtractor() {
        log.info("Trying to get the key and value types here");
    }
}
