package org.nrg.xnat.customforms.customvariable.migration.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatDatatypeprotocolI;
import org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI;
import org.nrg.xdat.model.XnatFielddefinitiongroupI;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatFielddefinitiongroup;
import org.nrg.xdat.om.XnatFielddefinitiongroupField;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.customvariable.migration.event.CustomVariableMigrationEvent;
import org.nrg.xnat.customforms.customvariable.migration.execption.CustomVariableMigrationException;
import org.nrg.xnat.customforms.customvariable.migration.model.CustomVariable;
import org.nrg.xnat.customforms.customvariable.migration.model.DataIntegrityFailureReport;
import org.nrg.xnat.customforms.customvariable.migration.model.DataIntegrityFailureReportItem;
import org.nrg.xnat.customforms.customvariable.migration.model.DataIntegrityItem;
import org.nrg.xnat.customforms.customvariable.migration.model.FieldDefinition;
import org.nrg.xnat.customforms.customvariable.migration.reviewer.MigrationDataReviewer;
import org.nrg.xnat.features.CustomFormsFeatureFlags;
import org.nrg.xnat.customforms.helpers.CustomVariableMigrationHelper;
import org.nrg.xnat.customforms.pojo.CollatedLegacyCustomVariable;
import org.nrg.xnat.customforms.pojo.ComponentPojo;
import org.nrg.xnat.customforms.pojo.LegacyCustomVariable;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.service.CustomFormManagerService;
import org.nrg.xnat.customforms.service.CustomFormPermissionsService;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.utils.WorkflowUtils;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.nrg.xnat.customforms.utils.CustomFormsConstants.EMPTY_FORM_DATA_FOR_CUSTOM_VARIABLE;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.NO_DATA_AVAILABLE_FOR_CUSTOM_VARIABLE;

@Service
@Slf4j
public class LegacyCustomVariableMigrator {

    @Autowired
    public LegacyCustomVariableMigrator(final JdbcTemplate template,
                                        final CustomFormPermissionsService customFormPermissionsService,
                                        final CustomVariableFormService formService,
                                        final CustomVariableAppliesToService customVariableAppliesToService,
                                        final CustomFormManagerService customFormManagerService,
                                        final XnatUserProvider userProvider,
                                        final NrgEventService eventService,
                                        final ExecutorService executorService,
                                        final ObjectMapper objectMapper,
                                        final SiteConfigPreferences siteConfigPreferences,
                                        final CustomFormsFeatureFlags customFormsFeatureFlags
    ) {
        this.template = template;
        this.formService = formService;
        this.customVariableAppliesToService = customVariableAppliesToService;
        this.customFormManagerService = customFormManagerService;
        this.primaryAdminUserProvider = userProvider;
        this.customFormPermissionsService = customFormPermissionsService;
        this.eventService = eventService;
        this.executorService = executorService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.customFormsFeatureFlags = customFormsFeatureFlags;

        this.objectMapper = objectMapper;
        objectMapperEscapeNonAscii = objectMapper.copy();
        objectMapperEscapeNonAscii.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
    }

    /**
     * Generates the FormsIO components array from XnatFielddefinitiongroup
     *
     * @param fieldDefinitionGroup - The legacy custom variable
     * @return
     */

    private ArrayNode buildComponentsForFormsIO(final XnatFielddefinitiongroup fieldDefinitionGroup, final UUID formUUID) {
        String description = fieldDefinitionGroup.getDescription();
        String field_definition_id = fieldDefinitionGroup.getId();
        ArrayNode componentsArrayNode = objectMapper.createArrayNode();
        //Create a HTML Title Node
        ObjectNode componentTitleNode = objectMapper.createObjectNode();
        //Create a container node to be able to separate the Custom Variable Data
        ObjectNode containerNode = objectMapper.createObjectNode();
        containerNode.put("key", formUUID.toString());
        containerNode.put("type", "container");
        containerNode.put("input", true);
        containerNode.put("label", formUUID.toString());
        containerNode.put("hideLabel", true);
        containerNode.put("tableView", false);
        ArrayNode containerComponentsArrayNode = objectMapper.createArrayNode();
        if (!field_definition_id.equalsIgnoreCase("DEFAULT")) {
            componentTitleNode.put("input", false);
            componentTitleNode.put("html", "<p><span class=\"text-tiny\" style=\"font-family:Arial, Helvetica, sans-serif;\"><b> Form UUID: " + formUUID.toString() + "</b></span></p>");
            componentTitleNode.put("type", "content");
            if (null != description)
                componentTitleNode.put("description", description);
            else {
                componentTitleNode.put("description", field_definition_id);
            }
            containerComponentsArrayNode.add(componentTitleNode);
        }

        ArrayList<XnatFielddefinitiongroupField> fields = fieldDefinitionGroup.getFields_field();
        for (XnatFielddefinitiongroupField f : fields) {
            String fieldLabel = f.getName();
            //String, Integer, Float, Boolean, Date
            String fieldType = f.getDatatype();
            Boolean isRequired = f.getRequired();
            List<XnatFielddefinitiongroupFieldPossiblevalueI> possibleValues = f.getPossiblevalues_possiblevalue();
            ObjectNode fieldNode = buildFieldNode(fieldLabel, fieldType, isRequired, f.getXmlpath(), possibleValues);
            containerComponentsArrayNode.add(fieldNode);
        }
        containerNode.set("components", containerComponentsArrayNode);
        componentsArrayNode.add(containerNode);
        return componentsArrayNode;
    }

    /**
     * A helper to convert the legacy Field Definition Groups to FORMSIO JSONs
     *
     * @param fieldDefinitionGroup - FieldDefinitionGroup
     * @return - String: If found, Custom Field Definition Group represented as FormIO Json or null
     */

    private String convertToFormJson(XnatFielddefinitiongroupI fieldDefinitionGroup, final UUID formUUID) throws JsonProcessingException {
        ObjectNode parentNode = objectMapper.createObjectNode();
        parentNode.put("display", "form");
        parentNode.put("title", fieldDefinitionGroup.getId());
        ObjectNode settingsNode = objectMapper.createObjectNode();
        parentNode.set("settings", settingsNode);
        ArrayNode componentsArrayNode = buildComponentsForFormsIO((XnatFielddefinitiongroup)fieldDefinitionGroup,formUUID);
        parentNode.set("components", componentsArrayNode);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentNode);
    }

    private ObjectNode buildFieldNode(final String fieldLabel, final String fieldType, final Boolean isRequired, final String fieldXmlPath, final List<XnatFielddefinitiongroupFieldPossiblevalueI> possibleValues) {
        ObjectNode fieldNode = objectMapper.createObjectNode();
        fieldNode.put("label", fieldLabel);
        fieldNode.put("inline", false);
        fieldNode.put("tableView", false);
        fieldNode.put("optionsLabelPosition", "right");
        fieldNode.put("selectThreshold", 0.3);
        if (fieldType.equalsIgnoreCase("DATE")) {
            String preferredDateFormat = "MM/dd/yyyy";
            String dateFormat = siteConfigPreferences.getUiDateFormat();
            if (null != dateFormat) {
                preferredDateFormat = dateFormat;
            }
            fieldNode.put("format", preferredDateFormat);
            fieldNode.put("type", "xnatdate");
         } else {
            fieldNode.put("widget", "choicejs");
            if (null != possibleValues && possibleValues.size() > 0) {
                fieldNode.put("widget", "choicesjs");
                fieldNode.put("type", "select");
                ObjectNode fieldDataValuesNode = objectMapper.createObjectNode();
                ArrayNode dataValuesArrayNode = objectMapper.createArrayNode();
                for (XnatFielddefinitiongroupFieldPossiblevalueI p : possibleValues) {
                    ObjectNode valueNode = objectMapper.createObjectNode();
                    valueNode.put("label", p.getDisplay() == null ? p.getPossiblevalue() : p.getDisplay());
                    valueNode.put("value", p.getPossiblevalue());
                    dataValuesArrayNode.add(valueNode);
                }
                fieldDataValuesNode.set("values", dataValuesArrayNode);
                fieldNode.set("data", fieldDataValuesNode);
                fieldNode.put("searchEnabled", false);
                fieldNode.put("type", "select");
            } else {
                if (fieldType.equalsIgnoreCase("INTEGER") || fieldType.equalsIgnoreCase("FLOAT")) {
                    fieldNode.put("mask", false);
                    fieldNode.put("tableView", false);
                    fieldNode.put("delimiter", false);
                    fieldNode.put("type", "xnatNumber");
                    //Setting this to true, sets the decimal precision to 2 by FormIO
                    //fieldNode.put("requireDecimal", fieldType.equalsIgnoreCase("FLOAT"));
                    fieldNode.put("inputFormat", "plain");
                    if (fieldType.equalsIgnoreCase("INTEGER")) {
                        fieldNode.put("decimalLimit", 0);
                        fieldNode.put("requireDecimal", false);
                        fieldNode.put("validate", "{integer: true, step:1}");
                    }else {
                        fieldNode.put("decimalLimit", 20);
                    }
                    fieldNode.put("inputFormat", "plain");
                    fieldNode.put("truncateMultipleSpaces", false);
                    fieldNode.put("input", true);
                } else if (fieldType.equalsIgnoreCase("BOOLEAN")) {
                    fieldNode.put("inline", true);
                    ArrayNode dataValuesArrayNode = objectMapper.createArrayNode();
                    ObjectNode valueNode = objectMapper.createObjectNode();
                    valueNode.put("label", "True");
                    valueNode.put("value", "true");
                    dataValuesArrayNode.add(valueNode);
                    valueNode = objectMapper.createObjectNode();
                    valueNode.put("label", "False");
                    valueNode.put("value", "false");
                    dataValuesArrayNode.add(valueNode);
                    fieldNode.set("values", dataValuesArrayNode);
                    fieldNode.put("dataType", "boolean");
                    fieldNode.put("type", "xnatRadio");
                } else if (fieldType.equalsIgnoreCase("STRING")) {
                    fieldNode.put("type", "textfield");
                }
            }
        }
        ObjectNode fieldValidateNode = objectMapper.createObjectNode();
        fieldValidateNode.put("required", isRequired);
        fieldValidateNode.put("onlyAvailableItems", true);
        fieldNode.set("validate", fieldValidateNode);
        fieldNode.put("input", true);
        fieldNode.put("key", fieldLabel.toLowerCase());
        return fieldNode;
    }

    /**
    Converts a Legacy Custom Variable Definition to a FormIO based Custom Form
     As part of the conversion:
     a) FormIO form is created and associated to the relevant projects
     b) Data is moved into custom_fields
     c) Workflow entries are made to record the data migration
     d) Any search using the custom variables is migrated to the custom forms elements
     Migration is aborted if data in the custom variables does not pass data integirty check
     @param field_definition_id - the field definition to migrate
     @param tracking_id - the tracking id for the UI
     @param user - the user who requests the migration
     */

    @Transactional(rollbackFor = CustomVariableMigrationException.class)
    public void migrateToFormIO(final String field_definition_id, @Nullable final String tracking_id, final UserI user) throws CustomVariableMigrationException {
        if (!customFormsFeatureFlags.isCustomVariableMigrationEnabled()) {
            throw new CustomVariableMigrationException("Migration not enabled");
        }
        String queryStr = String.format(QUERY, field_definition_id, field_definition_id);
        CustomVariableMigrationHelper helper = new CustomVariableMigrationHelper(template);
        log.info("Request for custom variable migration received " + field_definition_id + " from user " + user.getUsername());
        List<LegacyCustomVariable> legacyCustomVariables = helper.doQuery(queryStr);
        String tId = tracking_id;
        if (null == tracking_id) {
            tId = String.format("LegacyCustom_(%s)_(%s)", field_definition_id,System.currentTimeMillis());
        }
        final String trackingId = tId;
        if ( legacyCustomVariables == null || legacyCustomVariables.isEmpty()) {
            eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(),  trackingId, "Invalid custom variable definition id"));
            throw new CustomVariableMigrationException("No custom variable definition for " + field_definition_id);
        }

        List<CollatedLegacyCustomVariable> collatedCustomVariables = helper.collate(legacyCustomVariables);
        final CollatedLegacyCustomVariable matched = collatedCustomVariables.get(0);
        final String queuedMsg = "Custom Variable Migration Queued ";
        eventService.triggerEvent(CustomVariableMigrationEvent.waiting(user.getID(), trackingId, queuedMsg));
        executorService.submit(() -> {
                migrate(user, matched, trackingId);
        });
    }

    private void  migrate(final UserI user,
                         final CollatedLegacyCustomVariable collatedLegacyCustomVariable,
                         final String trackingId) throws CustomVariableMigrationException {
        UserI privilegeUser = user;
        final UserI adminUser = primaryAdminUserProvider.get();
        if (Roles.checkRole(user, CustomFormsConstants.DATAFORM_MANAGER_ROLE)) {
            //The form manager may not have access to the project
            privilegeUser = adminUser;
        }
        if (!Roles.isSiteAdmin(privilegeUser)) {
            checkProjectAccess(privilegeUser, collatedLegacyCustomVariable, trackingId);
        }

        final String dataType = collatedLegacyCustomVariable.getDataType();
        final String dataTypeSingularName = ElementSecurity.GetSingularDescription(dataType);
        final String projectSingularName = ElementSecurity.GetSingularDescription(XnatProjectdata.SCHEMA_ELEMENT_NAME);
;        final String dataTypePluralName = ElementSecurity.GetPluralDescription(dataType);
        final String fieldDefinitionId = collatedLegacyCustomVariable.getId();
        final List<String> projectIds = collatedLegacyCustomVariable.getProjectIds();
        if (projectIds.isEmpty()) {
            eventService.triggerEvent(CustomVariableMigrationEvent.complete(user.getID(),  trackingId, "No " + projectSingularName + " associated"));
            log.info("No projects were associated with the field definition. Marking task complete");
            return;
        }
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectIds.get(0), privilegeUser, false);
        XnatFielddefinitiongroupI fieldDefinitionGroup = getStudyProtocolByDefinitionId(project, fieldDefinitionId);
        Hashtable<String, DataIntegrityFailureReport> migrationReviewDetails = new Hashtable<String, DataIntegrityFailureReport>();
        Hashtable<String, Integer> migrationDetails = new Hashtable<String, Integer>();
        List<DataIntegrityFailureReport> projectsFailedDataIntegrity = new ArrayList<DataIntegrityFailureReport>();
        List<MigrationDataReviewer> projectsClearedToMigrate = new ArrayList<MigrationDataReviewer>();

        for (String projectId : projectIds) {
            eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(), trackingId, String.format("Reviewing custom variable set %s for %s in %s ", fieldDefinitionId, dataTypePluralName, projectId)));
            try {
                MigrationDataReviewer migrationDataReviewer = new MigrationDataReviewer(template, dataType, projectId, fieldDefinitionGroup);

                migrationDataReviewer.init();
                Hashtable<String, List<CustomVariable>> entityCustomVariableHash = migrationDataReviewer.getEntityCustomVariableData();
                DataIntegrityFailureReport dataIntegrityFailureReport = migrationDataReviewer.reviewData(entityCustomVariableHash);
                if (!dataIntegrityFailureReport.getDataIntegrityReportItems().isEmpty()) {
                    projectsFailedDataIntegrity.add(dataIntegrityFailureReport);
                    eventService.triggerEvent(CustomVariableMigrationEvent.warn(user.getID(), trackingId, String.format("Encountered data integrity issues in set %s for  %s in %s ", fieldDefinitionId, dataTypePluralName, dataIntegrityFailureReport.getProjectId())));
                } else {
                    projectsClearedToMigrate.add(migrationDataReviewer);
                }
            }catch(DataAccessException de) {
                eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(),  trackingId, String.format("Data access failed for %s. Cause: %s",fieldDefinitionId, de.getMessage())));
                log.error("Data Access Exception", de);
                throw new CustomVariableMigrationException(String.format("Could not access data %s. Cause: ", de.getMessage()));
            }
        }
        if (!projectsFailedDataIntegrity.isEmpty()) {
            eventService.triggerEvent(CustomVariableMigrationEvent.warn(user.getID(), trackingId, String.format("Skipping migration. Please resolve data integrity issues in all " + projectSingularName + " using this custom variable definition first, details being sent via email", fieldDefinitionId)));
            notifyProjectMigrationAborted(user, dataTypeSingularName, projectsFailedDataIntegrity);
            eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(), trackingId, String.format("Details sent via email", fieldDefinitionId)));
            return;
        }
        if (projectsClearedToMigrate.isEmpty()) {
            eventService.triggerEvent(CustomVariableMigrationEvent.complete(user.getID(),  trackingId, String.format("Migration process complete for %s",fieldDefinitionId)));
            return;
        }
        eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Data review complete for %s",fieldDefinitionId)));
        eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Generating Custom Form from the Custom Variable Definition for %s",dataTypeSingularName)));
        final String formUUIDStr = associateFormIOForms(user, dataType, projectsClearedToMigrate, fieldDefinitionGroup);
        eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, "Completed associating Custom Form to " + projectSingularName));

            for (MigrationDataReviewer projectReviewer : projectsClearedToMigrate) {
                final String projectId = projectReviewer.getProjectId();
                eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Migrating data for  %s in %s ", dataTypePluralName, projectId)));
                int migratedCount = -1;
                try {
                    migratedCount = moveColumns(user, dataType, projectReviewer, fieldDefinitionGroup, trackingId, formUUIDStr);
                }catch(Exception e ) {
                    log.error("Could not migrate columns for " + projectId, e);
                }
                migrationDetails.put(projectId, migratedCount);
                if (migratedCount < 0) {
                    log.error("Could not move columns for " + projectId);
                    eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(),  trackingId, String.format("Failed to  move %s data for %s ",dataTypeSingularName,  projectId)));
                    throw new CustomVariableMigrationException("Failed to migrate data for " + projectId);
                }else if (migratedCount > 0) {
                    eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Completed migrating data for %s %s in %s %s ", migratedCount, dataTypeSingularName,projectSingularName, projectId)));
                }
                try {
                    eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Detaching custom variable definition  for %s ", projectId)));
                    detachFieldDefinitionFromProject(privilegeUser, dataType, projectId, fieldDefinitionGroup);
                    eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Completed detaching custom variable definition  for %s ", projectId)));
                }catch(Exception e) {
                    log.error("Could not detach field definition from  " + projectId);
                    eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(),  trackingId, String.format("Could not detach field definition from %s ", projectId)));
                    throw new CustomVariableMigrationException("Failed to migrate data for " + projectId);
                }
        }
        if (!migrationDetails.contains(new Integer(-1))) {
            try {
                log.info("Updating stored searches to refer to custom fields");
                eventService.triggerEvent(CustomVariableMigrationEvent.complete(user.getID(),  trackingId, "Investigating available stored search referring the custom variable"));
                List<String> duplicateFieldNames = doDuplicateFieldNamesExist(collatedLegacyCustomVariable.getFieldDefinitionGroupId());
                List<SearchNotMigrated> searchesNotMigrated = migrateStoredSearcheRefreencesFromCustomVariableToCustomForm(privilegeUser,  dataType, duplicateFieldNames, formUUIDStr);
                if (!searchesNotMigrated.isEmpty()) {
                    notifyWarning(user, searchesNotMigrated);
                    eventService.triggerEvent(CustomVariableMigrationEvent.warn(user.getID(),  trackingId, String.format("%s associated stored searches could not be migrated. Manual edit of these stored searches is required. Email with details has been sent", searchesNotMigrated.size())));
                }else {
                    log.info("Email being sent");
                    notify(privilegeUser, user, dataTypePluralName, projectSingularName, migrationDetails, formUUIDStr, fieldDefinitionId);
                    log.info("Email sent");
                }
                eventService.triggerEvent(CustomVariableMigrationEvent.complete(user.getID(),  trackingId, "Migration complete"));
            }catch(Exception e) {
                eventService.triggerEvent(CustomVariableMigrationEvent.fail(user.getID(),  trackingId, String.format("Failed to replace stored search references. Cause: %s  ", e.getMessage())));
                log.error("Could not migrate stored searches",e);
            }
        }
    }

    private void checkProjectAccess(final UserI user,
                                      final CollatedLegacyCustomVariable collatedLegacyCustomVariable,
                                      final String trackingId) {
        final List<String> projectIds = collatedLegacyCustomVariable.getProjectIds();
        boolean accessDenied = false;
        String deniedProjects = "";
        for (String p: projectIds) {
            eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, "Verifying access permission to project " + p));
            if (!Permissions.canDeleteProject(user, p)) {
                accessDenied = true;
                deniedProjects += p + " ";
            }
        }
        if (accessDenied) {
            throw new CustomVariableMigrationException("User does not have access to all the projects " + deniedProjects );
        }


    }

    private String getFormIOJson(final XnatFielddefinitiongroupI fieldDefinitionGroup, final UUID formUUID){
        try {
            log.info("Constructing FormIO JSON");
            //Build the JSON
            String form = convertToFormJson(fieldDefinitionGroup, formUUID);
            log.info("Completed  FormIO JSON construction");
            return form;
        } catch(JsonProcessingException jpe) {
            log.error("Could not construct form", jpe);
            throw new CustomVariableMigrationException("Encountered " + jpe.getMessage() + " while migrating to custom form " + fieldDefinitionGroup.getId());
        }
    }

    private String associateFormIOForms(final UserI user,   final String dataType, final List<MigrationDataReviewer> projectsClearedToMigrate, final XnatFielddefinitiongroupI fieldDefinitionGroup) {
        try {
            CustomVariableForm form = new CustomVariableForm();
            final UUID formUUID = UUID.randomUUID();
            final String formioJsonContent = getFormIOJson(fieldDefinitionGroup, formUUID);
            JsonNode proposed = objectMapper.readTree(formioJsonContent);
            form.setFormIOJsonDefinition(proposed);
            form.setFormUuid(formUUID);
            form.setFormCreator(user.getUsername());
            formService.saveOrUpdate(form);

            long formId = form.getId();
            RowIdentifier existingFormPrimaryKey = new RowIdentifier();
            existingFormPrimaryKey.setFormId(formId);
            existingFormPrimaryKey.setAppliesToId(-1);

            if (null != customVariableAppliesToService) {
                for (MigrationDataReviewer projectReviewer : projectsClearedToMigrate) {
                    final String projectId = projectReviewer.getProjectId();
                    log.info("Associating form " + formId + " to project " + projectId);
                    UserOptionsPojo userOptionsPojo = new UserOptionsPojo(dataType, null, null, null);
                    ComponentPojo projComponent = new ComponentPojo();
                    projComponent.setValue(projectId);
                    projComponent.setLabel(projectId);
                    customFormManagerService.save(user, userOptionsPojo, Collections.singletonList(projComponent), proposed, existingFormPrimaryKey);
                    log.info("Associated form " + formId + " to project " + projectId);
                }
            }
            return formUUID.toString();
        }catch(Exception e) {
            log.error("Failed to associate project to form", e);
            throw new CustomVariableMigrationException("Could not associate forms to projects:  " + e.getMessage());
        }
    }

    private XnatFielddefinitiongroupI getStudyProtocolByDefinitionId(final XnatProjectdata project, final String fieldDefinitionId) {
        XnatFielddefinitiongroupI matchedFieldDefinition = null;
        List<XnatAbstractprotocol> abstractProtocols  = project.getStudyprotocol();
        for (XnatAbstractprotocol studyProtocol : abstractProtocols) {
            if (studyProtocol instanceof XnatDatatypeprotocolI) {
                XnatDatatypeprotocolI datatypeProtocol = (XnatDatatypeprotocolI) studyProtocol;
                List<XnatFielddefinitiongroupI> definitions = datatypeProtocol.getDefinitions_definition();
                for (XnatFielddefinitiongroupI def: definitions) {
                    if (def.getId().equals(fieldDefinitionId)) {
                        matchedFieldDefinition = def;
                        break;
                    }
                }
                if (matchedFieldDefinition != null) {
                    break;
                }
            }
        }
        return matchedFieldDefinition;
    }


    private int moveColumns(final UserI user, final String dataType, MigrationDataReviewer projectReviewer, final XnatFielddefinitiongroupI fieldDefinitiongroup, final String trackingId, final String formUUIDStr) throws Exception {
        int migratedCount = 0;
        final String projectId = projectReviewer.getProjectId();
        log.info("Moving columns for " + dataType + " project " + projectId);
        final String dataTypeSingularName = ElementSecurity.GetSingularDescription(dataType);
        final String dataTypePluralName = ElementSecurity.GetPluralDescription(dataType);
        final String projectSingularName = ElementSecurity.GetSingularDescription(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        final List<String> distinctEntityIds = projectReviewer.getDistinctEntityIds();
        final List<String> fieldNames = projectReviewer.getFieldNames();
        final String tableName = projectReviewer.getTableNane();
        final String entityColumnName = projectReviewer.getEntityColumnName();

        final Hashtable<String, List<CustomVariable>> entityCustomVariable = projectReviewer.getEntityCustomVariableData();
        final List<FieldDefinition>  fieldDefinitions = projectReviewer.getFieldDefinitions();
        final String inSql = String.join(",", Collections.nCopies(fieldDefinitions.size(), "?"));

        if (distinctEntityIds.isEmpty() || fieldNames.isEmpty()) {
            log.info("No data captured. Nothing to move");
            eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("No  data captured for %s using the custom variable definition  in %s %s ",dataTypeSingularName, projectSingularName,  projectId)));
            return 0;
        }
        eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Data to be migrated for  %s %s", distinctEntityIds.size(), dataTypePluralName)));
        for (String entityId : entityCustomVariable.keySet()) {
            final List<CustomVariable> customVariables = entityCustomVariable.get(entityId);
            int saved = saveAsCustomField(dataType, entityId, customVariables,  fieldDefinitions, formUUIDStr);
            String workflowMessage = null;
            if (saved == NO_DATA_AVAILABLE_FOR_CUSTOM_VARIABLE) {
                eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(), trackingId, String.format("No data found to move for %s ", entityId)));
                workflowMessage = String.format("Migrated custom variable (no data found) to custom fields with FormIO for %s",  entityId);
            } else if ((saved == EMPTY_FORM_DATA_FOR_CUSTOM_VARIABLE) || saved > 0) {
                String deleteQuery = String.format("delete from %s f  where f.name in (%s)   and f.%s = '%s'",tableName, inSql, entityColumnName, entityId);
                deleteCustomVariableFields(deleteQuery, fieldNames);
                eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(), trackingId, String.format("Data moved for %s ", entityId)));
                workflowMessage = String.format("Migrated custom variable to custom fields with FormIO for %s",  entityId);
            }
            if (workflowMessage != null ) {
                try {
                    EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, workflowMessage, "");
                    final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildAdminWorkflow(user, dataType, entityId, eventDetails);
                    final EventMetaI eventInfo = workflow.buildEvent();
                    WorkflowUtils.complete(workflow, eventInfo);
                    eventService.triggerEvent(CustomVariableMigrationEvent.progress(user.getID(),  trackingId, String.format("Created migration workflow entry for %s ", entityId)));
                }catch(Exception e) {
                    log.error("Could not save workflow for " + entityId, e);
                    throw e;
                }
            }
            ++migratedCount;
        }
        return migratedCount;
    }

    private List<String> doDuplicateFieldNamesExist(final int fieldDefinitionId) {
        final String CHECK_DUPLICATE_QUERY = "select name from xnat_fielddefinitiongroup_field where name in (select name from xnat_fielddefinitiongroup_field where fields_field_xnat_fielddefiniti_xnat_fielddefinitiongroup_id = " + fieldDefinitionId +") and fields_field_xnat_fielddefiniti_xnat_fielddefinitiongroup_id != " + fieldDefinitionId;
        List<String> matchingNames = template.queryForList(CHECK_DUPLICATE_QUERY, String.class);
        return matchingNames;
    }

    private  int saveAsCustomField(final String dataType, final String entityId, final List<CustomVariable> customVariables, List<FieldDefinition> fieldDefinitions, final String formUUIDStr) throws IOException, SQLException {
        log.info("Saving custom fields for " + entityId);
        if (customVariables == null || customVariables.isEmpty()) {
            log.info("Nothing to save");
            return NO_DATA_AVAILABLE_FOR_CUSTOM_VARIABLE;
        }

        Hashtable<String, String> fieldNameDatatype = new Hashtable<String, String>();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            fieldNameDatatype.put(fieldDefinition.getName().toLowerCase(), fieldDefinition.getDatatype());
        }
        //Get the existing dynamic fields
        String tableName = "xnat_experimentdata";
        if (dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            tableName = "xnat_subjectdata";
        }
        String selectQuery = "select custom_fields from " + tableName + " where id = ?";
        PGobject existing_custom_fields =  template.queryForObject(selectQuery, new Object[]{entityId}, PGobject.class);
        ObjectNode existingJsonNode = null;
        if (existing_custom_fields != null) {
            existingJsonNode = (ObjectNode)objectMapper.readTree(existing_custom_fields.getValue());
        }
        log.info("Converting data to custom field json for " + entityId);
        final String customFieldJson = buildJson(customVariables, fieldNameDatatype, existingJsonNode, formUUIDStr);
        if (customFieldJson == null) {
           return EMPTY_FORM_DATA_FOR_CUSTOM_VARIABLE;
        }
        String updateQuery = "update " + tableName + " set custom_fields = ? where id = ?";
        PGobject jsonbObj = new PGobject();
        jsonbObj.setType("jsonb");
        jsonbObj.setValue(customFieldJson);

        int rowsAffected = template.update(updateQuery, new Object[]{jsonbObj, entityId }, new int[]{Types.OTHER, Types.VARCHAR});
        log.info("Saved data to custom field json for " + entityId);
        return rowsAffected;
    }

    private  void deleteCustomVariableFields(final String deleteQuery, List<String> fieldNames) {
        log.info("Deleting the legacy custom fields for " + deleteQuery);
        template.update(deleteQuery, fieldNames.toArray());
        log.info("Delete complete");
    }

    @Nullable
    private String buildJson(final List<CustomVariable> customVariables, Hashtable<String, String> fieldNameDatatype , final ObjectNode existingJsonNode, final String formUUIDStr) throws JsonProcessingException {
        ObjectNode rootNode = objectMapperEscapeNonAscii.createObjectNode();
        if (existingJsonNode != null) {
            rootNode = existingJsonNode;
        }
        ObjectNode legacyCustomVariableRootNode = objectMapperEscapeNonAscii.createObjectNode();
        boolean foundData = false;
        for (CustomVariable customVariable : customVariables) {
            final String variableDataType = fieldNameDatatype.get(customVariable.getName());
            if (null != customVariable.getField()) {
                if (variableDataType.equalsIgnoreCase("INTEGER")) {
                    legacyCustomVariableRootNode.put(customVariable.getName(), Integer.parseInt(customVariable.getField()));
                } else if (variableDataType.equalsIgnoreCase("FLOAT")) {
                    //We dont want to loose precision hence not using Float.valueOf
                    legacyCustomVariableRootNode.put(customVariable.getName(), Double.valueOf(customVariable.getField()));
                } else if (variableDataType.equalsIgnoreCase("BOOLEAN")) {
                    //Null value will be set to false
                    legacyCustomVariableRootNode.put(customVariable.getName(), Boolean.parseBoolean(customVariable.getField()));
                } else { //date and string
                    legacyCustomVariableRootNode.put(customVariable.getName(), customVariable.getField());
                }
                foundData = true;
            }
        }
        rootNode.set(formUUIDStr, legacyCustomVariableRootNode);
        return foundData ? objectMapperEscapeNonAscii.writeValueAsString(rootNode) : null;
    }

    private void detachFieldDefinitionFromProject(final UserI user, final String dataType, final String projectId, final XnatFielddefinitiongroupI fieldDefinitionGroup) throws Exception{
        log.info("Detaching field definition group for datatyoe " + dataType + " from project " + projectId);
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        if (null != project) {
            boolean detached = false;
            if (project.getStudyprotocol() !=null && !project.getStudyprotocol().isEmpty()) {
                List<XnatAbstractprotocol> studyProtocols = project.getStudyprotocol();
                for (XnatAbstractprotocol protocol : studyProtocols) {
                    if (protocol instanceof XnatDatatypeprotocol) {
                        XnatDatatypeprotocol datatypeprotocol = (XnatDatatypeprotocol)protocol;
                        if (datatypeprotocol.getDataType().equals(dataType)) {
                            List<XnatFielddefinitiongroup> fielddefinitiongroups =  datatypeprotocol.getDefinitions_definition();
                            int matchingIndex = -1;
                            int i=0;
                            for (XnatFielddefinitiongroup f : fielddefinitiongroups) {
                                if (f.getId().equals(fieldDefinitionGroup.getId()) && f.getDataType().equals(dataType)) {
                                    matchingIndex = i;
                                    break;
                                }
                                ++i;
                            }
                            if (matchingIndex != -1) {
                                datatypeprotocol.removeDefinitions_definition(matchingIndex);
                                detached = true;
                                break;
                            }
                        }
                    }
                }
                if (detached) {
                    EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Migrated custom variable field definition " + fieldDefinitionGroup.getId() + " to custom fields with FormIO for " + dataType , "");
                    final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, eventDetails);
                    final EventMetaI eventInfo = workflow.buildEvent();
                    project.save(user, false, true,eventInfo);
                    WorkflowUtils.complete(workflow, eventInfo);
                    log.info("Detached field definition group from project " + projectId);
                }
            }
        }
    }

    private List<SearchNotMigrated> migrateStoredSearcheRefreencesFromCustomVariableToCustomForm(final UserI user, final String dataType, final List<String> duplicateFieldNames, final String formUUIDStr) throws Exception {
        List<XdatStoredSearch> searchesForDataType =  XdatStoredSearch.getXdatStoredSearchsByField(XdatStoredSearch.SCHEMA_ELEMENT_NAME+"/root_element_name",dataType, user, false);
        final String dataTypeUnderScore = dataType.replace(":", "_").toUpperCase();
        List<SearchNotMigrated> searchesNotMigrated = new ArrayList<SearchNotMigrated>();
        if (searchesForDataType.isEmpty()) {
            return searchesNotMigrated;
        }
        for (XdatStoredSearch search : searchesForDataType) {
            List<String> listOfReferencesToDuplicates = searchContainsReferenceToDuplicates(search,duplicateFieldNames, dataTypeUnderScore);
            if (!listOfReferencesToDuplicates.isEmpty()) {
                SearchNotMigrated sNotMigrated = new SearchNotMigrated(search, listOfReferencesToDuplicates);
                searchesNotMigrated.add(sNotMigrated);
                continue;
            }
            ArrayList<XdatSearchField> searchFields = search.getSearchField();
            //Since a Stored Search could contain custom variables which have not migrated, we need to duplicate
            ArrayList<XdatSearchField> copiesForCustomFields = new ArrayList<XdatSearchField>();
            for (XdatSearchField field : searchFields) {
                String fieldId = field.getFieldId();
                if (fieldId.startsWith(dataTypeUnderScore + "_FIELD_MAP=")) {
                    if (!storedSearchHasAlreadyMigrated(search, field)) {
                        copiesForCustomFields.add(field);
                    }
                }
            }
            for (XdatSearchField field : copiesForCustomFields) {
                XdatSearchField newField = new XdatSearchField();
                newField.setItem(field.getItem());
                String fieldId = newField.getFieldId();
                int indexOfEqualTo = fieldId.indexOf("=");
                String customVariableName = fieldId.substring(indexOfEqualTo+1);
                //XNAT:SUBJECTDATA_CUSTOM-FORM_B5488620-6387-4965-BDD6-BD14C84986E7_S
                String customFormBasedFieldId =  dataType.toUpperCase() + "_CUSTOM-FORM_" + formUUIDStr.toUpperCase() + "_" + customVariableName.toUpperCase();
                newField.setFieldId(customFormBasedFieldId);

                String fieldType = newField.getType();
                if (fieldType.equals("string")) {
                    newField.setType("textfield");
                }else if (fieldType.equals("integer") || fieldType.equals("float")) {
                    newField.setType("number");
                }else if (fieldType.equals("boolean")) {
                    newField.setType("radio");
                }else if (fieldType.equals("date")) {
                    newField.setType("day");
                }
                searchFields.add(newField);
            }
            if (!copiesForCustomFields.isEmpty()) {
                EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Migrated field definitions to custom_fields with FormIO for " + search.getId(), "");
                final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(null, user, XdatStoredSearch.SCHEMA_ELEMENT_NAME, search.getId(), search.getId(), eventDetails);
                final EventMetaI eventInfo = workflow.buildEvent();
                search.save(user,true, false, eventInfo);
                WorkflowUtils.complete(workflow, eventInfo);
            }
        }
        return searchesNotMigrated;
    }

    private List<String> searchContainsReferenceToDuplicates(final XdatStoredSearch search,final List<String> duplicateFieldNames, final String dataTypeUnderScore) throws Exception {
        List<String> listOfReferencesToDuplicates = new ArrayList<String>();
        if (duplicateFieldNames == null || duplicateFieldNames.isEmpty()) {
            return listOfReferencesToDuplicates;
        }
        ArrayList<XdatSearchField> searchFields = search.getSearchFields();
        boolean found = false;
        for (XdatSearchField sf: searchFields) {
            final String fieldId = sf.getFieldId();
            if (fieldId.startsWith(dataTypeUnderScore + "_FIELD_MAP=")) {
                String[] parts = fieldId.split("=");
                if (parts.length == 2) {
                    String customVariableName = parts[1];
                    if (duplicateFieldNames.contains(customVariableName)) {
                        listOfReferencesToDuplicates.add(fieldId);
                    }
                }
            }
        }
        return listOfReferencesToDuplicates;
    }

    private void notifyProjectMigrationAborted(final UserI user, final String dataTypeSingularName, List<DataIntegrityFailureReport> projectsFailedDataIntegrity) {
        final String projectSingularName = ElementSecurity.GetSingularDescription(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        StringJoiner msgCollector = new StringJoiner(" <br> ");
        msgCollector.add("Custom variables were not migrated. Please resolve the following problems: ");
        for (DataIntegrityFailureReport dataIntegrityFailureReport: projectsFailedDataIntegrity) {
           msgCollector.add(String.format("%s : %s <br>", projectSingularName, dataIntegrityFailureReport.getProjectId()));
           msgCollector.add("Each of the following custom variable values has data validation issues");
           msgCollector.add(" ");
           for (DataIntegrityFailureReportItem dIfr: dataIntegrityFailureReport.getDataIntegrityReportItems()) {
               if (!dIfr.getDataIntegrityItems().isEmpty()) {
                   msgCollector.add(String.format(" %s ID: %s", dataTypeSingularName, dIfr.getEntityId()));
                   for (DataIntegrityItem dI: dIfr.getDataIntegrityItems()) {
                       msgCollector.add(String.format(" Field: %s Type: %s Value: %s", dI.getFieldName(), dI.getExpectedFormat(), dI.getDataFound()));
                   }
               }
           }
            msgCollector.add(" ");
       }
        String from = XDAT.getSiteConfigPreferences().getAdminEmail();
        try {
            XDAT.getMailService().sendHtmlMessage(from, user.getEmail(), "IMP: " + TurbineUtils.GetSystemName() + " custom variable migration aborted ", msgCollector.toString());
        }catch(Exception e) {
            log.error("Could not notify user. Wanted to send " + msgCollector.toString(), e);
        }

    }


    private void notifyWarning(final UserI user, List<SearchNotMigrated> searchedNotMigrated) {
        String msg = "The following searches could not be migrated as they contain references to custom variables which occur in multiple custom variable definitions";
        msg += "<br> Each search has to be manually edited to replace references to the custom variables";
        msg += "<br>";
        for (SearchNotMigrated s: searchedNotMigrated) {
            List<String> fields = s.getDuplicateFieldNames();
            if (!fields.isEmpty()) {
                msg += "<br> Search ID: " + s.getSearch().getId();
                msg += "<br> Errant Fields: ";
                for (String f: fields) {
                    msg+= "<br>" + f;
                }
            }
        }
        msg+= "For each of the fields, in the field id, replace  the _FIELD_MAP= with _CUSTOM_FIELD_MAP=";
        String from = XDAT.getSiteConfigPreferences().getAdminEmail();
        try {
            XDAT.getMailService().sendHtmlMessage(from, user.getEmail(), "IMP: " + TurbineUtils.GetSystemName() + " custom variable migration needs attention", msg);
        }catch(Exception e) {
            log.error("Could not notify user. Wanted to send " + msg, e);
        }
    }

    private void notify(final UserI privilegedUser, final UserI user, final String dataType, final String projectStr, final Hashtable<String, Integer> migrationDetails, final String formUUIDStr, final String fieldDefinitionId) {
        StringBuilder msgBuilder = new StringBuilder(String.format("Custom Variable definition %s  for the %s in following %s was migrated to Custom Form %s", fieldDefinitionId, dataType, projectStr, formUUIDStr));
        msgBuilder.append("<br>");

        Set<String> projectIds = migrationDetails.keySet();
        List<String> projectOwnerEmailIds = getProjectOwnerEmails(privilegedUser, projectIds);
        for (String p: projectIds) {
            msgBuilder.append(String.format("<br>  ID: %s Total %s migrated %s", p, dataType, migrationDetails.get(p)));
        }
        msgBuilder.append("<br><br>").append("All the field names generated in the custom form are identical to the field names in the migrated custom variable definition");
        msgBuilder.append("<br>");
        String msg = msgBuilder.toString();
        String from = XDAT.getSiteConfigPreferences().getAdminEmail();
        try {
            XDAT.getMailService().sendHtmlMessage(from, new String[]{user.getEmail()}, projectOwnerEmailIds.stream().toArray(String[] ::new), "IMP: " + TurbineUtils.GetSystemName() + " custom variable migration complete", msg);
        }catch(Exception me) {
            log.error("Could not notify user. Wanted to send " + msg, me);
        }
    }

    private List<String> getProjectOwnerEmails(final UserI user, final Set<String> projectIds) {
        List<String> emails = new ArrayList<String>();
        for (String p: projectIds) {
            List<String> pOwnerEmails = getProjectOwnerEmails(user, p);
            if (pOwnerEmails != null) {
                emails.addAll(pOwnerEmails);
            }else {
                log.info("Could not fetch owner email ids for " + p);
            }
        }
        return emails;
    }

    private List<String> getProjectOwnerEmails(final UserI user, final String projectId) {
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        List<String> emailIds  = project.getGroupMembers(Groups.OWNER_GROUP);
        return emailIds;
    }

    private boolean storedSearchHasAlreadyMigrated(final XdatStoredSearch search, final XdatSearchField field) {
        boolean hasMigrated  = false;
        final String fieldId = field.getFieldId();
        ArrayList<XdatSearchField> searchFields = search.getSearchField();
        for (XdatSearchField f : searchFields) {
            String fId = f.getFieldId();
            String migratedId = fieldId.replace("_FIELD_MAP=", "_CUSTOM_FIELD_MAP=");
            if (fieldId != fId && fId.equals(migratedId)) {
                hasMigrated = true;
                break;
            }
        }
        return hasMigrated;
    }

    private class SearchNotMigrated {

        public SearchNotMigrated(XdatStoredSearch s, List<String> duplicateFieldNames) {
            this.search = s;
            this.duplicateFieldNames = duplicateFieldNames;
        }
        public XdatStoredSearch getSearch() {return search;}

        public List<String> getDuplicateFieldNames() {
            return duplicateFieldNames;
        }

        XdatStoredSearch search;
        List<String> duplicateFieldNames = new ArrayList<String>();
    }


    private final String QUERY="select f.xnat_fielddefinitiongroup_id,  f.id, f.description,f.shareable, f.project_specific, df.xnat_datatypeprotocol_fieldgroups_id as dpf_id, a.data_type,  a.xnat_projectdata_id from xnat_projectdata p"
            +  " inner join xnat_abstractprotocol a on a.xnat_projectdata_id=p.id"
            + " left join xnat_datatypeprotocol d on d.xnat_abstractprotocol_id=a.xnat_abstractprotocol_id"
            + " left  join xnat_datatypeprotocol_fieldgroups df on d.xnat_abstractprotocol_id=df.xnat_datatypeprotocol_xnat_abstractprotocol_id"
            + " left  join xnat_fielddefinitiongroup f on df.xnat_fielddefinitiongroup_xnat_fielddefinitiongroup_id = f.xnat_fielddefinitiongroup_id"
            + " where f.id != 'default'  and (f.id='%s' or f.xnat_fielddefinitiongroup_id = %s) group by a.xnat_projectdata_id, a.data_type, f.id, f.description, f.xnat_fielddefinitiongroup_id, df.xnat_datatypeprotocol_fieldgroups_id";


    private final JdbcTemplate template;
    private final CustomVariableFormService formService;
    private final CustomVariableAppliesToService customVariableAppliesToService;
    private final CustomFormManagerService customFormManagerService;
    private final CustomFormPermissionsService customFormPermissionsService;
    private final XnatUserProvider primaryAdminUserProvider;
    private final NrgEventService eventService;
    private final ExecutorService executorService;
    private final SiteConfigPreferences siteConfigPreferences;
    private final CustomFormsFeatureFlags customFormsFeatureFlags;
    private final ObjectMapper objectMapper;
    private final ObjectMapper objectMapperEscapeNonAscii;


}
