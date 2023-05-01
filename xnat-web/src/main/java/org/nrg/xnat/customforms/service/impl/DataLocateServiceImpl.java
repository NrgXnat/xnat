package org.nrg.xnat.customforms.service.impl;

import org.nrg.framework.constants.Scope;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.customforms.helpers.CustomFormHelper;
import org.nrg.xnat.customforms.pojo.FormIOJsonToXnatCustomField;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.service.CustomVariableFormAppliesToService;
import org.nrg.xnat.customforms.service.DataLocateService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataLocateServiceImpl implements DataLocateService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CustomVariableFormAppliesToService selectionService;


    @Autowired
    public DataLocateServiceImpl(final CustomVariableFormAppliesToService selectionService, final JdbcTemplate jdbcTemplate) {
        this.selectionService = selectionService;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public boolean hasDataBeenAcquired(final CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        boolean dataHasBeenAcquired = false;
        if (customVariableFormAppliesTo == null) {
            return false;
        }
        CustomVariableAppliesTo customVariableAppliesTo = customVariableFormAppliesTo.getCustomVariableAppliesTo();
        if (customVariableAppliesTo != null) {
            Integer rowCount = getFieldCountsSavedInDatabase(customVariableFormAppliesTo);
            if (null != rowCount && rowCount > 0) {
                dataHasBeenAcquired = true;
            }
        }
        return dataHasBeenAcquired;
    }

    private Integer getFieldCountsSavedInDatabase(final CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        CustomVariableAppliesTo customVariableAppliesTo = customVariableFormAppliesTo.getCustomVariableAppliesTo();
        RowIdentifier rowIdentifier = customVariableFormAppliesTo.getRowIdentifier();
        CustomVariableForm form = customVariableFormAppliesTo.getCustomVariableForm();
        String dataType = customVariableAppliesTo.getDataType();
        List<CustomVariableFormAppliesTo> formsBeingUsedByOthers = selectionService.findByFormIdByExclusion(form.getId(), rowIdentifier);
        List<String> projectIds = new ArrayList<String>();
        if (customVariableAppliesTo.getScope().equals(Scope.Project)) {
            projectIds.add(customVariableAppliesTo.getEntityId());
        }
        boolean isSiteWide = true;
        if (formsBeingUsedByOthers != null && formsBeingUsedByOthers.size() > 0) {
            for (CustomVariableFormAppliesTo c : formsBeingUsedByOthers) {
                CustomVariableAppliesTo other = c.getCustomVariableAppliesTo();
                if (other.getScope().equals(Scope.Project)) {
                    String projectId = other.getEntityId();
                    if (projectId != null && !projectIds.contains(projectId)) {
                        projectIds.add(projectId);
                        isSiteWide = false;
                    }
                }
            }
        }

        final Set<String> fieldNames = CustomFormHelper.getFormObjects(form, false)
                .stream()
                .map(FormIOJsonToXnatCustomField::getJsonRootName)
                .collect(Collectors.toSet());
        //No fields on the form
        if (fieldNames.isEmpty()) {
            return 0;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("fieldNames", fieldNames);
        if (!isSiteWide) {
            parameters.addValue("projectIds", projectIds);
        }
        //Build Query
        final String query = buildQuery(dataType, form.getFormUuid(), isSiteWide);

        return jdbcTemplate.queryForObject(query, parameters, Integer.class);
    }

    private String buildQuery(final String dataType, final UUID formUUID, final boolean isSiteWide) {
        final String fieldsTable = getTableName(dataType);
        final String formUuidStr = formUUID.toString();

        String queryStr = "select count(*) from (SELECT DISTINCT ON (s.id) id, d.key, d.value FROM ";
        queryStr += fieldsTable + "  s JOIN jsonb_each_text(s.custom_fields -> '" + formUuidStr + "' ) d ON true ";

        String whereClause = " where d.key in  (:fieldNames) ";
        if (!isSiteWide) {
            whereClause += " and s.project in (:projectIds)";
        }
        queryStr += whereClause + ") as fields";
        return queryStr;
    }

    private String getTableName(final String dataType) {
        return getXdatName(dataType);
    }

    private String getXdatName(final String dataType) {
        if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME) || dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            return dataType.replace(CustomFormsConstants.DELIMITER, "_");
        } else { //Some experiment
            return XnatExperimentdata.SCHEMA_ELEMENT_NAME.replace(CustomFormsConstants.DELIMITER, "_");
        }
    }

    private String getForeignKey(final String dataType) {
        if (dataType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME) || dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            return "fields_field_" + getXdatName(dataType) + "_id";
        } else { //Some experiment
            return "fields_field_xnat_experimentdat_id";
        }
    }
}
