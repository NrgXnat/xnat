package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_PROJECT_GROUPS;

@Component
@Slf4j
public class ProjectGroupsExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, List<String>> {
    private static final String QUERY_PROJECT_GROUPS = "SELECT id AS glroups FROM xdat_usergroup WHERE tag = :" + PARAM_PROJECT_ID;

    @Autowired
    public ProjectGroupsExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_PROJECT_GROUPS, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getCacheProperties() {
        return NON_EXPIRING_TTL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> extract(final String projectId, final Object... parameters) {
        if (isInvalidExtractRequest(projectId, parameters)) {
            return Collections.emptyList();
        }

        log.debug("Extracting groups for project {}", projectId);

        return getTemplate().queryForList(QUERY_PROJECT_GROUPS, new MapSqlParameterSource(PARAM_PROJECT_ID, projectId), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return getAllProjectIds();
    }
}
