/*
 * web: org.nrg.xnat.restlet.resources.ExperimentResource
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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatSubjectdata;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.ActionNameAbsent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.IDAbsent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.JustificationAbsent;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.archive.Rename;
import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
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
import org.xml.sax.SAXException;

public class ExperimentResource extends ItemResource {
    public ExperimentResource(Context context, Request request, Response response) {
        super(context, request, response);

        _experimentId = (String) getParameter(request, "EXPT_ID");
        if (StringUtils.isBlank(_experimentId)) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }

        final String projectId = (String) getParameter(request, "PROJECT_ID");
        if (StringUtils.isNotBlank(projectId)) {
            final UserI user = getUser();
            _project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
            _existing = XnatExperimentdata.GetExptByProjectIdentifier(projectId, _experimentId, user, false);
        }

        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public Representation represent(final Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        if (_experiment == null && StringUtils.isNotBlank(_experimentId)) {
            final UserI user = getUser();
            _experiment = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, user, false);

            if (_project != null) {
                if (_experiment == null) {
                    _experiment = XnatExperimentdata.GetExptByProjectIdentifier(_project.getId(), _experimentId, user, false);
                }
            }
        }

        if (_experiment == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified experiment.");
            return null;
        }

        if (StringUtils.equals(filepath, "status")) {
            return returnStatus(_experiment, mediaType);
        } else if (StringUtils.equals(filepath, "history")) {
            try {
                return buildChangesets(_experiment.getItem(), _experiment.getStringProperty("ID"), mediaType);
            } catch (Exception e) {
                logger.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                return null;
            }
        } else if (StringUtils.startsWith(filepath, "projects")) {
            return representProjectsForArchivableItem(_experiment.getLabel(), _experiment.getPrimaryProject(false), _experiment.getProjectDatas(), mediaType);
        } else {
            return representItem(_experiment.getItem(), mediaType);
        }
    }

    @Override
    public void handlePut() {
        try {
            XFTItem template = null;
            if (_existing != null) {
                template = _existing.getItem().getCurrentDBVersion();
            }

            XFTItem item = loadItem(null, true, template);

            final UserI user = getUser();
            if (item == null) {
                String xsiType = getQueryVariable("xsiType");
                if (xsiType != null) {
                    item = XFTItem.NewItem(xsiType, user);
                }
            }

            if (item == null) {
                if (_project != null) {
                    XnatSubjectassessordata om = (XnatSubjectassessordata) XnatSubjectassessordata.GetExptByProjectIdentifier(_project.getId(), _experimentId, user, false);
                    if (om != null) {
                        item = om.getItem();
                    }
                }

                if (item == null) {
                    XnatSubjectassessordata om = (XnatSubjectassessordata) XnatSubjectassessordata.getXnatExperimentdatasById(_experimentId, null, false);
                    if (om != null) {
                        item = om.getItem();
                    }
                }
            }

            if (item == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
                return;
            }

            if (!item.instanceOf("xnat:experimentData")) {
                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only xnat:Subject documents can be PUT to this address.");
                return;
            }

            _experiment = (XnatExperimentdata) BaseElement.GetGeneratedItem(item);

            if (filepath != null && !filepath.equals("")) {
                if (filepath.startsWith("projects/")) {

                    if(!isSharingAllowed(user, _experiment.getProject())){
                        return;
                    }

                    String newProjectS = filepath.substring(9);
                    XnatProjectdata newProject = XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
                    String newLabel = getQueryVariable("label");
                    if (newProject != null) {
                        int index = 0;
                        XnatExperimentdataShare matched = null;
                        for (XnatExperimentdataShareI pp : _experiment.getSharing_share()) {
                            if (pp.getProject().equals(newProject.getId())) {
                                matched = (XnatExperimentdataShare) pp;
                                if (newLabel != null && !pp.getLabel().equals(newLabel)) {
                                    pp.setLabel(newLabel);
                                    BaseXnatExperimentdata.SaveSharedProject((XnatExperimentdataShare) pp, _experiment, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.RENAME_IN_SHARED_PROJECT));
                                }
                                break;
                            }
                            index++;
                        }

                        if (getQueryVariable("primary") != null && getQueryVariable("primary").equals("true")) {
                            changeExperimentPrimaryProject(_experiment, _project, newProject, newLabel, matched, index);
                            return;
                        } else {
                            if (matched == null) {

                                if (newLabel != null) {
                                    XnatExperimentdata temp = XnatExperimentdata.GetExptByProjectIdentifier(newProject.getId(), newLabel, null, false);
                                    if (temp != null) {
                                        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Label already in use:" + newLabel);
                                        return;
                                    }
                                }
                                if (Permissions.canCreate(user, _experiment.getXSIType() + "/project", newProject.getId())) {
                                    shareExperimentToProject(user, newProject, _experiment, newLabel);
                                } else {
                                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in the " + newProject.getId() + " project.");
                                    return;
                                }
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project:" + newProject.getId());
                                return;
                            }
                        }

                        returnDefaultRepresentation();
                    } else {
                        setGuestDataResponse("Unable to identify project: " + newProjectS);
                        return;
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return;
                }
            } else {
                if (_experiment.getLabel() == null) {
                    _experiment.setLabel(_experimentId);
                }
                //MATCH PROJECT
                if (_project == null && _experiment.getProject() != null) {
                    _project = XnatProjectdata.getXnatProjectdatasById(_experiment.getProject(), user, false);
                }

                if (_project != null) {
                    if (_experiment.getProject() == null || _experiment.getProject().equals("")) {
                        _experiment.setProject(_project.getId());
                    } else if (!_experiment.getProject().equals(_project.getId())) {
                        boolean matched = false;
                        for (XnatExperimentdataShareI pp : _experiment.getSharing_share()) {
                            if (pp.getProject().equals(_project.getId())) {
                                matched = true;
                                break;
                            }
                        }

                        if (!matched) {
                            XnatExperimentdataShare pp = new XnatExperimentdataShare(user);
                            pp.setProject(_project.getId());
                            _experiment.setSharing_share(pp);
                        }
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted experiment record must include the project attribute.");
                    return;
                }

                // Find the pre-existing experiment
                if (_existing == null) {
                    _existing = getExistingExperiment(_experiment);
                }

                if (_existing == null) {
                    if (!Permissions.canCreate(user, _experiment)) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in this project.");
                        return;
                    }
                    //IS NEW
                    if (StringUtils.isBlank(_experiment.getId())) {
                        _experiment.setId(XnatExperimentdata.CreateNewID());
                    }

                    setSubject(_experiment.getItem());
                } else {
                    if (StringUtils.isBlank(_experiment.getId())) {
                        _experiment.setId(_existing.getId());
                    }

                    //MATCHED
                    if (!_existing.getProject().equals(_experiment.getProject())) {
                        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Project must be modified through separate URI.");
                        return;
                    }

                    if (!Permissions.canEdit(user, _experiment)) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for experiments in this project.");
                        return;
                    }

                    setSubject(_existing.getItem());

                    if (getQueryVariable("label") != null && !getQueryVariable("label").equals("")) {
                        if (!_experiment.getLabel().equals(_existing.getLabel())) {
                            _experiment.setLabel(_existing.getLabel());
                        }
                        String label = getQueryVariable("label");

                        if (!label.equals(_existing.getLabel())) {
                            XnatExperimentdata match = XnatExperimentdata.GetExptByProjectIdentifier(_project.getId(), label, user, false);
                            if (match != null) {
                                getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified label is already in use.");
                                return;
                            }

                            rename(_project, _existing, label, user);
                        }
                        return;
                    }
                }

                boolean allowDataDeletion = false;
                if (getQueryVariable("allowDataDeletion") != null && getQueryVariable("allowDataDeletion").equals("true")) {
                    allowDataDeletion = true;
                }
                PersistentWorkflowI wrk = WorkflowUtils.buildOpenWorkflow(user, _experiment.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(_experiment.getXSIType(), (_existing == null))));
                EventMetaI c = wrk.buildEvent();

                if (isQueryVariableTrue(XNATRestConstants.FIX_SCAN_TYPES) || containsAction(XNATRestConstants.FIX_SCAN_TYPES)) {
                    if (_experiment instanceof XnatImagesessiondata) {
                        FixScanTypes.builder().experiment(_experiment).user(user).project(_project).allowSave(false).eventMeta(c).build().call();
                    }
                }

                if (StringUtils.isNotBlank(_experiment.getLabel()) && !XftStringUtils.isValidId(_experiment.getId())) {
                    getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in experiment label.");
                    return;
                }

                final ValidationResults vr = _experiment.validate();

                if (vr != null && !vr.isValid()) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, vr.toFullString());
                    return;
                }

                try {
                    //check for unexpected modifications of ID, Project and label
                    if (_existing != null && !StringUtils.equals(_existing.getId(), _experiment.getId())) {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "ID cannot be modified");
                        return;
                    }

                    if (_existing != null && !StringUtils.equals(_existing.getProject(), _experiment.getProject())) {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Project must be modified through separate URI.");
                        return;
                    }

                    //MATCHED
                    if (_existing != null && !StringUtils.equals(_existing.getLabel(), _experiment.getLabel())) {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Label must be modified through separate URI.");
                        return;
                    }

                    // Preserve the previous version of the experiment before we save it.
                    XnatExperimentdata previous = getExistingExperiment(_experiment);

                    if (_existing == null ? create(_experiment, false, allowDataDeletion, wrk, c) : update(_experiment, false, allowDataDeletion, wrk, c)) {
                        if (_project.getArcSpecification().getQuarantineCode() != null && _project.getArcSpecification().getQuarantineCode().equals(1)) {
                            _experiment.quarantine(user);
                        }

                        if (_experiment instanceof XnatImagesessiondata && previous != null) {
                            anonymize((XnatImagesessiondata) _experiment, (XnatImagesessiondata) previous);
                        }
                    }
                } catch (Exception e1) {
                    WorkflowUtils.fail(wrk, c);
                    throw e1;
                }

                postSaveManageStatus(_experiment);

                if (Permissions.canEdit(user, _experiment.getItem())) {
                    if ((isQueryVariableTrue(XNATRestConstants.PULL_DATA_FROM_HEADERS) || containsAction(XNATRestConstants.PULL_DATA_FROM_HEADERS)) && _experiment instanceof XnatImagesessiondata) {
                        try {
                            wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, _experiment.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.DICOM_PULL));
                            assert wrk != null;
                            c = wrk.buildEvent();
                            try {
                                PullSessionDataFromHeaders pull = new PullSessionDataFromHeaders((XnatImagesessiondata) _experiment, user, allowDataDeletion(), isQueryVariableTrue("overwrite"), false, c);
                                pull.call();
                                WorkflowUtils.complete(wrk, c);
                            } catch (Exception e) {
                                WorkflowUtils.fail(wrk, c);
                                throw e;
                            }

                        } catch (SAXException e) {
                            logger.error("Error processing XML", e);
                            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Error processing XML:" + e.getMessage());
                        } catch (ValidationException e) {
                            logger.error("Error validating the item", e);
                            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Error validating the item" + e.getMessage());
                        } catch (Exception e) {
                            logger.error("Unknown error encountered", e);
                            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                            return;
                        }
                    }

                    if (isQueryVariableTrue(XNATRestConstants.TRIGGER_PIPELINES) || containsAction(XNATRestConstants.TRIGGER_PIPELINES)) {
                        XDAT.getContextService().getBean(PipelineService.class).launchAutoRun(_experiment, isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL), user);
                    }
                }
            }

            returnString(_experiment.getId(), (_existing == null) ? Status.SUCCESS_CREATED : Status.SUCCESS_OK);
        } catch (InvalidValueException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.error("", e);
        } catch (ActionException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.error("", e);
        }
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();
        if (_experiment == null && _experimentId != null) {
            _experiment = XnatExperimentdata.getXnatExperimentdatasById(_experimentId, user, false);

            if (_experiment == null && _project != null) {
                _experiment = XnatExperimentdata.GetExptByProjectIdentifier(_project.getId(), _experimentId, user, false);
            }
        }

        if (_experiment == null) {
            setGuestDataResponse("Unable to find the specified experiment.");
            return;
        }

        deleteItem(_project, _experiment);
    }

    private void setSubject(final XFTItem item) throws Exception {
        //MATCH SUBJECT
        XnatSubjectdata subject;
        if (item.instanceOf(XnatSubjectassessordata.SCHEMA_ELEMENT_NAME)) {
            final XnatSubjectassessordata assessor = (XnatSubjectassessordata) _experiment;

            if (StringUtils.isNotBlank(getQueryVariable("subject_ID"))) {
                assessor.setSubjectId(getQueryVariable("subject_ID"));
            }

            if (StringUtils.isNotBlank(assessor.getSubjectId())) {
                subject = getSubject(assessor);

                if (subject == null && _existing != null) {
                    subject = ((XnatSubjectassessordata) _existing).getSubjectData();
                    if (subject != null) {
                        assessor.setSubjectId(subject.getId());
                    }
                }

                if (subject == null) {
                    final String subjectId = XnatSubjectdata.CreateNewID();
                    final UserI user = getUser();
                    subject = new XnatSubjectdata(user);
                    subject.setProject(_project.getId());
                    subject.setLabel(assessor.getSubjectId());
                    subject.setId(subjectId);
                    if (!Permissions.canCreate(user, subject)) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for subjects in this project.");
                        return;
                    }
                    BaseXnatSubjectdata.save(subject, false, true, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.AUTO_CREATE_SUBJECT));
                    assessor.setSubjectId(subject.getId());
                }
            }
        }
    }

    private XnatSubjectdata getSubject(XnatSubjectassessordata assessor) {
        final UserI     user    = getUser();
        XnatSubjectdata subject = XnatSubjectdata.getXnatSubjectdatasById(assessor.getSubjectId(), user, false);
        if (subject != null) {
            return subject;
        }

        if (StringUtils.isNotBlank(assessor.getProject()) && StringUtils.isNotBlank(assessor.getLabel())) {
            subject = XnatSubjectdata.GetSubjectByProjectIdentifier(assessor.getProject(), assessor.getSubjectId(), user, false);
        }
        if (subject != null) {
            return subject;
        }

        for (final XnatExperimentdataShareI pp : assessor.getSharing_share()) {
            subject = XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), assessor.getSubjectId(), user, false);
            if (subject != null) {
                break;
            }
        }
        return subject;
    }

    private void anonymize(final XnatImagesessiondata session, final XnatImagesessiondata previous) throws BaseXnatExperimentdata.UnknownPrimaryProjectException {
        if (XDAT.getBoolSiteConfigurationProperty("rerunProjectAnonOnRename", false) && StringUtils.isNotBlank(session.getSubjectId()) && !StringUtils.equalsIgnoreCase(session.getSubjectId(), previous.getSubjectId())) {
            PersistentWorkflowI anonWrk = null;
            try {
                // re-apply this project's edit script
                anonWrk = WorkflowUtils.buildOpenWorkflow(this.getUser(), session.getItem(), this.newEventInstance(CATEGORY.DATA, Rename.ANONYMIZATION_POST_MODIFICATION));
                session.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) _experiment, _experiment.getProject(), session.getArchiveRootPath(), true));
                WorkflowUtils.complete(anonWrk, anonWrk.buildEvent());
            } catch (TransactionException e) {
                if (anonWrk!=null) {
                    try {
                        WorkflowUtils.fail(anonWrk, anonWrk.buildEvent());
                    } catch (Exception ex) {
                        logger.error("Error saving workflow entry", ex);
                    }
                }
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            } catch (Exception e) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            }
        }
    }

    private XnatExperimentdata getExistingExperiment(XnatExperimentdata currExp) {
        XnatExperimentdata retExp = null;
        if (currExp.getId() != null) {
            retExp = XnatExperimentdata.getXnatExperimentdatasById(currExp.getId(), null, completeDocument);
        }

        final UserI user = getUser();
        if (retExp == null && currExp.getProject() != null && currExp.getLabel() != null) {
            retExp = XnatExperimentdata.GetExptByProjectIdentifier(currExp.getProject(), currExp.getLabel(), user, completeDocument);
        }

        if (retExp == null) {
            for (XnatExperimentdataShareI pp : currExp.getSharing_share()) {
                retExp = XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), pp.getLabel(), user, completeDocument);
                if (retExp != null) {
                    break;
                }
            }
        }
        return retExp;
    }

    private final String _experimentId;

    private XnatProjectdata    _project    = null;
    private XnatExperimentdata _experiment = null;
    private XnatExperimentdata _existing   = null;
}
