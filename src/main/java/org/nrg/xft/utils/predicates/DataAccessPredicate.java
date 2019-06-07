package org.nrg.xft.utils.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.*;

@SuppressWarnings("deprecation")
@Getter
@Accessors(prefix = "_")
@Slf4j
public abstract class DataAccessPredicate implements Predicate<String> {
    public DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel) {
        _service = service;
        _template = template;
        _user = user;
        _accessLevel = validateAccessLevel(accessLevel);
        _parameters = Collections.emptyMap();
        _project = null;
        _query = QUERY_EXPERIMENT;
    }

    public DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project) throws NotFoundException {
        this(service, template, user, accessLevel, project, null);
    }

    public DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project, final String subject) throws NotFoundException {
        _service = service;
        _template = template;
        _user = user;
        _accessLevel = validateAccessLevel(accessLevel);

        _project = StringUtils.defaultIfBlank(project, null);
        final Pair<Map<String, String>, String> validated = validate(_project, StringUtils.defaultIfBlank(subject, null));
        _parameters = validated.getLeft();
        _query = validated.getRight();
    }

    protected abstract boolean applyImpl(final String objectId);

    @Override
    public boolean apply(final String objectId) {
        final boolean accessible = applyImpl(objectId);
        if (!accessible && !getMissing().contains(objectId)) {
            getDenied().add(objectId);
        }
        return accessible;
    }

    public boolean hasErrorState() {
        return !getDenied().isEmpty() || !getMissing().isEmpty() || !getFailed().isEmpty();
    }

    protected Map<String, String> verifyProjectSubjectExists(final String project, final String subject) throws NotFoundException {
        final Map<String, String> parameters = ImmutableMap.of("project", project, "subject", subject);
        if (!getTemplate().queryForObject(QUERY_PROJECT_SUBJECT_EXISTS, parameters, Boolean.class)) {
            throw new NotFoundException("No such project \"" + project + "\" and subject \"" + subject + "\" exist");
        }
        return parameters;
    }

    protected Map<String, String> verifyProjectExists(final String project) throws NotFoundException {
        final Map<String, String> parameters = ImmutableMap.of("project", project);
        if (!getTemplate().queryForObject(QUERY_PROJECT_EXISTS, parameters, Boolean.class)) {
            throw new NotFoundException("No project with ID \"" + project + "\" exists");
        }
        return parameters;
    }

    protected Map<String, String> verifySubjectExists(final String subject) throws NotFoundException {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("subject", subject);

        final String  query;
        final boolean hasProject = StringUtils.isNotBlank(_project);
        if (hasProject) {
            builder.put("project", _project);
            query = QUERY_PROJECT_SUBJECT_EXISTS;
        } else {
            query = QUERY_SUBJECT_EXISTS;
        }
        final ImmutableMap<String, String> parameters = builder.build();
        if (!getTemplate().queryForObject(query, parameters, Boolean.class)) {
            throw new NotFoundException(hasProject ? "No subject with ID or label \"" + subject + "\" exists in the project \"" + _project + "\"" : "No subject with ID \"" + subject + "\" exists");
        }
        return parameters;
    }

    private AccessLevel validateAccessLevel(final AccessLevel accessLevel) {
        switch (accessLevel) {
            case Collaborator:
                log.warn("You are using a plugin that uses the deprecated Collaborator access level. Please check for an updated version of the plugin, as this deprecated annotation may not be supported in future versions of XNAT.");
                return Read;

            case Member:
                log.warn("You are using a plugin that uses the deprecated Member access level. Please check for an updated version of the plugin, as this deprecated annotation may not be supported in future versions of XNAT.");
                return Edit;

            case Owner:
                log.warn("You are using a plugin that uses the deprecated Owner access level. Please check for an updated version of the plugin, as this deprecated annotation may not be supported in future versions of XNAT.");
                return Delete;

            default:
                return accessLevel;
        }
    }

    private Pair<Map<String, String>, String> validate(final String project, final String subject) throws NotFoundException {
        final boolean hasProject = StringUtils.isNotBlank(project);
        final boolean hasSubject = StringUtils.isNotBlank(subject);

        final Map<String, String> parameters;
        final String              query;
        if (hasProject && hasSubject) {
            parameters = verifyProjectSubjectExists(project, subject);
            query = QUERY_PROJECT_SUBJECT_EXPERIMENT;
        } else if (hasProject) {
            parameters = verifyProjectExists(project);
            query = QUERY_PROJECT_EXPERIMENT;
        } else if (hasSubject) {
            parameters = verifySubjectExists(subject);
            query = QUERY_SUBJECT_EXPERIMENT;
        } else {
            parameters = Collections.emptyMap();
            query = QUERY_EXPERIMENT;
        }
        return ImmutablePair.of(parameters, query);
    }

    private static String getExistsQuery(final String query) {
        return "SELECT EXISTS(" + query + ")";
    }

    private static final String QUERY_PROJECT                           = "SELECT id AS project FROM xnat_projectdata WHERE id = :project";
    private static final String QUERY_SUBJECT                           = "SELECT " +
                                                                          "    p.id AS project, " +
                                                                          "    s.id AS subject_id " +
                                                                          "FROM " +
                                                                          "    xnat_subjectdata s " +
                                                                          "    LEFT JOIN xnat_projectdata p ON s.project = p.id " +
                                                                          "WHERE " +
                                                                          "    s.id = :subject";
    private static final String QUERY_EXPERIMENT                        = "SELECT " +
                                                                          "    e.id AS experiment_id, " +
                                                                          "    e.label AS experiment_label, " +
                                                                          "    m.element_name || '/project' AS secured_property, " +
                                                                          "    e.project AS project " +
                                                                          "FROM " +
                                                                          "    xnat_experimentdata e " +
                                                                          "    LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                          "WHERE " +
                                                                          "    e.id = :experiment";
    private static final String QUERY_PROJECT_SUBJECT                   = "SELECT  " +
                                                                          "    p.id AS project,  " +
                                                                          "    s.id AS subject_id,  " +
                                                                          "    COALESCE(pp.label, s.label) AS subject_label  " +
                                                                          "FROM  " +
                                                                          "    xnat_projectdata p  " +
                                                                          "    LEFT JOIN xnat_subjectdata s ON p.id = s.project  " +
                                                                          "    LEFT JOIN xnat_projectparticipant pp ON p.id = pp.project  " +
                                                                          "WHERE  " +
                                                                          "    (pp.project = :project OR s.project = :project) AND  " +
                                                                          "    (pp.label = :subject OR pp.subject_id = :subject OR s.label = :subject OR s.id = :subject)";
    private static final String QUERY_PROJECT_EXPERIMENT                = "SELECT " +
                                                                          "    COALESCE(part.project, subject.project) AS project, " +
                                                                          "    assessor.subject_id AS subject_id, " +
                                                                          "    COALESCE(part.label, subject.label) AS subject_label, " +
                                                                          "    expt.id AS experiment_id, " +
                                                                          "    COALESCE(share.label, expt.label) AS experiment_label, " +
                                                                          "    xme.element_name AS data_type, " +
                                                                          "    xme.element_name || '/project' AS secured_property " +
                                                                          "FROM " +
                                                                          "    xnat_experimentdata expt " +
                                                                          "    LEFT JOIN xnat_subjectassessordata assessor ON assessor.id = expt.id " +
                                                                          "    LEFT JOIN xnat_subjectdata subject ON assessor.subject_id = subject.id " +
                                                                          "    LEFT JOIN xnat_experimentdata_share share ON expt.id = share.sharing_share_xnat_experimentda_id AND share.project = :project " +
                                                                          "    LEFT JOIN xnat_projectparticipant part ON part.subject_id = assessor.subject_id AND part.project = :project " +
                                                                          "    LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id " +
                                                                          "WHERE " +
                                                                          "    (((expt.id = :experiment OR expt.label = :experiment) AND expt.project = :project) OR " +
                                                                          "     ((share.sharing_share_xnat_experimentda_id = :experiment OR share.label = :experiment) AND share.project = :project))";
    private static final String QUERY_SUBJECT_EXPERIMENT                = "SELECT  " +
                                                                          "    subject.project AS project,  " +
                                                                          "    assessor.subject_id AS subject_id,  " +
                                                                          "    subject.label AS subject_label,  " +
                                                                          "    expt.id AS experiment_id,  " +
                                                                          "    expt.label AS experiment_label,  " +
                                                                          "    xme.element_name AS data_type,  " +
                                                                          "    xme.element_name || '/project' AS secured_property  " +
                                                                          "FROM  " +
                                                                          "    xnat_experimentdata expt  " +
                                                                          "    LEFT JOIN xnat_subjectassessordata assessor ON assessor.id = expt.id  " +
                                                                          "    LEFT JOIN xnat_subjectdata subject ON assessor.subject_id = subject.id  " +
                                                                          "    LEFT JOIN xnat_projectdata project ON subject.project = project.id  " +
                                                                          "    LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id  " +
                                                                          "WHERE  " +
                                                                          "    subject.id = :subject AND  " +
                                                                          "    (expt.id = :experiment OR expt.label = :experiment)";
    private static final String QUERY_PROJECT_SUBJECT_EXPERIMENT        = QUERY_PROJECT_EXPERIMENT + " AND " +
                                                                          "    (((subject.id = :subject OR subject.label = :subject) AND subject.project = :project) OR " +
                                                                          "     ((part.subject_id = :subject OR part.label = :subject) AND part.project = :project))";
    private static final String QUERY_PROJECT_EXISTS                    = getExistsQuery(QUERY_PROJECT);
    private static final String QUERY_SUBJECT_EXISTS                    = getExistsQuery(QUERY_SUBJECT);
    private static final String QUERY_EXPERIMENT_EXISTS                 = getExistsQuery(QUERY_EXPERIMENT);
    private static final String QUERY_PROJECT_SUBJECT_EXISTS            = getExistsQuery(QUERY_PROJECT_SUBJECT);
    private static final String QUERY_PROJECT_EXPERIMENT_EXISTS         = getExistsQuery(QUERY_PROJECT_EXPERIMENT);
    private static final String QUERY_PROJECT_SUBJECT_EXPERIMENT_EXISTS = getExistsQuery(QUERY_PROJECT_SUBJECT_EXPERIMENT);

    private final List<String>           _denied  = new ArrayList<>();
    private final List<String>           _missing = new ArrayList<>();
    private final Map<String, Throwable> _failed  = new HashMap<>();

    private final PermissionsServiceI        _service;
    private final NamedParameterJdbcTemplate _template;
    private final UserI                      _user;
    private final AccessLevel                _accessLevel;
    private final Map<String, String>        _parameters;
    private final String                     _project;
    private final String                     _query;
}
