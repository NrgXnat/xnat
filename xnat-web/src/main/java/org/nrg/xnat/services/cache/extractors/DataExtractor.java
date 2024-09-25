package org.nrg.xnat.services.cache.extractors;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.nrg.framework.jcache.JCacheHelper;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface DataExtractor<K, V> {
    String PARAM_DATA_TYPE     = "dataType";
    String PARAM_EXPERIMENT_ID = "experimentId";
    String PARAM_PROJECT_ID    = "projectId";
    String PARAM_PROJECT_IDS   = "projectIds";
    String PARAM_SUBJECT_ID    = "subjectId";
    String PARAM_USERNAME      = "username";
    String PARAM_USER_IDS      = "userIds";

    Map<String, Object> NON_EXPIRING_TTL = ImmutableMap.of(JCacheHelper.CONFIG_EXPIRY, Duration.ETERNAL);

    String getCacheGroup();

    String getCacheName();

    Class<K> getKeyType();

    Class<V> getValueType();

    /**
     * If the {@link #getValueType() value type} for this extractor is <em>not</em> a map, this method is ignored. If
     * the value <em>is</em> a map, the value returned here indicates that the map should be partitioned and stored by
     * its individual keys rather than as a whole map. Due to type erasure, the cache creation process can't easily
     * determine the value type of the map, so it should be supplied here. In order for a partitioned map to work
     * properly, the cache <em>and</em> map keys <em>must</em> be strings.
     * <p>
     * The main use for a partitioned map is when the value type is a collection of some sort, like a list or set.
     * <p>
     * The default implementation of this method returns <pre>null</pre>.
     *
     * @return Returns the map's value type if the map should be partitioned.
     */
    <T> Class<T> getPartitionValueType();

    /**
     * Indicates whether the value type is a map partitioned by the primary cache key and the map key. Refer to {@link
     * #getPartitionValueType()} for more information on how partitioned maps work.
     *
     * @return Returns <pre>true</pre> if this cache uses a partitioned map value, <pre>false</pre> otherwise.
     */
    boolean isPartitionedMap();

    /**
     * Defines the properties for the cache configuration. These may include:
     *
     * <ul>
     *     <li>{@link org.nrg.framework.jcache.JCacheHelper#CONFIG_EXPIRY}: a <code>Duration</code> instance for the cache time-to-live configuration (defaults to 10 minutes)</li>
     *     <li>{@link org.nrg.framework.jcache.JCacheHelper#CONFIG_MANAGEMENT}: Indicates whether JMX management should be enabled for the cache (defaults to <code>true</code>)</li>
     *     <li>{@link org.nrg.framework.jcache.JCacheHelper#CONFIG_STATISTICS}: Indicates whether statistics should be enabled for the cache (defaults to <code>true</code>)</li>
     *     <li>{@link org.nrg.framework.jcache.JCacheHelper#CONFIG_STORE_BY_VALUE}: Indicates whether store-by-value should be enabled for the cache (defaults to <code>false</code>)</li>
     * </ul>
     * <p>
     * The default implementation for this method returns an empty map.
     *
     * @return A map containing any configuration options that should be set to a non-default value.
     */
    default Map<String, Object> getCacheProperties() {
        return Collections.emptyMap();
    }

    /**
     * Extracts a value for the submitted key and parameters. If the key itself is sufficient for retrieving the value,
     * e.g. a project ID, you don't need to provide any extra parameters. The type, order, and necessity of the extra
     * parameters is strictly dependent on the extractor implementation.
     *
     * @param key        The item cache key
     * @param parameters Any extra parameters necessary to extract the value
     *
     * @return The extracted value.
     */
    V extract(K key, Object... parameters);

    /**
     * Gets all keys for the target data, useful when initializing a cache. Note that this method does <i>not</i>
     * provide the list of keys currently in the cache, but a list of potential keys based on system data.
     * <p>
     * The default implementation of this method returns an empty list.
     *
     * @return A list of keys for the target data.
     */
    default List<K> getKeys() {
        return Collections.emptyList();
    }

    /**
     * Gets all keys for item partitions, which can be used to create compound cache keys for partitioned maps.
     * <p>
     * The default implementation of this method returns an empty list.
     *
     * @return A list of keys for item partitions.
     */
    default List<String> getPartitionKeys() {
        return Collections.emptyList();
    }

    /**
     * Initializes its values based on the keys it expects to find. For example, an extractor for user groups would get
     * a list of groups on the system, then iterate through the list, initializing and caching each group.
     * <p>
     * The default implementation of this method returns calls the {@link #getKeys()} method, calls the {@link
     * #extract(Object, Object...)} method for each key, and caches the result using the specified key.
     *
     * @return The total number of items that were initialized.
     */
    // CACHING: It might make sense to make this return Future<Integer> but current usage expects int.
    default int initialize(Cache<K, V> cache) {
        final List<K> keys = getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return 0;
        }
        keys.forEach(key -> cache.put(key, extract(key)));
        return keys.size();
    }
}
