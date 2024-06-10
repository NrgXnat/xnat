/*
 * web: org.nrg.xnat.restlet.resources.ScanList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.action.ActionException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;

public class ScanList extends QueryOrganizerResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ScanList.class);

	XnatProjectdata proj=null;
	XnatSubjectdata sub=null;
	XnatImagesessiondata session=null;
	XnatImagescandata scan=null;

	public ScanList(Context context, Request request, Response response) {
		super(context, request, response);

		String pID= (String)getParameter(request,"PROJECT_ID");
		final UserI user = getUser();
		if(pID != null){
				proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

				String subID= (String)getParameter(request,"SUBJECT_ID");
				if(subID!=null){
				sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
						.getId(), subID, user, false);

					if(sub==null){
					sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
																  false);
					if (sub != null
							&& (proj != null && !sub.hasProject(proj.getId()))) {
						sub = null;
					}
					}

					if(sub!=null){
					String exptID = (String) getParameter(request,
							"ASSESSED_ID");
					session = XnatImagesessiondata
							.getXnatImagesessiondatasById(exptID, user, false);
					if (session != null
							&& (proj != null && !session.hasProject(proj
									.getId()))) {
						session = null;
					}

						if(session==null){
						session = (XnatImagesessiondata) XnatImagesessiondata
								.GetExptByProjectIdentifier(proj.getId(),
															exptID, user, false);
						}

						if(session!=null){
						this.getVariants().add(
								new Variant(MediaType.APPLICATION_JSON));
						this.getVariants()
								.add(new Variant(MediaType.TEXT_HTML));
							this.getVariants().add(new Variant(MediaType.TEXT_XML));
						}else{
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
						}
					}else{
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					}
				}else{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}else{
				String exptID= (String)getParameter(request,"ASSESSED_ID");
			session = XnatImagesessiondata.getXnatImagesessiondatasById(exptID,
																		user, false);

				if(session==null){
				session = (XnatImagesessiondata) XnatImagesessiondata
						.GetExptByProjectIdentifier(proj.getId(), exptID, user,
													false);
				}

				if(session!=null){
					this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
					this.getVariants().add(new Variant(MediaType.TEXT_HTML));
					this.getVariants().add(new Variant(MediaType.TEXT_XML));
				}else{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XnatImagescandata.SCHEMA_ELEMENT_NAME,true));

	}


	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		XFTItem item = null;

		try {
			String dataType=null;
			if(this.session instanceof XnatMrsessiondata){
				dataType="xnat:mrScanData";
			}else if(this.session instanceof XnatPetsessiondata){
				dataType="xnat:petScanData";
			}else if(this.session instanceof XnatCtsessiondata){
				dataType="xnat:ctScanData";
			}
			item=this.loadItem(dataType,true);

			final UserI user = getUser();
			if(item == null){
				String xsiType=this.getQueryVariable("xsiType");
				if(xsiType!=null){
					item=XFTItem.NewItem(xsiType, user);
				}
			}

			if(item==null){
				this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
				return;
			}

			if(item.instanceOf("xnat:imageScanData")){
				scan = (XnatImagescandata)BaseElement.GetGeneratedItem(item);

				//MATCH SESSION
				if(this.session!=null){
					scan.setImageSessionId(this.session.getId());
				}else{
					if(scan.getImageSessionId()!=null && !scan.getImageSessionId().equals("")){
						this.session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(scan.getImageSessionId(), user, false);

						if(this.session==null && this.proj!=null){
							this.session=(XnatImagesessiondata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), scan.getImageSessionId(), user, false);
						}
						if(this.session!=null){
							scan.setImageSessionId(this.session.getId());
						}
					}
				}

				if(scan.getImageSessionId()==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED,"Specified scan must reference a valid image session.");
					return;
				}

				if(this.session==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Specified image session doesn't exist.");
					return;
				}

				if(this.getQueryVariable("type")!=null){
					scan.setType(this.getQueryVariable("type"));
				}

				//FIND PRE-EXISTING
				XnatImagescandata existing=null;

				if(scan.getXnatImagescandataId()!=null){
					existing=(XnatImagescandata)XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scan.getXnatImagescandataId(), user, completeDocument);
				}

				if(scan.getId()!=null){
					CriteriaCollection cc= new CriteriaCollection("AND");
					cc.addClause("xnat:imageScanData/ID", scan.getId());
					cc.addClause("xnat:imageScanData/image_session_ID", scan.getImageSessionId());
					ArrayList<XnatImagescandata> scans=XnatImagescandata.getXnatImagescandatasByField(cc, user, completeDocument);
					if(scans.size()>0){
						existing=scans.get(0);
					}
				}

				if(existing==null){
					if(!Permissions.canEdit(user, this.session)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for sessions in this project.");
						return;
					}
					//IS NEW
					if(scan.getId()==null || scan.getId().equals("")){
						String query = "SELECT count(id) AS id_count FROM xnat_imageScanData WHERE image_session_id='" + this.session.getId() + "' AND id='";

						String login = null;
						if (user != null){
							login= user.getUsername();
						}
						try {
							int i=1;
							Long idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + i + "';", "id_count", user.getDBName(), login);
							while (idCOUNT > 0){
								i++;
								idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + i + "';", "id_count", user.getDBName(), login);
							}

							scan.setId("" + i);
						} catch (Exception e) {
							logger.error("",e);
						}
					}
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified scan already exists.");
					return;
					//MATCHED
				}

				boolean allowDataDeletion=false;
				if(this.getQueryVariable("allowDataDeletion")!=null && this.getQueryVariable("allowDataDeletion").equals("true")){
					allowDataDeletion=true;
				}



				final ValidationResults vr = scan.validate();

				if (vr != null && !vr.isValid())
				{
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,vr.toFullString());
					return;
				}
		           Authorizer.getInstance().authorizeSave(session.getItem(), user);

				create(session, scan, false, allowDataDeletion, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(scan.getXSIType(), scan==null)));
				
				this.returnSuccessfulCreateFromList(scan.getId());
			}else{
				this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only Scan documents can be PUT to this address.");
			}
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
			return;
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			logger.error("",e);
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.error("",e);
		}
	}



	@Override
	public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
		ArrayList<String> al=new ArrayList<String>();
		al.add("xnat_imagescandata_id");
		al.add("ID");
		al.add("type");
		al.add("quality");
		al.add("xsiType");
		al.add("note");
		al.add("series_description");

		return al;
	}

	public String getDefaultElementName(){
		return "xnat:imageScanData";
	}

	@Override
	public Representation getRepresentation(Variant variant) {
		if(scan!=null){
			return new ItemXMLRepresentation(scan.getItem(),MediaType.TEXT_XML);
		}else{
			Representation rep=super.getRepresentation(variant);
			if(rep!=null)return rep;

			XFTTable table;
			try {
				final String re=this.getRootElementName();

				final UserI user = getUser();
				final QueryOrganizer qo = new QueryOrganizer(re, user, ViewManager.ALL);

				this.populateQuery(qo);

				CriteriaCollection cc= new CriteriaCollection("AND");
				cc.addClause(re+"/image_session_id", session.getId());
				qo.setWhere(cc);

				String query = qo.buildQuery();

					table=XFTTable.Execute(query, user.getDBName(), userName);

				table = formatHeaders(table, qo, "xnat:imageScanData/ID",
						String.format("/data/experiments/%s/scans/",session.getId()));
			} catch (Exception e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}

			MediaType mt = overrideVariant(variant);
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			if (table != null)
				params.put("totalRecords", table.size());
			return this.representTable(table, mt, params);
		}
	}
}
