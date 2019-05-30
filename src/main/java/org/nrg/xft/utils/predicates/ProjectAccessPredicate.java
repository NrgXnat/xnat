package org.nrg.xft.utils.predicates;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class ProjectAccessPredicate extends DataAccessPredicate {
    public static final String UNASSIGNED = "Unassigned";

    public ProjectAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel) {
        super(service, template, user, accessLevel);
    }

    @Override
    protected boolean applyImpl(final String project) {
        try {
            final UserI user = getUser();
            if (StringUtils.equalsIgnoreCase(UNASSIGNED, project)) {
                return getAccessLevel() == AccessLevel.Read || Groups.hasAllDataAdmin(user);
            }
            verifyProjectExists(project);
            switch (getAccessLevel()) {
                case Read:
                    return getService().canRead(user, "xnat:subjectData/project", project);

                case Edit:
                    return getService().canEdit(user, "xnat:projectData/ID", project);

                case Delete:
                    return getService().canDelete(user, "xnat:projectData/ID", project);
            }
        } catch (NotFoundException e) {
            getMissing().add(project);
        } catch (Exception e) {
            getFailed().put("An error occurred while testing " + getAccessLevel().code() + " access to the project " + project + " for the user " + getUser().getUsername() + ". Failing permissions check to be safe, but this may not be correct.", e);
        }
        return false;
    }
}
