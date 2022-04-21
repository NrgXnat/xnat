/*
 * web: org.nrg.xnat.restlet.resources.SubjAssessmentResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.transaction.TransactionException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatSubjectdata;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.FixScanTypes;
import org.nrg.xnat.restlet.actions.PullSessionDataFromHeaders;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.services.archive.PipelineService;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import static org.nrg.xft.event.XftItemEventI.CREATE;

public class SubjAssessmentResource extends SubjAssessmentAbst {
    XnatProjectdata proj = null;
    XnatSubjectdata subject = null;
    XnatSubjectassessordata expt = null;
    String exptID = null;
    XnatSubjectassessordata existing;
    String subID = null;

    public SubjAssessmentResource(Context context, Request request, Response response) throws InsufficientAuthenticationException {
        super(context, request, response);

        final UserI user = getUser();
        final String pID = (String) getParameter(request, "PROJECT_ID");
        if (pID != null) {
            proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
        }

        if (proj == null) {
            setGuestDataResponse();
            return;
        }

        subID = (String) getParameter(request, "SUBJECT_ID");
        if (subID != null) {
            subject = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
                    .getId(), subID, user, false);

            if (subject == null) {
                subject = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
                        false);
                if (subject != null
                        && (proj != null && !subject.hasProject(proj.getId()))) {
                    subject = null;
                }
            }
        }

        exptID = (String) getParameter(request, "EXPT_ID");
        if (exptID != null) {
            if (proj != null) {
                if (existing == null) {
                    existing = (XnatSubjectassessordata) XnatExperimentdata
                            .GetExptByProjectIdentifier(proj.getId(), exptID,
                                    user, false);
                }
            }

            if (existing == null) {
                existing = (XnatSubjectassessordata) XnatExperimentdata
                        .getXnatExperimentdatasById(exptID, user, false);
                if (existing != null
                        && (proj != null && !existing.hasProject(proj.getId()))) {
                    existing = null;
                }
            }

            this.getVariants().add(new Variant(MediaType.TEXT_HTML));
            this.getVariants().add(new Variant(MediaType.TEXT_XML));
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.EXPERIMENT_DATA, false));
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    private XnatSubjectdata getExistingSubject(XnatProjectdata proj, String subjectId) {
        final UserI user = getUser();
        // First check if the subject is associated with the project,
        // if that fails check the global pool.
        XnatSubjectdata s = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjectId, user, false);
        if (s == null) {
            s = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);
        }
        return s;
    }

    private XnatSubjectassessordata getExistingExperiment(XnatSubjectassessordata currExp) {
        final UserI user = getUser();
        XnatSubjectassessordata retExp = null;
        if (currExp.getId() != null) {
            retExp = (XnatSubjectassessordata) XnatExperimentdata.getXnatExperimentdatasById(currExp.getId(), null, completeDocument);
        }

        if (retExp == null && currExp.getProject() != null && currExp.getLabel() != null) {
            retExp = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(currExp.getProject(), currExp.getLabel(), user, completeDocument);
        }

        if (retExp == null) {
            for (XnatExperimentdataShareI pp : currExp.getSharing_share()) {
                retExp = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), pp.getLabel(), user, completeDocument);
                if (retExp != null) {
                    break;
                }
            }
        }
        return retExp;
    }

    @Override
    public void handlePut() {
        final UserI user = getUser();
        try {
            final boolean allowDataDeletion = isQueryVariableTrue("allowDataDeletion");
            final String specifiedProjectId = proj != null ? proj.getId() : null;

            XFTItem template = null;
            if (existing != null && !allowDataDeletion) {
                template = existing.getItem().getCurrentDBVersion();
            }

            XFTItem item = this.loadItem(null, true, template);

            if (item == null) {
                String xsiType = this.getQueryVariable("xsiType");
                if (xsiType != null) {
                    item = XFTItem.NewItem(xsiType, user);
                }
            }

            if (item == null) {
                if (proj != null) {
                    XnatSubjectassessordata om = (XnatSubjectassessordata) XnatSubjectassessordata.GetExptByProjectIdentifier(specifiedProjectId, this.exptID, user, false);
                    if (om != null) {
                        item = om.getItem();
                    }
                }

                if (item == null) {
                    XnatSubjectassessordata om = (XnatSubjectassessordata) XnatSubjectassessordata.getXnatExperimentdatasById(this.exptID, null, false);
                    if (om != null) {
                        item = om.getItem();
                    }
                }
            }

            if (item == null) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
                return;
            }

            if (item.instanceOf("xnat:subjectAssessorData")) {
                expt = (XnatSubjectassessordata) BaseElement.GetGeneratedItem(item);

                final String currentProjectId = expt.getProject();

                if (filepath != null && !filepath.equals("")) {
                    if (filepath.startsWith("projects/")) {

                        if(!isSharingAllowed(user, currentProjectId)){
                            return;
                        }

                        String newProjectS = filepath.substring(9);
                        XnatProjectdata newProject = XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
                        String newLabel = this.getQueryVariable("label");
                        if (newProject != null) {
                            final String newProjectId = newProject.getId();
                            if (currentProjectId.equals(newProjectId)) {
                                this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project:" + newProjectId);
                                return;
                            }

                            if (!Permissions.canRead(user, expt)) {
                                this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Specified user account has insufficient privileges for experiments in this project.");
                                return;
                            }

                            int index = 0;
                            XnatExperimentdataShare matched = null;
                            for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
                                if (pp.getProject().equals(newProject.getId())) {
                                    matched = (XnatExperimentdataShare) pp;
                                    if (newLabel != null && !pp.getLabel().equals(newLabel)) {
                                        shareExperimentToProject(user, newProject, expt, matched, newLabel);
                                    }
                                    break;
                                }
                                index++;
                            }

                            if (this.getQueryVariable("primary") != null && this.getQueryVariable("primary").equals("true")) {
                                if (newLabel == null || newLabel.equals("")) {
                                    newLabel = expt.getLabel();
                                }
                                if (newLabel == null || newLabel.equals("")) {
                                    newLabel = expt.getId();
                                }

                                changeExperimentPrimaryProject(expt, proj, newProject, newLabel, matched, index);
                                return;
                            } else {
                                if (matched == null) {
                                    if (newLabel != null) {
                                        XnatExperimentdata temp = XnatExperimentdata.GetExptByProjectIdentifier(newProjectId, newLabel, null, false);
                                        if (temp != null) {
                                            this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Label already in use:" + newLabel);
                                            return;
                                        }
                                    }
                                    if (Permissions.canCreate(user, expt.getXSIType() + "/project", newProjectId)) {
                                        shareExperimentToProject(user, newProject, expt, newLabel);
                                    } else {
                                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in the " + newProjectId + " project.");
                                        return;
                                    }
                                } else {
                                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project:" + newProjectId);
                                    return;
                                }
                            }

                            this.returnDefaultRepresentation();
                        } else {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to identify project: " + newProjectS);
                            return;
                        }
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return;
                    }
                } else {
                    if (expt.getLabel() == null) {
                        expt.setLabel(this.exptID);
                    }
                    //MATCH PROJECT
                    if (proj == null && currentProjectId != null) {
                        proj = XnatProjectdata.getXnatProjectdatasById(currentProjectId, user, false);
                    }

                    if (this.proj != null) {
                        if (currentProjectId == null || currentProjectId.equals("")) {
                            expt.setProject(specifiedProjectId);
                        } else if (!StringUtils.equals(currentProjectId, specifiedProjectId)) {
                            boolean matched = false;
                            for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
                                if (pp.getProject().equals(this.proj.getId())) {
                                    matched = true;
                                    break;
                                }
                            }

                            if (!matched) {
                                XnatExperimentdataShare pp = new XnatExperimentdataShare(user);
                                pp.setProject(this.proj.getId());
                                expt.setSharing_share(pp);
                            }
                        }
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted experiment record must include the project attribute.");
                        return;
                    }

                    // Find the pre-existing experiment
                    if (existing == null) {
                        existing = getExistingExperiment(expt);
                    }

                    //MATCH SUBJECT
                    if (this.subject != null) {
                        expt.setSubjectId(this.subject.getId());
                        unsetOldVisitAndSubtype();
                    } else {
                        if (StringUtils.isBlank(expt.getSubjectId()) && StringUtils.isNotEmpty(subID)) {
                            expt.setSubjectId(subID);
                            unsetOldVisitAndSubtype();
                        }

                        if (expt.getSubjectId() != null && !expt.getSubjectId().equals("")) {
                            this.subject = XnatSubjectdata.getXnatSubjectdatasById(expt.getSubjectId(), user, false);

                            if (this.subject == null && currentProjectId != null && expt.getLabel() != null) {
                                this.subject = XnatSubjectdata.GetSubjectByProjectIdentifier(currentProjectId, expt.getSubjectId(), user, false);
                            }

                            if (this.subject == null) {
                                for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
                                    this.subject = XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), expt.getSubjectId(), user, false);
                                    if (this.subject != null) {
                                        break;
                                    }
                                }
                            }

                            if (subject == null && existing != null) {
                                this.subject = existing.getSubjectData();
                                expt.setSubjectId(subject.getId());
                            }

                            if (this.subject == null) {
                                try {
                                    createNewSubject(user, expt.getSubjectId());
                                } catch (InsufficientPrivilegesException e) {
                                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User " + user.getUsername() + " has insufficient create privileges for subjects in this project.");
                                    return;
                                }
                            }
                        }
                    }


                    if (existing == null) {
                        if (!Permissions.canCreate(user, expt) && !Roles.isSiteAdmin(user)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in this project.");
                            return;
                        }
                        //IS NEW
                        if (expt.getId() == null || expt.getId().equals("")) {
                            expt.setId(XnatExperimentdata.CreateNewID());
                        }

                    } else {
                        if (expt.getId() == null || expt.getId().equals("")) {
                            expt.setId(existing.getId());
                        }

                        //MATCHED
                        if (!existing.getProject().equals(currentProjectId)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Project must be modified through separate URI.");
                            return;
                        }

                        if (!Permissions.canEdit(user, expt) && !Roles.isSiteAdmin(user)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for experiments in this project.");
                            return;
                        }

                        if (this.getQueryVariable("subject_ID") != null && !this.getQueryVariable("subject_ID").equals("")) {
                            if (!expt.getSubjectId().equals(this.getQueryVariable("subject_ID"))) {
                                XnatSubjectdata s = this.getExistingSubject(proj,
                                        this.getQueryVariable("subject_ID"));
                                if (s != null) {
                                    // \"subject_ID\" can be overloaded on both the subject's label
                                    // and XNAT unique subject identifier
                                    if (!expt.getSubjectId().equals(s.getId())) {
                                        // only accept subjects that are associated with this project
                                        if (s.hasProject(specifiedProjectId)) {
                                            expt.setSubjectId(s.getId());
                                            unsetOldVisitAndSubtype();
                                        }
                                    }
                                } else {
                                    try {
                                        createNewSubject(user, getQueryVariable("subject_ID"));
                                    } catch (ResourceException e) {
                                        this.getResponse().setStatus(e.getStatus(), "Specified user account has insufficient create privileges for subjects in this project.");
                                    } catch (InsufficientPrivilegesException e) {
                                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User " + user.getUsername() + " has insufficient create privileges for subjects in this project.");
                                        return;
                                    }
                                }
                            }
                        }

                        if (this.getQueryVariable("label") != null && !this.getQueryVariable("label").equals("")) {
                            if (!expt.getLabel().equals(existing.getLabel())) {
                                expt.setLabel(existing.getLabel());
                            }
                            String label = this.getQueryVariable("label");

                            if (!label.equals(existing.getLabel())) {
                                XnatExperimentdata match = XnatExperimentdata.GetExptByProjectIdentifier(specifiedProjectId, label, user, false);
                                if (match != null) {
                                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified label is already in use.");
                                    return;
                                }

                                rename(proj, existing, label, user);
                            }
                            return;
                        }
                    }

                    if (this.subject == null) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted experiment record must include the subject.");
                        return;
                    }

                    final PersistentWorkflowI workflow = WorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(expt.getXSIType(), (existing == null))));
                    final EventMetaI meta = workflow.buildEvent();

                    if (isQueryVariableTrue(XNATRestConstants.FIX_SCAN_TYPES) || containsAction(XNATRestConstants.FIX_SCAN_TYPES)) {
                        if (expt instanceof XnatImagesessiondata) {
                            FixScanTypes.builder().experiment(expt).user(user).project(proj).allowSave(false).eventMeta(meta).build().call();
                        }
                    }

                    if (StringUtils.isNotBlank(expt.getLabel()) && !XftStringUtils.isValidId(expt.getId())) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in experiment label.");
                        return;
                    }

                    final ValidationResults vr = expt.validate();

                    if (vr != null && !vr.isValid()) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, vr.toFullString());
                        return;
                    }

                    // Preserve the previous version of the experiment before we save it.
                    XnatSubjectassessordata previous = getExistingExperiment(expt);

                    //check for unexpected modifications of ID, Project and label
                    if (existing != null && !StringUtils.equals(existing.getId(), expt.getId())) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "ID cannot be modified");
                        return;
                    }

                    if (existing != null && !StringUtils.equals(existing.getProject(), currentProjectId)) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Project must be modified through separate URI.");
                        return;
                    }

                    //MATCHED
                    if (existing != null && !StringUtils.equals(existing.getLabel(), expt.getLabel())) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Label must be modified through separate URI.");
                        return;
                    }

                    if (create(expt, false, allowDataDeletion, workflow, meta)) {
                        if (existing != null) {
                            XDAT.triggerXftItemEvent(expt, CREATE);
                        }

                        if (this.proj.getArcSpecification().getQuarantineCode() != null && this.proj.getArcSpecification().getQuarantineCode().equals(1)) {
                            expt.quarantine(user);
                        }

                        final String subjectId = expt.getSubjectId();
                        if (previous != null && expt instanceof XnatImagesessiondata && subjectId != null && !subjectId.equals(previous.getSubjectId())) {
                            try {
                                // re-apply this project's edit script
                                expt.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) expt, currentProjectId, expt.getArchiveRootPath()));
                            } catch (TransactionException e) {
                                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                            }
                        }
                    }

                    postSaveManageStatus(expt);

                    if (Permissions.canEdit(user, expt.getItem())) {
                        if ((this.isQueryVariableTrue(XNATRestConstants.PULL_DATA_FROM_HEADERS) || this.containsAction(XNATRestConstants.PULL_DATA_FROM_HEADERS)) && expt instanceof XnatImagesessiondata) {
                            try {
                                final PersistentWorkflowI dicomPullWorkflow = PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.DICOM_PULL));
                                final EventMetaI dicomPullMeta = dicomPullWorkflow.buildEvent();
                                try {
                                    new PullSessionDataFromHeaders((XnatImagesessiondata) expt, user, this.allowDataDeletion(), this.isQueryVariableTrue("overwrite"), false, meta).call();
                                    WorkflowUtils.complete(dicomPullWorkflow, dicomPullMeta);
                                } catch (Exception e) {
                                    WorkflowUtils.fail(dicomPullWorkflow, dicomPullMeta);
                                    throw e;
                                }

                            } catch (SAXException | ValidationException e) {
                                logger.error("", e);
                                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                            } catch (Exception e) {
                                logger.error("", e);
                                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                                return;
                            }
                        }

                        if (this.isQueryVariableTrue(XNATRestConstants.TRIGGER_PIPELINES) || this.containsAction(XNATRestConstants.TRIGGER_PIPELINES)) {
                            XDAT.getContextService().getBean(PipelineService.class).launchAutoRun(expt, isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL), user);
                        }
                    }
                }

                this.returnString(expt.getId(), (existing == null) ? Status.SUCCESS_CREATED : Status.SUCCESS_OK);
            } else {
                this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only xnat:Subject documents can be PUT to this address.");
            }
        } catch (InvalidValueException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.error("", e);
        } catch (ActionException e) {
            this.getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.error("", e);
        }
    }

    private void unsetOldVisitAndSubtype() {
        // Don't do anything if this is a new expt
        if (existing == null) {
            return;
        }

        // Are we doing an intentional modification of visit & subtype?
        if (existing.getVisit() == null && expt.getVisit() != null ||
                existing.getProtocol() == null && expt.getProtocol() != null ||
                existing.getVisit() != null && "NULL".equals(expt.getVisit()) ||
                existing.getProtocol() != null && "NULL".equals(expt.getProtocol())) {
            return;
        }

        // Remove prior visit & subtype
        expt.setVisit("NULL");
        expt.setProtocol("NULL");
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();
        if (expt == null && exptID != null) {
            expt = (XnatSubjectassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);

            if (expt == null && proj != null) {
                expt = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID, user, false);
            }
        }

        if (expt == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified experiment.");
        } else {
            deleteItem(proj, expt);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Representation represent(Variant variant) {
        MediaType mt = overrideVariant(variant);

        final UserI user = getUser();
        if (expt == null && exptID != null) {
            expt = (XnatSubjectassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);

            if (expt == null && this.proj != null) {
                expt = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), exptID, user, false);
            }
        }

        if (expt != null) {
            if (filepath != null && !filepath.equals("") && filepath.equals("status")) {

                return returnStatus(expt, mt);
            } else if (filepath != null && !filepath.equals("") && filepath.equals("history")) {
                try {
                    return buildChangesets(expt.getItem(), expt.getStringProperty("ID"), mt);
                } catch (Exception e) {
                    logger.error("", e);
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                    return null;
                }
            } else if (filepath != null && !filepath.equals("") && filepath.startsWith("projects")) {
                XFTTable t = new XFTTable();
                ArrayList al = new ArrayList();
                al.add("label");
                al.add("ID");
                al.add("Secondary_ID");
                al.add("Name");
                t.initTable(al);

                Object[] row = new Object[4];
                row[0] = expt.getLabel();
                XnatProjectdata primary = expt.getPrimaryProject(false);
                row[1] = primary.getId();
                row[2] = primary.getSecondaryId();
                row[3] = primary.getName();
                t.rows().add(row);

                for (Map.Entry<XnatProjectdataI, String> entry : expt.getProjectDatas().entrySet()) {
                    row = new Object[4];
                    row[0] = entry.getValue();
                    row[1] = entry.getKey().getId();
                    row[2] = entry.getKey().getSecondaryId();
                    row[3] = entry.getKey().getName();
                    t.rows().add(row);
                }

                return representTable(t, mt, new Hashtable<>());
            } else {
                return this.representItem(expt.getItem(), mt);
            }
        } else {
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                    "Unable to find the specified experiment.");
            return null;
        }

    }

    private void createNewSubject(final UserI user, final String subjectLabel) throws Exception {
        this.subject = new XnatSubjectdata(user);
        this.subject.setProject(this.proj.getId());
        this.subject.setLabel(subjectLabel);
        if (!Permissions.canCreate(user, this.subject)) {
            throw new InsufficientPrivilegesException(user.getUsername(), proj.getId());
        }
        this.subject.setId(XnatSubjectdata.CreateNewID());
        BaseXnatSubjectdata.save(this.subject, false, true, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.AUTO_CREATE_SUBJECT));
        expt.setSubjectId(this.subject.getId());
        unsetOldVisitAndSubtype();
    }

}
