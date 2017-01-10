/*
 * web: org.nrg.xnat.restlet.resources.ProjSubExptList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.TriggerPipelines;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProjSubExptList extends SubjAssessmentAbst {
	XnatProjectdata proj=null;
	XnatSubjectdata subject=null;

	String pID=null;
	String subID=null;

	public ProjSubExptList(Context context, Request request, Response response) {
		super(context, request, response);

		pID = (String) getParameter(request, "PROJECT_ID");
		if (pID != null) {
			final UserI user = getUser();

			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

			if (proj == null) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
								   "Unable to identify project " + pID);
				return;
			}

			subID = (String) getParameter(request, "SUBJECT_ID");
			if (subID != null) {
				subject = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);

				if (subject == null) {
					subject = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);
					if (subject != null
						&& (proj != null && !subject.hasProject(proj
																		.getId()))) {
						subject = null;
					}
				}

				if (subject != null) {
					this.getVariants().add(
							new Variant(MediaType.APPLICATION_JSON));
					this.getVariants().add(new Variant(MediaType.TEXT_HTML));
					this.getVariants().add(new Variant(MediaType.TEXT_XML));
				} else {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
									   "Unable to identify subject " + subID);
				}
			} else {
				this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
				this.getVariants().add(new Variant(MediaType.TEXT_HTML));
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.EXPERIMENT_DATA, true));
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		try {
			final UserI user = getUser();

			XFTItem item = loadItem(null, true);

			if (item == null) {
				String xsiType = this.getQueryVariable("xsiType");
				if (xsiType != null) {
					item = XFTItem.NewItem(xsiType, user);
				}
			}

			if (item == null) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
				return;
			}

			if (item.instanceOf("xnat:subjectAssessorData")) {
				XnatSubjectassessordata expt = (XnatSubjectassessordata) BaseElement.GetGeneratedItem(item);

				//MATCH PROJECT
				if (this.proj == null && expt.getProject() != null) {
					proj = XnatProjectdata.getXnatProjectdatasById(expt.getProject(), user, false);
				}

				if (this.proj != null) {
					if (expt.getProject() == null || expt.getProject().equals("")) {
						expt.setProject(this.proj.getId());
					} else if (expt.getProject().equals(this.proj.getId())) {
					} else {
						boolean matched = false;
						for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
							if (pp.getProject().equals(this.proj.getId())) {
								matched = true;
								break;
							}
						}

						if (!matched) {
							XnatExperimentdataShare pp = new XnatExperimentdataShare((UserI) user);
							pp.setProject(this.proj.getId());
							expt.setSharing_share(pp);
						}
					}
				} else {
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted experiment record must include the project attribute.");
					return;
				}

				//MATCH SUBJECT
				if (this.subject != null) {
					expt.setSubjectId(this.subject.getId());
				} else {
					if (expt.getSubjectId() != null && !expt.getSubjectId().equals("")) {
						this.subject = XnatSubjectdata.getXnatSubjectdatasById(expt.getSubjectId(), user, false);

						if (this.subject == null && expt.getProject() != null && expt.getLabel() != null) {
							this.subject = XnatSubjectdata.GetSubjectByProjectIdentifier(expt.getProject(), expt.getSubjectId(), user, false);
						}

						if (this.subject == null) {
							for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
								this.subject = XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), expt.getSubjectId(), user, false);
								if (this.subject != null) {
									break;
								}
							}
						}

						if (this.subject == null) {
							this.subject = new XnatSubjectdata((UserI) user);
							this.subject.setProject(this.proj.getId());
							this.subject.setLabel(expt.getSubjectId());
							this.subject.setId(XnatSubjectdata.CreateNewID());
							create(this.subject, false, true, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.AUTO_CREATE_SUBJECT));
							expt.setSubjectId(this.subject.getId());
						}
					}
				}

				if (this.subject == null) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted experiment record must include the subject.");
					return;
				}

				//FIND PRE-EXISTING
				XnatSubjectassessordata existing = null;
				if (expt.getId() != null) {
					existing = (XnatSubjectassessordata) XnatExperimentdata.getXnatExperimentdatasById(expt.getId(), user, completeDocument);
				}

				if (existing == null && expt.getProject() != null && expt.getLabel() != null) {
					existing = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(expt.getProject(), expt.getLabel(), user, completeDocument);
				}

				if (existing == null) {
					for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
						existing = (XnatSubjectassessordata) XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), pp.getLabel(), user, completeDocument);
						if (existing != null) {
							break;
						}
					}
				}

				if (existing == null) {
					if (!Permissions.canCreate(user, expt)) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for experiments in this project.");
						return;
					}
					//IS NEW
					if (expt.getId() == null || expt.getId().equals("")) {
						expt.setId(XnatExperimentdata.CreateNewID());
					}
				} else {
					this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified experiment already exists.");
					return;
					//MATCHED
				}

				boolean allowDataDeletion = false;
				if (this.getQueryVariable("allowDataDeletion") != null && this.getQueryVariable("allowDataDeletion").equals("true")) {
					allowDataDeletion = true;
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

				create(expt, false, allowDataDeletion, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(expt.getXSIType(), (existing == null))));

				postSaveManageStatus(expt);

				if (Permissions.canEdit(user, expt.getItem())) {
					if (this.isQueryVariableTrue(XNATRestConstants.TRIGGER_PIPELINES) || this.containsAction(XNATRestConstants.TRIGGER_PIPELINES)) {
						TriggerPipelines tp = new TriggerPipelines(expt, this.isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL), user);
						tp.call();
					}
				}

				this.returnSuccessfulCreateFromList(expt.getId());
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only xnat:Subject documents can be PUT to this address.");
			}
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
			return;
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			logger.error("", e);
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.error("", e);
		}
	}

	@Override
	public Representation represent(Variant variant) {
		Representation rep=super.represent(variant);
		if(rep!=null) {
			return rep;
		}

		XFTTable table = null;

		if(proj==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify project " + pID);
			return null;
		}

		final UserI user = getUser();

		try {
			final SecurityValues values = new SecurityValues();
			values.put("xnat:subjectData/project", proj.getId());
			values.put("xnat:subjectData/sharing/share/project", proj.getId());

			final SchemaElement se= SchemaElement.GetElement(XnatSubjectdata.SCHEMA_ELEMENT_NAME);

			if (!Permissions.canRead(user,se,values))
			{
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Unable to read experiments for Project: " + proj.getId());
				return null;
			}
		} catch (Exception e1) {
			logger.error("", e1);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}

		if(subject!=null){
			subID=subject.getId();
		}

		try {
			final String rootElementName=this.getRootElementName();
			final QueryOrganizer qo = new QueryOrganizer(rootElementName,user,ViewManager.ALL);

			this.populateQuery(qo);

			CriteriaCollection where=new CriteriaCollection("AND");

			CriteriaCollection cc= new CriteriaCollection("OR");
			cc.addClause(rootElementName+"/project", proj.getId());
			cc.addClause(rootElementName+"/sharing/share/project", proj.getId());
			where.addClause(cc);

			if(subID!=null){
				CriteriaCollection cc2= new CriteriaCollection("AND");
				cc2.addClause("xnat:subjectAssessorData/subject_id", subID);
				where.addClause(cc2);
			}

            if(!ElementSecurity.IsSecureElement(rootElementName)){
                qo.addField("xnat:experimentData/extension_item/element_name");
                qo.addField("xnat:experimentData/project");
            }

			qo.setWhere(where);

			String query=qo.buildQuery();

			table=XFTTable.Execute(query, user.getDBName(), userName);

			if(table.size()>0){
				 if(!ElementSecurity.IsSecureElement(rootElementName)){
	                    List<Object[]> remove= new ArrayList<>();
	                    Hashtable<String, Boolean> checked = new Hashtable<>();

	                    String enS=qo.getFieldAlias("xnat:experimentData/extension_item/element_name");
	                    if(enS==null) {
	                        logger.error("Couldn't find property xnat:experimentData/extension_item/element_name for search",new Exception());
	                        this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	                        return null;
	                    }

	                    Integer en=table.getColumnIndex(enS.toLowerCase());
	                    
	                    for(Object[] row : table.rows()) {
	                        String element_name=(String)row[en];
	                        try{
	                            if(element_name==null){
	                                remove.add(row);
	                            }else{

	                                if(!checked.containsKey(element_name)){
	                                    if(Permissions.canRead(user, element_name + "/project",proj.getId())){
	                                        checked.put(element_name, Boolean.TRUE);
	                                    }else{
	                                        checked.put(element_name, Boolean.FALSE);
	                                    }
	                                }

	                                if(!checked.get(element_name)){
	                                    remove.add(row);
	                                }
	                            }
	                        } catch (Throwable e) {
	                            logger.debug("Problem occurred iterating secure elements", e);
	                            remove.add(row);
	                        }
	                    }

	                    table.rows().removeAll(remove);
	                }
				
				table=formatHeaders(table,qo,rootElementName+"/ID","/data/experiments/");

				final Integer labelI=table.getColumnIndex("label");
				final Integer idI=table.getColumnIndex(rootElementName+"/ID");
				if(labelI!=null && idI!=null){
					final XFTTable t= XFTTable.Execute("SELECT sharing_share_xnat_experimentda_id as id,label FROM xnat_experimentData_share WHERE project='"+ proj.getId() + "'", user.getDBName(), user.getUsername());
					final Hashtable lbls=t.toHashtable("id", "label");
					for(Object[] row:table.rows()){
						final String id=(String)row[idI];
						if(lbls.containsKey(id)){
							final String lbl=(String)lbls.get(id);
							if(null!=lbl && !lbl.equals("")){
								row[labelI]=lbl;
							}
						}
					}
				}
			}
			} catch (Exception e) {
				logger.error("",e);
			}

		Hashtable<String,Object> params= new Hashtable<>();
		if (table != null)
			params.put("totalRecords", table.size());
		return this.representTable(table, overrideVariant(variant), params);
	}
}
