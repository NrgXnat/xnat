/*
 * web: org.nrg.xnat.restlet.resources.ScanResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;

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
            XFTItem item = loadItem(null, true);

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

            if (filepath != null && !filepath.equals("")) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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

            //FIND PRE-EXISTING
            XnatImagescandata existing = null;

            if (scan.getXnatImagescandataId() != null) {
                existing = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scan.getXnatImagescandataId(), user, completeDocument);
            }

            if (scan.getId() != null) {
                CriteriaCollection cc = new CriteriaCollection("AND");
                cc.addClause("xnat:imageScanData/ID", scan.getId());
                cc.addClause("xnat:imageScanData/image_session_ID", scan.getImageSessionId());
                ArrayList<XnatImagescandata> scans = XnatImagescandata.getXnatImagescandatasByField(cc, user, completeDocument);
                if (scans.size() > 0) {
                    existing = scans.get(0);
                }
            }

            if (existing == null) {
                if (!Permissions.canEdit(user,session)) {
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
                if (!Permissions.canEdit(user,session)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for sessions in this project.");
                    return;
                }
                //MATCHED
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

            if (!Permissions.canDelete(getUser(), session) || XDAT.getBoolSiteConfigurationProperty("security.prevent-data-deletion", false)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
                return;
            }

            delete(session, scan, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getDeleteAction(scan.getXSIType())));

            // Above "delete" removes resources, but leaves dangling scan directory
            final Path scanDirPath = Paths.get(session.getCurrentSessionFolder(true), "SCANS", scan.getId());
            final File scanDir = scanDirPath == null ? null : scanDirPath.toFile();
            if (scanDir != null && scanDir.isDirectory() && scanDir.exists()) {
                scanDir.delete();

                // Now we have deleted the scan directory. If that was the last one, also remove the SCANS directory.
                final File scansDir = scanDir.getParentFile();
                if (scansDir != null && scansDir.isDirectory() && scansDir.exists()) {
                    final String[] otherScansInScansDir = scansDir.list();
                    if (otherScansInScansDir != null && otherScansInScansDir.length == 0) {
                        scansDir.delete();
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("There was an error running a query.", e);
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
                CriteriaCollection cc = new CriteriaCollection("AND");
                cc.addClause("xnat:imageScanData/ID", scanID);
                cc.addClause("xnat:imageScanData/image_session_ID", session.getId());
                ArrayList<XnatImagescandata> scans = XnatImagescandata.getXnatImagescandatasByField(cc, getUser(), completeDocument);
                if (scans.size() > 0) {
                    scan = scans.get(0);
                }
            }
        }
    }

    protected XnatImagescandata getScan() {
        return scan;
    }
}
