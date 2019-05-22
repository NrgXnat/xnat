package org.nrg.xapi.authorization;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.predicates.DataAccessPredicate;
import org.nrg.xft.utils.predicates.ExperimentAccessPredicate;
import org.nrg.xft.utils.predicates.ProjectAccessPredicate;
import org.nrg.xft.utils.predicates.SubjectAccessPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Checks whether the user is a site administrator.
 */
@Component
@Slf4j
public class DataObjectXapiAuthorization extends AbstractXapiAuthorization {
    @Autowired
    public DataObjectXapiAuthorization(final PermissionsServiceI service, final NamedParameterJdbcTemplate template) {
        _service = service;
        _template = template;
    }

    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) throws InsufficientPrivilegesException {
        final List<String> projects       = getProjects(joinPoint);
        final boolean      hasProjects    = !projects.isEmpty();
        final List<String> subjects       = getSubjects(joinPoint);
        final boolean      hasSubjects    = !subjects.isEmpty();
        final List<String> experiments    = getExperiments(joinPoint);
        final boolean      hasExperiments = !experiments.isEmpty();

        if (projects.size() > 1 && (hasSubjects || hasExperiments)) {
            throw new RuntimeException("Found request with subjects and/or experiments specified along with multiple projects. Multiple projects can only be evaluated without dependent data objects.");
        }

        if (subjects.size() > 1 && hasExperiments) {
            throw new RuntimeException("Found request with experiments specified along with multiple subject. Multiple subjects can only be evaluated without dependent data objects.");
        }

        final String              project = projects.isEmpty() ? null : projects.get(0);
        final DataAccessPredicate predicate;
        final List<String>        targets;
        try {
            if (hasProjects && !hasSubjects && !hasExperiments) {
                targets = projects;
                predicate = new ProjectAccessPredicate(_service, _template, user, accessLevel);
            } else if (hasSubjects && !hasExperiments) {
                targets = subjects;
                predicate = new SubjectAccessPredicate(_service, _template, user, accessLevel, project);
            } else {
                targets = experiments;
                predicate = new ExperimentAccessPredicate(_service, _template, user, accessLevel, project, subjects.isEmpty() ? null : subjects.get(0));
            }
        } catch (NotFoundException e) {
            throw new InsufficientPrivilegesException("Insufficient privileges to access the requested items");
        }
        final boolean checked = Iterables.all(targets, predicate);
        if (predicate.hasErrorState()) {
            if (!predicate.getFailed().isEmpty()) {
                log.error("Encountered {} errors trying to process projects ({}), subjects ({}), and experiments ({})", predicate.getFailed().size(), projects, subjects, experiments);
                for (final Map.Entry<String, Throwable> entry : predicate.getFailed().entrySet()) {
                    log.error(entry.getKey(), entry.getValue());
                }
            }
            if (!predicate.getMissing().isEmpty()) {
                throw new InsufficientPrivilegesException("Access denied for the following items: " + StringUtils.join(predicate.getMissing()));
            }
        }
        return checked;
    }

    @Override
    protected boolean considerGuests() {
        return true;
    }

    private final PermissionsServiceI        _service;
    private final NamedParameterJdbcTemplate _template;
}
