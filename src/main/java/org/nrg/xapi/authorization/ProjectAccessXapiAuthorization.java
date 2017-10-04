package org.nrg.xapi.authorization;

import org.nrg.xdat.security.helpers.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class ProjectAccessXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        final List<String> projectIds = getProjectIds(getJoinPoint());
        for (final String projectId : projectIds) {
            try {
                if (!Permissions.hasAccess(getUser(), projectId, getAccessLevel())) {
                    return false;
                }
            } catch (Exception e) {
                _log.error("An error occurred while testing " + getAccessLevel().code() + " access to the project " + projectId + " for the user " + getUsername() + ". Failing permissions check to be safe, but this may not be correct.", e);
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

    private static final Logger _log = LoggerFactory.getLogger(ProjectAccessXapiAuthorization.class);
}
