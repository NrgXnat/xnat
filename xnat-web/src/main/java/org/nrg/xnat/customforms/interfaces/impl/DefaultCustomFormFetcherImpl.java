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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@CustomFormFetcherAnnotation(type = CustomFormsConstants.PROTOCOL_UNAWARE)

public class DefaultCustomFormFetcherImpl implements CustomFormFetcherI {

    private final CustomVariableAppliesToService formAppliesToService;
    private final CustomVariableFormService formService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DefaultCustomFormFetcherImpl(final CustomVariableAppliesToService formAppliesToService, final CustomVariableFormService formService, final ObjectMapper objectMapper) {
        this.formAppliesToService = formAppliesToService;
        this.formService = formService;
        this.objectMapper = objectMapper;
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
            // No project, no entity. Just return enabled site forms.
            final List<CustomVariableAppliesTo> siteAppliesTos = formAppliesToService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Site, null, xsiType, null, null, null, CustomFormsConstants.ENABLED_STATUS_STRING);
            final List<CustomVariableFormAppliesTo> siteFormAppliesTos = FormsIOJsonUtils.pullOutFormAppliesTo(siteAppliesTos);
            return FormsIOJsonUtils.concatenate(siteFormAppliesTos, null, "Custom Variables", true, appendPreviousNextButtons, objectMapper);
        }

        // Find project id and entity's custom fields
        final XnatProjectdata project = getProject(id, projectIdQueryParam, xsiType, user);
        final String projectId;
        final List<UUID> formUuidsFromCustomFields;
        if (XnatProjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            if (null == project) {
                throw new NotFoundException("Did not find any project with ID " + id + (StringUtils.isNotEmpty(projectIdQueryParam) ? " or " + projectIdQueryParam : ""));
            }
            formUuidsFromCustomFields = getFormUuidsFromCustomFields(project);
            projectId = project.getId();
        } else if (XnatSubjectdata.SCHEMA_ELEMENT_NAME.equals(xsiType)) {
            // A Subject
            final XnatSubjectdata subject = getSubjectByIdOrLabel(id, project, user);
            if (ObjectUtils.allNull(subject, project)) {
                throw new NotFoundException("Did not find any subject with ID " + id + (StringUtils.isNotBlank(projectIdQueryParam) ? " or project " + projectIdQueryParam : ""));
            }
            projectId = subject == null ? project.getId() : subject.getProject();
            formUuidsFromCustomFields = getFormUuidsFromCustomFields(subject);
        } else {
            // An experiment
            final XnatExperimentdata experiment = getExperimentByIdOrLabel(id, project, user);
            if (ObjectUtils.allNull(experiment, project)) {
                throw new NotFoundException("Did not find any experiment with ID " + id + (StringUtils.isNotBlank(projectIdQueryParam) ? " or project " + projectIdQueryParam : ""));
            }
            projectId = experiment == null ? project.getId() : experiment.getProject();
            formUuidsFromCustomFields = getFormUuidsFromCustomFields(experiment);
        }

        // Get all site forms, even disabled
        final List<CustomVariableAppliesTo> siteAppliesTos = formAppliesToService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Site, null, xsiType, null, null, null, null);
        final List<CustomVariableFormAppliesTo> siteFormAppliesTos = FormsIOJsonUtils.pullOutFormAppliesTo(siteAppliesTos);

        // Get all project forms, even disabled and opted out
        final List<CustomVariableAppliesTo> projectAppliesTos = projectId != null
                ? formAppliesToService.filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope.Project, projectId, xsiType, null, null, null, Collections.emptyList())
                : Collections.emptyList();
        final List<CustomVariableFormAppliesTo> projectFormAppliesTos = FormsIOJsonUtils.pullOutFormAppliesTo(projectAppliesTos);

        // Find forms that aren't site-wide or on this project, but which we should load given that there is form data in the entity's custom fields.
        // XNAT-7540 We won't add any disabled or opted-out forms here, because they are still contained in the site and project lists (to be removed later).
        final List<CustomVariableForm> additionalForms = getAdditionalFormsFromCustomFieldUuids(formUuidsFromCustomFields, siteFormAppliesTos, projectFormAppliesTos);

        // Filter out site forms for which the project has opted out
        final List<CustomVariableFormAppliesTo> nonOptedOutSiteFormAppliesTos = FormsIOJsonUtils.removeSiteFormOptedOutByProject(siteFormAppliesTos, projectFormAppliesTos);

        // Combine all the forms and return their JSON representation
        // (This also filters out disabled forms)
        final List<CustomVariableFormAppliesTo> forms = Stream.concat(nonOptedOutSiteFormAppliesTos.stream(), projectFormAppliesTos.stream()).collect(Collectors.toList());
        return FormsIOJsonUtils.concatenate(forms, additionalForms, "Custom Variables", true, appendPreviousNextButtons, objectMapper);
    }

    private  List<UUID> getFormUuidsFromCustomFields(final XnatProjectdata project) {
        return getFormUuidsFromCustomFields(null == project ? null : project.getCustomFields());
    }

    private  List<UUID> getFormUuidsFromCustomFields(final XnatSubjectdata subject) {
        return getFormUuidsFromCustomFields(null == subject ? null : subject.getCustomFields());
    }

    private  List<UUID> getFormUuidsFromCustomFields(final XnatExperimentdata experiment) {
        return getFormUuidsFromCustomFields(null == experiment ? null : experiment.getCustomFields());
    }

    private  List<UUID> getFormUuidsFromCustomFields(final JsonNode customFields) {
        if (null == customFields || customFields.isNull() || customFields.isEmpty()) {
            return Collections.emptyList();
        }

        final Iterable<String> fieldNamesIterable = customFields::fieldNames;
        return StreamSupport.stream(fieldNamesIterable.spliterator(), false)
                .filter(Objects::nonNull)
                .map(fieldName -> {
                    try {
                        return UUID.fromString(fieldName);
                    } catch (IllegalArgumentException ignored) {
                        // They have some custom field which isn't keyed by a form UUID.
                        // This is not a problem.
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<CustomVariableForm> getAdditionalFormsFromCustomFieldUuids(final List<UUID> formUuidsFromCustomFields, final List<CustomVariableFormAppliesTo> siteForms, final List<CustomVariableFormAppliesTo> projectForms) {
        final Set<UUID> formUuidsWeAlreadyHave = Stream.concat(siteForms.stream(), projectForms.stream())
                .map(CustomVariableFormAppliesTo::getCustomVariableForm)
                .map(CustomVariableForm::getFormUuid)
                .collect(Collectors.toSet());
        return formUuidsFromCustomFields.stream()
                .filter(formUuid -> !formUuidsWeAlreadyHave.contains(formUuid))  // In Java 11+ can use Predicate.not for this instead of constructing a lambda
                .map(formService::findByUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
