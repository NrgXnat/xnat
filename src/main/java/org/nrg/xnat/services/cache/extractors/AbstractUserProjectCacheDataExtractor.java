package org.nrg.xnat.services.cache.extractors;

import org.nrg.xnat.services.cache.UserProjectCache;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractUserProjectCacheDataExtractor<K, V> extends AbstractDataExtractor<UserProjectCache, K, V> {
    protected AbstractUserProjectCacheDataExtractor(final UserProjectCache cache, final String cacheName, final NamedParameterJdbcTemplate template) {
        this(cache, cacheName, template, null);
    }

    protected <T> AbstractUserProjectCacheDataExtractor(final UserProjectCache cache, final String cacheName, final NamedParameterJdbcTemplate template, final Class<T> partitionValueType) {
        super(cache, cacheName, template, partitionValueType);
    }

    public String getCacheGroup() {
        return UserProjectCache.CACHE_NAME;
    }
}
