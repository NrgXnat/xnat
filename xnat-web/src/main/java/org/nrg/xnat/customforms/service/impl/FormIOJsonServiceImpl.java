/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.forms.models.pojo.FormFieldPojo;
import org.nrg.xdat.forms.services.FormIOJsonService;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.customforms.helpers.CustomFormHelper;
import org.nrg.xnat.customforms.pojo.FormIOJsonToXnatCustomField;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.customforms.utils.FormsIOJsonUtils;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FormIOJsonServiceImpl implements FormIOJsonService {

    private final CustomVariableAppliesToService customVariableAppliesToService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String PARAM_PROJECT_UUIDS = "projectExistingForm";
    private final String PARAM_PROJECT_ID = "projectId";
    private final String FORM_JSON_COLUMN_NAME = "formiojson_definition";
    private final String FORM_UUID_COLUMN_NAME = "form_uuid";


    @Autowired
    public FormIOJsonServiceImpl(final CustomVariableAppliesToService customVariableAppliesToService, final JdbcTemplate jdbcTemplate, final ObjectMapper objectMapper) {
        this.customVariableAppliesToService = customVariableAppliesToService;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.objectMapper = objectMapper;
    }

    /**
     * Returns list of Fields for dataType (xsiType), project, protocol, visit, visitSubType
     * The list is used to build the Search listing.
     * The list of fields include:
     *      all form fields that have been added to the project
     *      any form fields that exist due to sharing of data without adding forms from source project
     *
     * @param dataType     (@Nonnull) - xsiType
     * @param project      - project Id
     * @param protocol     - optional protocol name
     * @param visit        - optional visit
     * @param visitSubType - optional subtype of the visit
     * @return - List of FormFieldPojo
     */
    public List<FormFieldPojo> getFormsForObject(@Nonnull String dataType, String project, String protocol, String visit, String visitSubType) {
        List<FormFieldPojo> forms = new ArrayList();
        try {
            forms = getForm(Scope.Project, project, dataType, protocol, visit, visitSubType);
            return getAllFormsFromSharedSourceProjects(dataType, project, protocol, visit, visitSubType, forms);
        } catch (Exception e) {
            log.debug("Could not fetch form fields", e);
        }
        return forms;
    }

    /**
     * Returns list of Fields for dataType (xsiType), project, protocol, visit, visitSubType
     * removes duplicates of existing forms
     * The list is used to build the Search listing
     *
     * @param dataType     (@Nonnull) - xsiType
     * @param project      - project Id
     * @param protocol     - optional protocol name
     * @param visit        - optional visit
     * @param visitSubType - optional subtype of the visit
     * @param existingProjectForms - forms that have already been added to the project
     * @return - List of FormFieldPojo
     */
    private List<FormFieldPojo> getAllFormsFromSharedSourceProjects(@Nonnull String dataType, String project, String protocol, String visit, String visitSubType, final List<FormFieldPojo> existingProjectForms) {
        List<FormFieldPojo> forms = new ArrayList();
        List<String> existingFormUUIDs = existingProjectForms.stream().map(f -> f.getFormUUID().toString()).collect(Collectors.toList());
        List<Map<String,Object>> formRows = null;
        MapSqlParameterSource parameters = new MapSqlParameterSource(PARAM_PROJECT_ID, project);
            if (existingFormUUIDs.isEmpty()) {
                String query = getQueryToSelectForms(project, dataType, protocol,visit,visitSubType, false);
                formRows = jdbcTemplate.queryForList(query, parameters);
            }else {
                String query = getQueryToSelectForms(project, dataType, protocol,visit,visitSubType, true);
                parameters.addValue(PARAM_PROJECT_UUIDS, existingFormUUIDs);
                formRows = jdbcTemplate.queryForList(query, parameters);
            }
            if (null != formRows) {
                List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields = formRows.stream()
                        .map(mapEntry -> {
                            final UUID formUUID = (UUID)mapEntry.get(FORM_UUID_COLUMN_NAME);
                            final String formJsonAsStr = ((PGobject) mapEntry.get(FORM_JSON_COLUMN_NAME)).getValue();
                            JsonNode components = null;
                            try {
                                JsonNode formJson = objectMapper.readTree(formJsonAsStr);
                                components = formJson.get("components");
                            }catch (JsonProcessingException jpe) {log.debug("Encountered invalid json ", jpe);}
                            return CustomFormHelper.GetFormObj(formUUID, components);
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                forms.addAll(formIOJsonToXnatCustomFields);
            }
        return forms;
    }

    @Nullable
    private String getQueryToSelectForms(String scopeId, @Nonnull String dataType, String protocol, String visit, String visitSubtype, boolean filter) {
        String subQuery = null;
        if (dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            subQuery = "select uuid(fuuid) from " +
                    " ( select jsonb_object_keys(custom_fields) fuuid FROM xnat_subjectdata s " +
                    " inner join xnat_projectparticipant p ON p.subject_id = s.id " +
                    " where p.project = :"+ PARAM_PROJECT_ID +
                    " UNION " +
                    " select jsonb_object_keys(custom_fields) fuuid FROM xnat_subjectdata s where s.project='" + scopeId +  "'" +
                    ") uuids_table ";
        }else if (dataType.equals(XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
            subQuery = " select uuid(fuuid) from " +
                        "( select jsonb_object_keys(custom_fields) fuuid FROM xnat_experimentdata e " +
                        " inner join xnat_experimentdata_share eshare ON eshare.sharing_share_xnat_experimentda_id = e.id " +
                        " where eshare.project = :"+ PARAM_PROJECT_ID  +
                        " UNION " +
                        " select jsonb_object_keys(custom_fields) fuuid FROM xnat_experimentdata e where e.project=:"+ PARAM_PROJECT_ID +
		                ") uuids_table ";
        }
        if (subQuery != null) {
            if (filter) {
                subQuery = String.format("%s where form_uuid not in (:%s)", subQuery, PARAM_PROJECT_UUIDS);
            }
            return String.format("select %s, %s from xhbm_custom_variable_form where form_uuid in ( %s )",FORM_UUID_COLUMN_NAME,FORM_JSON_COLUMN_NAME, subQuery);
        }
        return null;
    }

    private List<FormFieldPojo> getForm(@Nonnull Scope scope, String scopeId, @Nonnull String dataType, String protocol, String visit, String visitSubtype) throws IOException, NotFoundException {
        List<FormFieldPojo> formFields = new ArrayList();
        List<FormFieldPojo> fields = getFields(scope, scopeId, dataType, protocol, visit, visitSubtype);
        formFields.addAll(fields);
        return formFields;
    }

    private List<FormFieldPojo> getFields(@Nonnull Scope scope, String scopeId, @Nonnull String dataType, String protocol, String visit, String visitSubtype) {

        List<FormFieldPojo> formFields = new ArrayList();
        List<String> statuses = new ArrayList<String>();
        statuses.add(CustomFormsConstants.ENABLED_STATUS_STRING);
        statuses.add(CustomFormsConstants.OPTED_OUT_STATUS_STRING);

        List<CustomVariableAppliesTo> projectSpecificSelections = customVariableAppliesToService.filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(scope, scopeId,
                dataType, protocol, visit,
                visitSubtype, statuses);
        List<CustomVariableAppliesTo> siteWideSelections = customVariableAppliesToService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Site, null,
                dataType, protocol, visit,
                visitSubtype, CustomFormsConstants.ENABLED_STATUS_STRING);
        //A project can opt out of the site wide form.
        //Go through all the site wide forms for a datatype/protoocol/visit/subtype
        //Include it only if the project has not opted out of the form
        List<CustomVariableFormAppliesTo> optedInSiteForms = FormsIOJsonUtils.removeSiteFormsOptedOutByProject(siteWideSelections, projectSpecificSelections);

        if (optedInSiteForms != null && optedInSiteForms.size() > 0) {
            for (CustomVariableFormAppliesTo siteWideSelection : optedInSiteForms) {
                formFields.addAll(getFormObj(siteWideSelection.getCustomVariableAppliesTo()));
            }
        }

        if (projectSpecificSelections != null && projectSpecificSelections.size() > 0) {
            for (CustomVariableAppliesTo projectSpecificSelection : projectSpecificSelections) {
                formFields.addAll(getFormObj(projectSpecificSelection));
            }
        }
        return formFields;

    }

    /**
     * Parses a configuration and converts the FormsIO Fields to XNAT Custom Fields
     *
     * @param c - CustomVariableAppliesTo from which the formsIO fields are to be extracted
     * @return List of FormIOJsonToXnatCustomField
     */
    private List<FormIOJsonToXnatCustomField> getFormObj(CustomVariableAppliesTo c) {
        // Convert the configuration into a new FormJson Pojo
        return c.getCustomVariableFormAppliesTos().stream()
                .filter(customVariableFormAppliesTo -> CustomFormsConstants.ENABLED_STATUS_STRING.equals(customVariableFormAppliesTo.getStatus()))
                .map(CustomVariableFormAppliesTo::getCustomVariableForm)
                .map(CustomFormHelper::GetFormObj)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


}
