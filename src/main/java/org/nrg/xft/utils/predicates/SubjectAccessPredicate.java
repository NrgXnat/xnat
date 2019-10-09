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

import static org.nrg.framework.constants.Scope.Subject;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class SubjectAccessPredicate extends DataAccessPredicate {
    public SubjectAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project) throws NotFoundException {
        super(service, template, user, accessLevel, Subject, project);
    }

    protected String getFailureMessage(final String subject) {
        return "An error occurred while testing " + getAccessLevel().code() + " access to the subject " + subject + (StringUtils.isNotBlank(getProject()) ? " under project " + getProject() : "") + " for the user " + getUser().getUsername() + ". Failing permissions check to be safe, but this may not be correct.";
    }
}
