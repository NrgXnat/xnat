/*
 * web: org.nrg.xnat.restlet.resources.SubjectResource
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
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.representations.TurbineScreenRepresentation;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.xml.sax.SAXParseException;

import static org.nrg.xft.event.XftItemEventI.CREATE;

public class SubjectResource extends ItemResource {
    private static final String PRIMARY = "primary";

    protected XnatProjectdata proj = null;
    protected String subID;
    protected XnatSubjectdata sub = null;
    protected XnatSubjectdata existing = null;

    public SubjectResource(Context context, Request request, Response response) {
        super(context, request, response);

        final UserI  user = getUser();
        final String pID  = (String) getParameter(request, "PROJECT_ID");
        if (pID != null) {
            proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
        }

        subID = (String) getParameter(request, "SUBJECT_ID");

        if (proj != null) {
            existing = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);
        }

        if (existing == null) {
            existing = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);
            if (existing != null && (proj != null && !existing.hasProject(proj.getId()))) {
                existing = null;
            }
        }

        this.getVariants().add(new Variant(MediaType.TEXT_HTML));
        this.getVariants().add(new Variant(MediaType.TEXT_XML));

        this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.SUBJECT_DATA, false));
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();
            XFTItem template = null;
            if (existing != null) {
                template = existing.getItem().getCurrentDBVersion();
            }

            XFTItem item = this.loadItem("xnat:subjectData", true, (this.isQueryVariableFalse("loadExisting")) ? null : template);

            if (item == null) {
                item = XFTItem.NewItem("xnat:subjectData", user);
            }

            if (item.instanceOf("xnat:subjectData")) {
                sub = new XnatSubjectdata(item);

                if (filepath != null && !filepath.equals("")) {
                    if (filepath.startsWith("projects/")) {
                        if (!Permissions.canRead(user,sub)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient privileges for subjects in this project.");
                            return;
                        }

                        String newProjectS = filepath.substring(9);
                        XnatProjectdata newProject = XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
                        String newLabel = this.getQueryVariable("label");

                        if (newProject != null) {
                            XnatProjectparticipant matched = null;
                            int index = 0;
                            for (XnatProjectparticipantI pp : sub.getSharing_share()) {
                                if (pp.getProject().equals(newProject.getId())) {
                                    matched = ((XnatProjectparticipant) pp);
                                    if (newLabel != null && (pp.getLabel() == null || (!pp.getLabel().equals(newLabel)))) {
                                        XnatSubjectdata temp = XnatSubjectdata.GetSubjectByProjectIdentifier(newProject.getId(), newLabel, null, false);
                                        if (temp != null) {
                                            this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Label already in use:" + newLabel);
                                            return;
                                        }

                                        pp.setLabel(newLabel);
                                        BaseXnatSubjectdata.SaveSharedProject((XnatProjectparticipant) pp, sub, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.CONFIGURED_PROJECT_SHARING));

                                        if (!this.isQueryVariableTrue(PRIMARY)) {
                                            this.returnDefaultRepresentation();
                                            return;
                                        }
                                    }
                                    break;
                                }
                                index++;
                            }

                            if (newLabel != null) {
                                XnatSubjectdata existing = XnatSubjectdata.getXnatSubjectdatasById(sub.getId(), user, false);
                                if (existing != null && !sub.getLabel().equals(existing.getLabel())) {
                                    sub.setLabel(existing.getLabel());
                                }
                            }

                            if (this.isQueryVariableTrue(PRIMARY)) {
                                if (!Permissions.canDelete(user,sub)) {
                                    this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient privileges for subjects in this project.");
                                    return;
                                }

                                EventMetaI c = BaseXnatSubjectdata.ChangePrimaryProject(user, sub, newProject, newLabel, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.MODIFY_PROJECT));

                                if (matched != null) {
                                    SaveItemHelper.authorizedRemoveChild(sub.getItem(), "xnat:subjectData/sharing/share", matched.getItem(), user, c);
                                    sub.removeSharing_share(index);
                                }
                            } else {
                                if (matched == null) {
                                    if (newLabel != null) {
                                        XnatSubjectdata temp = XnatSubjectdata.GetSubjectByProjectIdentifier(newProject.getId(), newLabel, null, false);
                                        if (temp != null) {
                                            this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Label already in use:" + newLabel);
                                            return;
                                        }
                                    }
                                    if (Permissions.canCreate(user,sub.getXSIType() + "/project", newProject.getId())) {
                                        XnatProjectparticipant pp = new XnatProjectparticipant(user);
                                        pp.setProject(newProject.getId());
                                        if (newLabel != null) pp.setLabel(newLabel);
                                        pp.setSubjectId(sub.getId());
                                        BaseXnatSubjectdata.SaveSharedProject(pp, sub, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.CONFIGURED_PROJECT_SHARING));
                                    } else {
                                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for subjects in the " + newProject.getId() + " project.");
                                        return;
                                    }
                                } else {
                                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project:" + newProject.getId());
                                    return;
                                }
                            }

                            this.returnDefaultRepresentation();
                        } else {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to identify project: " + newProjectS);
                        }
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {

                    if (this.proj == null && sub.getProject() != null) {
                        proj = XnatProjectdata.getXnatProjectdatasById(sub.getProject(), user, false);
                    }

                    if (this.proj != null) {
                        if (sub.getProject() == null || sub.getProject().equals("")) {
                            sub.setProject(this.proj.getId());

                            if (sub.getLabel() == null || sub.getLabel().equals("")) {
                                sub.setLabel(this.subID);
                            }
                        } else {
                            if (sub.getProject().equals(this.proj.getId())) {
                                if (sub.getLabel() == null || sub.getLabel().equals("")) {
                                    sub.setLabel(this.subID);
                                }
                            } else {
                                boolean matched = false;
                                for (XnatProjectparticipantI pp : sub.getSharing_share()) {
                                    if (pp.getProject().equals(this.proj.getId())) {
                                        matched = true;

                                        if (pp.getLabel() == null || pp.getLabel().equals("")) {
                                            pp.setLabel(this.subID);
                                        }
                                        break;
                                    }
                                }

                                if (!matched) {
                                    XnatProjectparticipant pp = new XnatProjectparticipant(user);
                                    pp.setProject(this.proj.getId());
                                    pp.setLabel(this.subID);
                                }
                            }
                        }
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted subject record must include the project attribute.");
                        return;
                    }

                    if (existing == null) {
                        if (sub.getId() != null) {
                            existing = XnatSubjectdata.getXnatSubjectdatasById(sub.getId(), user, false);
                        }

                        if (existing == null && sub.getProject() != null && sub.getLabel() != null) {
                            existing = XnatSubjectdata.GetSubjectByProjectIdentifier(sub.getProject(), sub.getLabel(), user, false);
                        }

                        if (existing == null) {
                            for (XnatProjectparticipantI pp : sub.getSharing_share()) {
                                existing = XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), pp.getLabel(), user, false);
                                if (existing != null) {
                                    break;
                                }
                            }
                        }
                    }


                    if (existing == null) {
                        if (!Permissions.canCreate(user,sub)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for subjects in this project.");
                            return;
                        }
                        //IS NEW
                        if (sub.getId() == null || sub.getId().equals("")) {
                            sub.setId(XnatSubjectdata.CreateNewID());
                        }


                    } else {
                        if (!existing.getProject().equals(sub.getProject())) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Project must be modified through separate URI.");
                            return;
                        }

                        if (!Permissions.canEdit(user,sub)) {
                            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient edit privileges for subjects in this project.");
                            return;
                        }
                        if (sub.getId() == null || sub.getId().equals("")) {
                            sub.setId(existing.getId());
                        }
                        						
						if(this.getQueryVariable("label")!=null && !this.getQueryVariable("label").equals("") ){
							String label=this.getQueryVariable("label");
							
							if(!label.equals(existing.getLabel())){

								if(!sub.getLabel().equals(existing.getLabel())){
									//set to old label
									sub.setLabel(existing.getLabel());
								}

								XnatSubjectdata match=XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), label,user, false);
								if(match!=null){
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified label is already in use.");
									return;
								}

								rename(proj, existing, label, user);
							}
							return;
						}
                    }

                    if (this.getQueryVariable("gender") != null) {
                        sub.setProperty("xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender", this.getQueryVariable("gender"));
                    }

                    if (StringUtils.isNotBlank(sub.getLabel()) && !XftStringUtils.isValidId(sub.getId())) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in subject label.");
                        return;
                    }

                    final ValidationResults vr = sub.validate();
                    if (vr != null && !vr.isValid()) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, vr.toFullString());
                        return;
                    }
                    
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, sub.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(sub.getXSIType(), (existing == null))));
                    EventMetaI c = wrk.buildEvent();

                    try {
						//check for unexpected modifications of ID and Project
						if(existing !=null && !StringUtils.equals(existing.getId(),sub.getId())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"ID cannot be modified");
							return;
						}
						
						if(existing !=null && !StringUtils.equals(existing.getProject(),sub.getProject())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Project must be modified through separate URI.");
							return;
						}                       


                        // Save the experiment.
                        if (SaveItemHelper.authorizedSave(sub, user, false, this.isQueryVariableTrue("allowDataDeletion"), c)) {
                            XDAT.triggerXftItemEvent(sub, CREATE);
                            WorkflowUtils.complete(wrk, c);
        					Users.clearCache(user);
                            MaterializedView.deleteByUser(user);

                            // If the label was changed, re apply the anonymization script on all the subject's imaging sessions.
                            boolean applyAnonScript = (null != existing && !(existing.getLabel().equals(sub.getLabel())));

                            if(applyAnonScript){
                               for(final XnatSubjectassessordata expt : sub.getExperiments_experiment("xnat:imageSessionData")){
                                    try{
                                       // re-apply this project's edit script
                                       expt.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) expt, sub.getLabel(), expt.getProject(), expt.getArchiveRootPath()));
                                    }
                                    catch (TransactionException e) {
                                       this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                                    }
                               }
                            }
                        }
                    } catch (Exception e) {
                        WorkflowUtils.fail(wrk, c);
                        throw e;
                    }
                    
                    postSaveManageStatus(sub);

                    returnString(sub.getId(), (existing == null) ? Status.SUCCESS_CREATED : Status.SUCCESS_OK);
                }
            } else {
                this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only xnat:Subject documents can be PUT to this address.");
            }
        } catch (SAXParseException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (InvalidValueException e) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.error("", e);
        } catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
		} catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.error("", e);
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();
        if (sub == null && subID != null) {
            sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);

            if (sub == null && proj != null) {
                sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);
            }
        }
        if (sub == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified subject.");
        } else {
            deleteItem(proj, sub);
        }
    }

    @Override
    public Representation represent(Variant variant) {
        MediaType mt = overrideVariant(variant);

        final UserI user = getUser();

        if (sub == null && subID != null) {
            sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);

            if (sub == null && proj != null) {
                sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);
            }
        }

        if (sub != null) {
            String filepath = getRequest().getResourceRef().getRemainingPart();
            if (filepath != null && filepath.contains("?")) {
                filepath = filepath.substring(0, filepath.indexOf("?"));
            }
            if (filepath != null && filepath.startsWith("/")) {
                filepath = filepath.substring(1);
            }
            if (filepath != null && filepath.equals("status")) {
                return returnStatus(sub, mt);
            } else if (StringUtils.startsWith(filepath, "projects")) {
                return representProjectsForArchivableItem(sub.getLabel(), sub.getPrimaryProject(false), sub.getProjectDatas(), mt);
            } else {
                return representItem(sub.getItem(), mt);
            }
        } else {
            final StringBuilder message = new StringBuilder("Unable to find the specified subject. ");
            if (proj == null) {
                message.append("When searching by subject ID only, you must specify the accession number and not the subject label, which is not unique across the XNAT system. ");
                message.append(subID).append(" is not a known subject accession ID.");
            } else {
                message.append("The project ").append(proj.getId()).append(" does not contain a subject identifiable by the ID or label ").append(subID).append(".");
            }
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, message.toString());
            return null;
        }

    }

    @Override
    public Representation representItem(XFTItem item, MediaType mt) {
        Representation representation = super.representItem(item, mt);

        if (representation != null && proj != null && representation instanceof TurbineScreenRepresentation && StringUtils.isNotBlank(proj.getId())) {
            // provides appropriate rendering if the caller is querying this subject in the context of a shared project
            ((TurbineScreenRepresentation) representation).setRunDataParameter("project", proj.getId());
        }

        return representation;
    }

}
