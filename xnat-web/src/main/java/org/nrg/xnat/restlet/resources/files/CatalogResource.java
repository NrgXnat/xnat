/*
 * web: org.nrg.xnat.restlet.resources.files.CatalogResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.restlet.representations.BeanRepresentation;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class CatalogResource extends XNATCatalogTemplate {
    public CatalogResource(Context context, Request request, Response response) throws ClientException {
        super(context, request, response, false);

        _filePathIsEmpty = checkForNonEmptyFilePath(getRequest().getResourceRef().getRemainingPart());

        initializeResourcesFromIdsAndCatalogs();

        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) {
        if (failFastDueToNonEmptyFilePath()) {
            return null;
        }

        getAllMatches();

        if (getResources().isEmpty()) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified catalog.");
            return null;
        }
        if (getResources().size() == 1) {
            final XnatAbstractresource resource = getResources().get(0);
            try {
                if (proj == null) {
                    if (parent.getItem().instanceOf("xnat:experimentData")) {
                        proj = ((XnatExperimentdata) parent).getPrimaryProject(false);
                    } else if (security.getItem().instanceOf("xnat:experimentData")) {
                        proj = ((XnatExperimentdata) security).getPrimaryProject(false);
                    }
                }

                if (resource.getItem().instanceOf("xnat:resourceCatalog")) {
                    final CatCatalogBean cat = ((XnatResourcecatalog) resource).getCleanCatalog(proj.getRootArchivePath(), isQueryVariableTrue("includeRootPath"), null, null);
                    if (cat != null) {
                        return new BeanRepresentation(cat, MediaType.TEXT_XML);
                    }
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find catalog file.");
                } else {
                    return new ItemXMLRepresentation(resource.getItem(), MediaType.TEXT_XML);
                }
            } catch (ElementNotFoundException e) {
                log.error("", e);
            }
        }

        return null;
    }

    @Override
    public void handlePut() {
        handlePost();
    }

    @Override
    public void handlePost() {
        if (failFastDueToNonEmptyFilePath()) {
            return;
        }

        if (parent != null && security != null) {
            final UserI user = getUser();
            try {
                if (Permissions.canEdit(user, security)) {
                    if (!getResources().isEmpty()) {
                        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified resource already exists.");
                        return;
                    }

                    final XFTItem item = loadItem("xnat:resourceCatalog", true);

                    if (item == null) {
                        getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
                        return;
                    }

                    if (item.instanceOf("xnat:resourceCatalog")) {
                        final XnatResourcecatalog catResource = (XnatResourcecatalog) BaseElement.GetGeneratedItem(item);

                        if (catResource.getXnatAbstractresourceId() != null) {
                            if (XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(catResource.getXnatAbstractresourceId(), user, false) != null) {
                                getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified catalog already exists.");
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Contains erroneous generated fields (xnat_abstractresource_id).");
                            }
                            return;
                        }

                        setCatalogAttributes(user, catResource);

                        catResource.setLabel(getResourceIds().get(0));

                        PersistentWorkflowI wrk = PersistentWorkflowUtils.getWorkflowByEventId(user, getEventId());
                        if (wrk == null && "SNAPSHOTS".equals(catResource.getLabel())) {
                            if (getSecurityItem() instanceof XnatExperimentdata) {
                                final Collection<? extends PersistentWorkflowI> workflows = PersistentWorkflowUtils.getOpenWorkflows(user, ((ArchivableItem) getSecurityItem()).getId());
                                if (workflows != null && workflows.size() == 1) {
                                    wrk = (WrkWorkflowdata) CollectionUtils.get(workflows, 0);
                                    if (!"xnat_tools/AutoRun.xml".equals(wrk.getPipelineName())) {
                                        wrk = null;
                                    } else {
                                        if (StringUtils.isBlank(wrk.getCategory())) {
                                            wrk.setCategory(EventUtils.CATEGORY.DATA);
                                            wrk.setType(EventUtils.TYPE.PROCESS);
                                            WorkflowUtils.save(wrk, wrk.buildEvent());
                                        }
                                    }
                                }
                            }
                        }

                        final boolean isNew;
                        if (wrk == null) {
                            isNew = true;
                            wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, getSecurityItem().getItem(), newEventInstance(EventUtils.CATEGORY.DATA, (getAction() != null) ? getAction() : EventUtils.CREATE_RESOURCE));
                        } else {
                            isNew = false;
                        }

                        assert wrk != null;
                        insertCatalog(catResource);

                        if (isNew) {
                            WorkflowUtils.complete(wrk, wrk.buildEvent());
                        }
                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only ResourceCatalog documents can be PUT to this address.");
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
                }

            } catch (ActionException e) {
                getResponse().setStatus(e.getStatus(), e.getMessage());
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                log.error("", e);
            }
        }
    }

    @Override
    public void handleDelete() {
        if (failFastDueToNonEmptyFilePath() || getResources().isEmpty() || parent == null || security == null) {
            return;
        }

        final UserI   user         = getUser();
        final XFTItem securityItem = security.getItem();
        final XFTItem parentItem   = parent.getItem();

        try {
            checkPermissionsAndStatus(user, securityItem);
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
            return;
        } catch (Exception e) {
            try {
                log.error("An error occurred trying to delete the specified resources on parent item {}/ID={} and security item {}/ID={}: {}", parentItem.getIDValue(), parentItem.getXSIType(), securityItem.getIDValue(), securityItem.getXSIType(), StringUtils.join(getResourceIds(), ", "), e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            } catch (XFTInitException | ElementNotFoundException ex) {
                log.error("An error occurred trying to delete resources", ex);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
            }
            return;
        }

        try {
            final Triple<XnatProjectdata, String, String> securityTriple = getProjectXsiTypeAndId(parent, security);
            if (securityTriple.getLeft() == null) {
                log.warn("Got a parent item of type {}/ID={} and security item of type {}/ID={}, but neither of these is a project, subject, or experiment.", parentItem.getIDValue(), parentItem.getXSIType(), securityItem.getIDValue(), securityItem.getXSIType());
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You can't directly delete insecure items");
            }

            if (proj == null) {
                proj = securityTriple.getLeft();
            }

            final String xsiType    = securityTriple.getMiddle();
            final String securityId = securityTriple.getRight();

            final List<String> ineligible = Lists.newArrayList(Iterables.transform(Iterables.filter(getResources(), new Predicate<XnatAbstractresource>() {
                @Override
                public boolean apply(final XnatAbstractresource resource) {
                    try {
                        return resource.getItem().isLocked() || !resource.getItem().isActive() && !resource.getItem().isQuarantine();
                    } catch (MetaDataException e) {
                        log.error("An error occurred trying to check the lock/active/quarantine status of the resource {} associated with {}/ID={}", resource.getXnatAbstractresourceId(), xsiType, securityId);
                        return true;
                    }
                }
            }), RESOURCE_TO_STRING_FUNCTION));

            if (!ineligible.isEmpty()) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Item " + securityItem.getXSIType() + "/ID=" + securityItem.getIDValue() + " has " + ineligible.size() + " resources that are either locked or are not active or quarantined and can't be deleted: " + StringUtils.join(ineligible));
            }

            final List<String> failed = new ArrayList<>();
            final String archivePath  = proj.getRootArchivePath();
            for (final XnatAbstractresource resource : getResources()) {
                final String              resourceId = getResourceDisplay(resource);
                final PersistentWorkflowI workflow   = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, xsiType, securityId, proj.getId(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.REMOVE_CATALOG + " " + resourceId));
                final EventMetaI          meta       = workflow.buildEvent();

                try {
                    resource.deleteWithBackup(archivePath, user, meta);
                    SaveItemHelper.authorizedRemoveChild(parentItem, xmlPath, resource.getItem(), user, meta);
                    PersistentWorkflowUtils.complete(workflow, meta);
                } catch (Exception e) {
                    failed.add(getResourceDisplay(resource));
                    workflow.setDetails(e.getMessage());
                    PersistentWorkflowUtils.fail(workflow, meta);
                }
            }
            if (!failed.isEmpty()) {
                if (failed.size() == getResources().size()) {
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Deletion failed for all resources");
                } else {
                    getResponse().setStatus(Status.SUCCESS_MULTI_STATUS, "Deleted resources as requested, but the following resources failed somehow: " + StringUtils.join(failed, ", "));
                }
            }
            XDAT.triggerXftItemEvent(xsiType, securityId, XftItemEvent.UPDATE);
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred trying to delete the specified resources", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    private void checkPermissionsAndStatus(final UserI user, final XFTItem securityItem) throws Exception {
        if (!Permissions.canDelete(user, security)) {
            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
        }
        if (securityItem.isLocked()) {
            //cannot modify item if it's locked
            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Item " + securityItem.getXSIType() + "/ID=" + securityItem.getIDValue() + " is locked, resource deletion not allowed.");
        }
        if (!securityItem.isActive() && !securityItem.isQuarantine()) {
            //cannot modify item if it isn't active or quarantined.
            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Item " + securityItem.getXSIType() + "/ID=" + securityItem.getIDValue() + " is not active or quarantined, resource deletion not allowed.");
        }
    }

    @Nonnull
    private Triple<XnatProjectdata, String, String> getProjectXsiTypeAndId(final ItemI parent, final ItemI security) throws ElementNotFoundException {
        final XFTItem parentItem   = parent.getItem();
        final XFTItem securityItem = security.getItem();
        if (parentItem.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
            final XnatExperimentdata experiment = (XnatExperimentdata) this.parent;
            return ImmutableTriple.of(experiment.getPrimaryProject(false), experiment.getXSIType(), experiment.getId());
        }
        if (securityItem.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
            final XnatExperimentdata experiment = (XnatExperimentdata) security;
            return ImmutableTriple.of(experiment.getPrimaryProject(false), experiment.getXSIType(), experiment.getId());
        }
        if (parentItem.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            final XnatSubjectdata subject = (XnatSubjectdata) parent;
            return ImmutableTriple.of(subject.getPrimaryProject(false), XnatSubjectdata.SCHEMA_ELEMENT_NAME, subject.getId());
        }
        if (securityItem.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            final XnatSubjectdata subject = (XnatSubjectdata) security;
            return ImmutableTriple.of(subject.getPrimaryProject(false), XnatSubjectdata.SCHEMA_ELEMENT_NAME, subject.getId());
        }
        if (parentItem.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
            final XnatProjectdata project = (XnatProjectdata) parent;
            return ImmutableTriple.of(project, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId());
        }
        if (securityItem.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
            final XnatProjectdata project = (XnatProjectdata) security;
            return ImmutableTriple.of(project, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId());
        }
        return ImmutableTriple.nullTriple();
    }

    private void getAllMatches() {
        getResources().clear();
        try {
            setCatalogs(loadCatalogs(getResourceIds(), false, true));
        } catch (Exception e) {
            log.error("An error occurred trying to load catalogs from the resource IDs: {}", getResourceIds(), e);
        }

        initializeResourcesFromIdsAndCatalogs();
    }

    private boolean checkForNonEmptyFilePath(String remainingUrlPart) {
        // we don't care about path separators or query parameters
        // everything else will be rejected
        return StringUtils.isBlank(remainingUrlPart) || remainingUrlPart.matches("^/+") || remainingUrlPart.matches("^/*\\?.*");
    }

    /**
     * See XNAT-1674.  If the client mistakenly passes a file path to us, slap the wrist.
     * This is better than discarding the file path and naively processing the request
     * (and say, deleting the whole catalog instead of the individual file delete that was desired).
     */
    private boolean failFastDueToNonEmptyFilePath() {
        if (_filePathIsEmpty) {
            return false;
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "This resource works at the catalog level only and does not accept file paths.  To work with the resource files, append '/files' after the resource ID.");
            return true;
        }
    }

    private void initializeResourcesFromIdsAndCatalogs() {
        if (hasCatalogs()) {
            for (final Object[] row : getCatalogs().rows()) {
                final String id    = Integer.toString((Integer) row[0]);
                final String label = (String) row[1];
                getResources().addAll(Lists.transform(Lists.newArrayList(Iterables.filter(getResourceIds(), new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable final String resourceId) {
                        return StringUtils.equalsAny(resourceId, id, label);
                    }
                })), new Function<String, XnatAbstractresource>() {
                    @Override
                    public XnatAbstractresource apply(final String resourceId) {
                        return XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(row[0], getUser(), false);
                    }
                }));
            }
        }
    }

    @Nonnull
    private static String getResourceDisplay(final XnatAbstractresource resource) {
        final String resourceLabel = resource.getLabel();
        return resource.getXnatAbstractresourceId() + (StringUtils.isBlank(resourceLabel) ? "" : " (" + resourceLabel + ")");
    }

    private static final Function<XnatAbstractresource, String> RESOURCE_TO_STRING_FUNCTION = new Function<XnatAbstractresource, String>() {
        @Override
        public String apply(final XnatAbstractresource resource) {
            return getResourceDisplay(resource);
        }
    };

    private final boolean _filePathIsEmpty;
}
