/*
 * web: org.nrg.xnat.restlet.resources.ScriptTriggerTemplateResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.ScriptTriggerTemplateService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ScriptTriggerTemplateResource extends AutomationResource {

    private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerTemplateResource.class);

    private static final String TEMPLATE_ID = "TEMPLATE_ID";

    private final String _templateId;

    private final ScriptTriggerService _triggerService;
    private final ScriptTriggerTemplateService _templateService;

    public ScriptTriggerTemplateResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _templateId = (String) getRequest().getAttributes().get(TEMPLATE_ID);

        final UserI user = getUser();

        if (getScope() == Scope.Site) {
            if (!Roles.isSiteAdmin(user)) {
                _log.warn(getRequestContext("User " + user.getLogin() + " attempted to access forbidden script trigger template resources"));
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Only site admins can view or update script trigger templates for the entire site.");
            }
        } else {
            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if (userHelperService == null) {
                _log.error("An error occurred trying to retrieve the user helper service. Can't proceed with permissions check.");
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to process your request. Please try again or, if the problem persists, contact your system administrator.");
            }
            if (!Roles.isSiteAdmin(user) || userHelperService.isOwner(getProjectId())) {
                _log.warn(getRequestContext("User " + user.getLogin() + " attempted to access forbidden script trigger template resources"));
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Only site admins and project owners can view or update script trigger templates for the project " + getProjectId() + ".");
            }
        }

        _triggerService = XDAT.getContextService().getBean(ScriptTriggerService.class);
        _templateService = XDAT.getContextService().getBean(ScriptTriggerTemplateService.class);

        // A bit much for now, but built to handle more scopes, e.g. subjects, users, etc.
        switch (getScope()) {
            case Project:
                XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(getProjectId(), user, false);
                if (project == null) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find the project with ID or alias " + getProjectId());
                }
                break;

            case Site:
                break;
            default:
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown scope " + getScope());
        }

        if (_log.isDebugEnabled()) {
            _log.debug(getRequestContext("Servicing script request for user " + user.getLogin()));
        }
    }

    @Override
    protected String getResourceType() {
        return ScriptTriggerTemplate.class.getSimpleName();
    }

    @Override
    protected String getResourceId() {
        return _templateId;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        if (StringUtils.isBlank(_templateId)) {
            // List the available templates (that method figures out if it's site-wide or for a project.
            return listTemplates(mediaType);
        }
        // If there was a template ID, then list the triggers for that template ID.
        try {
            return listTriggers(mediaType);
        } catch (ClientException e) {
            throw new ResourceException(e.getStatus(), e.getMessage(), e);
        }
    }

    @Override
    public void handlePut() {
        try {
            if (StringUtils.isNotBlank(_templateId)) {
                putTemplate();
            } else {
                throw new ClientException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "You must specify a template ID on the REST URL to PUT a template to the server.");
            }
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public void handleDelete() {
        try {
            if (StringUtils.isBlank(_templateId)) {
                throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "You must specify a template ID on the REST URL to DELETE a template from the server.");
            }
            ScriptTriggerTemplate template = _templateService.getByName(_templateId);
            if (template == null) {
                throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "The template ID " + _templateId + " was not found on the system.");
            }
            final String templateId = template.getTemplateId();
            final String entities = Joiner.on(", ").join(template.getAssociatedEntities());
            _templateService.delete(template);
            recordAutomationEvent(templateId, entities, "Delete", ScriptTriggerTemplate.class);
        } catch (ClientException e) {
            _log.info(e.getMessage());
            getResponse().setStatus(e.getStatus(), e.getMessage());
        }
    }

    /**
     * Lists the scripts at the specified scope and entity ID.
     *
     * @return A representation of the scripts available at the specified scope and entity ID (if specified).
     */
    private Representation listTemplates(MediaType mediaType) {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("NAME");
        columns.add("DESCRIPTION");

        XFTTable table = new XFTTable();
        table.initTable(columns);

        final List<ScriptTriggerTemplate> templates = StringUtils.isBlank(getProjectId()) ? _templateService.getAll() : _templateService.getTemplatesForEntity(getProjectId());
        for (final ScriptTriggerTemplate template : templates) {
            table.insertRowItems(template.getTemplateId(), template.getDescription());
        }

        return representTable(table, mediaType, null);
    }

    private Representation listTriggers(MediaType mediaType) throws ClientException {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("NAME");
        columns.add("SCRIPT_ID");
        columns.add("DATA_TYPE");
        columns.add("EVENT");
        columns.add("DESCRIPTION");

        XFTTable table = new XFTTable();
        table.initTable(columns);

        ScriptTriggerTemplate template = _templateService.getByName(_templateId);
        if (template == null) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find a template with ID " + _templateId);
        }

        for (final ScriptTrigger trigger : template.getTriggers()) {
            table.insertRowItems(trigger.getTriggerId(), trigger.getScriptId(), trigger.getAssociation(), trigger.getEvent(), trigger.getDescription());
        }

        return representTable(table, mediaType, null);
    }

    private void putTemplate() throws ClientException {
        final Representation entity = getRequest().getEntity();
        if (entity.getSize() == 0) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to find template parameters: no data sent?");
        }

        MediaType mediaType = entity.getMediaType();
        if (!mediaType.equals(MediaType.APPLICATION_WWW_FORM) && !mediaType.equals(MediaType.APPLICATION_JSON)) {
            throw new ClientException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "This function currently only supports " + MediaType.APPLICATION_WWW_FORM + " and " + MediaType.APPLICATION_JSON);
        }

        final ScriptTriggerTemplate found;
        if (mediaType.equals(MediaType.APPLICATION_WWW_FORM)) {
            try {
                found = createObjectFromFormData(ScriptTriggerTemplate.class);
            } catch (Exception e) {
                throw new ClientException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to handle the incoming form data.", e);
            }
        } else if (entity.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            try {
                final String text = entity.getText();
                found = getSerializer().deserializeJson(text, ScriptTriggerTemplate.class);
            } catch (IOException e) {
                throw new ClientException(Status.SERVER_ERROR_INTERNAL, "An error occurred processing the script properties", e);
            }
        } else {
            throw new ClientException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "This function currently only supports the media type " + MediaType.APPLICATION_JSON);
        }
        assert found != null;
        saveTemplate(found);
    }

    private void saveTemplate(final ScriptTriggerTemplate found) {
        final ScriptTriggerTemplate existing = _templateService.getByName(found.getTemplateId());
        if (existing == null) {
            final Set<ScriptTrigger> triggers = saveTriggers(found.getTriggers());
            if (triggers != null) {
                found.setTriggers(triggers);
            }
            _templateService.create(found);
            final String entities = Joiner.on(", ").join(found.getAssociatedEntities());
            recordAutomationEvent(found.getTemplateId(), entities, "Create", ScriptTriggerTemplate.class);
        } else {
            boolean isDirty = false;
            if (!existing.getTemplateId().equals(found.getTemplateId())) {
                existing.setTemplateId(found.getTemplateId());
                isDirty = true;
            }
            if (!existing.getDescription().equals(found.getDescription())) {
                existing.setDescription(found.getDescription());
                isDirty = true;
            }
            final Set<String> currentAssociates = new TreeSet<>(existing.getAssociatedEntities());
            final Set<String> proposedAssociates = new TreeSet<>(found.getAssociatedEntities());
            if (currentAssociates.size() != proposedAssociates.size() || !currentAssociates.equals(proposedAssociates)) {
                existing.setAssociatedEntities(proposedAssociates);
                isDirty = true;
            }
            final Set<ScriptTrigger> currentTriggers = new TreeSet<>(existing.getTriggers());
            final Set<ScriptTrigger> proposedTriggers = new TreeSet<>(found.getTriggers());
            if (currentTriggers.size() != proposedTriggers.size() || !currentTriggers.equals(proposedTriggers)) {
                existing.setTriggers(proposedTriggers);
                isDirty = true;
            }
            if (isDirty) {
                final Set<ScriptTrigger> triggers = saveTriggers(existing.getTriggers());
                if (triggers != null) {
                    existing.setTriggers(triggers);
                }
                _templateService.update(existing);
                final String entities = Joiner.on(", ").join(existing.getAssociatedEntities());
                recordAutomationEvent(found.getTemplateId(), entities, "Update", ScriptTriggerTemplate.class);
            }
        }
    }

    private Set<ScriptTrigger> saveTriggers(final Set<ScriptTrigger> triggers) {
        final Set<ScriptTrigger> persisted = new HashSet<>(triggers.size());
        for (final ScriptTrigger trigger : triggers) {
            final ScriptTrigger existing = _triggerService.getByTriggerId(trigger.getTriggerId());
            if (existing == null) {
                _triggerService.create(trigger);
                persisted.add(trigger);
                recordAutomationEvent(trigger.getTriggerId(), trigger.getAssociation(), "Create", ScriptTrigger.class);
            } else if (existing.compareTo(trigger) != 0) {
                if (!existing.getTriggerId().equals(trigger.getTriggerId())) {
                    existing.setTriggerId(trigger.getTriggerId());
                }
                if (!existing.getDescription().equals(trigger.getDescription())) {
                    existing.setDescription(trigger.getDescription());
                }
                if (!existing.getScriptId().equals(trigger.getScriptId())) {
                    existing.setScriptId(trigger.getScriptId());
                }
                if (!existing.getAssociation().equals(trigger.getAssociation())) {
                    existing.setAssociation(Scope.encode(Scope.Project, trigger.getAssociation()));
                }
                if (!existing.getEvent().equals(trigger.getEvent())) {
                    existing.setEvent(trigger.getEvent());
                }
                _triggerService.update(existing);
                persisted.add(existing);
                recordAutomationEvent(existing.getTriggerId(), existing.getAssociation(), "Update", ScriptTrigger.class);
            }
        }
        return persisted.size() > 0 ? persisted : null;
    }
}
