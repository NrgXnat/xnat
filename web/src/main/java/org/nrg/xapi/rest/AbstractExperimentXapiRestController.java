package org.nrg.xapi.rest;

import static org.nrg.framework.utilities.Reflection.getParameterizedTypeForClass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractExperimentXapiRestController<E extends XnatExperimentdata> extends AbstractXapiRestController {
    protected AbstractExperimentXapiRestController(final NamedParameterJdbcTemplate template, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) throws NoSuchFieldException, IllegalAccessException {
        super(userManagementService, roleHolder);
        _template = template;
        _parameterizedType = getParameterizedTypeForClass(getClass());
        _xsiType = (String) _parameterizedType.getField("SCHEMA_ELEMENT_NAME").get(null);
        final String tableName = StringUtils.replaceOnce(_xsiType, ":", "_").toLowerCase();
        _queryGetExperimentIdFromProjectAndIdOrLabel = String.format(QUERY_GET_EXPERIMENT_ID_FROM_PROJECT_AND_ID_OR_LABEL_TEMPLATE, tableName);
        _queryVerifyExperimentIdExists = String.format(QUERY_EXPERIMENT_BY_ID_EXISTS_TEMPLATE, tableName);
        log.debug("Creating API controller for XSI type {}, with table name {}", _xsiType, tableName);
    }

    /**
     * Gets the ID of an entity in the specified project with the specified ID or label. This includes experiments that
     * are shared into projects other than the original source project. For example, if you have an experiment with ID
     * <b>XNAT_E12345</b> that was originally created in project <b>A</b> with the label <b>A_EXPT_01</b> then shared
     * into project <b>B</b> with the label <b>B_SHARED_EXPT</b>, you can search by the following project IDs and
     * experiment IDs or labels:
     *
     * <ul>
     *     <li><b>A</b> and <b>XNAT_E12345</b></li>
     *     <li><b>B</b> and <b>XNAT_E12345</b></li>
     *     <li><b>A</b> and <b>A_EXPT_01</b></li>
     *     <li><b>B</b> and <b>B_SHARED_EXPT</b></li>
     * </ul>
     *
     * Searching by these parameters would result in returning the experiment ID of <b>XNAT_E12345</b>. The following
     * searches would <i>not</i> be valid, because the specified labels don't exist in those projects.
     *
     * <ul>
     *     <li><b>A</b> and <b>B_SHARED_EXPT</b></li>
     *     <li><b>B</b> and <b>A_EXPT_01</b></li>
     * </ul>
     *
     * Searching by these parameters would result in the method throwing {@link NotFoundException}.
     *
     * @param projectId The ID of the project to search for the ID or label
     * @param idOrLabel The ID or label to search for
     *
     * @return The canonical ID of the matching experiment
     *
     * @throws NotFoundException When no experiment exists in the specified project with the specified ID or label
     */
    protected String getEntityIdFromIdOrLabel(final String projectId, final String idOrLabel) throws NotFoundException {
        try {
            return _template.queryForObject(_queryGetExperimentIdFromProjectAndIdOrLabel, new MapSqlParameterSource(PARAM_PROJECT_ID, projectId).addValue(PARAM_ID_OR_LABEL, idOrLabel), String.class);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(_xsiType, projectId + ":" + idOrLabel);
        }
    }

    /**
     * Checks whether an experiment exists with the specified ID.
     *
     * @param id The ID to verify
     *
     * @throws NotFoundException When no experiment exists with the specified ID
     */
    protected void validateId(final String id) throws NotFoundException {
        if (!_template.queryForObject(_queryVerifyExperimentIdExists, new MapSqlParameterSource(PARAM_EXPERIMENT_ID, id), Boolean.class)) {
            throw new NotFoundException(_xsiType, id);
        }
    }

    /**
     * Checks whether an experiment exists in the specified project with the specified ID or label. Note that this just
     * calls {@link #getEntityIdFromIdOrLabel(String, String)} without returning the value.
     *
     * @param projectId The ID of the project to search for the ID or label
     * @param idOrLabel The ID or label to search for
     *
     * @throws NotFoundException When no experiment exists in the specified project with the specified ID or label
     */
    protected void validateId(final String projectId, final String idOrLabel) throws NotFoundException {
        getEntityIdFromIdOrLabel(projectId, idOrLabel);
    }

    /**
     * Checks whether the submitted entity has a value set for ID. If not, the submitted ID is set on the entity. If so,
     * the IDs are compared and {@link DataFormatException} is thrown if the IDs don't match. Lastly, the ID is
     * validated by calling {@link #validateId(String)}.
     *
     * @param id     The ID to verify
     * @param entity The entity to verify
     *
     * @throws DataFormatException When the submitted ID and entity ID don't match
     * @throws NotFoundException   When the ID doesn't match an existing experiment
     */
    @SuppressWarnings("unused")
    protected void validateEntityId(final String id, final E entity) throws DataFormatException, NotFoundException {
        if (StringUtils.isBlank(entity.getId())) {
            entity.setId(id);
        } else if (!StringUtils.equals(id, entity.getId())) {
            throw new DataFormatException("The submitted " + entity.getXSIType() + " didn't match the specified ID.");
        }
        validateId(id);
    }

    /**
     * Checks whether the submitted entity has values set for project and ID. If not, the submitted project and ID are
     * set on the entity (the ID is resolved by calling {@link #getEntityIdFromIdOrLabel(String, String)}). If so,
     * the project and ID/label are compared and {@link DataFormatException} is thrown if the IDs don't match. Lastly,
     * the project and ID/label are validated by calling {@link #validateId(String, String)}.
     *
     * @param projectId The ID of the project to search for the ID or label
     * @param idOrLabel The ID or label to search for
     * @param entity    The entity to verify
     *
     * @throws DataFormatException When the submitted ID and entity ID don't match
     * @throws NotFoundException   When the ID doesn't match an existing experiment
     */
    @SuppressWarnings("unused")
    protected void validateEntityId(final String projectId, final String idOrLabel, final E entity) throws DataFormatException, NotFoundException {
        if (StringUtils.isBlank(entity.getProject())) {
            entity.setProject(projectId);
        }
        if (StringUtils.isBlank(entity.getId())) {
            entity.setId(getEntityIdFromIdOrLabel(projectId, idOrLabel));
        }
        if (StringUtils.equals(projectId, entity.getProject()) && StringUtils.equalsAny(idOrLabel, entity.getId(), entity.getLabel())) {
            log.debug("Matched source experiment project {} and ID {} with entity", projectId, idOrLabel);
        } else if (entity.getSharing_share().stream().anyMatch(share -> StringUtils.equals(projectId, share.getProject()) && StringUtils.equals(idOrLabel, share.getLabel()))) {
            log.debug("Matched shared experiment project {} and ID {} with entity", projectId, idOrLabel);
        } else {
            throw new DataFormatException("The submitted " + entity.getXSIType() + " didn't match the specified project and ID or label.");
        }
        validateId(projectId, idOrLabel);
    }

    private static final String PARAM_PROJECT_ID                                              = "projectId";
    private static final String PARAM_EXPERIMENT_ID                                           = "experimentId";
    private static final String PARAM_ID_OR_LABEL                                             = "idOrLabel";
    private static final String QUERY_GET_EXPERIMENT_ID_FROM_PROJECT_AND_ID_OR_LABEL_TEMPLATE = "SELECT " +
                                                                                                "    d.id " +
                                                                                                "FROM " +
                                                                                                "    %s d " +
                                                                                                "        LEFT JOIN xnat_experimentdata x ON d.id = x.id " +
                                                                                                "        LEFT JOIN xnat_experimentdata_share s ON x.id = s.sharing_share_xnat_experimentda_id " +
                                                                                                "WHERE " +
                                                                                                "    :" + PARAM_ID_OR_LABEL + " IN (d.id, x.label) AND x.project = :" + PARAM_PROJECT_ID + " OR " +
                                                                                                "    :" + PARAM_ID_OR_LABEL + " IN (d.id, s.label) AND s.project = :" + PARAM_PROJECT_ID + "";
    private static final String QUERY_EXPERIMENT_BY_ID_EXISTS_TEMPLATE                        = "SELECT EXISTS(SELECT id FROM %s WHERE id = :" + PARAM_EXPERIMENT_ID + ")";

    private final NamedParameterJdbcTemplate          _template;
    private final Class<? extends XnatExperimentdata> _parameterizedType;
    private final String                              _xsiType;
    private final String                              _queryGetExperimentIdFromProjectAndIdOrLabel;
    private final String                              _queryVerifyExperimentIdExists;
}
