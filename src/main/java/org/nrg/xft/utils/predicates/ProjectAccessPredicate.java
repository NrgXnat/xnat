package org.nrg.xft.utils.predicates;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.nrg.framework.constants.Scope.Project;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class ProjectAccessPredicate extends DataAccessPredicate {
    public static final String UNASSIGNED = "Unassigned";

    public ProjectAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel) {
        super(service, template, user, accessLevel, Project);
    }

    @Override
    protected boolean preapprove(final String project) {
        return StringUtils.equalsIgnoreCase(UNASSIGNED, project) && (getAccessLevel() == AccessLevel.Read || Groups.hasAllDataAdmin(getUser()));
    }

    @Override
    protected String getFailureMessage(final String project) {
        return "An error occurred while testing " + getAccessLevel().code() + " access to the project " + project + " for the user " + getUser().getUsername() + ". Failing permissions check to be safe, but this may not be correct.";
    }

    private static final String SECURED_PROPERTY_READ    = "xnat:subjectData/project";
    private static final String SECURED_PROPERTY_DEFAULT = "xnat:projectData/ID";
}
