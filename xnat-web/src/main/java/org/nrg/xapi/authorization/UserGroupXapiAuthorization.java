package org.nrg.xapi.authorization;

import org.h2.util.StringUtils;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        final Map<String, UserGroupI> groupsForUser = Groups.getGroupsForUser(user);
        final List<String> groupsToAdd = getGroups(getJoinPoint());
        for (final String group : groupsToAdd) {
            try {
                int indexOfEndOfProject = group.lastIndexOf("_");
                String proj = group.substring(0,indexOfEndOfProject);
                String end = group.substring(indexOfEndOfProject+1);
                if(!(StringUtils.equals(end,"owner")||StringUtils.equals(end,"member")||StringUtils.equals(end,"collaborator")) || !Permissions.isProjectOwner(user, proj)){
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
