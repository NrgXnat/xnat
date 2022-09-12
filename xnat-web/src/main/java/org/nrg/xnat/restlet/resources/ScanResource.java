/*
 * web: org.nrg.xnat.restlet.resources.ScanResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatImagescandataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatImagescandata;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.PullScanDataFromHeaders;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class ScanResource extends ItemResource {
    private static final Logger logger = LoggerFactory.getLogger(ScanResource.class);

    protected XnatProjectdata proj;
    protected XnatImagesessiondata session = null;
    protected XnatImagescandata scan = null;
    protected String scanID = null;

    public ScanResource(Context context, Request request, Response response) {
        super(context, request, response);

        final UserI user = getUser();

        final String pID = (String) getParameter(request, "PROJECT_ID");
        if (pID != null) {
            proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
        }

        String assessedID = (String) getParameter(request, "ASSESSED_ID");
        if (assessedID != null) {
            session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(assessedID, user, false);
            if (session != null && (proj != null && !session.hasProject(proj.getId()))) {
                session = null;
            }

            if (session == null && proj != null) {
                session = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), assessedID, user, false);
            }

            scanID = (String) getParameter(request, "SCAN_ID");
            if (scanID != null) {
                getVariants().add(new Variant(MediaType.TEXT_HTML));
                getVariants().add(new Variant(MediaType.TEXT_XML));
            }

            fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XnatImagescandata.SCHEMA_ELEMENT_NAME, false));
        } else {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a session ID for this request.");
        }
    }


    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        final UserI user = getUser();
        if (user.isGuest()) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "No authenticated user found.");
        }

        try {
            if (StringUtils.isNotBlank(filepath)) {
                // Share scan
                String projectPrefix = "projects/";
                if (!filepath.startsWith(projectPrefix)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return;
                }
                String newProjectS = filepath.replaceFirst("^" + projectPrefix, "");
                XnatProjectdata newProject = XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
                if (newProject == null) {
                    setGuestDataResponse("Unable to identify project: " + newProjectS);
                    return;
                }
                if (session == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Could not load session");
                    return;
                }

                searchForScan();
                if (scan == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Could not load scan");
                }

                // Has session already been shared?
                XnatExperimentdataShare matchedSession = null;
                for (XnatExperimentdataShareI pp : session.getSharing_share()) {
                    if (pp.getProject().equals(newProject.getId())) {
                        matchedSession = (XnatExperimentdataShare) pp;
                        break;
                    }
                }
                if (matchedSession == null) {
                    if (Permissions.canCreate(user, session.getXSIType() + "/project", newProject.getId())) {
                        shareExperimentToProject(user, newProject, session, new XnatExperimentdataShare(user),
                                session.getLabel(), false);
                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in the " + newProject.getId() + " project.");
                        return;
                    }
                }

                XnatImagescandataShare matched = null;
                for (XnatImagescandataShareI pp : scan.getSharing_share()) {
                    if (pp.getProject().equals(newProject.getId())) {
                        matched = (XnatImagescandataShare) pp;
                        break;
                    }
                }

                if (matched != null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project: "
                            + newProject.getId());
                    return;
                }

                if (Permissions.canCreate(user, scan.getXSIType() + "/project", newProject.getId())) {
                    shareScanToProject(user, newProject, scan);
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in the " + newProject.getId() + " project.");
                    return;
                }

                returnDefaultRepresentation();
            } else {// Not scan sharing activity
                XnatImagescandata existing = null;

                //Find preexisting scan by scanID in url
                if (session!=null && StringUtils.isNotEmpty(scanID)) {
                    existing=queryForScan(user,scanID,session.getId());
                }

                //XNAT-7015 this used to just pass null, but we changed it to pass the xsi-type when it is known
                final String existingType=(existing==null) ? null : existing.getXSIType();
                XFTItem item = loadItem(existingType, true);

                if (item == null) {
                    String xsiType = getQueryVariable("xsiType");
                    if (xsiType != null) {
                        item = XFTItem.NewItem(xsiType, user);
                    }
                }

                if (item == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
                    return;
                }

                if (!item.instanceOf("xnat:imageScanData")) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Only Scan documents can be PUT to this address. Expected: xnat:imageScanData Received: " + item.getXSIType());
                    return;
                }

                if(item.getXSIType().equals("xnat:imageScanData")){
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Multiple scan modalities can be associated here.  Please retry with the specification of a particular modality (i.e. ?xsiType=xnat:mrScanData).");
                    return;
                }

                scan = (XnatImagescandata) BaseElement.GetGeneratedItem(item);

                //MATCH SESSION
                if (session != null) {
                    scan.setImageSessionId(session.getId());
                } else {
                    if (scan.getImageSessionId() != null && !scan.getImageSessionId().equals("")) {
                        session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(scan.getImageSessionId(), user, false);

                        if (session == null && proj != null) {
                            session = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), scan.getImageSessionId(), user, false);
                        }
                        if (session != null) {
                            scan.setImageSessionId(session.getId());
                        }
                    }
                }

                if (proj != null) {
                    scan.setProject(proj.getId());
                } else if (session != null) {
                    scan.setProject(session.getProject());
                }

                if (scan.getImageSessionId() == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Specified scan must reference a valid image session.");
                    return;
                }

                if (session == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Specified image session doesn't exist.");
                    return;
                }

                if (scan.getId() == null) {
                    scan.setId(scanID);
                }

                if (getQueryVariable("type") != null) {
                    scan.setType(getQueryVariable("type"));
                }

                if(existing==null){
                    //recheck in case anything changed during loadItem... like if a scan xml was in the request body
                    if (scan.getXnatImagescandataId() != null) {
                        existing = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scan.getXnatImagescandataId(), user, completeDocument);
                    }

                    if (existing != null && scan.getId() != null) {
                        existing=queryForScan(user,scan.getId(),scan.getImageSessionId());
                    }
                }

                if (existing == null) {
                    if (!Permissions.canEdit(user, session)) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for sessions in this project.");
                        return;
                    }
                    //IS NEW
                    if (scan.getId() == null || scan.getId().equals("")) {
                        String query = "SELECT count(id) AS id_count FROM xnat_imageScanData WHERE image_session_id='" + session.getId() + "' AND id='";

                        String login = user.getUsername();
                        try {
                            int i = 1;
                            Long idCOUNT = (Long) PoolDBUtils.ReturnStatisticQuery(query + i + "';", "id_count", user.getDBName(), login);
                            while (idCOUNT > 0) {
                                i++;
                                idCOUNT = (Long) PoolDBUtils.ReturnStatisticQuery(query + i + "';", "id_count", user.getDBName(), login);
                            }
                            scan.setId("" + i);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }

                } else {
                    //MATCHED
                    if (!Permissions.canEdit(user, session)) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for sessions in this project.");
                        return;
                    }

                    if(!StringUtils.equals(XnatImagescandata.SCHEMA_ELEMENT_NAME,existing.getXSIType()) && !StringUtils.equals(existing.getXSIType(),scan.getXSIType())){
                        //operation would change xsi:type, which isn't allowed... unless the type was xnat:imageScanData (which we'd want to allow them to fix)
                        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified xsiType differs from existing xsiType");
                        return;
                    }
                }

                boolean allowDataDeletion = false;
                if (getQueryVariable("allowDataDeletion") != null && getQueryVariable("allowDataDeletion").equals("true")) {
                    allowDataDeletion = true;
                }

                final ValidationResults vr = scan.validate();

                if (vr != null && !vr.isValid()) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, vr.toFullString());
                    return;
                }

                Authorizer.getInstance().authorizeSave(session.getItem(), user);
                create(session, scan, false, allowDataDeletion, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(scan.getXSIType(), scan == null)));

                if (isQueryVariableTrue(XNATRestConstants.PULL_DATA_FROM_HEADERS) || containsAction(XNATRestConstants.PULL_DATA_FROM_HEADERS)) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, session.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.DICOM_PULL));
                    EventMetaI c = wrk.buildEvent();
                    try {
                        PullScanDataFromHeaders pull = new PullScanDataFromHeaders(scan, user, allowDataDeletion, false, c);
                        pull.call();
                        WorkflowUtils.complete(wrk, c);
                    } catch (Exception e) {
                        WorkflowUtils.fail(wrk, c);
                        throw e;
                    }
                }
            }
        } catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
			return;
		} catch (InvalidValueException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.error("", e);
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.error("", e);
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handleDelete() {

        searchForScan();

        if (scan == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified scan.");
            return;
        }

        if (filepath != null && !filepath.equals("")) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        try {
        	boolean prevent_delete=StringUtils.contains(XDAT.getSiteConfigurationProperty("security.prevent-data-deletion-override", "[]"), session.getItem().getStatus())?false: XDAT.getBoolSiteConfigurationProperty("security.prevent-data-deletion", false);
        	
        	if (!Permissions.canDelete(getUser(), session) || prevent_delete) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
                return;
            }

            delete(session, scan, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getDeleteAction(scan.getXSIType())));

            // Above "delete" removes resources, but leaves dangling scan directory
            XNATUtils.removeScanDir(session, scan);

        } catch (SQLException e) {
            logger.error("There was an error running a query.", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        } catch (ConfigServiceException e) {
        	logger.error("There was an error.", e);
        	getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        } catch (Exception e) {
            logger.error("There was an error.", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }


    @Override
    public Representation represent(Variant variant) {
        searchForScan();

        if (scan != null) {
            return representItem(scan.getItem(), overrideVariant(variant));
        }

        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified scan.");
        return null;
    }

    protected void searchForScan() {
        if (scan == null && scanID != null) {
            if (session != null) {
                scan=queryForScan(getUser(),scanID,session.getId());
            }
        }
    }

    @Nullable
    private XnatImagescandata queryForScan(final UserI user, final String scanId, final String sessionId){
        final CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause("xnat:imageScanData/ID", scanId);
        cc.addClause("xnat:imageScanData/image_session_ID", sessionId);
        final List<XnatImagescandata> scans = XnatImagescandata.getXnatImagescandatasByField(cc, user, completeDocument);
        return scans.isEmpty() ? null : scans.get(0);
    }

    protected XnatImagescandata getScan() {
        return scan;
    }
}
