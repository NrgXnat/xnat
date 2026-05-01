package org.nrg.framework.jcache;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

public interface GenericCacheEventListener<K, V> extends CacheEntryCreatedListener<K, V>,
                                                         CacheEntryUpdatedListener<K, V>,
                                                         CacheEntryRemovedListener<K, V>,
                                                         CacheEntryExpiredListener<K, V> {
}
