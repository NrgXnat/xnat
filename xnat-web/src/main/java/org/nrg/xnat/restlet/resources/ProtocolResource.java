/*
 * web: org.nrg.xnat.restlet.resources.ProtocolResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.XftItemException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;

@Component
@Slf4j
public class ProtocolResource extends ItemResource {
    public ProtocolResource(final Context context, final Request request, final Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_XML));

        final String projectId = (String) getParameter(request, "PROJECT_ID");
        if (StringUtils.isBlank(projectId)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify an ID for the protocol's project.");
        }

        final UserI user = getUser();

        _project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        if (_project == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find a project with the ID " + projectId);
        }

        try {
            if (request.getMethod() != Method.GET && !Permissions.canEdit(user, _project)) {
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for this project.");
            }
        } catch (Exception e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to determine the user's permissions for project " + _project.getId());
        }

        _protocolId = (String) getParameter(request, "PROTOCOL_ID");
        _dataType = getQueryVariable("dataType");

        if (StringUtils.isAllBlank(_protocolId, _dataType)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You specify either an existing data protocol ID or the data type for the protocol to be retrieved.");
        }

        final XnatDatatypeprotocol protocolById = (XnatDatatypeprotocol) XnatAbstractprotocol.getXnatAbstractprotocolsById(_protocolId, user, true);
        if (protocolById != null) {
            _protocol = protocolById;
        } else {
            _protocol = null;
        }
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
    public Representation represent(final Variant variant) throws ResourceException {
        try {
            final XnatDatatypeprotocol protocol = ObjectUtils.defaultIfNull(_protocol, getXnatDatatypeprotocol(getUser(), _dataType));
            if (protocol == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified protocol by data type or protocol ID");
                return null;
            }
            return representItem(protocol.getItem(), overrideVariant(variant));
        } catch (Exception e) {
            log.error("An error occurred when user '{}' tried to retrieve the protocol for the '{}' data type in project '{}'", getUser().getUsername(), _dataType, _project);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to retrieve the protocol for the '" + _dataType + "' data type in project '" + _project + "'");
        }
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();

            final XnatDatatypeprotocol existing  = _protocol != null ? new XnatDatatypeprotocol(_protocol.getItem().getCurrentDBVersion()) : StringUtils.isNotBlank(_dataType) ? getXnatDatatypeprotocol(user, _dataType) : null;
            final XFTItem              submitted = loadItem(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME, true, existing != null ? existing.getItem() : null);

            if (!submitted.instanceOf(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME)) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "The submitted object is not the appropriate data type: should be " + XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME + " but is actually " + submitted.getXSIType());
                return;
            }

            final XnatDatatypeprotocol protocol = new XnatDatatypeprotocol(submitted);
            if (StringUtils.isBlank(protocol.getProject())) {
                protocol.setProperty("xnat_projectdata_id", _project.getId());
            }
            if (StringUtils.isBlank(protocol.getId())) {
                protocol.setId(_protocol == null ? protocol.getDataType() : _protocol.getId());
            }
            final String gender = getQueryVariable("gender");
            if (StringUtils.isNotBlank(gender)) {
                protocol.setProperty("xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender", gender);
            }

            final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, _project.getItem(), newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Modified event data-type protocol."));
            try {
                SaveItemHelper.authorizedSave(protocol, user, false, true, workflow.buildEvent());

                // We only need to trigger an event if something changed in how the field definitions relate to projects.
                final Changed changed = hasChanged(protocol);
                switch (changed) {
                    case ProjectSpecific:
                        // If the added groups were all project specific, we just need to update that project.
                        XDAT.triggerXftItemEvent(_project, XftItemEvent.UPDATE);
                        break;

                    case SiteWide:
                        XDAT.triggerXftItemEvent(protocol, _protocol == null ? XftItemEvent.CREATE : XftItemEvent.UPDATE);
                        break;

                    case Unchanged:
                        log.info("Something happened with the protocol {} but it doesn't seem to have changed.", StringUtils.defaultIfBlank(protocol.getDescription(), protocol.getName()));
                        break;


                }
                XDAT.triggerXftItemEvent(_project, XftItemEvent.UPDATE);
                PersistentWorkflowUtils.complete(workflow, workflow.buildEvent());
                MaterializedView.deleteByUser(user);
                returnXML(protocol.getItem());
            } catch (Exception e) {
                PersistentWorkflowUtils.fail(workflow, workflow.buildEvent());
                throw e;
            }
        } catch (SAXParseException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
            log.error("An error was detected in format for the protocol definition", e);
        } catch (ActionException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            log.error("An unknown error occurred trying to store the protocol", e);
        }
    }

    @Override
    public void handleDelete() {
        if (_protocol == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }

        final UserI user = getUser();

        try {
            final PersistentWorkflowI workflow          = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, _project.getItem(), this.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Deleted event data-type protocol."));
            final boolean             isProjectSpecific = XnatDatatypeprotocol.isProjectSpecific(_protocol);
            if (isProjectSpecific) {
                XDAT.triggerXftItemEvent(_project, XftItemEvent.UPDATE);
            } else {
                XDAT.triggerXftItemEvent(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME, _protocolId, XftItemEvent.DELETE);
            }
            try {
                SaveItemHelper.authorizedDelete(_protocol.getItem().getCurrentDBVersion(), user, workflow.buildEvent());
                PersistentWorkflowUtils.complete(workflow, workflow.buildEvent());
            } catch (Exception e) {
                PersistentWorkflowUtils.fail(workflow, workflow.buildEvent());
                throw e;
            }
            Users.clearCache(user);
            MaterializedView.deleteByUser(user);
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    @Nonnull
    private XnatDatatypeprotocol getXnatDatatypeprotocol(final UserI user, final String dataType) throws Exception {
        final XnatDatatypeprotocol existing = (XnatDatatypeprotocol) _project.getProtocolByDataType(dataType);
        if (existing != null) {
            return existing;
        }

        final ElementSecurity elementSecurity = ElementSecurity.GetElementSecurity(dataType);
        if (elementSecurity == null) {
            throw new XftItemException("Tried to get the element security instance for the data type \"" + dataType + "\" but it wasn't found. This means something's very wrong.");
        }
        final GenericWrapperElement element  = GenericWrapperElement.GetElement(dataType);
        final XnatDatatypeprotocol  protocol = new XnatDatatypeprotocol(user);
        protocol.setProperty("xnat_projectdata_id", _project.getId());
        protocol.setDataType(element.getXSIType());
        protocol.setId(_project.getId() + "_" + element.getSQLName());
        if (StringUtils.isBlank((String) protocol.getProperty("name"))) {
            protocol.setProperty("name", elementSecurity.getPluralDescription());
        }
        if (StringUtils.equals(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME, protocol.getXSIType())) {
            protocol.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/data-type", protocol.getProperty("data-type"));
            protocol.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/project-specific", "false");
        }

        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, _project.getItem(), this.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Modified event data-type protocol."));
        try {
            SaveItemHelper.authorizedSave(protocol, user, false, false, workflow.buildEvent());
            if (XnatDatatypeprotocol.isProjectSpecific(protocol)) {
                XDAT.triggerXftItemEvent(_project, XftItemEvent.UPDATE);
            } else {
                XDAT.triggerXftItemEvent(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME, _protocolId, XftItemEvent.CREATE);
            }
            PersistentWorkflowUtils.complete(workflow, workflow.buildEvent());
            return protocol;
        } catch (Exception e) {
            PersistentWorkflowUtils.fail(workflow, workflow.buildEvent());
            throw e;
        }
    }

    private Changed hasChanged(final XnatDatatypeprotocol protocol) {
        // Determine whether the updated protocol is project specific.
        final boolean projectSpecific = XnatDatatypeprotocol.isProjectSpecific(protocol);

        // If the "existing" protocol doesn't exist, this is a new protocol and the effect is whatever
        // the protocol is as far as project specific vs site wide.
        if (_protocol == null) {
            return projectSpecific ? Changed.ProjectSpecific : Changed.SiteWide;
        }

        // If the state of the protocol changed from project specific to site wide or vice versa,
        // then it's a site-wide change, since changing from site wide to project specific means
        // other projects need to update to remove the now non-site-wide protocol.
        if (XnatDatatypeprotocol.isProjectSpecific(_protocol) != projectSpecific) {
            return Changed.SiteWide;
        }

        // Otherwise we just see if the existing and updated protocols are the same. If so, we need to update to whatever
        // scope the protocol is set for.
        return protocol.equals(_protocol) ? Changed.Unchanged : projectSpecific ? Changed.ProjectSpecific : Changed.SiteWide;
    }

    enum Changed {
        Unchanged,
        ProjectSpecific,
        SiteWide
    }

    private final XnatProjectdata      _project;
    private final XnatDatatypeprotocol _protocol;
    private final String               _protocolId;
    private final String               _dataType;
}
