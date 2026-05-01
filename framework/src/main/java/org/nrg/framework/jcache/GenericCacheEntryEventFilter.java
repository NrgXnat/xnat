package org.nrg.framework.jcache;

import lombok.extern.slf4j.Slf4j;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

@Slf4j
public class GenericCacheEntryEventFilter<K, V> implements CacheEntryEventFilter<K, V> {
    @Override
    public boolean evaluate(final CacheEntryEvent<? extends K, ? extends V> event) throws CacheEntryListenerException {
        return true;
    }
}
