/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.interfaces.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.interfaces.CustomFormFetcherI;
import org.nrg.xnat.customforms.interfaces.annotations.CustomFormFetcherAnnotation;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.customforms.utils.FormsIOJsonUtils;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Component
@CustomFormFetcherAnnotation(type = CustomFormsConstants.PROTOCOL_UNAWARE)

public class DefaultCustomFormFetcherImpl implements CustomFormFetcherI {

    private final CustomVariableAppliesToService formAppliesToService;
    private final CustomVariableFormService formService;

    @Autowired
    public DefaultCustomFormFetcherImpl(final CustomVariableAppliesToService formAppliesToService, final CustomVariableFormService formService) {
        this.formAppliesToService = formAppliesToService;
        this.formService = formService;
    }

    /**
     * Gets custom form for a given xsiType, Entity Id
     * If the id is null, custom form is built as a stackable collection of forms.
     *
     *
     * @param user - User
     * @param xsiType - The xsiType of the entity
     * @param id - id of the entity
     * @param projectIdQueryParam - Optional project id
     * @param visitId - Optional Visit id (required if you are creating a new xnat entity within a Protocol)
     * @param subType - Optional Subtype of the entity
     * @return String - Enabled Custom Form JSON; {} if no form is found
     *
     */
    public String getCustomForm(final UserI user, final String xsiType, final String id,
                                final String projectIdQueryParam, final String visitId,
                                final String subType, final boolean appendPreviousNextButtons) throws Exception {

        if ((null == id || id.equalsIgnoreCase("NULL")) && (null == projectIdQueryParam || projectIdQueryParam.equalsIgnoreCase("") || projectIdQueryParam.equalsIgnoreCase("NULL"))) {
            List<CustomVariableAppliesTo> forms = formAppliesToService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Site, null, xsiType, null, null, null, CustomFormsConstants.ENABLED_STATUS_STRING);
            List<CustomVariableFormAppliesTo> formsToConcatenate = new ArrayList<CustomVariableFormAppliesTo>();
            for (CustomVariableAppliesTo c : forms) {
                formsToConcatenate.addAll(c.getCustomVariableFormAppliesTos());
            }
            String concatenatedJson = FormsIOJsonUtils.concatenate(formsToConcatenate, null, "Custom Variables", true, appendPreviousNextButtons);
            return concatenatedJson;
        }

        final XnatProjectdata project = getProject(id, projectIdQueryParam, xsiType, user);
        final String projectId;
        final List<CustomVariableForm> formByCustomFieldUUID;
        if (XnatProjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            if (null == project) {
                throw new Exception("Did not find any project with ID " + id + (StringUtils.isNotBlank(projectIdQueryParam) ? " or " + projectIdQueryParam : ""));
            }
            formByCustomFieldUUID = getFormsInCustomFields(project);
            projectId = project.getId();
        } else if (XnatSubjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            // A Subject
            final XnatSubjectdata subject = getSubjectByIdOrLabel(id, project, user);
            if (ObjectUtils.allNull(subject, project)) {
                throw new Exception("Did not find any subject with ID " + id + (StringUtils.isNotBlank(projectIdQueryParam) ? " or project " + projectIdQueryParam : ""));
            }
            projectId = subject == null ? project.getId() : subject.getProject();
            formByCustomFieldUUID = getFormsInCustomFields(subject);
        } else {
            // An experiment
            final XnatExperimentdata experiment = getExperimentByIdOrLabel(id, project, user);
            if (ObjectUtils.allNull(experiment, project)) {
                throw new Exception("Did not find any experiment with ID " + id + (StringUtils.isNotBlank(projectIdQueryParam) ? " or project " + projectIdQueryParam : ""));
            }
            projectId = experiment == null ? project.getId() : experiment.getProject();
            formByCustomFieldUUID = getFormsInCustomFields(experiment);
        }
        return getConcatenatedFormsJSON(projectId, xsiType, formByCustomFieldUUID, appendPreviousNextButtons);
    }

    private String getConcatenatedFormsJSON(final String projectId, final String xsiType, final List<CustomVariableForm> formByCustomFieldUUID, final boolean appendPreviousNextButtons) throws JsonProcessingException {
        List<CustomVariableFormAppliesTo> customVariableFormAppliesTo = getFormsToConcatenate(projectId, xsiType);
        List<CustomVariableForm> copyFormsByUUID = new ArrayList<>();
        copyFormsByUUID.addAll(formByCustomFieldUUID);
        copyFormsByUUID.removeIf(form -> formExists(customVariableFormAppliesTo, form.getFormUuid()));
        return  FormsIOJsonUtils.concatenate(customVariableFormAppliesTo, copyFormsByUUID, "Custom Variables", true, appendPreviousNextButtons);
    }

    private  boolean formExists(final List<CustomVariableFormAppliesTo> list, final UUID formUUID){
        return list.stream().anyMatch(customVariableFormAppliesTo -> formUUID.equals(customVariableFormAppliesTo.getCustomVariableForm().getFormUuid()));
    }

    private  List<CustomVariableForm> getFormsInCustomFields(final XnatProjectdata project) {
        return getFormsForKeysInCustomFields(getCustomFieldKeys(project));
    }

    private  List<CustomVariableForm> getFormsInCustomFields(final XnatSubjectdata subject) {
        return getFormsForKeysInCustomFields(getCustomFieldKeys(subject));
    }

    private  List<CustomVariableForm> getFormsInCustomFields(final XnatExperimentdata experiment) {
        return getFormsForKeysInCustomFields(getCustomFieldKeys(experiment));
    }

    private  List<CustomVariableForm> getFormsForKeysInCustomFields(final List<String> customFieldKeys) {
        List<CustomVariableForm> formsByKeys = new ArrayList<>();
        if (customFieldKeys.isEmpty()) {
            return formsByKeys;
        }
        customFieldKeys.forEach(key -> {
           CustomVariableForm form =  formService.findByUuid(UUID.fromString(key));
           if (null != form) {
               formsByKeys.add(form);
           }
        });
        return formsByKeys;
    }

    private List<String> getCustomFieldKeys(final XnatProjectdata project) {
        if (null == project) {
            return new ArrayList<>();
        }
        return getCustomFieldKeys(project.getCustomFields());
    }

    private List<String> getCustomFieldKeys(final XnatSubjectdata subject) {
        if (null == subject) {
            return new ArrayList<>();
        }
        return getCustomFieldKeys(subject.getCustomFields());
    }

    private List<String> getCustomFieldKeys(final XnatExperimentdata experiment) {
        if (null == experiment) {
            return new ArrayList<>();
        }
        return getCustomFieldKeys(experiment.getCustomFields());
    }

    private List<String> getCustomFieldKeys(final JsonNode customFields) {
        List<String> keys = new ArrayList<>();
        if (null == customFields || customFields.isNull()) {
            return keys;
        }
        Iterator<String> iterator = customFields.fieldNames();
        iterator.forEachRemaining(e -> keys.add(e));
        return keys;
    }

    private List<CustomVariableFormAppliesTo> getFormsToConcatenate(final String projectId, final String xsiType) {
        List<CustomVariableFormAppliesTo> forms = new ArrayList<CustomVariableFormAppliesTo>();

        List<String> statuses = new ArrayList<String>();
        statuses.add(CustomFormsConstants.ENABLED_STATUS_STRING);
        statuses.add(CustomFormsConstants.OPTED_OUT_STATUS_STRING);

        List<CustomVariableAppliesTo> projectForms = formAppliesToService.filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Project, projectId, xsiType, null, null, null, statuses);
        List<CustomVariableAppliesTo> siteForms = formAppliesToService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Site, null, xsiType, null, null, null, CustomFormsConstants.ENABLED_STATUS_STRING);
        List<CustomVariableFormAppliesTo> optedInForms = FormsIOJsonUtils.removeSiteFormsOptedOutByProject(siteForms, projectForms);
        forms.addAll(optedInForms);
        for (CustomVariableAppliesTo c : projectForms) {
            forms.addAll(c.getCustomVariableFormAppliesTos());
        }
        return forms;
    }

    private XnatProjectdata getProject(final String idOrAlias, final String projectIdQueryParam, final String xsiType, final UserI user) {
        if (XnatProjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            // Try using the primary id
            final XnatProjectdata project = getProjectByIdOrAliasSkipCache(idOrAlias, user);
            if (null != project) {
                return project;
            }
        }
        return getProjectByIdOrAliasSkipCache(projectIdQueryParam, user);

    }

    private XnatProjectdata getProjectByIdOrAliasSkipCache(final String idOrAlias, final UserI user) {
        final XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(idOrAlias, user, false);
        if (null != project) {
            return project;
        }
        final List<XnatProjectdata> matches = AutoXnatProjectdata.getXnatProjectdatasByField("xnat:projectData/aliases/alias/alias", idOrAlias, user, false);
        if (!matches.isEmpty()) {
            return matches.get(0);
        }
        return null;
    }

    private XnatSubjectdata getSubjectByIdOrLabel(final String idOrLabel, final XnatProjectdata project, final UserI user) {
        final XnatSubjectdata subject = XnatSubjectdata.getXnatSubjectdatasById(idOrLabel, user, false);
        if (subject != null) {
            return subject;
        }
        if (project != null) {
            return XnatSubjectdata.GetSubjectByProjectIdentifier(project.getId(), idOrLabel, user, false);
        }
        return null;
    }

    private XnatExperimentdata getExperimentByIdOrLabel(final String idOrLabel, final XnatProjectdata project, final UserI user) {
        final XnatExperimentdata experiment = XnatExperimentdata.getXnatExperimentdatasById(idOrLabel, user, false);
        if (experiment != null) {
            return experiment;
        }
        if (project != null) {
            return XnatExperimentdata.GetExptByProjectIdentifier(project.getId(), idOrLabel, user, false);
        }
        return null;
    }
}
