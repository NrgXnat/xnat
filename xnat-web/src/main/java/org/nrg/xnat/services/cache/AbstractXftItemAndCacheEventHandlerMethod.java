package org.nrg.xnat.services.cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.jcache.DefaultGenericCacheEntryListener;
import org.nrg.framework.jcache.GenericCacheEventListener;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xnat.services.cache.extractors.DataExtractor;

import javax.cache.Cache;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;

/**
 * Provides both {@link AbstractXftItemEventHandlerMethod} implementation and the functionality for managing multiple
 * caches under a single namespace.
 */
@SuppressWarnings("WeakerAccess")
@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractXftItemAndCacheEventHandlerMethod extends AbstractXftItemEventHandlerMethod {
    public static final String CACHE_KEYS = "cacheKeys";

    private final JCacheHelper                                   _cacheHelper;
    private final Cache<String, List<String>>                    _keysCache;
    private final Map<String, DataExtractor<?, ?>>               _extractors;
    private final List<GenericCacheEventListener<String, ItemI>> _cacheEventListeners;

    /**
     * Creates the super class using the default <b>CacheEventListenerAdapter</b> implementation for the underlying default functionality.
     */
    @SuppressWarnings("unused")
    protected AbstractXftItemAndCacheEventHandlerMethod(final JCacheHelper cacheHelper, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(cacheHelper, Collections.emptyList(), Collections.emptyList(), first, criteria);
    }

    protected AbstractXftItemAndCacheEventHandlerMethod(final JCacheHelper cacheHelper, final List<DataExtractor<?, ?>> extractors, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(cacheHelper, extractors, Collections.emptyList(), first, criteria);
    }

    /**
     * Creates the super class using the submitted <b>CacheEventListener</b> instance for the underlying default functionality.
     */
    @SuppressWarnings("unused")
    protected AbstractXftItemAndCacheEventHandlerMethod(final JCacheHelper cacheHelper, final GenericCacheEventListener<String, ItemI> cacheEventListener, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(cacheHelper, Collections.emptyList(), Collections.singletonList(cacheEventListener), first, criteria);
    }

    /**
     * Creates the super class using the submitted <b>CacheEventListener</b> instance for the underlying default functionality.
     */
    protected AbstractXftItemAndCacheEventHandlerMethod(final JCacheHelper cacheHelper, final List<DataExtractor<?, ?>> extractors, final List<GenericCacheEventListener<String, ItemI>> cacheEventListeners, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        super(first, criteria);
        _cacheHelper         = cacheHelper;
        _keysCache           = cacheHelper.getCacheOfLists(CACHE_KEYS, String.class, String.class);
        _extractors          = extractors.stream().collect(Collectors.toMap(DataExtractor::getCacheName, Function.identity()));
        _cacheEventListeners = CollectionUtils.isNotEmpty(cacheEventListeners) ? cacheEventListeners : Collections.singletonList(new DefaultGenericCacheEntryListener<>());

        initializeCaches();
        log.debug("XFT item event handler method and cache event listener created with {} cache event listener instances, {} criteria specified", getCacheEventListeners().size(), criteria.length + 1);
    }

    /**
     * Defines the top-level name for the cache implementation. Note that this is used as a prefix for caches controlled
     * by the implementation rather than the full name of a particular cache.
     *
     * @return The top-level cache name.
     */
    @SuppressWarnings("unused")
    public abstract String getCacheName();

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract boolean handleEventImpl(final XftItemEventI event);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected Cache<String, List<String>> getKeysCache() {
        return _keysCache;
    }

    protected <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        return getCache(cacheName, keyType, valueType, Collections.emptyMap());
    }

    protected <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType, final Map<String, Object> properties) {
        final Cache<K, V> cache = _cacheHelper.getCache(cacheName, keyType, valueType, properties);
        log.debug("Retrieved cache {} with key {} and value {}", cacheName, keyType, valueType);
        return cache;
    }

    protected <K, V> Cache<K, V> getCache(final String cacheName) {
        return _cacheHelper.getCache(cacheName);
    }

    protected <K, V> V getCacheItem(final String cacheId, final K itemId, final Class<V> itemClass, final Object... parameters) {
        log.debug("Retrieving item with ID {} and class {} from cache {}", itemId, itemClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, V> extractor = (DataExtractor<K, V>) getExtractors().get(cacheId);
        Cache<K, V>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return ObjectUtils.getIfNull(cache.get(itemId), new CacheItemSupplier<>(cache, extractor, cacheId, itemId, itemClass, parameters));
    }

    protected <K, V> boolean isCacheEmpty(final String cacheId, final K itemId) {
        DataExtractor<K, V> extractor = (DataExtractor<K, V>) getExtractors().get(cacheId);
        Cache<K, V>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return !cache.containsKey(itemId);
    }

    protected <K, V> List<V> getCacheList(final String cacheId, final K itemId, final Class<V> listItemClass, final Object... parameters) {
        log.debug("Retrieving list with ID {} and item class {} from cache {}", itemId, listItemClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, List<V>> extractor = (DataExtractor<K, List<V>>) getExtractors().get(cacheId);
        Cache<K, List<V>>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return ObjectUtils.getIfNull(cache.get(itemId), new CacheListSupplier<>(cache, extractor, cacheId, itemId, listItemClass, parameters));
    }

    protected <K, L, R> Map<L, R> getCacheMap(final String cacheId, final K itemId, final Class<L> mapKeyClass, final Class<R> mapValueClass, final Object... parameters) {
        log.debug("Retrieving map with ID {}, key class {}, and value class {} from cache {}", itemId, mapKeyClass, mapValueClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, Map<L, R>> extractor = (DataExtractor<K, Map<L, R>>) getExtractors().get(cacheId);
        Cache<K, Map<L, R>>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return ObjectUtils.getIfNull(cache.get(itemId), new CacheMapSupplier<>(cache, extractor, cacheId, itemId, mapKeyClass, mapValueClass, parameters));
    }

    protected <K, V> V forceCacheItem(final String cacheId, final K itemId, final Class<V> itemClass, final Object... parameters) {
        log.debug("Forcing item with ID {} and class {} from cache {}", itemId, itemClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, V> extractor = (DataExtractor<K, V>) getExtractors().get(cacheId);
        Cache<K, V>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return new CacheItemSupplier<>(cache, extractor, cacheId, itemId, itemClass, parameters).get();
    }

    protected <K, V> List<V> forceCacheList(final String cacheId, final K itemId, final Class<V> listItemClass, final Object... parameters) {
        log.debug("Forcing list with ID {} and item class {} from cache {}", itemId, listItemClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, List<V>> extractor = (DataExtractor<K, List<V>>) getExtractors().get(cacheId);
        Cache<K, List<V>>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return new CacheListSupplier<>(cache, extractor, cacheId, itemId, listItemClass, parameters).get();
    }

    protected <K, L, R> Map<L, R> forceCacheMap(final String cacheId, final K itemId, final Class<L> mapKeyClass, final Class<R> mapValueClass, final Object... parameters) {
        log.debug("Forcing map with ID {}, key class {}, and value class {} from cache {}", itemId, mapKeyClass, mapValueClass, cacheId);
        //noinspection unchecked
        DataExtractor<K, Map<L, R>> extractor = (DataExtractor<K, Map<L, R>>) getExtractors().get(cacheId);
        Cache<K, Map<L, R>>         cache     = getCache(cacheId, extractor.getKeyType(), extractor.getValueType());
        return new CacheMapSupplier<>(cache, extractor, cacheId, itemId, mapKeyClass, mapValueClass, parameters).get();
    }

    /**
     * Gets a cached item from the specified cache, using the cache partition and item ID as a compound cache key. If the
     * item is not already cached, it will be generated by the {@link DataExtractor} implementation associated with the
     * specified cache.
     *
     * @param cacheId       The ID of the cache from which to retrieve the partitioned cache item
     * @param partitionId   The ID of the partition to use when generating the compound cache key
     * @param itemId        The unique ID of the specific item to be retrieved from the cache
     * @param mapValueClass The type of the object to be returned from the cache
     * @param parameters    Any parameters that may be required to generate the item if not already present in the cache
     * @param <R>           The type of the object to be returned from the cache
     *
     * @return Returns the requested item from the cache if present or as generated by the extractor and subsequently cached
     */
    protected <R> R getCacheMapPartitionItem(final String cacheId, final String partitionId, final String itemId, final Class<R> mapValueClass, final Object... parameters) {
        final String cacheKey = createCompoundCacheKeyFromElements(itemId, partitionId);
        log.debug("Retrieving map partition with ID {} and value class {} from cache {}", cacheKey, mapValueClass, cacheId);
        //noinspection unchecked
        DataExtractor<String, Map<String, R>> extractor = (DataExtractor<String, Map<String, R>>) getExtractors().get(cacheId);
        Cache<String, R>                      cache     = getCache(cacheId, String.class, mapValueClass);
        return ObjectUtils.getIfNull(cache.get(cacheKey), () -> {
            log.debug("No map partition with ID {} and value class {} found in cache {}, calling extractor", cacheKey, mapValueClass, cacheId);
            final Map<String, R> map = extractor.extract(itemId, parameters);
            map.forEach((key, value) -> {
                final String compoundKey = createCompoundCacheKeyFromElements(itemId, key);
                cache.put(compoundKey, value);
                List<String> cacheKeys = getKeysCache().containsKey(cacheId) ? getKeysCache().get(cacheId) : new ArrayList<>();
                cacheKeys.add(compoundKey);
                getKeysCache().put(cacheId, cacheKeys);
            });
            return map.get(partitionId);
        });
    }

    protected void cacheObject(final String cacheId, final String itemId, final Object object) {
        if (object == null) {
            log.warn("I was asked to cache an object with ID '{}' but the object was null.", cacheId);
            return;
        }
        log.trace("Request to cache item '{}' in cache {}, evaluating", itemId, cacheId);
        if (has(cacheId, itemId)) {
            log.trace("Cache entry '{}' exists and force update not specified, evaluating for change", cacheId);
            final Object existing = getCache(cacheId).get(itemId);
            if (object.equals(existing)) {
                log.trace("Existing and updated cached objects for entry '{}' are identical, returning without updating", cacheId);
                return;
            }
            log.trace("Cache entry '{}' already exists but differs from updated cache item:\n\nExisting:\n{}\nUpdated:\n{}", cacheId, existing, object);
        }
        forceCacheObject(cacheId, itemId, object);
    }

    private <T> boolean has(final String cacheId, final T itemId) {
        log.debug("Checking if item with ID {} exists in cache {}", itemId, cacheId);
        return getCache(cacheId).containsKey(itemId);
    }

    protected <T> void forceCacheObject(final String cacheId, final T itemId, final Object object) {
        final Object candidate = object instanceof Provider ? ((Provider<?>) object).get() : object;
        final Object target;
        if (candidate instanceof Map) {
            //noinspection rawtypes,unchecked
            target = checkMapForNullKey(cacheId, (Map) candidate);
        } else if (candidate instanceof Multimap) {
            //noinspection rawtypes,unchecked
            target = checkMultimapForNullKey(cacheId, (Multimap) candidate).asMap();
        } else {
            target = candidate;
        }
        log.trace("Storing cache entry '{}' with object of type: {}", cacheId, target.getClass().getName());
        getCache(cacheId).put(itemId, target); // CACHING: getCache().put(cacheId, target);
    }

    protected <K, V> Map<K, V> buildImmutableMap(final List<Map<K, V>> maps) {
        // The keys set keeps track of keys that have already been added
        // so that we don't add them again.
        final Set<K> keys = new HashSet<>();

        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (final Map<K, V> map : maps) {
            builder.putAll(Maps.filterKeys(map, entry -> !keys.contains(entry)));
            keys.addAll(map.keySet());
        }
        final ImmutableMap<K, V> map = builder.build();
        log.debug("Created immutable map combining {} maps, resulting in {} total entries", maps.size(), map.size());
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

    protected List<Object> evict(final String cacheName, final Collection<String> cacheIds) {
        return cacheIds.stream().map(cacheId -> evict(cacheName, cacheId)).collect(Collectors.toList());
    }

    protected Object evict(final String cacheName, final String cacheId) {
        log.debug("Evicting cache entry '{}' from cache {}", cacheId, cacheName);
        return getCache(cacheName).getAndRemove(cacheId);
    }

    /**
     * Evicts all entries in the specified cache partition.
     *
     * @param cacheId       The ID of the cache
     * @param partitionId   The ID of the partition
     * @param mapValueClass The value type
     * @param <R>           The value type
     */
    protected <R> void evictCacheMapPartition(final String cacheId, final String partitionId, final Class<R> mapValueClass) {
        if (!getKeysCache().containsKey(cacheId)) {
            log.info("I was asked to evict all items from partition {} from cache {}, but I have no keys stored for that", partitionId, cacheId);
            return;
        }
        Cache<String, R>                 cache     = getCache(cacheId, String.class, mapValueClass);
        String                           regex     = "^" + partitionId + ":.*$";
        final Map<Boolean, List<String>> splitKeys = getKeysCache().get(cacheId).stream().collect(Collectors.partitioningBy(key -> key.matches(regex)));

        // Keys that don't match the pattern are retained, so replace the keys cache for the cache ID with these remaining keys.
        getKeysCache().put(cacheId, splitKeys.get(false));

        // Keys that match the pattern are to be removed from the cache.
        splitKeys.get(true).forEach(cache::remove);
    }

    protected <R> void evictCacheMapPartition(final String cacheId, final String partitionId, final String itemId) {
        if (!getKeysCache().containsKey(cacheId)) {
            log.info("I was asked to evict item {} from partition {} from cache {}, but I have no keys stored for that", itemId, partitionId, cacheId);
            return;
        }
        getCache(cacheId, String.class, List.class).remove(createCompoundCacheKeyFromElements(itemId, partitionId));
    }

    protected static String createCompoundCacheKeyFromElements(final Object... elements) {
        return StringUtils.join(elements, ":");
    }

    private void initializeCaches() {
        _extractors.forEach((cacheName, extractor) -> {
            // CACHING: Need to make it so that we can add custom or configuration-specific listeners to caches, but for
            //  now there are defaults configured in JCacheHelper.
            if (extractor.isPartitionedMap()) {
                getCache(cacheName, String.class, extractor.getPartitionValueType(), extractor.getCacheProperties());
            } else {
                getCache(cacheName, extractor.getKeyType(), extractor.getValueType(), extractor.getCacheProperties());
            }
        });
    }

    @Data
    @AllArgsConstructor
    protected class CacheItemSupplier<K, V> implements Supplier<V> {
        private final Cache<K, V>         _cache;
        private final DataExtractor<K, V> _extractor;
        private final String              _cacheId;
        private final K                   _itemId;
        private final Class<V>            _itemClass;
        private final Object[]            _parameters;

        protected CacheItemSupplier(final String cacheId, final K itemId, final Class<V> itemClass, final Object... parameters) {
            _cacheId    = cacheId;
            _itemId     = itemId;
            _itemClass  = itemClass;
            _parameters = parameters;
            //noinspection unchecked
            _extractor = (DataExtractor<K, V>) getExtractors().get(cacheId);
            _cache     = AbstractXftItemAndCacheEventHandlerMethod.this.getCache(cacheId, _extractor.getKeyType(), _extractor.getValueType());
        }

        @Override
        public V get() {
            log.debug("No item with ID {} and class {} found in cache {}, calling extractor", _itemId, _itemClass, _cacheId);
            final V item = _extractor.extract(_itemId, _parameters);
            if (item != null) {
                _cache.put(_itemId, item);
                return item;
            }
            return null;
        }
    }

    @Data
    private static class CacheListSupplier<K, V> implements Supplier<List<V>> {
        private final Cache<K, List<V>>         _cache;
        private final DataExtractor<K, List<V>> _extractor;
        private final String                    _cacheId;
        private final K                         _itemId;
        private final Class<V>                  _listItemClass;
        private final Object[]                  _parameters;

        @Override
        public List<V> get() {
            log.debug("No list with ID {} and item class {} found in cache {}, calling extractor", _itemId, _listItemClass, _cacheId);
            final List<V> list = _extractor.extract(_itemId, _parameters);
            _cache.put(_itemId, list);
            return list;
        }
    }


    @Data
    private static class CacheMapSupplier<K, L, R> implements Supplier<Map<L, R>> {
        private final Cache<K, Map<L, R>>         _cache;
        private final DataExtractor<K, Map<L, R>> _extractor;
        private final String                      _cacheId;
        private final K                           _itemId;
        private final Class<L>                    _mapKeyClass;
        private final Class<R>                    _mapValueClass;
        private final Object[]                    _parameters;

        @Override
        public Map<L, R> get() {
            log.debug("No map with ID {}, key class {}, and value class {} found in cache {}, calling extractor", _itemId, _mapKeyClass, _mapValueClass, _cacheId);
            final Map<L, R> map = _extractor.extract(_itemId, _parameters);
            _cache.put(_itemId, map);
            return map;
        }
    }

    private static <K, V> Multimap<K, V> checkMultimapForNullKey(final String cacheId, final Multimap<K, V> map) {
        try {
            if (map.containsKey(null)) {
                log.warn("I was asked to cache a multimap with the ID {}, but this multimap has a null key. This could indicate a corrupt index hash in the database. Removing to allow execution to proceed. The value(s) stored under the null key are: {}", cacheId, map.removeAll(null));
            }
            return map;
        } catch (UnsupportedOperationException e) {
            log.warn("I was asked to cache a multimap with the ID {}, but this multimap has a null key. This could indicate a corrupt index hash in the database. The multimap is immutable, so I'm creating a copy without the null key to allow execution to proceed. The value(s) stored under the null key are: {}", cacheId, map.get(null));
            return ImmutableMultimap.copyOf(Multimaps.filterKeys(map, Objects::nonNull));
        }
    }

    private static <K, V> Map<K, V> checkMapForNullKey(final String cacheId, final Map<K, V> map) {
        try {
            if (map.containsKey(null)) {
                log.warn("I was asked to cache a map with the ID {}, but this map has a null key. This could indicate a corrupt index hash in the database. Removing to allow execution to proceed. The value stored under the null key is: {}", cacheId, map.remove(null));
            }
            return map;
        } catch (UnsupportedOperationException e) {
            log.warn("I was asked to cache a map with the ID {}, but this map has a null key. This could indicate a corrupt index hash in the database. The map is immutable, so I'm creating a copy without the null key to allow execution to proceed. The value stored under the null key is: {}", cacheId, map.get(null));
            return ImmutableMap.copyOf(Maps.filterKeys(map, Objects::nonNull));
        }
    }
}
