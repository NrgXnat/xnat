package org.nrg.xft.utils.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import java.util.*;

import static org.nrg.framework.constants.Scope.Project;
import static org.nrg.framework.constants.Scope.Subject;
import static org.nrg.xdat.security.helpers.AccessLevel.*;

@SuppressWarnings("deprecation")
@Getter
@Accessors(prefix = "_")
@Slf4j
public abstract class DataAccessPredicate implements Predicate<String> {
    protected DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final Scope scope) {
        _service = service;
        _template = template;
        _user = user;
        _accessLevel = validateAccessLevel(accessLevel);
        _scope = scope;

        _project = null;
        _subject = null;

        final Pair<String, Map<String, String>> properties = getInitialQueryAndParameters();
        _query = properties.getLeft();
        _parameters = properties.getRight();
    }

    protected DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final Scope scope, final String project) throws NotFoundException {
        this(service, template, user, accessLevel, scope, project, null);
    }

    protected DataAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final Scope scope, final String project, final String subject) throws NotFoundException {
        _service = service;
        _template = template;
        _user = user;
        _accessLevel = validateAccessLevel(accessLevel);
        _scope = scope;

        _project = StringUtils.defaultIfBlank(project, null);
        _subject = StringUtils.defaultIfBlank(subject, null);

        final boolean hasProject = StringUtils.isNotBlank(_project);
        final boolean hasSubject = StringUtils.isNotBlank(_subject);
        if (hasProject && hasSubject) {
            _query = QUERY_PROJECT_SUBJECT_EXPERIMENT;
            _parameters = validateProjectSubject(_project, _subject);
        } else if (hasProject) {
            _query = _scope == Subject ? QUERY_PROJECT_SUBJECT : QUERY_PROJECT_EXPERIMENT;
            _parameters = validateProject(_project);
        } else if (hasSubject) {
            _query = QUERY_SUBJECT_EXPERIMENT;
            _parameters = validateSubject(_subject);
        } else {
            final Pair<String, Map<String, String>> properties = getInitialQueryAndParameters();
            _query = properties.getLeft();
            _parameters = properties.getRight();
        }
    }

    /**
     * Returns a formatted failure message explaining the context of an exception.
     *
     * @param entityId The ID of the entity being evaluated when the failure occurred.
     *
     * @return A formatted message that can be logged for forensics purposes.
     */
    protected abstract String getFailureMessage(final String entityId);

    @Override
    public boolean apply(final String entityId) {
        if (preapprove(entityId)) {
            return true;
        }
        final boolean accessible = applyImpl(entityId);
        if (!accessible && !getMissing().contains(entityId)) {
            getDenied().add(entityId);
        }
        return accessible;
    }

    /**
     * Indicates whether there were any denied, missing, or failed ID checks.
     *
     * @return Returns <b>true</b> if there were any denied, missing, or failed ID checks, <b>false</b> otherwise.
     */
    public boolean hasErrorState() {
        return !getDenied().isEmpty() || !getMissing().isEmpty() || !getFailed().isEmpty();
    }

    /**
     * Provides a way to approve the entity ID before other checks. The default implementation of this method always returns <b>false</b>.
     *
     * @param entityId The entity to test for preapproval.
     *
     * @return Returns <b>true</b> if the entity is preapproved, <b>false</b> otherwise.
     */
    protected boolean preapprove(final String entityId) {
        return false;
    }

    private boolean applyImpl(final String objectId) {
        final UserI user = getUser();
        try {
            final Map<String, Object> properties = identify(objectId);
            return evaluate(user, properties, objectId);
        } catch (NotFoundException e) {
            getMissing().add(objectId);
        } catch (Exception e) {
            getFailed().put(getFailureMessage(objectId), e);
        }
        return false;
    }

    /**
     * Executes the query identified for the predicate context with the preset parameter set and the ID of the particular instance ID to be evaluated.
     * The contents of the map returned vary based on context, but may include the <b>secured_property</b> key when the {@link #getProject() project parameter}
     * is specified for the predicate. That indicates the appropriate property to test in the current project context.
     *
     * @param entityId The entity to be identified.
     *
     * @return A map of properties for the entity if the entity was located.
     *
     * @throws NotFoundException When no entity can be identified based on the specified ID and predicate context.
     */
    @Nonnull
    protected Map<String, Object> identify(final String entityId) throws NotFoundException {
        log.debug("Testing for existence of object \"{}\" with parameters \"{}\" and query: {}", entityId, _parameters, _query);
        final Map<String, String> parameters = new HashMap<>(_parameters);
        parameters.put(_scope.code(), entityId);
        try {
            return getTemplate().queryForMap(_query, parameters);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException(parameters.toString());
        }
    }

    /**
     * Tests whether the user can perform the {@link #getAccessLevel() specified action} on the identified entity.
     *
     * @param user       The user to be evaluated.
     * @param properties Entity properties from the {@link #identify(String)} method.
     * @param entityId   The ID of the entity the user is trying to access.
     *
     * @return Returns <b>true</b> if the user can access the specified entity at the requested level, <b>false</b> otherwise.
     *
     * @throws Exception When an error occurs.
     */
    protected boolean evaluate(final UserI user, final Map<String, Object> properties, final String entityId) throws Exception {
        final String projectId = getProject();
        if (StringUtils.isNotBlank(projectId)) {
            switch (getAccessLevel()) {
                case Delete:
                    return getService().canDelete(user, getProject(), entityId);

                case Edit:
                    return getService().canEdit(user, getProject(), entityId);

                default:
                    return getService().canRead(user, getProject(), entityId);
            }
        }
        final String primaryId;
        switch (getScope()) {
            case Project:
                primaryId = entityId;
                break;
            case Subject:
                primaryId = (String) properties.get("subject_id");
                break;
            default:
                primaryId = (String) properties.get("experiment_id");
        }
        if (properties.containsKey("secured_property")) {
            final String securedProperty = (String) properties.get("secured_property");
            switch (getAccessLevel()) {
                case Delete:
                    return getService().canDelete(user, securedProperty, (Object) primaryId);

                case Edit:
                    return getService().canEdit(user, securedProperty, (Object) primaryId);

                default:
                    return getService().canRead(user, securedProperty, (Object) primaryId);
            }
        } else {
            switch (getAccessLevel()) {
                case Delete:
                    return getService().canDelete(user, primaryId);

                case Edit:
                    return getService().canEdit(user, primaryId);

                default:
                    return getService().canRead(user, primaryId);
            }
        }
    }

    private Pair<String, Map<String, String>> getInitialQueryAndParameters() {
        final String              query;
        final Map<String, String> parameters;
        switch (_scope) {
            case Project:
                query = QUERY_PROJECT;
                parameters = ImmutableMap.of(ACCESS_CODE, _accessLevel.code());
                break;
            case Subject:
                query = QUERY_SUBJECT;
                parameters = Collections.emptyMap();
                break;
            default:
                query = QUERY_EXPERIMENT;
                parameters = Collections.emptyMap();
        }
        return ImmutablePair.of(query, parameters);
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

    private Map<String, String> validateProjectSubject(final String project, final String subject) throws NotFoundException {
        final Map<String, String> parameters = ImmutableMap.of(PROJECT_CODE, project, SUBJECT_CODE, subject);
        if (!getTemplate().queryForObject(QUERY_PROJECT_SUBJECT_EXISTS, parameters, Boolean.class)) {
            throw new NotFoundException("No such project \"" + project + "\" and subject \"" + subject + "\" exist");
        }
        return parameters;
    }

    private Map<String, String> validateProject(final String project) throws NotFoundException {
        final Map<String, String> parameters = ImmutableMap.of(PROJECT_CODE, project, ACCESS_CODE, _accessLevel.code());
        if (!getTemplate().queryForObject(QUERY_PROJECT_EXISTS, parameters, Boolean.class)) {
            throw new NotFoundException("No project with ID \"" + project + "\" exists");
        }
        return parameters;
    }

    private Map<String, String> validateSubject(final String subject) throws NotFoundException {
        final Map<String, String> parameters = ImmutableMap.of(SUBJECT_CODE, subject);
        if (!getTemplate().queryForObject(QUERY_SUBJECT_EXISTS, parameters, Boolean.class)) {
            throw new NotFoundException("No subject with ID \"" + subject + "\" exists");
        }
        return parameters;
    }

    private static String getExistsQuery(final String query) {
        return "SELECT EXISTS(" + query + ")";
    }

    private static final String QUERY_PROJECT                    = "SELECT " +
                                                                   "    id AS project_id, " +
                                                                   "    CASE WHEN :access = 'read' THEN 'xnat:subjectData/property' ELSE 'xnat:projectData/ID' END AS secured_property " +
                                                                   "FROM " +
                                                                   "    xnat_projectdata " +
                                                                   "WHERE id = :prj";
    private static final String QUERY_SUBJECT                    = "SELECT id AS subject_id, 'xnat:subjectData/project' AS secured_property FROM xnat_subjectdata WHERE id = :subj";
    private static final String QUERY_EXPERIMENT                 = "SELECT " +
                                                                   "    e.id AS experiment_id, " +
                                                                   "    e.label AS experiment_label, " +
                                                                   "    m.element_name AS data_type, " +
                                                                   "    m.element_name || '/project' AS secured_property " +
                                                                   "FROM " +
                                                                   "    xnat_experimentdata e " +
                                                                   "    LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                   "WHERE " +
                                                                   "    e.id = :experiment";
    private static final String QUERY_PROJECT_SUBJECT            = "SELECT " +
                                                                   "    p.id AS project_id, " +
                                                                   "    COALESCE(s.id, ss.id) AS subject_id, " +
                                                                   "    COALESCE(pp.label, s.label) AS subject_label, " +
                                                                   "    'xnat:subjectData' || CASE WHEN pp.project = :prj THEN '/sharing/share/project' ELSE '/project' END AS secured_property " +
                                                                   "FROM " +
                                                                   "    xnat_projectdata p " +
                                                                   "    LEFT JOIN xnat_subjectdata s ON p.id = s.project " +
                                                                   "    LEFT JOIN xnat_projectparticipant pp ON p.id = pp.project " +
                                                                   "    LEFT JOIN xnat_subjectdata ss ON pp.subject_id = ss.id " +
                                                                   "WHERE " +
                                                                   "    (pp.project = :prj AND (pp.label = :subj OR pp.subject_id = :subj)) OR " +
                                                                   "    (s.project = :prj AND (s.label = :subj OR s.id = :subj))";
    private static final String QUERY_PROJECT_EXPERIMENT         = "SELECT " +
                                                                   "    COALESCE(p.project, s.project) AS project_id, " +
                                                                   "    COALESCE(p.label, s.label) AS subject_label, " +
                                                                   "    COALESCE(sh.label, x.label) AS experiment_label, " +
                                                                   "    CASE WHEN ((x.id = :experiment OR x.label = :experiment) AND x.project = :prj) THEN e.element_name || '/project' ELSE e.element_name || '/sharing/share/project' END AS secured_property, " +
                                                                   "    a.subject_id AS subject_id, " +
                                                                   "    x.id AS experiment_id, " +
                                                                   "    e.element_name AS data_type, " +
                                                                   "    p.project AS shared_project, " +
                                                                   "    s.project AS source_project, " +
                                                                   "    p.label AS shared_subject, " +
                                                                   "    s.label AS source_subject, " +
                                                                   "    sh.label AS shared_label, " +
                                                                   "    x.label AS source_label " +
                                                                   "FROM " +
                                                                   "    xnat_experimentdata x " +
                                                                   "    LEFT JOIN xnat_subjectassessordata a ON a.id = x.id " +
                                                                   "    LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id " +
                                                                   "    LEFT JOIN xnat_experimentdata_share sh ON x.id = sh.sharing_share_xnat_experimentda_id AND sh.project = :prj " +
                                                                   "    LEFT JOIN xnat_projectparticipant p ON p.subject_id = a.subject_id AND p.project = :prj " +
                                                                   "    LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id " +
                                                                   "WHERE " +
                                                                   "    (((x.id = :experiment OR x.label = :experiment) AND x.project = :prj) OR " +
                                                                   "     ((sh.sharing_share_xnat_experimentda_id = :experiment OR sh.label = :experiment) AND sh.project = :prj))";
    private static final String QUERY_SUBJECT_EXPERIMENT         = "SELECT " +
                                                                   "    subject.project AS project_id, " +
                                                                   "    assessor.subject_id AS subject_id, " +
                                                                   "    subject.label AS subject_label, " +
                                                                   "    expt.id AS experiment_id, " +
                                                                   "    expt.label AS experiment_label, " +
                                                                   "    xme.element_name AS data_type, " +
                                                                   "    xme.element_name || '/project' AS secured_property " +
                                                                   "FROM " +
                                                                   "    xnat_experimentdata expt " +
                                                                   "    LEFT JOIN xnat_subjectassessordata assessor ON assessor.id = expt.id " +
                                                                   "    LEFT JOIN xnat_subjectdata subject ON assessor.subject_id = subject.id " +
                                                                   "    LEFT JOIN xnat_projectdata project ON subject.project = project.id " +
                                                                   "    LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id " +
                                                                   "WHERE " +
                                                                   "    subject.id = :subj AND " +
                                                                   "    (expt.id = :experiment OR expt.label = :experiment)";
    private static final String QUERY_PROJECT_SUBJECT_EXPERIMENT = QUERY_PROJECT_EXPERIMENT + " AND " +
                                                                   "    (((s.id = :subj OR s.label = :subj) AND s.project = :prj) OR " +
                                                                   "     ((p.subject_id = :subj OR p.label = :subj) AND p.project = :prj))";

    private static final String QUERY_PROJECT_EXISTS                    = getExistsQuery(QUERY_PROJECT);
    private static final String QUERY_SUBJECT_EXISTS                    = getExistsQuery(QUERY_SUBJECT);
    private static final String QUERY_EXPERIMENT_EXISTS                 = getExistsQuery(QUERY_EXPERIMENT);
    private static final String QUERY_PROJECT_SUBJECT_EXISTS            = getExistsQuery(QUERY_PROJECT_SUBJECT);
    private static final String QUERY_PROJECT_EXPERIMENT_EXISTS         = getExistsQuery(QUERY_PROJECT_EXPERIMENT);
    private static final String QUERY_PROJECT_SUBJECT_EXPERIMENT_EXISTS = getExistsQuery(QUERY_PROJECT_SUBJECT_EXPERIMENT);

    private static final String SUBJECT_CODE = Subject.code();
    private static final String PROJECT_CODE = Project.code();
    private static final String ACCESS_CODE  = "access";

    private final List<String>           _denied  = new ArrayList<>();
    private final List<String>           _missing = new ArrayList<>();
    private final Map<String, Throwable> _failed  = new HashMap<>();

    private final PermissionsServiceI        _service;
    private final NamedParameterJdbcTemplate _template;
    private final UserI                      _user;
    private final AccessLevel                _accessLevel;
    private final Scope                      _scope;
    private final String                     _project;
    private final String                     _subject;
    private final Map<String, String>        _parameters;
    private final String                     _query;
}
