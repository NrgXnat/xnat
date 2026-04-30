package org.nrg.framework.jcache;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.generics.GenericUtils;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Getter
@Accessors(prefix = "_")
@Slf4j
public class JCacheHelper {
    public static final String            EHCACHE_PROVIDER_DEFAULT  = "org.ehcache.jsr107.EhcacheCachingProvider";
    public static final String            EHCACHE_URI_DEFAULT       = "jcache-ehcache.xml";
    public static final String            REDISSON_PROVIDER_DEFAULT = "org.redisson.jcache.JCachingProvider";
    public static final String            REDISSON_URI_DEFAULT      = "jcache-redisson.yaml";
    public static final String            JCACHE_PROVIDER_DEFAULT   = EHCACHE_PROVIDER_DEFAULT;
    public static final String            JCACHE_PROVIDER_ENV       = "hibernate.javax.cache.provider";
    public static final String            JCACHE_URI_DEFAULT        = EHCACHE_URI_DEFAULT;
    public static final String            JCACHE_URI_ENV            = "hibernate.javax.cache.uri";
    public static final String            CONFIG_EXPIRY             = "expiry";
    public static final String            CONFIG_MANAGEMENT         = "management";
    public static final String            CONFIG_STATISTICS         = "statistics";
    public static final String            CONFIG_STORE_BY_VALUE     = "storeByValue";
    public static final Predicate<String> IS_REDISSON               = cacheProvider -> StringUtils.equals(cacheProvider, REDISSON_PROVIDER_DEFAULT);

    private final CacheManager                          _springCacheManager;
    private final javax.cache.CacheManager              _javaXCacheManager;
    private final String                                _cacheProvider;
    private final String                                _cacheUri;
    private final Config                                _redissonConfig;
    private final Map<String, Pair<Class<?>, Class<?>>> _registry;

    @Autowired
    public JCacheHelper(final CacheManager springCacheManager, final Properties hibernateProperties) throws IOException {
        _springCacheManager = springCacheManager;
        _javaXCacheManager  = ((JCacheCacheManager) _springCacheManager).getCacheManager();
        _cacheProvider      = hibernateProperties.getProperty(JCACHE_PROVIDER_ENV, JCACHE_PROVIDER_DEFAULT);
        _cacheUri           = hibernateProperties.getProperty(JCACHE_URI_ENV, StringUtils.startsWith("org.ehcache", _cacheProvider) ? EHCACHE_URI_DEFAULT : REDISSON_URI_DEFAULT);
        _redissonConfig     = isRedissonConfigured() ? createRedissonConfig() : null;
        _registry           = new ConcurrentHashMap<>();
        _javaXCacheManager.getCacheNames().forEach(cacheName -> {
            final Cache<?, ?> cache = _javaXCacheManager.getCache(cacheName);
            //noinspection unchecked
            final CompleteConfiguration<?, ?> configuration = cache.getConfiguration(CompleteConfiguration.class);
            _registry.put(cacheName, Pair.of(configuration.getKeyType(), configuration.getValueType()));
        });
    }

    @SuppressWarnings("unused")
    public static String getCachingProviderClass(final String cacheUri) {
        return IS_REDISSON.test(cacheUri)
               ? JCacheHelper.REDISSON_PROVIDER_DEFAULT
               : JCacheHelper.EHCACHE_PROVIDER_DEFAULT;
    }

    public static CachingProvider getCachingProvider(final String cacheProvider) {
        List<CachingProvider> providers = GenericUtils.convertToTypedList(Caching.getCachingProviders());
        log.debug("Found {} caching providers: {}", providers.size(), providers.stream().map(CachingProvider::getClass).map(Class::getName).collect(Collectors.joining(", ")));
        if (providers.size() == 1) {
            return Caching.getCachingProvider();
        }
        return Caching.getCachingProvider(cacheProvider);
    }

    public javax.cache.CacheManager getJavaXCacheManager() {
        return _javaXCacheManager;
    }

    @SuppressWarnings("unused")
    public List<String> getCacheNames() {
        return GenericUtils.convertToTypedList(getJavaXCacheManager().getCacheNames());
    }

    public CompleteConfiguration<?, ?> getCacheConfiguration(final String cacheName) {
        final Cache<?, ?> cache = getCache(cacheName);
        if (cache == null) {
            return null;
        }
        //noinspection unchecked
        return cache.getConfiguration(CompleteConfiguration.class);
    }

    public <K, V> Cache<K, V> getCache(final String cacheName) {
        final Optional<Pair<Class<?>, Class<?>>> optional = getCacheTypes(cacheName);
        if (optional.isEmpty()) {
            return null;
        }
        final Pair<Class<?>, Class<?>> keyAndValueTypes = optional.get();
        //noinspection unchecked
        return (Cache<K, V>) getCache(cacheName, keyAndValueTypes.getKey(), keyAndValueTypes.getValue());
    }

    public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        return getCache(cacheName, keyType, valueType, Collections.emptyMap());
    }

    public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType, final Map<String, Object> properties) {
        try {
            Cache<K, V> existing = getJavaXCacheManager().getCache(cacheName, keyType, valueType);
            if (existing != null) {
                log.info("Got request for cache named {} with key type {} and value type {}: that cache already exists so returning that", cacheName, keyType, valueType);
                return existing;
            }
        } catch (ClassCastException e) {
            //noinspection unchecked
            CompleteConfiguration<?, ?> configuration = getJavaXCacheManager().getCache(cacheName).getConfiguration(CompleteConfiguration.class);
            throw new CacheException("The cache named " + cacheName + " already exists, but the key and value types aren't compatible: you requested Cache<" + keyType.getName() + ", " + valueType.getName() + ">, but the existing instance is Cache<" + configuration.getKeyType() + ", " + configuration.getValueType() + ">");
        }
        return createCache(cacheName, keyType, valueType, properties);
    }

    @SuppressWarnings("unused")
    public <K, V> Cache<K, List<V>> getCacheOfLists(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        return getCacheOfLists(cacheName, keyType, valueType, Collections.emptyMap());
    }

    public <K, V> Cache<K, List<V>> getCacheOfLists(final String cacheName, final Class<K> keyType, final Class<V> valueType, final Map<String, Object> properties) {
        // CACHING: This is all very suboptimal.
        try {
            Cache<K, ?> existing = getJavaXCacheManager().getCache(cacheName, keyType, List.class);
            if (existing != null) {
                log.info("Got request for cache named {} with key type {} and a list of value type {}: that cache already exists so returning that", cacheName, keyType, valueType);
                //noinspection unchecked
                return (Cache<K, List<V>>) existing;
            }
        } catch (ClassCastException e) {
            //noinspection unchecked
            CompleteConfiguration<?, ?> configuration = getJavaXCacheManager().getCache(cacheName).getConfiguration(CompleteConfiguration.class);
            throw new CacheException("The cache named " + cacheName + " already exists, but the key and value types aren't compatible: you requested Cache<" + keyType.getName() + ", List<" + valueType.getName() + ">>, but the existing instance is Cache<" + configuration.getKeyType() + ", " + configuration.getValueType() + ">");
        }
        final Cache<K, ?> created = createCache(cacheName, keyType, List.class, properties);
        //noinspection unchecked
        return (Cache<K, List<V>>) created;
    }

    @SuppressWarnings("unused")
    public <K, V, T> Cache<K, Map<V, T>> getCacheOfMaps(final String cacheName, final Class<K> keyType, final Class<V> mapKeyType, final Class<T> mapValueType) {
        return getCacheOfMaps(cacheName, keyType, mapKeyType, mapValueType, Collections.emptyMap());
    }

    public <K, V, T> Cache<K, Map<V, T>> getCacheOfMaps(final String cacheName, final Class<K> keyType, final Class<V> mapKeyType, final Class<T> mapValueType, final Map<String, Object> properties) {
        // CACHING: This is all very suboptimal.
        try {
            Cache<K, ?> existing = getJavaXCacheManager().getCache(cacheName, keyType, Map.class);
            if (existing != null) {
                log.info("Got request for cache named {} with key type {} and a map with key type {} and value type {}: that cache already exists so returning that", cacheName, keyType, mapKeyType, mapValueType);
                //noinspection unchecked
                return (Cache<K, Map<V, T>>) existing;
            }
        } catch (ClassCastException e) {
            //noinspection unchecked
            CompleteConfiguration<?, ?> configuration = getJavaXCacheManager().getCache(cacheName).getConfiguration(CompleteConfiguration.class);
            throw new CacheException("The cache named " + cacheName + " already exists, but the key and value types aren't compatible: you requested Cache<" + keyType.getName() + ", Map<" + mapKeyType.getName() + ", " + mapValueType.getName() + ">>, but the existing instance is Cache<" + configuration.getKeyType() + ", " + configuration.getValueType() + ">");
        }
        final Cache<K, ?> created = createCache(cacheName, keyType, Map.class, properties);
        //noinspection unchecked
        return (Cache<K, Map<V, T>>) created;
    }

    public org.springframework.cache.Cache getSpringCache(final String cacheName) {
        log.info("Trying to get the cache {} from the Spring cache manager", cacheName);
        return getSpringCacheManager().getCache(cacheName);
    }

    public Optional<Pair<Class<?>, Class<?>>> getCacheTypes(final String cacheName) {
        return Optional.ofNullable(_registry.get(cacheName));
    }

    @Nonnull
    private <K, V> Cache<K, V> createCache(String cacheName, Class<K> keyType, Class<V> valueType, final Map<String, Object> properties) {
        log.info("Got request for cache named {} with key type {} and value type {}: that cache doesn't exist so creating it", cacheName, keyType, valueType);
        final Configuration<K, V> configuration = getConfiguration(keyType, valueType, properties);
        final Cache<K, V>         cache         = getJavaXCacheManager().createCache(cacheName, configuration);
        // CACHING: Need to make this configurable/customizable, but for now just use these defaults.
        cache.registerCacheEntryListener(new MutableCacheEntryListenerConfiguration<>(new FactoryBuilder.SingletonFactory<>(new DefaultGenericCacheEntryListener<>()),
                                                                                      new FactoryBuilder.SingletonFactory<>(new GenericCacheEntryEventFilter<>()),
                                                                                      false, false));
        _registry.put(cacheName, Pair.of(keyType, valueType));
        return cache;
    }

    @SuppressWarnings("SameParameterValue")
    private <K, V> javax.cache.configuration.Configuration<K, V> getConfiguration(final Class<K> keyType, final Class<V> valueType, final Map<String, Object> properties) {
        log.debug("Creating cache configuration for key type {} and value type {}", keyType, valueType);

        // CACHING: Make configuration customizable on a per-cache level through extractor properties
        MutableConfiguration<K, V> jcacheConfig = new MutableConfiguration<>();
        jcacheConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf((Duration) properties.getOrDefault(CONFIG_EXPIRY, Duration.TEN_MINUTES)))
                    .setManagementEnabled((Boolean) properties.getOrDefault(CONFIG_MANAGEMENT, true))
                    .setStatisticsEnabled((Boolean) properties.getOrDefault(CONFIG_STATISTICS, true))
                    .setStoreByValue((Boolean) properties.getOrDefault(CONFIG_STORE_BY_VALUE, _redissonConfig != null)) // CACHING: This fixes the XFTItem munging issue, but must be true for distributed cache.
                    .setTypes(keyType, valueType);
        return _redissonConfig == null ? jcacheConfig : RedissonConfiguration.fromConfig(_redissonConfig, jcacheConfig);
    }

    private boolean isRedissonConfigured() {
        return IS_REDISSON.test(_cacheProvider);
    }

    private Config createRedissonConfig() throws IOException {
        try (InputStream inputStream = new PathMatchingResourcePatternResolver().getResource(_cacheUri).getURI().toURL().openStream()) {
            return Config.fromYAML(inputStream);
        }
    }
}
