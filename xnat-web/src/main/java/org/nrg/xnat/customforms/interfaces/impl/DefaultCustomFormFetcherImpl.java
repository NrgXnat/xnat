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
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
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

        if (XnatProjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            // A project
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(id, user, false);
            if (null == project) {
                //A new project?
                if (projectIdQueryParam != null) {
                    project = XnatProjectdata.getXnatProjectdatasById(projectIdQueryParam, user, false);
                }
            }
            if (null == project) {
                throw new Exception("Did not find any project with ID " + id + " or " + projectIdQueryParam);
            }
            List<CustomVariableForm> formByCustomFieldUUID = getFormsInCustomFields(project);
            return getConcatenatedFormsJSON(project.getId(), xsiType, formByCustomFieldUUID, appendPreviousNextButtons);
        } else if (XnatSubjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            // A Subject
            XnatSubjectdata subject = XnatSubjectdata.getXnatSubjectdatasById(id, user, false);
            String projectId = null;
            if (null == subject) {
                if (null != projectIdQueryParam) {
                    projectId = projectIdQueryParam;
                } else {
                    throw new Exception("Did not find any subject with ID " + id + " or project with " + projectIdQueryParam);
                }
            } else {
                if (null != projectIdQueryParam) {
                    projectId = projectIdQueryParam;
                }else {
                    projectId = subject.getProject();
                }
            }
            List<CustomVariableForm> formByCustomFieldUUID = getFormsInCustomFields(subject);
            return getConcatenatedFormsJSON(projectId, xsiType, formByCustomFieldUUID, appendPreviousNextButtons);
        } else {
            // An experiment
            XnatExperimentdata experiment = XnatExperimentdata.getXnatExperimentdatasById(id, user, false);
            String projectId = null;
            if (null == experiment) { // A new experiment being created
                if (null != projectIdQueryParam) {
                    projectId = projectIdQueryParam;
                } else {
                    throw new Exception("Did not find any experiment with ID " + id);
                }
            } else {
                if (null != projectIdQueryParam) {
                    projectId = projectIdQueryParam;
                }else {
                    projectId = experiment.getProject();
                }
            }
            List<CustomVariableForm> formByCustomFieldUUID = getFormsInCustomFields(experiment);
            return getConcatenatedFormsJSON(projectId, xsiType, formByCustomFieldUUID, appendPreviousNextButtons);
        }
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

}
