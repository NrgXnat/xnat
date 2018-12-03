package org.nrg.xapi.authorization;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Checks whether user can access the system user list.
 */
@Component
public class UserGroupXapiAuthorization extends AbstractXapiAuthorization {

    @Override
    protected boolean checkImpl() {
        final UserI user = getUser();
        if(Roles.isSiteAdmin(user)){
            return true;
        }
        final List<String> groupsToAdd = getGroups(getJoinPoint());
        for (final String group : groupsToAdd) {
            try {
                int indexOfEndOfProject = group.lastIndexOf("_");
                String proj = group.substring(0,indexOfEndOfProject);
                String end = group.substring(indexOfEndOfProject+1);
                if (!StringUtils.equalsAny(end, "owner", "member", "collaborator") || !Permissions.isProjectOwner(user, proj)) {
                    return false;
                }
            } catch (Exception e) {
                _log.error("An error occurred while testing checking whether user " + user.getUsername() + " could add the requested groups. Failing permissions check to be safe, but this may not be correct.", e);
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

    private static final Logger _log = LoggerFactory.getLogger(UserGroupXapiAuthorization.class);
}
