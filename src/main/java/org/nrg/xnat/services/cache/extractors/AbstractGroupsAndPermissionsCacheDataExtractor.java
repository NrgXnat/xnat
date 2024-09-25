package org.nrg.xnat.services.cache.extractors;

import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractGroupsAndPermissionsCacheDataExtractor<K, V> extends AbstractDataExtractor<GroupsAndPermissionsCache, K, V> {
    protected AbstractGroupsAndPermissionsCacheDataExtractor(final GroupsAndPermissionsCache cache, final String cacheName, final NamedParameterJdbcTemplate template) {
        this(cache, cacheName, template, null);
    }

    protected <T> AbstractGroupsAndPermissionsCacheDataExtractor(final GroupsAndPermissionsCache cache, final String cacheName, final NamedParameterJdbcTemplate template, final Class<T> partitionValueType) {
        super(cache, cacheName, template, partitionValueType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheGroup() {
        return GroupsAndPermissionsCache.CACHE_NAME;
    }
}
