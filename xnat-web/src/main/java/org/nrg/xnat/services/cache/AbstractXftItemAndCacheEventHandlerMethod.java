package org.nrg.xnat.services.cache;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.*;

import static lombok.AccessLevel.PRIVATE;

/**
 * Provides both {@link AbstractXftItemEventHandlerMethod} implementation and the default implementations for <b>CacheEventListener</b> methods
 * from the <b>CacheEventListenerAdapter</b> class.
 */
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractXftItemAndCacheEventHandlerMethod extends AbstractXftItemEventHandlerMethod implements CacheEventListener {
    /**
     * Creates the super class using the default <b>CacheEventListenerAdapter</b> implementation for the underlying default functionality.
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final CacheManager cacheManager, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(cacheManager, null, first, criteria);
    }

    /**
     * Creates the super class using the submitted <b>CacheEventListener</b> instance for the underlying default functionality.
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final CacheManager cacheManager, final CacheEventListener cacheEventListener, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        super(first, criteria);
        _cache = cacheManager.getCache(getCacheName());
        _cacheEventListener = ObjectUtils.defaultIfNull(cacheEventListener, new CacheEventListenerAdapter());
        registerCacheEventListener();
        log.debug("XFT item event handler method and cache event listener created with a cache event listener instance of type {}, {} criteria specified", getCacheEventListener().getClass().getName(), criteria.length + 1);
    }

    abstract public String getCacheName();

    /**
     * Returns the timestamp indicating when the specified cache entry was last updated. If the entry was only
     * inserted and not updated, the insert time is returned.
     *
     * @param cacheId The ID of the cache entry to check.
     *
     * @return The date and time of the latest update to the specified cache entry.
     */
    public Date getCacheEntryLastUpdateTime(final String cacheId) {
        if (!has(cacheId)) {
            log.trace("Trying to check the last update time for cache entry '{}', but that is not in the cache.", cacheId);
            return null;
        }
        final long lastUpdateTime = getLatestOfCreationAndUpdateTime(cacheId);
        log.trace("Checked last update time for cache entry '{}' and found: {}", cacheId, lastUpdateTime);
        return new Date(lastUpdateTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract protected boolean handleEventImpl(final XftItemEventI event);

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        log.trace("Put element with cache ID '{}' into cache {}", element.getObjectKey(), cache.getName());
        getCacheEventListener().notifyElementPut(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        log.trace("Updated element with cache ID '{}' in cache {}", element.getObjectKey(), cache.getName());
        getCacheEventListener().notifyElementUpdated(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        log.trace("Removed element with cache ID '{}' from cache {}", element.getObjectKey(), cache.getName());
        getCacheEventListener().notifyElementRemoved(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        log.trace("Expired element with cache ID '{}' from cache {}", element.getObjectKey(), cache.getName());
        getCacheEventListener().notifyElementExpired(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        log.trace("Evicted element with cache ID '{}' from cache {}", element.getObjectKey(), cache.getName());
        getCacheEventListener().notifyElementEvicted(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        log.trace("Removed all elements from cache {}", cache.getName());
        getCacheEventListener().notifyRemoveAll(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        log.debug("I'm being disposed of, how sad.");
        getCacheEventListener().dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected static String createCacheIdFromElements(final String... elements) {
        return StringUtils.join(elements, ":");
    }

    protected long getLatestOfCreationAndUpdateTime(final String cacheId) {
        return getEhCache().get(cacheId).getLatestOfCreationAndUpdateTime();
    }

    protected net.sf.ehcache.Cache getEhCache() {
        final Object nativeCache = getCache().getNativeCache();
        if (nativeCache instanceof net.sf.ehcache.Cache) {
            return ((net.sf.ehcache.Cache) nativeCache);
        }
        throw new RuntimeException("The native cache is not an ehcache instance, but instead is " + nativeCache.getClass().getName());
    }

    @SuppressWarnings("unused")
    protected void cacheObject(final String cacheId, final Provider<Object> provider) {
        cacheObject(cacheId, provider.get());
    }

    protected void cacheObject(final String cacheId, final Object object) {
        if (object == null) {
            log.warn("I was asked to cache an object with ID '{}' but the object was null.", cacheId);
            return;
        }
        log.trace("Request to cache entry '{}', evaluating", cacheId);
        final boolean hasCacheId = has(cacheId);
        if (hasCacheId) {
            log.trace("Cache entry '{}' exists and force update not specified, evaluating for change", cacheId);
            final Object existing = getCachedObject(cacheId, Object.class);
            if (object.equals(existing)) {
                log.trace("Existing and updated cached objects for entry '{}' are identical, returning without updating", cacheId);
                return;
            }
            log.trace("Cache entry '{}' already exists but differs from updated cache item:\n\nExisting:\n{}\nUpdated:\n{}", cacheId, existing, object);
        }
        forceCacheObject(cacheId, object);
    }

    @SuppressWarnings("unused")
    protected void forceCacheObject(final String cacheId, final Provider<Object> provider) {
        forceCacheObject(cacheId, provider.get());
    }

    protected void forceCacheObject(final String cacheId, final Object object) {
        log.trace("Storing cache entry '{}' with object of type: {}", cacheId, object.getClass().getName());
        getCache().put(cacheId, object);
    }

    protected <T> T getCachedObject(final String cacheId, final Class<? extends T> type) {
        try {
            return getCache().get(cacheId, type);
        } catch (IllegalStateException e) {
            log.error("Got an IllegalStateException trying to retrieve cache ID '{}' as an object of type {}", cacheId, type.getName(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getCachedList(final String cacheId) {
        final List<T> elements = getCachedObject(cacheId, List.class);
        if (elements != null) {
            log.trace("Found cached list containing {} items for cache ID '{}'", elements.size(), cacheId);
            return ImmutableList.copyOf(elements);
        }
        log.trace("Got a request for cached list '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> Set<T> getCachedSet(final String cacheId) {
        final Set<T> elements = getCachedObject(cacheId, Set.class);
        if (elements != null) {
            log.trace("Found cached set containing {} items for cache ID '{}'", elements.size(), cacheId);
            return ImmutableSet.copyOf(elements);
        }
        log.trace("Got a request for cached set '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <K, V> Map<K, V> getCachedMap(final String cacheId) {
        final Map<K, V> map = getCachedObject(cacheId, Map.class);
        if (map != null) {
            log.trace("Found cached map containing {} items for cache ID '{}'", map.size(), cacheId);
            return ImmutableMap.copyOf(map);
        }
        log.trace("Got a request for cached map '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <K, V> ImmutableListMultimap<K, V> getCachedListMultimap(final String cacheId) {
        final ListMultimap<K, V> map = getCachedObject(cacheId, ListMultimap.class);
        if (map != null) {
            log.trace("Found cached map containing {} items for cache ID '{}'", map.size(), cacheId);
            return ImmutableListMultimap.copyOf(map);
        }
        log.trace("Got a request for cached map '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    /**
     * As the name implies, this clears the contents of the cache.
     */
    protected void clearCache() {
        log.info("Clearing the contents of the {} cache.", getCacheName());
        getCache().clear();
    }

    @SuppressWarnings("unchecked")
    protected <K, V> Map<K, V> buildImmutableMap(final Map<K, V>... maps) {
        // The keys set keeps track of keys that have already been added
        // so that we don't add them again.
        final Set<K> keys = new HashSet<>();

        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (final Map<K, V> map : maps) {
            builder.putAll(Maps.filterKeys(map, new Predicate<K>() {
                @Override
                public boolean apply(@Nullable final K entry) {
                    return !keys.contains(entry);
                }
            }));
            keys.addAll(map.keySet());
        }
        final ImmutableMap<K, V> map = builder.build();
        log.debug("Created immutable map combining {} maps, resulting in {} total entries", maps.length, map.size());
        return map;
    }

    @SuppressWarnings("unchecked")
    protected <T> Set<T> buildImmutableSet(final Collection<T>... collections) {
        final ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        for (final Collection<T> list : collections) {
            builder.addAll(list);
        }
        return builder.build();
    }

    protected void evict(final List<String> cacheIds) {
        for (final String cacheId : cacheIds) {
            evict(cacheId);
        }
    }

    protected void evict(final String cacheId) {
        log.debug("Evicting cache entry '{}'", cacheId);
        getCache().evict(cacheId);
    }

    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param cacheId The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to a project cache entry, false otherwise.
     */
    private boolean has(final String cacheId) {
        return getCache().get(cacheId) != null;
    }

    private void registerCacheEventListener() {
        final Object nativeCache = getCache().getNativeCache();
        if (nativeCache instanceof net.sf.ehcache.Cache) {
            ((net.sf.ehcache.Cache) nativeCache).getCacheEventNotificationService().registerListener(this);
            log.debug("Registered user project cache as net.sf.ehcache.Cache listener");
        } else {
            log.warn("I don't know how to handle the native cache type {}", nativeCache.getClass().getName());
        }
    }

    private final CacheEventListener _cacheEventListener;
    private final Cache              _cache;
}
