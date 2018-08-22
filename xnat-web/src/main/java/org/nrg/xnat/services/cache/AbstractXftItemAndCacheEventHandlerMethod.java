package org.nrg.xnat.services.cache;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
        log.debug("XFT item event handler method and cache event listener created with a cache event listener instance of type {}, {} criteria specified", _cacheEventListener.getClass().getName(), criteria.length + 1);
    }

    abstract public String getCacheName();

    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param cacheId The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to a project cache entry, false otherwise.
     */
    public boolean has(final String cacheId) {
        return getCache().get(cacheId) != null;
    }

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
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        log.debug("XNAT-5730: Element with key '{}' removed from cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementRemoved(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        final Object objectKey   = element.getObjectKey();
        final Object objectValue = element.getObjectValue();
        log.debug("XNAT-5730: Element with key '{}' added to cache '{}' with a value of '{}' (type: {})", objectKey, cache.getName(), ObjectUtils.defaultIfNull(objectValue, "").toString(), objectValue == null ? "<null>" : objectValue.getClass().getName());
        if (objectKey instanceof String && StringUtils.equals("user:admin:groups", (String) objectKey)) {
            final Object value;
            if (objectValue instanceof List) {
                value = StringUtils.join((List) objectValue, ", ");
            } else {
                value = objectValue.toString();
            }
            log.debug("XNAT-5730: Cache entry '{}' added to cache with value: {}", objectKey, value);
        }
        _cacheEventListener.notifyElementPut(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        final Object objectKey   = element.getObjectKey();
        final Object objectValue = element.getObjectValue();
        log.debug("XNAT-5730: Element with key '{}' updated in cache '{}' with a value of '{}' (type: {})", objectKey, cache.getName(), ObjectUtils.defaultIfNull(objectValue, "").toString(), objectValue == null ? "<null>" : objectValue.getClass().getName());
        if (objectKey instanceof String && StringUtils.equals("user:admin:groups", (String) objectKey)) {
            final Object value;
            if (objectValue instanceof List) {
                value = StringUtils.join((List) objectValue, ", ");
            } else {
                value = objectValue.toString();
            }
            log.debug("XNAT-5730: Cache entry '{}' updated in cache with value: {}", objectKey, value);
        }
        _cacheEventListener.notifyElementUpdated(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        log.debug("XNAT-5730: Element with key '{}' expired in cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementExpired(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        log.debug("XNAT-5730: Element with key '{}' evicted from cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementEvicted(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        log.debug("XNAT-5730: All elements removed from cache '{}'", cache.getName());
        _cacheEventListener.notifyRemoveAll(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        log.debug("I'm being disposed of, how sad.");
        _cacheEventListener.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        log.debug("Cloning is possibly unethical and yet it's happening here anyway.");
        return super.clone();
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

    protected void cacheObject(final String cacheId, final Object object) {
        cacheObject(cacheId, object, false);
    }

    // Nowhere is yet using force update, but I think it will be used eventually. The suppress annotation is just to eliminate IntelliJ nag.
    @SuppressWarnings("SameParameterValue")
    protected void cacheObject(final String cacheId, final Object object, boolean forceUpdate) {
        if (object == null) {
            log.warn("I was asked to cache an object with ID '{}' but the object was null.", cacheId);
        }
        log.trace("Request to cache entry '{}', evaluating", cacheId);
        final boolean hasCacheId = has(cacheId);
        if (!forceUpdate && hasCacheId) {
            log.trace("Cache entry '{}' exists and force update not specified, evaluating for change", cacheId);
            final Object existing = getCachedObject(cacheId, Object.class);
            if (object == null && existing == null) {
                log.trace("Both existing and updated cached objects for entry '{}' are null, returning without updating", cacheId);
                return;
            }
            if (object != null && object.equals(existing)) {
                log.trace("Existing and updated cached objects for entry '{}' are identical, returning without updating", cacheId);
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("Cache entry '{}' already exists but differs from updated cache item:\n\nExisting:\n{}\nUpdated:\n{}", cacheId, existing, object);
            }
        }
        log.debug("Storing cache entry '{}' with object of type: {}", cacheId, object == null ? "null" : object.getClass().getName());
        getCache().put(cacheId, object);
    }

    protected <T> T getCachedObject(final String cacheId, final Class<? extends T> type) {
        try {
            final T t = getCache().get(cacheId, type);
            if (StringUtils.equals("user:admin:groups", cacheId) && List.class.isAssignableFrom(type)) {
                final List list = (List) t;
                log.info("Found cached list for cache ID '{}': {}", cacheId, StringUtils.join(list, ", "));
            }
            return t;
        } catch (IllegalStateException e) {
            log.error("Got an IllegalStateException trying to retrieve cache ID '{}' as an object of type {}", cacheId, type.getName(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getCachedList(final String cacheId) {
        final List<T> elements = getCachedObject(cacheId, List.class);
        if (elements != null) {
            log.info("Found cached list for cache ID '{}': {}", cacheId, StringUtils.join(elements, ", "));
            return ImmutableList.copyOf(elements);
        }
        log.warn("Got a request for cache entry '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> Set<T> getCachedSet(final String cacheId) {
        final Set<T> elements = getCachedObject(cacheId, Set.class);
        if (elements != null) {
            return ImmutableSet.copyOf(elements);
        }
        log.warn("Got a request for cache entry '{}', but when I retrieved the entry it was null.", cacheId);
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <K, V> Map<K, V> getCachedMap(final String cacheId) {
        final Map<K, V> map = getCachedObject(cacheId, Map.class);
        if (map != null) {
            return ImmutableMap.copyOf(map);
        }
        log.warn("Got a request for cache entry '{}', but when I retrieved the entry it was null.", cacheId);
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
        return builder.build();
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
