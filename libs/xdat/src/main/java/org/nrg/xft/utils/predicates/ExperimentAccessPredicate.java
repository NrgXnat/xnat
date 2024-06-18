package org.nrg.xft.utils.predicates;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.nrg.framework.constants.Scope.Experiment;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class ExperimentAccessPredicate extends DataAccessPredicate {
    public ExperimentAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project, final String subject) throws NotFoundException {
        super(service, template, user, accessLevel, Experiment, project, subject);
    }

    protected String getFailureMessage(final String experiment) {
        final boolean hasProject = StringUtils.isNotBlank(getProject());
        final boolean hasSubject = StringUtils.isNotBlank(getSubject());
        final String  location;
        if (hasProject && hasSubject) {
            location = " under project " + getProject() + " and subject " + getSubject();
        } else if (hasProject) {
            location = " under project " + getProject();
        } else if (hasSubject) {
            location = " under subject " + getSubject();
        } else {
            location = "";
        }
        return "An error occurred while testing " + getAccessLevel().code() + " access to the experiment " + experiment + location + " for the user " + getUser().getUsername() + ". Failing permissions check to be safe, but this may not be correct.";
    }
}
