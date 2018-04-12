package org.nrg.xnat.services.cache;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;

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
    protected AbstractXftItemAndCacheEventHandlerMethod() {
        this(null);
    }

    /**
     * Creates the super class using the default <b>CacheEventListenerAdapter</b> implementation for the underlying default functionality.
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(null, first, criteria);
    }

    /**
     * Creates the super class using the submitted <b>CacheEventListener</b> instance for the underlying default functionality. 
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final CacheEventListener cacheEventListener) {
        _cacheEventListener = ObjectUtils.defaultIfNull(cacheEventListener, new CacheEventListenerAdapter());
        log.debug("XFT item event handler method and cache event listener created with a cache event listener instance of type {}, no criteria specified", _cacheEventListener.getClass().getName());
    }

    /**
     * Creates the super class using the submitted <b>CacheEventListener</b> instance for the underlying default functionality.
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final CacheEventListener cacheEventListener, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        super(first, criteria);
        _cacheEventListener = ObjectUtils.defaultIfNull(cacheEventListener, new CacheEventListenerAdapter());
        log.debug("XFT item event handler method and cache event listener created with a cache event listener instance of type {}, {} criteria specified", _cacheEventListener.getClass().getName(), criteria.length + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract protected boolean handleEventImpl(final XftItemEvent event);

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

    private final CacheEventListener _cacheEventListener;
}
