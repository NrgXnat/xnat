package org.nrg.xnat.utils.predicates;

import com.google.common.base.Predicate;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class ProjectPermissionPredicate implements Predicate<String> {
    public ProjectPermissionPredicate(final UserI user) {
        _user = user;
    }

    @Override
    public boolean apply(final String project) {
        try {
            return !PrearcUtils.canModify(getUser(), project);
        } catch (Exception e) {
            log.error("An error occurred checking if the user {} has edit access to the project {}", getUser().getUsername(), project, e);
            return true;
        }
    }

    private final UserI _user;
}
