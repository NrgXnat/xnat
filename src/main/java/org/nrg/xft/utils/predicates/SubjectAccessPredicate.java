package org.nrg.xft.utils.predicates;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class SubjectAccessPredicate extends DataAccessPredicate {
    public SubjectAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project) throws NotFoundException {
        super(service, template, user, accessLevel, project);
    }

    @Override
    public boolean apply(final String subject) {
        final UserI user = getUser();
        try {
            verifySubjectExists(subject);
            switch (getAccessLevel()) {
                case Read:
                    return getService().canRead(user, "xnat:subjectData/project", getProject());

                case Edit:
                    return getService().canEdit(user, "xnat:subjectData/project", getProject());

                case Delete:
                    return getService().canDelete(user, "xnat:subjectData/project", getProject());
            }
        } catch (NotFoundException e) {
            getMissing().add(subject);
        } catch (Exception e) {
            getFailed().put("An error occurred while testing " + getAccessLevel().code() + " access to the subject " + subject + " in project " + getProject() + " for the user " + getUser().getUsername() + ". Failing permissions check to be safe, but this may not be correct.", e);
        }
        return false;
    }
}
