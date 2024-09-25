package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_GROUPS;

@Component
@Slf4j
public class GroupsExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, UserGroup> {
    @Autowired
    public GroupsExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_GROUPS, template);
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
    public UserGroup extract(final String groupId, final Object... parameters) {
        if (isInvalidExtractRequest(groupId, parameters)) {
            return null;
        }

        log.debug("Extracting group with ID {}", groupId);

        try {
            return new UserGroup(groupId, getTemplate());
        } catch (ItemNotFoundException e) {
            log.info("Asked to get user group {}, but it doesn't seem to exist, returning null", groupId);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Groups.getAllGroupIds(getTemplate());
    }
}
