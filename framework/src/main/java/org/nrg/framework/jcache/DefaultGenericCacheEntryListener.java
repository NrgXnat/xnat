package org.nrg.framework.jcache;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.impl.events.CacheEventAdapter;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

@Slf4j
public class DefaultGenericCacheEntryListener<K, V> extends CacheEventAdapter<K, V> implements GenericCacheEventListener<K, V> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        events.forEach(event -> onCreation(event.getKey(), event.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        events.forEach(event -> onUpdate(event.getKey(), event.getOldValue(), event.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        events.forEach(event -> onRemoval(event.getKey(), event.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        events.forEach(event -> onExpiry(event.getKey(), event.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreation(K key, V newValue) {
        log.info("Created a new cache entry {}: {}", key, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUpdate(K key, V oldValue, V newValue) {
        log.info("Updated an existing cache entry {}:\n * Old value: {}\n * New value: {}", key, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRemoval(K key, V removedValue) {
        log.info("Removed an existing cache entry {}: {}", key, removedValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onEviction(K key, V evictedValue) {
        log.info("Evicted an existing cache entry {}: {}", key, evictedValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onExpiry(K key, V expiredValue) {
        log.info("Expired an existing cache entry {}: {}", key, expiredValue);
    }
}
