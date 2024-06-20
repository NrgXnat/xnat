package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_USER_GROUPS;

@Component
@Slf4j
public class UserGroupsExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, List<String>> {
    private final UserManagementServiceI _userService;

    @Autowired
    public UserGroupsExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template, final UserManagementServiceI userService) {
        super(cache, CACHE_USER_GROUPS, template);
        _userService = userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> extract(final String username, final Object... parameters) {
        if (isInvalidExtractRequest(username, parameters)) {
            return Collections.emptyList();
        }

        log.info("Initializing user group IDs cache entry for user '{}'", username);

        if (!_userService.exists(username)) {
            throw new NrgServiceRuntimeException("The user " + username + " does not exist");
        }

        final List<String> groupIds = getTemplate().queryForList(GroupsAndPermissionsCache.QUERY_GET_GROUPS_FOR_USER, new MapSqlParameterSource(PARAM_USERNAME, username), String.class);
        log.debug("Found {} user group IDs cache entry for user '{}'", groupIds.size(), username);
        return groupIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Users.getUsernames(getTemplate());
    }
}
