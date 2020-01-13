package org.nrg.xapi.authorization;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Checks whether user can access the system user list.
 */
@Component
@Slf4j
public class UserGroupXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        if (Roles.isSiteAdmin(user)) {
            return true;
        }
        final List<String> groupsToAdd = getGroups(joinPoint);
        for (final String group : groupsToAdd) {
            try {
                final String project = StringUtils.substringBeforeLast(group, "_");
                final String end     = StringUtils.substringAfterLast(group, "_");
                if (!StringUtils.equalsAny(end, "owner", "member", "collaborator") || !Permissions.isProjectOwner(user, project)) {
                    return false;
                }
            } catch (Exception e) {
                log.error("An error occurred while testing checking whether user " + user.getUsername() + " could add the requested groups. Failing permissions check to be safe, but this may not be correct.", e);
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
