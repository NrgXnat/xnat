package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_USER_LAST_UPDATED;

@Component
@Slf4j
public class UserLastUpdateExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, Date> {
    @Autowired
    public UserLastUpdateExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_USER_LAST_UPDATED, template);
    }

    /**
     * This method implementation returns a new date with each call to track the latest cache update for each user.
     *
     * @param username   The name of the user
     * @param parameters No parameters required
     *
     * @return A new date object.
     */
    @Override
    public Date extract(final String username, final Object... parameters) {
        return new Date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Users.getUsernames(getTemplate());
    }
}
