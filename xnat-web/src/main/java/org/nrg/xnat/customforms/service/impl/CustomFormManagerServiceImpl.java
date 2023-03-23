package org.nrg.xnat.customforms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.NonUniqueObjectException;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.events.CustomFormEvent;
import org.nrg.xnat.customforms.exceptions.CustomFormFetcherNotFoundException;
import org.nrg.xnat.customforms.exceptions.CustomVariableNameClashException;
import org.nrg.xnat.customforms.exceptions.InsufficientPermissionsException;
import org.nrg.xnat.customforms.interfaces.CustomFormFetcherI;
import org.nrg.xnat.customforms.interfaces.annotations.CustomFormFetcherAnnotation;
import org.nrg.xnat.customforms.pojo.ComponentPojo;
import org.nrg.xnat.customforms.pojo.SubmissionPojo;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.XnatFormsIOEnv;
import org.nrg.xnat.customforms.pojo.formio.FormAppliesToPoJo;
import org.nrg.xnat.customforms.pojo.formio.PseudoConfiguration;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.service.CustomFormManagerService;
import org.nrg.xnat.customforms.service.CustomFormPermissionsService;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormAppliesToService;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.service.DataLocateService;
import org.nrg.xnat.customforms.service.ObjectSaverService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.customforms.utils.FormsIOJsonUtils;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.nrg.xnat.features.CustomFormsFeatureFlags;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.nrg.xnat.customforms.events.CustomFormEventI.CREATE;
import static org.nrg.xnat.customforms.events.CustomFormEventI.DELETE;
import static org.nrg.xnat.customforms.events.CustomFormEventI.UPDATE;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.COMPONENTS_KEY;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.COMPONENTS_KEY_FIELD;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.COMPONENTS_TYPE_FIELD;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.COMPONENT_CONTENT_TYPE;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.CONTAINER_KEY;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.DISPLAY_KEY;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.LABEL_KEY;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.SETTINGS_KEY;
import static org.nrg.xnat.customforms.utils.CustomFormsConstants.TITLE_KEY;


@Service
@Slf4j
public class CustomFormManagerServiceImpl implements CustomFormManagerService {

    private final CustomVariableAppliesToService selectionService;
    private final CustomVariableFormService formService;
    private final CustomVariableFormAppliesToService customVariableFormAppliesToService;
    private final ObjectSaverService objectSaver;
    private final CustomFormPermissionsService customFormPermissionsService;
    private final DataLocateService dataLocateService;
    private final CustomFormsFeatureFlags featureFlags;
    private final XnatPluginBeanManager xnatPluginBeanManager;
    private final XnatUserProvider userProvider;

    private final CustomFormFetcherI formFetcher;


    @Autowired
    public CustomFormManagerServiceImpl(final CustomVariableAppliesToService selectionService,
                                        final CustomVariableFormService formService,
                                        final CustomVariableFormAppliesToService customVariableFormAppliesToService,
                                        final ObjectSaverService objectSaver,
                                        final CustomFormPermissionsService customFormPermissionsService,
                                        final DataLocateService dataLocateService,
                                        final List<CustomFormFetcherI> customFormFetchers,
                                        final CustomFormsFeatureFlags featureFlags,
                                        final XnatUserProvider userProvider,
                                        final XnatPluginBeanManager xnatPluginBeanManager) {
        this.selectionService = selectionService;
        this.formService = formService;
        this.customVariableFormAppliesToService = customVariableFormAppliesToService;
        this.objectSaver = objectSaver;
        this.customFormPermissionsService = customFormPermissionsService;
        this.dataLocateService = dataLocateService;
        this.featureFlags = featureFlags;
        this.xnatPluginBeanManager = xnatPluginBeanManager;
        this.userProvider = userProvider;

        formFetcher = getCustomFormFetcher(customFormFetchers);
    }

    /**
     * Saves form association configuration passed through ClientPojo object.
     *
     * @param data - JSON into POJO which contains multiple paths to which the form JSON is to be saved
     * @param formDefinition
     * @param user       - User
     */
    @Override
    public String save(final SubmissionPojo data, final JsonNode formDefinition, final UserI user) throws InsufficientPermissionsException {
        final String problem = validateSavePermission(data, user);
        if (StringUtils.isNotBlank(problem)) {
            throw new InsufficientPermissionsException(problem);
        }

        String formId = null;
        String datatype = data.getXnatDatatype().getValue();
        String siteWideStr = data.getIsThisASiteWideConfiguration();
        String existingFormPrinaryKey = data.getIdCustomVariableFormAppliesTo();
        int zIndex = data.getzIndex();
        RowIdentifier rowId = RowIdentifier.Unmarshall(existingFormPrinaryKey);
        if (null == rowId) {
            throw new IllegalArgumentException("Incorrect row id received");
        }
        boolean isSiteWideForm = siteWideStr.equalsIgnoreCase(CustomFormsConstants.IS_SITEWIDE_YES);
        List<ComponentPojo> protocols = data.getXnatProtocol();
        List<ComponentPojo> visits = data.getXnatVisit();
        List<ComponentPojo> subTypes = data.getXnatSubtype();
        List<ComponentPojo> projects = data.getXnatProject();

        if (protocols == null) {
            protocols = new ArrayList<>();
        }
        if (visits == null) {
            visits = new ArrayList<>();
        }
        if (subTypes == null) {
            subTypes = new ArrayList<>();
        }
        if (protocols.size() > 0) {
            //Is a protocol specific customForm for the datatype
            if (subTypes.size() > 0) {
                for (ComponentPojo s : subTypes) {
                    //Encoded as: PROTOCOL_NAME : VISIT_ID : SUBTYPE_NAME
                    String encodedSubtype = s.getValue();
                    String[] tokens = encodedSubtype.split(CustomFormsConstants.DELIMITER);
                    String protocol = tokens[0];
                    String visit_name = tokens[1];
                    String subType = tokens[2];
                    UserOptionsPojo userOptionsPojo = new UserOptionsPojo(datatype, protocol, visit_name, subType);
                    userOptionsPojo.setZIndex(zIndex);
                    if (isSiteWideForm) {
                        //Save for the site:  datatype, protocol, visit, subtype
                        formId = save(user, userOptionsPojo, null, formDefinition, rowId);
                    } else {
                        //Is for specific projects
                        if (null != projects && projects.size() > 0) {
                            for (ComponentPojo proj : projects) {
                                //Encoded as: PROTOCOL_NAME : PROJECT_ID
                                String encodedProject = proj.getValue();
                                tokens = encodedProject.split(CustomFormsConstants.DELIMITER);
                                String protocolName = tokens[0];
                                String projectId = tokens[1];
                                if (protocolName.equals(protocol)) {
                                    //Save for this datatype, project, protocol, visit, subtype
                                    formId = save(user, userOptionsPojo, projects, formDefinition, rowId);
                                }
                            }
                        }
                    }
                }
            } else {
                //Only Protocol and Visit
                if (visits.size() > 0) {
                    for (ComponentPojo v : visits) {
                        //PROTOCOL_NAME:VISIT_ID
                        String encodedVisit = v.getValue();
                        String[] tokens = encodedVisit.split(CustomFormsConstants.DELIMITER);
                        String protocol = tokens[0];
                        String visitName = tokens[1];
                        UserOptionsPojo userOptionsPojo = new UserOptionsPojo(datatype, protocol, visitName, null);
                        userOptionsPojo.setZIndex(zIndex);
                        if (isSiteWideForm) {
                            //Save for the site:  datatype, protocol, visit
                            formId = save(user, userOptionsPojo, null, formDefinition, rowId);
                        } else {
                            //Is for specific projects
                            if (null != projects && projects.size() > 0) {
                                for (ComponentPojo proj : projects) {
                                    //Encoded as: PROTOCOL_NAME : PROJECT_ID
                                    String encodedProject = proj.getValue();
                                    tokens = encodedProject.split(CustomFormsConstants.DELIMITER);
                                    String protocolName = tokens[0];
                                    String projectId = tokens[1];
                                    if (protocolName.equals(protocol)) {
                                        //Save for this datatype, project, protocol, visit
                                        formId = save(user, userOptionsPojo, projects, formDefinition, rowId);
                                    }

                                }
                            }
                        }
                    }
                } else {
                    //Only Protocol provided - all visits under this datatype use the same form
                    for (ComponentPojo p : protocols) {
                        //This has the protocolId and we will be using the protocolname
                        //String protocol = p.getValue();
                        String protocol = p.getLabel();
                        UserOptionsPojo userOptionsPojo = new UserOptionsPojo(datatype, protocol, null, null);
                        userOptionsPojo.setZIndex(zIndex);
                        if (isSiteWideForm) {
                            //Save for the site:  datatype, protocol, visit
                            formId = save(user, userOptionsPojo, null, formDefinition, rowId);
                        } else {
                            if (null != projects && projects.size() > 0) {
                                for (ComponentPojo proj : projects) {
                                    //Encoded as: PROTOCOL_NAME : PROJECT_ID
                                    String encodedProject = proj.getValue();
                                    String[] tokens = encodedProject.split(CustomFormsConstants.DELIMITER);
                                    String protocolName = tokens[0];
                                    String projectId = tokens[1];
                                    if (protocolName.equals(protocol)) {
                                        //Save for this datatype, project, protocol, visit
                                        formId = save(user, userOptionsPojo, projects, formDefinition, rowId);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } else {
            //Is a datatype specific customForm
            UserOptionsPojo userOptionsPojo = new UserOptionsPojo(datatype, null, null, null);
            userOptionsPojo.setZIndex(zIndex);
            formId = save(user, userOptionsPojo, projects, formDefinition, rowId);
        }
        return formId;
    }

    /**
     * Check if user has permission to save form associations
     * <p>
     * User can save any form if they are a site admin or a form manager.
     * Otherwise, they can only save forms on projects they own.
     *
     * @param submission Form submission to save
     * @param user The user attempting to save
     * @return A string explaining the problem, if there is one.
     *         A null value means there is no problem and the user has permission to save.
     */
    private String validateSavePermission(final SubmissionPojo submission, final UserI user) {
        // Check permissions. Is the user allowed to do what they intend to do?
        if (Roles.isSiteAdmin(user.getUsername()) || Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE)) {
            // User is an admin or form manager, they can do whatever they want. No problems here.
            return null;
        }

        // User is not an admin or form manager.

        if (!featureFlags.isProjectOwnerFormCreationEnabled()) {
            return "User cannot create a project form";
        }

        if (submission.getIsThisASiteWideConfiguration().equalsIgnoreCase(CustomFormsConstants.IS_SITEWIDE_YES)) {
            // Only admins or form managers are allowed to add a site-wide form.
            return "User cannot create a site wide form";
        }

        // Check that user is an owner of all projects they want to add the form to
        final List<String> notOwnerProjects = submission
                .getXnatProject()
                .stream()
                .map(ComponentPojo::getValue)
                .filter(projectId -> !Permissions.isProjectOwner(user, projectId))
                .collect(Collectors.toList());
        if (notOwnerProjects.isEmpty()) {
            return null;
        }

        final boolean singular = notOwnerProjects.size() == 1;
        return "User cannot create a project form in project" +
                (singular ? " " : "s ") +
                String.join(", ", notOwnerProjects);
    }

    private CustomFormFetcherI getCustomFormFetcher(final List<CustomFormFetcherI> formFetchers) {
        final Map<String, CustomFormFetcherI> formFetchersByAnnotation = formFetchers.stream()
                .map(fetcher -> {
                    final CustomFormFetcherAnnotation annotation = fetcher.getClass().getAnnotation(CustomFormFetcherAnnotation.class);
                    return annotation == null || annotation.type() == null ? null : Pair.of(annotation.type(), fetcher);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        // If we have a protocol-aware fetcher use that, otherwise use the default fetcher.
        final CustomFormFetcherI protocolAware = formFetchersByAnnotation.get(CustomFormsConstants.PROTOCOL_PLUGIN_AWARE);
        return protocolAware != null ? protocolAware : formFetchersByAnnotation.get(CustomFormsConstants.PROTOCOL_UNAWARE);
    }

    /**
     * Enables a form
     * @param user - the user requesting
     * @param formAppliesToId - the row identifier for the join table
     * @return - boolean - true if succeeds
     * @throws InsufficientPermissionsException
     */
    @Override public boolean enableForm(final UserI user, final String formAppliesToId) throws InsufficientPermissionsException {
        boolean enabled = false;
        try {
            RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesToId);
            CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
            if (StringUtils.equals(CustomFormsConstants.OPTED_OUT_STATUS_STRING, customVariableFormAppliesTo.getStatus())) {
                return true;
            }
            if (customVariableFormAppliesTo != null && customFormPermissionsService.isUserAuthorized(user, customVariableFormAppliesTo)) {
                customVariableFormAppliesTo.setStatus(CustomFormsConstants.ENABLED_STATUS_STRING);
                objectSaver.saveCustomVariableFormAppliesTo(customVariableFormAppliesTo);
                createWorkFlowEntry(user, customVariableFormAppliesTo, rowId.getFormId(), "Form Enabled");
                enabled = true;
                triggerEvent(customVariableFormAppliesTo.getCustomVariableForm(),
                        customVariableFormAppliesTo.getCustomVariableAppliesTo().getDataType(), UPDATE);
            }
        } catch (Exception e) {
            if (e instanceof InsufficientPermissionsException) {
                throw e;
            }
        }
        return enabled;
    }


    /**
     * Disables a form
     * @param user - the user requesting
     * @param formAppliesId - the row identifier for the join table
     * @return boolean - success status
     * @throws Exception
     */

    @Override public boolean disableForm(final UserI user, final String formAppliesId) throws Exception {
        boolean disabled = false;
        try {
            RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesId);
            CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
            if (StringUtils.equals(CustomFormsConstants.OPTED_OUT_STATUS_STRING, customVariableFormAppliesTo.getStatus())) {
                return true;
            }
            if (customVariableFormAppliesTo != null && customFormPermissionsService.isUserAuthorized(user, customVariableFormAppliesTo)) {
                customVariableFormAppliesTo.setStatus(CustomFormsConstants.DISABLED_STATUS_STRING);
                objectSaver.saveCustomVariableFormAppliesTo(customVariableFormAppliesTo);
                createWorkFlowEntry(user, customVariableFormAppliesTo, rowId.getFormId(), "Form disabled");
                triggerEvent(customVariableFormAppliesTo.getCustomVariableForm(),
                        customVariableFormAppliesTo.getCustomVariableAppliesTo().getDataType(), UPDATE);
                disabled = true;
            }
        } catch (Exception e) {
            log.error("Disable request for " + formAppliesId + " encountered exception", e);
            throw e;
        }
        return disabled;
    }

    private void triggerEvent(final CustomVariableForm form, final String dataType, final String status) {
        UUID formUUID = form.getFormUuid();
        if (null != formUUID) {
            final CustomFormEvent.Builder builder = CustomFormEvent.builder()
                    .xsiType(dataType)
                    .uuid(formUUID.toString())
                    .action(status);
            XDAT.triggerEvent(builder.build());
        }

    }

    /**
     * Modifies display order of  a form
     * @param user - the user requesting
     * @param formIdStr - the form Id for which the display order is to be modified
     * @return boolean - success status
     * @throws Exception
     */

    @Override public boolean modifyDisplayOrder(final UserI user, final Integer displayOrder, final String formIdStr) throws Exception {
        boolean modified = false;
        try {
            CustomVariableForm form = formService.findByUuid(UUID.fromString(formIdStr));
            if (form != null && form.getCustomVariableFormAppliesTos().size() > 0) {
                boolean isAuthorized = false;
                if (customFormPermissionsService.isUserAdminOrDataManager(user)) {
                    isAuthorized = true;
                }else {
                    CustomVariableAppliesTo appliesTo = form.getCustomVariableFormAppliesTos().get(0).getCustomVariableAppliesTo();
                    if (appliesTo.getScope().equals(Scope.Project)) {
                        String projId = appliesTo.getEntityId();
                        if (customFormPermissionsService.isUserProjectOwner(user, projId)) {
                            isAuthorized = true;
                        }
                    }
                }
                if (isAuthorized) {
                    form.setzIndex(displayOrder);
                    formService.saveOrUpdate(form);
                    createWorkFlowEntry(user, form.getCustomVariableFormAppliesTos().get(0), form.getId(), "Form display order modified");
                    modified = true;
                }else {
                    throw new InsufficientPermissionsException("User not authorized");
                }
            }
        } catch (Exception e) {
            log.error("Display ordermodification request for " + formIdStr + " encountered exception", e);
            throw e;
        }
        return modified;
    }


    /**
     * Delete a form. If there is any entity which has used the form to save data, the form is only disabled
     * @param user - the user who requests
     * @param formAppliesId  - the row identifier for the join table
     * @return String - the status - Delete or Disabled
     * @throws Exception
     */

    @Override public String deleteForm(final UserI user, final String formAppliesId) throws Exception {
        String status = null;
        try {
            RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesId);
            CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
            if (null !=customVariableFormAppliesTo && customFormPermissionsService.isUserAuthorized(user, customVariableFormAppliesTo)) {
                //If data does not exist, delete the form.
                //Else disable the form
                boolean dataHasBeenStoredForTheForm = dataLocateService.hasDataBeenAcquired(customVariableFormAppliesTo);
                if (dataHasBeenStoredForTheForm) {
                    customVariableFormAppliesTo.setStatus(CustomFormsConstants.DISABLED_STATUS_STRING);
                    objectSaver.saveCustomVariableFormAppliesTo(customVariableFormAppliesTo);
                    status = CustomFormsConstants.DISABLED_STATUS_STRING;
                    triggerEvent(customVariableFormAppliesTo.getCustomVariableForm(),
                            customVariableFormAppliesTo.getCustomVariableAppliesTo().getDataType(), UPDATE);
                } else {
                    deleteSafely(rowId, customVariableFormAppliesTo);
                    deleteForm(rowId);
                    status = CustomFormsConstants.DELETED;
                    //Delete all project entries which have opted out of this form
                    deleteOptedOutSafely(rowId);
                    triggerEvent(customVariableFormAppliesTo.getCustomVariableForm(),
                            customVariableFormAppliesTo.getCustomVariableAppliesTo().getDataType(), DELETE);
                }
                createWorkFlowEntry(user, customVariableFormAppliesTo, rowId.getFormId(), String.format("Form %s STATUS: %s",rowId.getFormId(), status));
            }
        } catch (Exception e) {
            log.error("Disable request for " + formAppliesId + " encountered exception", e);
            throw e;
        }
        return status;
    }


    /**
     * Add projects to a form
     * @param user - the user who requests
     * @param rowIdentifier - the row identifier for the join table
     * @param projects - list of project ids that need to be added
     * @return - boolean - success status
     * @throws Exception
     */
    @Override public boolean optProjectsIntoForm(final UserI user, final RowIdentifier rowIdentifier, final List<String> projects) throws IllegalArgumentException {
        CustomVariableFormAppliesTo formAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowIdentifier);
        boolean savedAll = true;
        if (formAppliesTo == null) {
            throw new IllegalArgumentException("Form with id: " + rowIdentifier + " not found");
        }
        final String formStatus = formAppliesTo.getStatus();
        //Custom Form Manager may not have access to the project
        final UserI authorizedUser = getAuthorizedUser(user);
        for (String project : projects) {
            XnatProjectdata projectdata = AutoXnatProjectdata.getXnatProjectdatasById(project, authorizedUser, false);
            if (projectdata == null) {
                throw new IllegalArgumentException("All projects not found");
            }
        }
        final String dataType = formAppliesTo.getCustomVariableAppliesTo().getDataType();
        final String protocol = formAppliesTo.getCustomVariableAppliesTo().getProtocol();
        final String visit = formAppliesTo.getCustomVariableAppliesTo().getVisit();
        final String subType = formAppliesTo.getCustomVariableAppliesTo().getSubType();
        UserOptionsPojo userOptionsPojo = new UserOptionsPojo(dataType, protocol, visit, subType);
        for (String project : projects) {
            CustomVariableForm form = formAppliesTo.getCustomVariableForm();
            List<CustomVariableFormAppliesTo> overlappingFormAppliesTos = customVariableFormAppliesToService.findByFormId(form.getId()).stream()
                    .filter(apTo -> Objects.nonNull(apTo.getCustomVariableAppliesTo().getEntityId()))
                    .filter(apTo -> apTo.getCustomVariableAppliesTo().getEntityId().equals(project))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(overlappingFormAppliesTos)) {
                CustomVariableFormAppliesTo overlappingAppliesTo = overlappingFormAppliesTos.get(0);
                deleteSafely(overlappingAppliesTo.getRowIdentifier(), overlappingAppliesTo);
            } else {
                CustomVariableAppliesTo customVariableAppliesTo = getCustomVariableAppliesTo(userOptionsPojo, project);
                objectSaver.saveOnlyAppliesToAndAssign(customVariableAppliesTo, formAppliesTo.getCustomVariableForm(), user, formStatus);
                savedAll = true;
            }
            createWorkFlowEntry(user, project, rowIdentifier.getFormId(), "Form Added");
        }
        return savedAll;
    }


    /**
     * Promote a form from a project wide status to a site wide status
     * @param user - the user who requests the operation
     * @param formAppliesToPoJos - list of the row identifier for the join table
     * @return - boolean - success status of the promote operation
     * @throws NotFoundException
     */
    @Override public boolean promoteForm(final UserI user, List<FormAppliesToPoJo> formAppliesToPoJos) throws NotFoundException, CustomVariableNameClashException {
        if (formAppliesToPoJos != null && formAppliesToPoJos.size() == 1) {
            return promoteForm(user, formAppliesToPoJos.get(0).getIdCustomVariableFormAppliesTo());
        }
        int first = 1;
        Long formId = null;
        boolean sameFormIdCheckFailed = false;
        for (FormAppliesToPoJo formAppliesToPoJo : formAppliesToPoJos) {
            RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesToPoJo.getIdCustomVariableFormAppliesTo());
            if (first == 1) {
                formId = rowId.getFormId();
                first = 0;
            } else {
                if (rowId.getFormId() != formId) {
                    sameFormIdCheckFailed = true;
                }
            }
            if (sameFormIdCheckFailed) {
                break;
            }
        }
        if (sameFormIdCheckFailed) {
            return false;
        } else {
            first = 1;
            boolean firstPromoted = false;
            for (FormAppliesToPoJo formAppliesToPoJo : formAppliesToPoJos) {
                if (first == 1) {
                    firstPromoted = promoteForm(user, formAppliesToPoJo.getIdCustomVariableFormAppliesTo());
                    first = 0;
                } else {
                    if (firstPromoted) {
                        //Now just delete all the other form associations
                        RowIdentifier rowIdentifier = RowIdentifier.Unmarshall(formAppliesToPoJo.getIdCustomVariableFormAppliesTo());
                        CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowIdentifier);
                        if (customVariableFormAppliesTo != null) {
                            createWorkFlowEntry(user, customVariableFormAppliesTo, rowIdentifier.getFormId(),"Form promoted and detached " );
                            deleteSafely(rowIdentifier, customVariableFormAppliesTo);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Promte a form
     * @param user - user who requests
     * @param formAppliesToId - the row identifier for the join table
     * @return boolean - success status
     * @throws NotFoundException
     */

    @Override public boolean promoteForm(final UserI user, final String formAppliesToId) throws NotFoundException, CustomVariableNameClashException {
        boolean promoted = false;
        RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesToId);
        CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
        if (customVariableFormAppliesTo != null) {
            List<CustomVariableFormAppliesTo> formsToCheckForClash = new ArrayList<CustomVariableFormAppliesTo>();
            UserOptionsPojo userOptionsPojo = UserOptionsPojo.Get(customVariableFormAppliesTo.getCustomVariableAppliesTo());
            List<CustomVariableFormAppliesTo> siteForms = customVariableFormAppliesToService.findAllFormsByExclusion(userOptionsPojo, rowId.getFormId());
            if (siteForms != null) {
                formsToCheckForClash.addAll(siteForms);
            }
            JsonNode proposed = customVariableFormAppliesTo.getCustomVariableForm().getFormIOJsonDefinition();
            checkDefinitionsForClashes(formsToCheckForClash, proposed);
            CustomVariableAppliesTo appliesTo = customVariableFormAppliesTo.getCustomVariableAppliesTo();
            if (appliesTo.getEntityId() != null) {
                //Replace the Scope from project to Site
                appliesTo = customVariableFormAppliesTo.getCustomVariableAppliesTo();
                appliesTo.setScope(Scope.Site);
                appliesTo.setEntityId(null);
                objectSaver.saveCustomVariableAppliesTo(appliesTo);
                createWorkFlowEntry(user, customVariableFormAppliesTo, rowId.getFormId(),"Form promoted  " );
                promoted = true;
            }
        } else {
            throw new NotFoundException("Form with id" + formAppliesToId + " not found");
        }
        return promoted;
    }

    /**
     * Opt out of a form
     * @param user - user who requests
     * @param formAppliesToId - the row identifier for the join table
     * @param projectIds - List of project ids that are opting out
     * @return - boolean - success status
     * @throws InsufficientPermissionsException
     * @throws IllegalArgumentException
     */

    @Override public boolean optOutOfForm(final UserI user, final String formAppliesToId, final List<String> projectIds) throws InsufficientPermissionsException, IllegalArgumentException {
        boolean successStatus = false;
        try {
            RowIdentifier rowId = RowIdentifier.Unmarshall(formAppliesToId);
            long formId = rowId.getFormId();
            boolean userAuthorized = true;
            final UserI authorizedUser = getAuthorizedUser(user);
            for (String projectId : projectIds ) {
                XnatProjectdata projectdata = AutoXnatProjectdata.getXnatProjectdatasById(projectId, authorizedUser, false);
                if (null == projectdata) {
                  throw new InsufficientPermissionsException("Invalid project");
                }
                userAuthorized = userAuthorized && customFormPermissionsService.isUserAdminOrDataManager(user) || customFormPermissionsService.isUserProjectOwner(user, projectdata.getId());
            }
            if (userAuthorized) {
                    CustomVariableFormAppliesTo customVariableFormAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
                    //Is the form already associated with the project? If so, just change the status
                    if (null != customVariableFormAppliesTo) {
                        CustomVariableAppliesTo appliesTo = customVariableFormAppliesTo.getCustomVariableAppliesTo();
                        if (null != appliesTo) {
                            if (appliesTo.getScope().equals(Scope.Site)) {
                                //Look for any project specific form with the same formId
                                for (String projectId : projectIds ) {
                                    CustomVariableFormAppliesTo formAppliesToProject = customVariableFormAppliesToService.findForProject(projectId, formId);
                                    if (formAppliesToProject != null) {
                                        if (formAppliesToProject.getStatus().equals(CustomFormsConstants.OPTED_OUT_STATUS_STRING)) {
                                            continue;
                                        }
                                        formAppliesToProject.setStatus(CustomFormsConstants.OPTED_OUT_STATUS_STRING);
                                        objectSaver.saveCustomVariableFormAppliesTo(formAppliesToProject);
                                        createWorkFlowEntry(user, customVariableFormAppliesTo, formId,"Form opted out" );
                                        continue;
                                    }else {
                                        List<CustomVariableAppliesTo> projectAppliesTos =  selectionService.filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                                                Scope.Project, projectId, appliesTo.getDataType(), appliesTo.getProtocol(),
                                                appliesTo.getVisit(), appliesTo.getSubType(),null
                                        );
                                        if (projectAppliesTos != null && projectAppliesTos.size() > 0) {
                                            CustomVariableAppliesTo projectAppliesTo = projectAppliesTos.get(0);
                                            createNew(formId, user, projectAppliesTo, CustomFormsConstants.OPTED_OUT_STATUS_STRING);
                                        }else {
                                            //Create a new formSelection
                                            createNew(formId, user, UserOptionsPojo.Get(appliesTo), projectId, CustomFormsConstants.OPTED_OUT_STATUS_STRING);
                                        }
                                        createWorkFlowEntry(user, projectId, formId,"Form opted out" );
                                    }
                                }
                                return true;
                            } else {
                                // A form that is shared between projects
                                //Just remove the association between the form and the project
                                //Be safe - get the correct association
                                List<CustomVariableAppliesTo> projectAppliesTos =  selectionService.filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                                        Scope.Project, null, appliesTo.getDataType(), appliesTo.getProtocol(),
                                        appliesTo.getVisit(), appliesTo.getSubType(),null
                                );
                                //If the form is associated to only one project. OptOut is not allowed
                                if (projectAppliesTos != null && projectAppliesTos.size() == 1) {
                                    throw new IllegalArgumentException("Opt Out operation is not allowed for a form associated with single project");
                                }
                                UserOptionsPojo optionsPojo = UserOptionsPojo.Get(appliesTo);
                                List<CustomVariableFormAppliesTo> customVariableFormAppliesTos = customVariableFormAppliesToService.findAllSpecificProjectForm(optionsPojo, projectIds, formId);
                                if (null != customVariableFormAppliesTos) {
                                    for (CustomVariableFormAppliesTo c: customVariableFormAppliesTos) {
                                        deleteSafely(c.getRowIdentifier(), c);
                                        createWorkFlowEntry(user, c, formId,"Form opted out and detached" );
                                    }
                                    setFormAsSiteWideDisabled(user,formId, optionsPojo);
                                    return true;
                                }
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Form identified by " + formAppliesToId + " not found");
                    }
                } else {
                    throw new InsufficientPermissionsException("User " + user.getUsername() + " does not have sufficient permissions to perform the Opt Out operation");
                }

        } catch (Exception e) {
            log.error("Could not find the form to opt out of", e);
            throw e;
        }
        return successStatus;
    }

    private void setFormAsSiteWideDisabled(final UserI user, final long formId, final UserOptionsPojo userOptionsPojo) {
        //Apply the form Site Wide and set its status to disabled
        List<CustomVariableFormAppliesTo> forms = customVariableFormAppliesToService.findByFormId(formId);
        if (forms == null || forms.size() < 1) {
            //Create a site wide form
            createNew(formId, user, userOptionsPojo, null, CustomFormsConstants.DISABLED_STATUS_STRING);
            EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Site wide custom Form Associated for " + userOptionsPojo.getDataType() + " DISABLED", "");
            try {
                final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildAdminWorkflow(user, "custom_form", Long.toString(formId), eventDetails);
                final EventMetaI eventInfo = workflow.buildEvent();
                WorkflowUtils.complete(workflow, eventInfo);
            }catch(Exception e) {
                log.error("Could not save workflow for custom form creation at the site level" , e);
            }
        }

    }

    private void deleteSafely(final RowIdentifier rowId, final CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        try {
            customVariableFormAppliesToService.delete(customVariableFormAppliesTo);
        } catch (NonUniqueObjectException e) {
            formService.evict(customVariableFormAppliesTo.getCustomVariableForm());
            selectionService.evict(customVariableFormAppliesTo.getCustomVariableAppliesTo());
            customVariableFormAppliesToService.delete(customVariableFormAppliesTo);
        }
        //Is there any other form associated with the appliesTo?
        List<CustomVariableFormAppliesTo> appliesTos = customVariableFormAppliesToService.findByAppliesToId(rowId.getAppliesToId());
        if (appliesTos == null || appliesTos.size() < 1) {
            selectionService.delete(rowId.getAppliesToId());
        }
    }

    private void deleteForm(final RowIdentifier rowId) {
        List<CustomVariableFormAppliesTo> forms = customVariableFormAppliesToService.findByFormId(rowId.getFormId());
        if (forms == null || forms.size() < 1) {
            formService.delete(rowId.getFormId());
        }
    }


    private void deleteOptedOutSafely(final RowIdentifier rowId) {
        List<CustomVariableFormAppliesTo> optedOutForms = customVariableFormAppliesToService.findOptedOutByFormId(rowId.getFormId());
        if (optedOutForms != null && optedOutForms.size() > 0) {
            for (CustomVariableFormAppliesTo optedOut : optedOutForms) {
                RowIdentifier optedOutRow = optedOut.getRowIdentifier();
                deleteSafely(optedOutRow, optedOut);
            }
        }
    }

    /**
     * Save the form
     * @param user                   - user who requests to save the form
     * @param userOptionsPojo        - the options selected by the user in the UI
     * @param entityIds              - the list of project ids
     * @param formDefinition               - the JSON representation of the form
     * @param existingFormPrimaryKey - the primary key of the form in case its an edit of a form
     */
    @Override
    public String save(final UserI user,
                       final UserOptionsPojo userOptionsPojo,
                       final List<ComponentPojo> entityIds,
                       final JsonNode formDefinition,
                       final RowIdentifier existingFormPrimaryKey
    ) {
        if (existingFormPrimaryKey == null) {
            throw new NullPointerException("Row Identifier can not be null");
        }

        //Is it a new form?
        boolean newForm = existingFormPrimaryKey.getFormId() == -1;
        if (newForm) {
            final UUID formUUID = UUID.randomUUID();
            CustomVariableForm form = new CustomVariableForm();
            JsonNode containerizedNode = appendContainerToForm(formUUID, formDefinition);
            form.setFormIOJsonDefinition(containerizedNode);
            form.setzIndex(userOptionsPojo.getZIndex());
            form.setFormUuid(formUUID);
            form.setFormCreator(user.getUsername());
            if (entityIds != null && entityIds.size() > 0) {
                entityIds.forEach(entityId -> {
                    String projectId = toProjectId(entityId);
                    List<CustomVariableAppliesTo> customVariableAppliesTo = selectionService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                            Scope.Project, projectId, userOptionsPojo.getDataType(), userOptionsPojo.getProtocol(),
                            userOptionsPojo.getVisit(), userOptionsPojo.getSubType(), null, true);
                    if (customVariableAppliesTo != null && customVariableAppliesTo.size() > 0) {
                        CustomVariableAppliesTo projectAppliesTo = customVariableAppliesTo.get(0);
                        objectSaver.saveOnlyFormAndAssign(projectAppliesTo, form, user, CustomFormsConstants.ENABLED_STATUS_STRING);
                    } else {
                        createNew(form, user, userOptionsPojo, projectId, CustomFormsConstants.ENABLED_STATUS_STRING);
                    }
                    try {
                        EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Custom Form " + form.getFormUuid() + " created  for " + userOptionsPojo.getDataType() + " in " + projectId, "");
                        final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, "custom_form",Long.toString(form.getId()), projectId, eventDetails);
                        final EventMetaI          eventMeta = workflow.buildEvent();
                        WorkflowUtils.complete(workflow, eventMeta);
                    }catch(Exception e) {
                        log.error("Could not save workflow", e);
                    }
                });
            } else {
                List<CustomVariableAppliesTo> customVariableAppliesTo = selectionService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                        Scope.Site, null, userOptionsPojo.getDataType(), userOptionsPojo.getProtocol(),
                        userOptionsPojo.getVisit(), userOptionsPojo.getSubType(), null, true);
                if (customVariableAppliesTo != null && customVariableAppliesTo.size() > 0) {
                    CustomVariableAppliesTo projectAppliesTo = customVariableAppliesTo.get(0);
                    objectSaver.saveOnlyFormAndAssign(projectAppliesTo, form, user, CustomFormsConstants.ENABLED_STATUS_STRING);
                } else {
                    createNew(form, user, userOptionsPojo, null, CustomFormsConstants.ENABLED_STATUS_STRING);
                }
                EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Site wide custom Form created for " + userOptionsPojo.getDataType() , "");
                try {
                    final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildAdminWorkflow(user, "custom_form", Long.toString(form.getId()), eventDetails);
                    final EventMetaI eventInfo = workflow.buildEvent();
                    WorkflowUtils.complete(workflow, eventInfo);
                }catch(Exception e) {
                    log.error("Could not save workflow for custom form creation at the site level" , e);
                }
            }
            triggerEvent(form, userOptionsPojo.getDataType(), CREATE);
            return form.getFormUuid().toString();
        } else {
            //Not a new form
            CustomVariableForm form = formService.findById(existingFormPrimaryKey.getFormId());
            if (form != null) {
                form.setFormIOJsonDefinition(formDefinition);
                form.setzIndex(userOptionsPojo.getZIndex());
                if (entityIds != null && entityIds.size() > 0) {
                    entityIds.forEach(entityId -> {
                        String projectId = toProjectId(entityId);
                        List<CustomVariableAppliesTo> customVariableAppliesTo = selectionService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                                Scope.Project, projectId, userOptionsPojo.getDataType(), userOptionsPojo.getProtocol(),
                                userOptionsPojo.getVisit(), userOptionsPojo.getSubType(), null, true);
                        if (customVariableAppliesTo != null && customVariableAppliesTo.size() > 0) {
                            CustomVariableAppliesTo projectAppliesTo = customVariableAppliesTo.get(0);
                            objectSaver.saveOnlyFormAndAssign(projectAppliesTo, form, user, CustomFormsConstants.ENABLED_STATUS_STRING);
                        } else {
                            createNew(form, user, userOptionsPojo, projectId, CustomFormsConstants.ENABLED_STATUS_STRING);
                        }
                        try {
                            EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Custom Form " + form.getFormUuid().toString() + " edited  for " + userOptionsPojo.getDataType() + " in " + projectId, "");
                            final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, "custom_form",Long.toString(form.getId()), projectId, eventDetails);
                            final EventMetaI          eventMeta = workflow.buildEvent();
                            WorkflowUtils.complete(workflow, eventMeta);
                        }catch(Exception e) {
                            log.error("Could not save workflow", e);
                        }
                    });
                } else {
                    List<CustomVariableAppliesTo> customVariableAppliesTo = selectionService.filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(
                            Scope.Site, null, userOptionsPojo.getDataType(), userOptionsPojo.getProtocol(),
                            userOptionsPojo.getVisit(), userOptionsPojo.getSubType(), null, true);
                    if (customVariableAppliesTo != null && customVariableAppliesTo.size() > 0) {
                        CustomVariableAppliesTo appliesTo = customVariableAppliesTo.get(0);
                        objectSaver.saveOnlyFormAndAssign(appliesTo, form, user, CustomFormsConstants.ENABLED_STATUS_STRING);
                    } else {
                        createNew(form, user, userOptionsPojo, null, CustomFormsConstants.ENABLED_STATUS_STRING);
                    }
                    EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, "Site wide custom Form " + form.getFormUuid().toString() +" edited. Associated with " + userOptionsPojo.getDataType() , "");
                    try {
                        final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildAdminWorkflow(user, "custom_form", Long.toString(form.getId()), eventDetails);
                        final EventMetaI eventInfo = workflow.buildEvent();
                        WorkflowUtils.complete(workflow, eventInfo);
                    }catch(Exception e) {
                        log.error("Could not save workflow for custom form creation at the site level" , e);
                    }
                }
                triggerEvent(form, userOptionsPojo.getDataType(), UPDATE);
                return form.getFormUuid().toString();
            }
            return null;
        }
    }

    /**
     * Get all configured custom forms for a project which are Enabled.
     *
     * @param projectId - the project id
     * @return - matched rows as serialized PseudoConfiguration
     */
    public List<PseudoConfiguration> getAllCustomForms(final String projectId) {
        List<PseudoConfiguration> configurations = new ArrayList<>();
        List<CustomVariableForm> filter = new ArrayList<CustomVariableForm>();

        List<CustomVariableForm> customVariableForms = formService.getAllEagerly();
        if (null != customVariableForms) {
            for (CustomVariableForm form : customVariableForms) {
                List<CustomVariableFormAppliesTo> formAppliesTos = form.getCustomVariableFormAppliesTos();
                if (!formAppliesTos.isEmpty()) {
                    PseudoConfiguration configuration = setBasicElements(form, formAppliesTos.get(0));
                    Scope scope = Scope.Project;
                    if (projectId != null) {
                        boolean projectInvolved = false;
                        List<FormAppliesToPoJo> appliesToPoJos = new ArrayList<>();
                        List<FormAppliesToPoJo> projectFormAppliesTos = new ArrayList<>();
                        for (CustomVariableFormAppliesTo formAppliesTo : formAppliesTos) {
                            CustomVariableAppliesTo appliesTo = formAppliesTo.getCustomVariableAppliesTo();
                            FormAppliesToPoJo formAppliesToPoJo = new FormAppliesToPoJo(formAppliesTo, formAppliesTo.getStatus());
                            if (appliesTo.getScope().equals(Scope.Site)) {
                                formAppliesToPoJo.setEntityId("Site");
                                appliesToPoJos.add(formAppliesToPoJo);
                                projectInvolved = true;
                                scope = Scope.Site;
                            } else if (appliesTo.getScope().equals(Scope.Project) && appliesTo.getEntityId().equals(projectId)) {
                                projectFormAppliesTos.add(formAppliesToPoJo);
                                projectInvolved = true;
                            }
                        }
                        if (projectInvolved == false) {
                            continue;
                        }
                        appliesToPoJos.addAll(projectFormAppliesTos);
                        configuration.setScope(scope);
                        configuration.setAppliesToList(appliesToPoJos);
                    } else{
                        List<FormAppliesToPoJo> appliesToPoJos = new ArrayList<>();
                        List<FormAppliesToPoJo> projectFormAppliesTos = new ArrayList<>();
                        for (CustomVariableFormAppliesTo formAppliesTo : formAppliesTos) {
                            FormAppliesToPoJo formAppliesToPoJo = new FormAppliesToPoJo(formAppliesTo, formAppliesTo.getStatus());
                            if (formAppliesTo.getCustomVariableAppliesTo().getScope().equals(Scope.Site)) {
                                scope = Scope.Site;
                                formAppliesToPoJo.setEntityId("Site");
                                appliesToPoJos.add(formAppliesToPoJo);
                            } else {
                                projectFormAppliesTos.add(formAppliesToPoJo);
                            }
                        }
                        appliesToPoJos.addAll(projectFormAppliesTos);
                        configuration.setAppliesToList(appliesToPoJos);
                        configuration.setScope(scope);
                    }
                    configurations.add(configuration);
                }
            }
        }
        return configurations;
    }

    private PseudoConfiguration setBasicElements(CustomVariableForm form, CustomVariableFormAppliesTo formAppliesTo) {
        PseudoConfiguration configuration = new PseudoConfiguration();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jNode = form.getFormIOJsonDefinition();
            String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jNode);
            configuration.setContents(pretty);
        } catch (JsonProcessingException jpe) {
            log.debug("Could not process json", jpe);
        }
        configuration.setFormUUID(form.getFormUuid().toString());
        configuration.setFormId(Long.toString(form.getId()));
        configuration.setFormDisplayOrder(form.getzIndex());
        configuration.setPath(formAppliesTo.getCustomVariableAppliesTo().pathAsString());
        configuration.setDoProjectsShareForm(formAppliesTo.doProjectsShareForm());
        configuration.setDateCreated(form.getCreated());
        configuration.setUsername(form.getFormCreator());
        configuration.setHasData(dataLocateService.hasDataBeenAcquired(formAppliesTo));
        return configuration;
    }

    /**
     * Gets the Concatenated Custom Form for a xsiType
     * @param user: User who requests the forms
     * @param xsiType: The XsiType of the data
     * @param id: The Id of the data for which form is required
     * @param projectId: The project ID to which the data belongs
     * @param visitId: The Visit Id
     * @param subtype: The SubType of the data
    * @return String: The concatenated forms; returns null if no forms exist
     */
    @Override
    @Nullable
    public String getCustomForm(final UserI user, final String xsiType, final String id, final String projectId,
                                final String visitId, final String subtype, final boolean appendPreviousNextButtons) throws Exception {
        if (formFetcher == null) {
            log.trace("No Custom Fetcher beans");
            throw new CustomFormFetcherNotFoundException("No form fetching beans", new IllegalArgumentException());
        }

        return formFetcher.getCustomForm(user, xsiType, id, projectId, visitId, subtype, appendPreviousNextButtons);
    }

    @Override public boolean checkCustomFormForData(final RowIdentifier rowId) throws Exception {
        CustomVariableFormAppliesTo formAppliesTo = customVariableFormAppliesToService.findByRowIdentifier(rowId);
        return dataLocateService.hasDataBeenAcquired(formAppliesTo);
    }

    private List<String> toProjectIds(final List<ComponentPojo> entityIds) {
        List<String> projectIds = new ArrayList<String>();
        if (entityIds == null || entityIds.isEmpty()) {
            return null;
        }
        entityIds.forEach(entityId -> {
            projectIds.add(toProjectId(entityId));
        });
        return projectIds;
    }

    private String toProjectId(final ComponentPojo entityId) {
        if (entityId == null) {
            return null;
        }
        String encodedProject = entityId.getValue();
        String[] tokens = encodedProject.split(CustomFormsConstants.DELIMITER);
        String projectId = null;
        if (tokens != null && tokens.length == 1) {
            projectId = tokens[0];
        } else {
            projectId = tokens[1];
        }
        return projectId;
    }

    private void checkDefinitionsForClashes(
            final List<CustomVariableFormAppliesTo> formAppliesTos,
            final JsonNode proposed
    ) throws CustomVariableNameClashException {
        if (formAppliesTos != null && formAppliesTos.size() > 0) {
            for (CustomVariableFormAppliesTo formAppliesTo : formAppliesTos) {
                CustomVariableForm form = formAppliesTo.getCustomVariableForm();
                if (null != form) {
                    JsonNode inDatabase = form.getFormIOJsonDefinition();
                    FormsIOJsonUtils.checkForNameClash(inDatabase, proposed);
                }
            }
        }
    }

    private void createNew(final long formId, final UserI user,
                           final UserOptionsPojo userOptions,
                           final String entityId, final String status) {
        CustomVariableAppliesTo customVariableAppliesTo = getCustomVariableAppliesTo(userOptions, entityId);
        CustomVariableForm form = formService.findById(formId);
        objectSaver.saveOnlyAppliesToAndAssign(customVariableAppliesTo, form, user, status);
    }

    private boolean createNew(final long formId, final UserI user,
                           final CustomVariableAppliesTo customVariableAppliesTo,
                           final String status) {
        CustomVariableForm form = formService.findById(formId);
        return objectSaver.saveOnlyAppliesToAndAssign(customVariableAppliesTo, form, user, status);
    }


    @Override public void createNew(final CustomVariableForm form, final UserI user,
                           final UserOptionsPojo userOptions,
                           final String entityId, final String status) {
        CustomVariableAppliesTo customVariableAppliesTo = getCustomVariableAppliesTo(userOptions, entityId);
        objectSaver.saveAll(customVariableAppliesTo, form, user, status);
    }

    @Override
    public XnatFormsIOEnv getFormsEnvironment() {
        return new XnatFormsIOEnv(
                xnatPluginBeanManager.getPluginBeans().containsKey(CustomFormsConstants.PROTOCOLS_PLUGIN_IDENTIFIER),
                featureFlags
        );
    }


    private void createWorkFlowEntry(final UserI user, final CustomVariableFormAppliesTo customVariableFormAppliesTo, final long formId, final String reason ) {
        Scope scope = customVariableFormAppliesTo.getCustomVariableAppliesTo().getScope();
        String dataType =  CustomFormsConstants.CUSTOM_FORM_DATATYPE_FOR_WRKFLOW;
        String id = Long.toString(formId);
        EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, reason, "FormId: " + formId);
        try {
            if(scope.equals(Scope.Project) ) {
                final String projectId = customVariableFormAppliesTo.getCustomVariableAppliesTo().getEntityId();
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, dataType,id, projectId, eventDetails);
                final EventMetaI          eventMeta = workflow.buildEvent();
                WorkflowUtils.complete(workflow, eventMeta);
            }else {
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.buildAdminWorkflow( user, dataType,id, eventDetails);
                final EventMetaI          eventMeta = workflow.buildEvent();
                WorkflowUtils.complete(workflow, eventMeta);
            }
        }catch(Exception e) {
            log.error("Could not save workflow for custom form creation at the site level" , e);
        }
    }

    private void createWorkFlowEntry(final UserI user, final String projectId, final long formId, final String reason ) {
        String dataType =  CustomFormsConstants.CUSTOM_FORM_DATATYPE_FOR_WRKFLOW;
        String id = Long.toString(formId);
        EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, EventUtils.MODIFY_VIA_WEB_SERVICE, reason, "FormId: " + formId);
        try {
            if(projectId != null ) {
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, dataType,id, projectId, eventDetails);
                final EventMetaI          eventMeta = workflow.buildEvent();
                WorkflowUtils.complete(workflow, eventMeta);
            }else {
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.buildAdminWorkflow( user, dataType,id, eventDetails);
                final EventMetaI          eventMeta = workflow.buildEvent();
                WorkflowUtils.complete(workflow, eventMeta);
            }
        }catch(Exception e) {
            log.error("Could not save workflow for custom form creation at the site level" , e);
        }
    }


    private CustomVariableAppliesTo getCustomVariableAppliesTo(final UserOptionsPojo userOptions,
                                                               final String entityId) {
        CustomVariableAppliesTo customVariableAppliesTo = new CustomVariableAppliesTo();
        customVariableAppliesTo.setDataType(userOptions.getDataType());
        if (null != userOptions.getProtocol()) customVariableAppliesTo.setProtocol(userOptions.getProtocol());
        if (null != userOptions.getVisit()) customVariableAppliesTo.setVisit(userOptions.getVisit());
        if (null != userOptions.getSubType()) customVariableAppliesTo.setSubType(userOptions.getSubType());
        if (null != userOptions.getScanType()) customVariableAppliesTo.setScanType(userOptions.getScanType());
        if (entityId != null) {
            customVariableAppliesTo.setScope(Scope.Project);
            customVariableAppliesTo.setEntityId(entityId);
        } else {
            customVariableAppliesTo.setScope(Scope.Site);
        }
        return customVariableAppliesTo;
    }

    private JsonNode appendContainerToForm(UUID formUUID, JsonNode formDefinition) throws NullPointerException {
        //Extract the title, display and setting from the form created
        //Add a container layer whose key is the UUID
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode containerizedParentNode = objectMapper.createObjectNode();
        JsonNode titleNode = formDefinition.path(TITLE_KEY);
        JsonNode displayType = formDefinition.path(DISPLAY_KEY);
        final String title = titleNode.asText();
        final String display = displayType.asText();
        final JsonNode settingsNode = formDefinition.path(SETTINGS_KEY);
        containerizedParentNode.put("display", display);
        containerizedParentNode.put("title", title);
        containerizedParentNode.set("settings", settingsNode);
        ArrayNode containerizedParentComponentsNode = objectMapper.createArrayNode();
        ObjectNode containerdNode = objectMapper.createObjectNode();
        containerdNode.put(COMPONENTS_KEY_FIELD, formUUID.toString());
        containerdNode.put(COMPONENTS_TYPE_FIELD, CONTAINER_KEY);
        containerdNode.put("input", true);
        containerdNode.put(LABEL_KEY, formUUID.toString());
        containerdNode.put("tableView", false);
        ArrayNode componentsArrayNode = objectMapper.createArrayNode();
        componentsArrayNode.add(getFormUUIDInfoInContainer(objectMapper, formUUID));
        JsonNode existingComponentNode = formDefinition.at("/" + COMPONENTS_KEY);
        if (existingComponentNode != null && existingComponentNode.isArray()) {
            for (final JsonNode eComp : existingComponentNode) {
                componentsArrayNode.add(eComp);
            }
        }
        containerdNode.set(COMPONENTS_KEY, componentsArrayNode);
        containerizedParentComponentsNode.add(containerdNode);
        containerizedParentNode.set(COMPONENTS_KEY, containerizedParentComponentsNode);
        return containerizedParentNode;
    }

    private UserI getAuthorizedUser(final UserI user) {
        UserI authorizedUser = user;
        try {
            if (Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE)) {
                authorizedUser = Users.getUser(userProvider.getLogin());
            }
        } catch (Exception ignored) {}
        return authorizedUser;
    }

    private JsonNode getFormUUIDInfoInContainer(final ObjectMapper objectMapper, final UUID formUUID) {
        ObjectNode formInfodNode = objectMapper.createObjectNode();
        formInfodNode.put(COMPONENTS_KEY_FIELD, COMPONENT_CONTENT_TYPE);
        String htmlText = "<p><span class=\"text-tiny\" style=\"font-family:Arial, Helvetica, sans-serif;\"><b>Form UUID:" + formUUID + "</b></span></p>";
        formInfodNode.put("html", htmlText);
        formInfodNode.put(COMPONENTS_TYPE_FIELD, COMPONENT_CONTENT_TYPE);
        formInfodNode.put("input", false);
        formInfodNode.put(LABEL_KEY, COMPONENT_CONTENT_TYPE);
        formInfodNode.put("tableView", false);
        formInfodNode.put("refreshOnChange", false);
        return formInfodNode;
    }

}
