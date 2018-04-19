package org.nrg.xnat.services.cache;

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

/**
 * Provides both {@link AbstractXftItemEventHandlerMethod} implementation and the default implementations for <b>CacheEventListener</b> methods
 * from the <b>CacheEventListenerAdapter</b> class.
 */
@Getter(PROTECTED)
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
     * {@inheritDoc}
     */
    @Override
    abstract protected boolean handleEventImpl(final XftItemEventI event);

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        log.debug("Element with key '{}' removed from cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementRemoved(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        log.debug("Element with key '{}' put into cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementPut(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        log.debug("Element with key '{}' updated in cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementUpdated(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        log.debug("Element with key '{}' expired in cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementExpired(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        log.debug("Element with key '{}' evicted from cache '{}'", element.getObjectKey(), cache.getName());
        _cacheEventListener.notifyElementEvicted(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        log.debug("All elements removed from cache '{}'", cache.getName());
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
        getCache().put(cacheId, object);
    }

    protected <T> T getCachedObject(final String cacheId, final Class<? extends T> type) {
        return getCache().get(cacheId, type);
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getCachedList(final String cacheId) {
        return Lists.newArrayList(getCache().get(cacheId, List.class));
    }

    @SuppressWarnings("unchecked")
    protected <T> Set<T> getCachedSet(final String cacheId) {
        return Sets.newHashSet(getCache().get(cacheId, Set.class));
    }

    protected <K, V> Map<K, V> getCachedMap(final String key) {
        //noinspection unchecked
        return Maps.<K, V>newHashMap(getCache().get(key, Map.class));
    }

    private void registerCacheEventListener() {
        final Object nativeCache = _cache.getNativeCache();
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
