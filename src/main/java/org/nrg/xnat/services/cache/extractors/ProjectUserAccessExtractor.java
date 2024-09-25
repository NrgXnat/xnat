package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.cache.DefaultUserProjectCache;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProjectUserAccessExtractor extends AbstractUserProjectCacheDataExtractor<String, String> {
    private final SerializerService _serializer;

    public ProjectUserAccessExtractor(final @Lazy UserProjectCache cache, final NamedParameterJdbcTemplate template, final SerializerService serializer) {
        super(cache, DefaultUserProjectCache.CACHE_PROJECT_USER_ACCESS, template);
        _serializer = serializer;
    }

    @Override
    public String extract(final String projectId, final Object... parameters) {
        if (isInvalidExtractRequest(projectId, parameters)) {
            return null;
        }

        log.info("Extracting users and access levels for project '{}'", projectId);
        final XnatProjectdata  project = getCache().get(null, projectId);
        final List<UserGroupI> groups;
        try {
            groups = project.getGroups();
        } catch (Exception e) {
            throw new NrgServiceRuntimeException("An error occurred trying to retrieve groups for the project " + projectId, e);
        }
        final Map<String, List<AccessLevel>> userAccessLevels = groups.stream()
                                                                      .map(UserGroupI::getUsernames)
                                                                      .flatMap(Collection::stream)
                                                                      .distinct()
                                                                      .map(ProjectUserAccessExtractor::getSafeUser)
                                                                      .collect(Collectors.toMap(UserI::getUsername, user -> Permissions.getAllUserProjectAccess(user, projectId)));
        log.debug("Found {} users with access levels for project {}", userAccessLevels.size(), projectId);
        try {
            return _serializer.toJson(userAccessLevels);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to serialize user access levels for the project " + projectId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return getAllProjectIds();
    }

    private static UserI getSafeUser(final String username) {
        try {
            return new XDATUser(username);
        } catch (UserNotFoundException | UserInitException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to retrieve user " + username, e);
        }
    }
}
