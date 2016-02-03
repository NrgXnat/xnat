/*
 * org.nrg.xnat.restlet.resources.files.XNATTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources.files;

import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class XNATTemplate extends SecureResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(XNATTemplate.class);

	XnatProjectdata proj=null;
	XnatSubjectdata sub=null;

	ArrayList<XnatExperimentdata> expts = new ArrayList<XnatExperimentdata>();

	ArrayList<XnatImagescandata> scans = new ArrayList<XnatImagescandata>();

	ArrayList<XnatReconstructedimagedata> recons = new ArrayList<XnatReconstructedimagedata>();

	ArrayList<XnatExperimentdata> assesseds = new ArrayList<XnatExperimentdata>();

	String type=null;

	ItemI parent = null;

	ItemI security = null;

	String xmlPath = null;

	public XNATTemplate(Context context, Request request, Response response) {
		super(context, request, response);

			String pID= (String)getParameter(request,"PROJECT_ID");
			if(pID!=null){
				proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

				if(proj==null){
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Unable to identify project");
					return;
				}
			}

			String subID= (String)getParameter(request,"SUBJECT_ID");
			if(subID!=null){
				if(this.proj!=null)
				sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
						.getId(), subID,user, false);

				if(sub==null){
				sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
						false);
				if(sub!=null && (proj!=null && !sub.hasProject(proj.getId()))){
				    sub=null;
				}
				}

				if(sub==null){
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Unable to identify subject");
					return;
				}
			}

		String assessid = (String) getParameter(request,
				"ASSESSED_ID");
			if(assessid!=null){
			for(String s: StringUtils.CommaDelimitedStringToArrayList(assessid)){
				XnatExperimentdata assessed = XnatImagesessiondata.getXnatImagesessiondatasById(
					s, user, false);

				if(assessed!=null && (proj!=null && !assessed.hasProject(proj.getId()))){
				    assessed=null;
				}

				if (assessed == null && proj!=null) {
					assessed = (XnatImagesessiondata) XnatImagesessiondata
							.GetExptByProjectIdentifier(proj.getId(), s,
									user, false);
				}

				if(assessed!=null){
					try {
						if(assessed.canRead(user))
							assesseds.add(assessed);
					} catch (Exception e) {
					}
				}

				if (assesseds.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
							"Unable to identify image session");
					return;
				}
			}
		}

			type= (String)getParameter(request,"TYPE");

			String exptID= (String)getParameter(request,"EXPT_ID");
			if(exptID!=null){
			for(String s: StringUtils.CommaDelimitedStringToArrayList(exptID)){
				XnatExperimentdata expt = XnatExperimentdata.getXnatExperimentdatasById(s,
						user, false);

				if (expt == null && proj!=null) {
					expt = (XnatExperimentdata) XnatExperimentdata
							.GetExptByProjectIdentifier(proj.getId(), s,
									user, false);
				}

				if (expt != null && assesseds.size()>0) {
					if(type==null){
						type="out";
					}
				}

				if(expt!=null){
					try {
						if(expt.canRead(user))expts.add(expt);
					} catch (Exception e) {}
				}else if(assesseds.size()>0){
					for(XnatExperimentdata assessed:assesseds)
						for(XnatImageassessordataI iad:((XnatImagesessiondata)assessed).getMinimalLoadAssessors()){
							if(iad.getId().equals(s)
									|| (iad.getLabel()!=null && iad.getLabel().equals(s))){
								try {
									if(((XnatImageassessordata)iad).canRead(user))expts.add(((XnatImageassessordata)iad));
								} catch (Exception e) {
								}
							}else if(s.equals("*") || s.equals("ALL")){
								try {
									if(((XnatImageassessordata)iad).canRead(user))expts.add(((XnatImageassessordata)iad));
								} catch (Exception e) {
								}
							}else{
								try {
									GenericWrapperElement gwe = GenericWrapperElement.GetElement(s);

									if(((XnatImageassessordata)iad).getItem().instanceOf(gwe.getFullXMLName())){
										if(((XnatImageassessordata)iad).canRead(user))expts.add(((XnatImageassessordata)iad));
									}
								} catch (Exception e) {
								}
							}
						}
				}
			}

			if (expts.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Unable to identify experiment");
					return;
				}
			}

		String scanID= (String)getUrlEncodedParameter(request,"SCAN_ID");
		if (scanID != null && this.assesseds.size()>0) {
			
			scanID=scanID.replace("[SLASH]", "/");//this is such an ugly hack.  If a slash is included in the scan type and thus in the URL, it breaks the GET command.  Even if it is properly escaped.  So, I'm adding this alternative encoding of slash to allow us to work around the issue.  Hopefully Spring MVC will eliminate it.
			
			CriteriaCollection cc = new CriteriaCollection("OR");
			for(XnatExperimentdata assessed:this.assesseds){
				CriteriaCollection subcc = new CriteriaCollection("AND");
				subcc.addClause("xnat:imageScanData/image_session_ID", assessed
						.getId());
				if(scanID.equals("*") || scanID.equals("ALL")){

				}else if(scanID.indexOf(",")==-1){
					subcc.addClause("xnat:imageScanData/ID", scanID);
				}else{
					CriteriaCollection subsubcc = new CriteriaCollection("OR");
					for(String s:StringUtils.CommaDelimitedStringToArrayList(scanID, true)){
						subsubcc.addClause("xnat:imageScanData/ID", s);
					}
					subcc.add(subsubcc);
				}
				cc.add(subcc);
				
				subcc = new CriteriaCollection("AND");
				subcc.addClause("xnat:imageScanData/image_session_ID", assessed
						.getId());
				if(scanID.equals("*") || scanID.equals("ALL")){

				}else if(scanID.indexOf(",")==-1){
					if(scanID.equals("NULL")){
						CriteriaCollection subsubcc = new CriteriaCollection("OR");
						subsubcc.addClause("xnat:imageScanData/type",""," IS NULL ",true);
						subsubcc.addClause("xnat:imageScanData/type","");
						subcc.add(subsubcc);
					}else{
						subcc.addClause("xnat:imageScanData/type", scanID.replace("[COMMA]", ","));
					}
				}else{
					CriteriaCollection subsubcc = new CriteriaCollection("OR");
					for(String s:StringUtils.CommaDelimitedStringToArrayList(scanID, true)){
						if(s.equals("NULL")){
							subsubcc.addClause("xnat:imageScanData/type",""," IS NULL ",true);
							subsubcc.addClause("xnat:imageScanData/type","");
						}else{
							subsubcc.addClause("xnat:imageScanData/type", s.replace("[COMMA]", ","));
				}
					}
					subcc.add(subsubcc);
				}
				cc.add(subcc);
			}

			scans = XnatImagescandata
					.getXnatImagescandatasByField(cc, user,
							completeDocument);

			if (scans.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Unable to identify scan");
					return;
				}
			}

		String reconID= (String)getUrlEncodedParameter(request,"RECON_ID");
		if (reconID != null && assesseds.size()>0) {
			CriteriaCollection cc = new CriteriaCollection("OR");

			for(XnatExperimentdata assessed:this.assesseds){
				CriteriaCollection subcc = new CriteriaCollection("AND");
				subcc.addClause("xnat:reconstructedImageData/image_session_ID",
						assessed.getId());
				if(reconID.equals("*") || reconID.equals("ALL")){

				}else if(reconID.indexOf(",")==-1){
					subcc.addClause("xnat:reconstructedImageData/ID", reconID);
				}else{
					CriteriaCollection subsubcc = new CriteriaCollection("OR");
					for(String s:StringUtils.CommaDelimitedStringToArrayList(reconID, true)){
						subsubcc.addClause("xnat:reconstructedImageData/ID", s);
					}
					subcc.add(subsubcc);
				}
				cc.add(subcc);

				subcc = new CriteriaCollection("AND");
				subcc.addClause("xnat:reconstructedImageData/image_session_ID",
						assessed.getId());
				if(reconID.equals("*") || reconID.equals("ALL")){

				}else if(reconID.indexOf(",")==-1){
					if(reconID.equals("NULL")){
						CriteriaCollection subsubcc = new CriteriaCollection("OR");
						subsubcc.addClause("xnat:reconstructedImageData/type",""," IS NULL ",true);
						subsubcc.addClause("xnat:reconstructedImageData/type","");
						subcc.add(subsubcc);
					}else{
						subcc.addClause("xnat:reconstructedImageData/type", reconID);
					}
				}else{
					CriteriaCollection subsubcc = new CriteriaCollection("OR");
					for(String s:StringUtils.CommaDelimitedStringToArrayList(reconID, true)){
						if(s.equals("NULL")){
							subsubcc.addClause("xnat:reconstructedImageData/type",""," IS NULL ",true);
							subsubcc.addClause("xnat:reconstructedImageData/type","");
						}else{
							subsubcc.addClause("xnat:reconstructedImageData/type", s.replace("[COMMA]", ","));
						}
					}
					subcc.add(subsubcc);
				}
				cc.add(subcc);
			}

			recons = XnatReconstructedimagedata
					.getXnatReconstructedimagedatasByField(cc, user,
							completeDocument);
			if (recons.size() > 0) {
					if(type==null){
						type="out";
					}
				}

			if (recons.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Unable to identify reconstruction");
					return;
				}
			}

	}

	public boolean allowMultipleMatches(){
		if(this instanceof FileList && this.getRequest().getMethod().equals(Method.GET)){
			return true;
		}else{
			return false;
		}
	}
	
	public ItemI getSecurityItem(){
		if(this.security!=null)return security;
		
		XnatExperimentdata assessed=null;
		if(this.assesseds.size()==1)assessed=assesseds.get(0);

		if (recons.size()>0) {
			return assessed;
		} else if (scans.size()>0) {
			return assessed;
		} else if (expts.size()>0) {
//			experiment
			return expts.get(0);
		}else if(sub!=null){
			return sub;
		}else if(proj!=null){
			return proj;
		}else{
			return null;
		}
	}

	public boolean insertCatalag(XnatResourcecatalog catResource,EventMetaI ci)
			throws InvalidArchiveStructure, Exception {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				XNATRestConstants.PREARCHIVE_TIMESTAMP);
        String uploadID = formatter.format(Calendar.getInstance().getTime());
		String resourceFolder=catResource.getLabel();

		XnatExperimentdata assessed=null;
		if(this.assesseds.size()==1)assessed=assesseds.get(0);

		if (recons.size()>0) {
			//reconstruction
			if (assessed == null) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Invalid session id.");
				//MATCHED
				return false;
			}

			XnatReconstructedimagedata recon=recons.get(0);

			if(recon.getId()!=null && !recon.getId().equals("")){
				uploadID=recon.getId();
			}

			XnatImagesessiondata session = (XnatImagesessiondata) assessed;
			String dest_path = FileUtils.AppendRootPath(session
					.getCurrentSessionFolder(true), "PROCESSED/" + uploadID
					+ "/");

			CatCatalogBean cat = new CatCatalogBean();
			if(catResource.getLabel()!=null){
				cat.setId(catResource.getLabel());
			}else{
				cat.setId(uploadID);
			}

			File dest=null;
			if(resourceFolder==null){
				dest = new File(new File(dest_path),cat.getId() + "_catalog.xml");
			}else{
				dest = new File(new File(dest_path,resourceFolder),cat.getId() + "_catalog.xml");
			}
			dest.getParentFile().mkdirs();

			try {
				FileWriter fw = new FileWriter(dest);
				cat.toXML(fw, true);
				fw.close();
			} catch (IOException e) {
				logger.error("",e);
			}

			catResource.setUri(dest.getAbsolutePath());

			if(type!=null){
				if(type.equals("in")){
					recon.setIn_file(catResource);
				}else{
					recon.setOut_file(catResource);
				}
			}else{
				recon.setOut_file(catResource);
			}

			SaveItemHelper.authorizedSave(recon,user, false, false,ci);
			return true;
		} else if (scans.size()>0) {
			XnatImagescandata scan=scans.get(0);
			//scan
			if (assessed == null) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_GONE,
						"Invalid session id.");
				//MATCHED
				return false;
			}

			if(scan.getId()!=null && !scan.getId().equals("")){
				uploadID=scan.getId();
			}

			XnatImagesessiondata session = (XnatImagesessiondata) assessed;
			String dest_path = FileUtils.AppendRootPath(session
					.getCurrentSessionFolder(true), "SCANS/" + uploadID + "/");

			CatCatalogBean cat = new CatCatalogBean();
			if(catResource.getLabel()!=null){
				cat.setId(catResource.getLabel());
			}else{
				cat.setId(uploadID);
			}

			File dest=null;
			if(resourceFolder==null){
				dest = new File(new File(dest_path),cat.getId() + "_catalog.xml");
			}else{
				dest = new File(new File(dest_path,resourceFolder),cat.getId() + "_catalog.xml");
			}
			dest.getParentFile().mkdirs();

			try {
				FileWriter fw = new FileWriter(dest);
				cat.toXML(fw, true);
				fw.close();
			} catch (IOException e) {
				logger.error("",e);
			}

			catResource.setUri(dest.getAbsolutePath());

			if(scan.getFile().size()==0){
				if(catResource.getContent()==null && scan.getType()!=null){
					catResource.setContent("RAW");
				}
			}

			scan.setFile(catResource);

			SaveItemHelper.authorizedSave(scan,user, false, false,ci);
			return true;
		} else if (expts.size()>0) {
//			experiment
			XnatExperimentdata expt=expts.get(0);
			XnatExperimentdata session = null;

			String dest_path=null;
			if(expt.getItem().instanceOf("xnat:imageAssessorData")){
				session = (XnatImagesessiondata) assessed;
				if(expt.getId()!=null && !expt.getId().equals("")){
					uploadID=expt.getId();
				}
				dest_path = FileUtils.AppendRootPath(((XnatImagesessiondata)session)
						.getCurrentSessionFolder(true), "ASSESSORS/"
						+ expt.getArchiveDirectoryName() + "/");
			}else{
				if(!expt.getItem().instanceOf("xnat:imageSessionData")){
					session = (XnatExperimentdata) expt;
				} else {
					session = (XnatImagesessiondata) expt;
				}
				dest_path = FileUtils.AppendRootPath(session.getCurrentSessionFolder(true), "RESOURCES/");
			}
			CatCatalogBean cat = new CatCatalogBean();
			if(catResource.getLabel()!=null){
				cat.setId(catResource.getLabel());
			}else{
				cat.setId(uploadID);
			}

			File dest=null;
			if(resourceFolder==null){
				dest = new File(new File(dest_path),cat.getId() + "_catalog.xml");
			}else{
				dest = new File(new File(dest_path,resourceFolder),cat.getId() + "_catalog.xml");
			}
			dest.getParentFile().mkdirs();

			try {
				FileWriter fw = new FileWriter(dest);
				cat.toXML(fw, true);
				fw.close();
			} catch (IOException e) {
				logger.error("",e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
						e.getMessage());
				return false;
			}

			catResource.setUri(dest.getAbsolutePath());

			if(expt.getItem().instanceOf("xnat:imageAssessorData")){
				XnatImageassessordata iad = (XnatImageassessordata)expt.getLightCopy();
				if(type!=null){
					if(type.equals("in")){
						iad.setIn_file(catResource);
					}else{
						iad.setOut_file(catResource);
					}
				}else{
					iad.setOut_file(catResource);
				}

				SaveItemHelper.authorizedSave(iad,user, false, false,ci);

			}else{
				XnatExperimentdata copy=session.getLightCopy();
				copy.setResources_resource(catResource);

				SaveItemHelper.authorizedSave(copy,user, false, false,ci);
			}
			return true;
		}else if(sub!=null){
			String dest_path=null;
			if(proj==null)proj=sub.getPrimaryProject(false);
			dest_path = FileUtils.AppendRootPath(proj.getRootArchivePath(),
					"subjects/" + sub.getArchiveDirectoryName() + "/");

			CatCatalogBean cat = new CatCatalogBean();
			if(catResource.getLabel()!=null){
				cat.setId(catResource.getLabel());
			}else{
				cat.setId(uploadID);
			}

			File dest=null;
			if(resourceFolder==null){
				dest = new File(new File(dest_path),cat.getId() + "_catalog.xml");
			}else{
				dest = new File(new File(dest_path,resourceFolder),cat.getId() + "_catalog.xml");
			}
			dest.getParentFile().mkdirs();

			try {
				FileWriter fw = new FileWriter(dest);
				cat.toXML(fw, true);
				fw.close();
			} catch (IOException e) {
				logger.error("",e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
						e.getMessage());
				return false;
			}

			catResource.setUri(dest.getAbsolutePath());
			
			XnatSubjectdata copy=sub.getLightCopy();
			copy.setResources_resource(catResource);

			SaveItemHelper.authorizedSave(copy,user, false, false,ci);
			return true;
		}else if(proj!=null){
			String dest_path=null;
			dest_path = FileUtils.AppendRootPath(proj.getRootArchivePath(),
					"resources/");

			CatCatalogBean cat = new CatCatalogBean();
			if(catResource.getLabel()!=null){
				cat.setId(catResource.getLabel());
			}else{
				cat.setId(uploadID);
			}

			File dest=null;
			if(resourceFolder==null){
				dest = new File(new File(dest_path),cat.getId() + "_catalog.xml");
			}else{
				dest = new File(new File(dest_path,resourceFolder),cat.getId() + "_catalog.xml");
			}
			dest.getParentFile().mkdirs();

			try {
				FileWriter fw = new FileWriter(dest);
				cat.toXML(fw, true);
				fw.close();
			} catch (IOException e) {
				logger.error("",e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
						e.getMessage());
				return false;
			}

			catResource.setUri(dest.getAbsolutePath());
			proj.setResources_resource(catResource);

			SaveItemHelper.authorizedSave(proj,user, false, false,ci);
			return true;
		}
		return true;
	}
	
	public void checkResourceIDs(ArrayList<String> resourceIDs) throws Exception{
		if(resourceIDs!=null){
			for (String resourceID : resourceIDs) {
				if(resourceID!=null){
					if(resourceID.contains("'")){
						throw new Exception("Possible SQL Injection attempt. ' is not allowed in resource labels: " + resourceID);
					}else{
						if(PoolDBUtils.HackCheck(resourceID)){
							throw new Exception("Possible SQL Injection attempt: " + resourceID);
						}
					}
				}
			}
		}
	}

	public XFTTable loadCatalogs(ArrayList<String> resourceIDs,
			boolean includeURI,boolean allowAll) throws Exception {
		checkResourceIDs(resourceIDs);
		
		StringBuffer query = new StringBuffer();
		String starterFields = "SELECT xnat_abstractresource_id,abst.label,xme.element_name ";

		if (recons.size()>0) {
			security = this.assesseds.get(0);
			parent = recons.get(0);
				if (type!=null && type.equals("in")) {
					xmlPath = "xnat:reconstructedImageData/in/file";
					query.append(starterFields);
					query.append(", 'reconstructions'::TEXT AS category, recon.id::TEXT AS cat_id");
					query.append(", recon.type::TEXT AS cat_desc");
					if (includeURI) {
						query.append(",'/experiments/' || recon.image_session_id");
						query.append(" || '/reconstructions/' || recon.id || '/in'");
						query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
					}
					query.append(" FROM recon_in_resource map ");
					query.append(" LEFT JOIN xnat_reconstructedimagedata recon ON map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=recon.xnat_reconstructedimagedata_id ");
					query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
					query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
					query.append(" WHERE (");
					int sC=0;
					for(XnatReconstructedimagedata recon:recons){
						if(sC++>0)query.append(" OR ");
						query.append("xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=");
						query.append(recon.getXnatReconstructedimagedataId());
					}
					query.append(") ");
					if (resourceIDs != null && resourceIDs.size() > 0) {
						int c = 0;
						query.append(" AND ( ");
						for (String resourceID : resourceIDs) {
							if (c++ > 0)
								query.append(" OR ");
							if (org.apache.commons.lang.StringUtils
									.isNumeric(resourceID)) {
								query
										.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
								query.append(resourceID);
								query.append(" OR abst.label='");
								query.append(resourceID);
								query.append("')");
							} else if(resourceID.equalsIgnoreCase("NULL")){
								query.append(" abst.label IS NULL");
							}
							else{
								query.append(" abst.label='");
								query.append(resourceID);
								query.append("'");
							}
						}
						query.append(")");
					}
				} else {
					xmlPath = "xnat:reconstructedImageData/out/file";
					query.append(starterFields);
					query.append(", 'reconstructions'::TEXT AS category, recon.id::TEXT AS cat_id");
					query.append(", recon.type::TEXT AS cat_desc");
					if (includeURI) {
						query.append(",'/experiments/' || recon.image_session_id");
						query.append(" || '/reconstructions/' || recon.id || '/out'");
						query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
					}
					query.append(" FROM recon_out_resource map ");
					query.append(" LEFT JOIN xnat_reconstructedimagedata recon ON map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=recon.xnat_reconstructedimagedata_id ");
					query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
					query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");

					query.append(" WHERE (");
					int sC=0;
					for(XnatReconstructedimagedata recon:recons){
						if(sC++>0)query.append(" OR ");
						query.append("xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=");
						query.append(recon.getXnatReconstructedimagedataId());
					}
					query.append(") ");

					if (resourceIDs != null && resourceIDs.size() > 0) {
						int c = 0;
						query.append(" AND ( ");
						for (String resourceID : resourceIDs) {
							if (c++ > 0)
								query.append(" OR ");
							if (org.apache.commons.lang.StringUtils
									.isNumeric(resourceID)) {
								query
										.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
								query.append(resourceID);
								query.append(" OR abst.label='");
								query.append(resourceID);
								query.append("')");
							} else if(resourceID.equalsIgnoreCase("NULL")){
								query.append(" abst.label IS NULL");
							}
							else{
								query.append(" abst.label='");
								query.append(resourceID);
								query.append("'");
							}
						}
						query.append(")");
					}
				}
		} else if (scans.size()>0) {
			security = this.assesseds.get(0);
			parent = scans.get(0);
			xmlPath = "xnat:imageScanData/file";
			query.append(starterFields);
			query.append(", 'scans'::TEXT AS category, scan.id::TEXT AS cat_id");
			query.append(", scan.type::TEXT AS cat_desc");
			if (includeURI) {
				query.append(",'/experiments/' || scan.image_session_id");
				query.append(" || '/scans/' || scan.id");
				query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
			}
			query.append(" FROM xnat_abstractresource abst LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
			query.append(" LEFT JOIN xnat_imagescandata scan ON abst.xnat_imagescandata_xnat_imagescandata_id=scan.xnat_imagescandata_id");
			query.append(" WHERE (");
			int sC=0;
			for(XnatImagescandata scan:scans){
				if(sC++>0)query.append(" OR ");
				query.append("xnat_imagescandata_xnat_imagescandata_id=");
				query.append(scan.getXnatImagescandataId());
			}
			query.append(") ");
			if (resourceIDs != null && resourceIDs.size() > 0) {
				int c = 0;
				query.append(" AND ( ");
				for (String resourceID : resourceIDs) {
					if (c++ > 0)
						query.append(" OR ");
					if (org.apache.commons.lang.StringUtils
							.isNumeric(resourceID)) {
						query.append(" (abst.xnat_abstractresource_id=");
						query.append(resourceID);
						query.append(" OR abst.label='");
						query.append(resourceID);
						query.append("')");
					} else if(resourceID.equalsIgnoreCase("NULL")){
						query.append(" abst.label IS NULL");
					}
					else{
						query.append(" abst.label='");
						query.append(resourceID);
						query.append("'");
					}
				}
				query.append(")");
			}
		} else if (expts.size()>0) {
			security = this.expts.get(0);
			parent = this.expts.get(0);
			if (assesseds.size()>0) {
				security = this.assesseds.get(0);
				if (type!=null && type.equals("in")) {
					xmlPath = "xnat:imageAssessorData/in/file";
					query.append(starterFields);
					query.append(", 'assessors'::TEXT AS category, expt.id::TEXT AS cat_id");
					query.append(", COALESCE(xes.singular,xmeexpt.element_name)::TEXT AS cat_desc");
					if (includeURI) {
						query.append(",'/experiments/' || xiad.imagesession_id");
						query.append(" || '/assessors/' || expt.id || '/in'");
						query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
					}
					query.append(" FROM img_assessor_in_resource map ");
					query.append(" LEFT JOIN xnat_experimentdata expt ON map.xnat_imageassessordata_id=expt.id ");
					if (includeURI) {
						query.append(" LEFT JOIN xnat_imageassessordata xiad ON expt.id=xiad.id ");
					}
					query.append(" LEFT JOIN xdat_meta_element xmeexpt ON expt.extension=xmeexpt.xdat_meta_element_id ");
					query.append(" LEFT JOIN xdat_element_security xes ON xmeexpt.element_name=xes.element_name ");
					query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
					query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
					query.append(" WHERE (");
					int sC=0;
					for(XnatExperimentdata expt:expts){
						if(sC++>0)query.append(" OR ");
						query.append("map.xnat_imageassessordata_id='");
						query.append(expt.getId());
						query.append("'");
					}
					query.append(") ");
					if (resourceIDs != null && resourceIDs.size() > 0) {
						int c = 0;
						query.append(" AND ( ");
						for (String resourceID : resourceIDs) {
							if (c++ > 0)
								query.append(" OR ");
							if (org.apache.commons.lang.StringUtils
									.isNumeric(resourceID)) {
								query
										.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
								query.append(resourceID);
								query.append(" OR abst.label='");
								query.append(resourceID);
								query.append("')");
							} else if(resourceID.equalsIgnoreCase("NULL")){
								query.append(" abst.label IS NULL");
							}
							else{
								query.append(" abst.label='");
								query.append(resourceID);
								query.append("'");
							}
						}
						query.append(")");
					}
				} else {
					xmlPath = "xnat:imageAssessorData/out/file";
					query.append(starterFields);
					query.append(", 'assessors'::TEXT AS category, expt.id::TEXT AS cat_id");
					query.append(", COALESCE(xes.singular,xmeexpt.element_name)::TEXT AS cat_desc");
					if (includeURI) {
						query.append(",'/experiments/' || xiad.imagesession_id");
						query.append(" || '/assessors/' || expt.id || '/out'");
						query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
					}
					query.append(" FROM img_assessor_out_resource map ");
					query.append(" LEFT JOIN xnat_experimentdata expt ON map.xnat_imageassessordata_id=expt.id ");
					if (includeURI) {
						query.append(" LEFT JOIN xnat_imageassessordata xiad ON expt.id=xiad.id ");
					}
					query.append(" LEFT JOIN xdat_meta_element xmeexpt ON expt.extension=xmeexpt.xdat_meta_element_id ");
					query.append(" LEFT JOIN xdat_element_security xes ON xmeexpt.element_name=xes.element_name ");
					query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
					query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
					query.append(" WHERE (");
					int sC=0;
					for(XnatExperimentdata expt:expts){
						if(sC++>0)query.append(" OR ");
						query.append("map.xnat_imageassessordata_id='");
						query.append(expt.getId());
						query.append("'");
					}
					query.append(") ");
					if (resourceIDs != null && resourceIDs.size() > 0) {
						int c = 0;
						query.append(" AND ( ");
						for (String resourceID : resourceIDs) {
							if (c++ > 0)
								query.append(" OR ");
							if (org.apache.commons.lang.StringUtils
									.isNumeric(resourceID)) {
								query
										.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
								query.append(resourceID);
								query.append(" OR abst.label='");
								query.append(resourceID);
								query.append("')");
							} else if(resourceID.equalsIgnoreCase("NULL")){
								query.append(" abst.label IS NULL");
							}
							else{
								query.append(" abst.label='");
								query.append(resourceID);
								query.append("'");
							}
						}
						query.append(")");
					}
				}

			} else if ((allowAll) && (this.isQueryVariableTrue("all") || resourceIDs != null)) {
				xmlPath = "xnat:experimentData/resources/resource";
				// resources

				query.append("SELECT * FROM (");
				query.append(starterFields);
				query.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id,''::TEXT AS cat_desc");
				if (includeURI) {
					query.append(",'/experiments/' || res_map.xnat_experimentdata_id");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_experimentdata_resource res_map");
				query.append(" JOIN xnat_abstractresource abst ON res_map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
				query.append(" JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" WHERE (");
				int sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("res_map.xnat_experimentdata_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				query.append("  UNION ");
				query.append(starterFields);
				query.append(", 'scans'::TEXT,isd.id,isd.type");
				if (includeURI) {
					query.append(",'/experiments/' || isd.image_session_id");
					query.append(" || '/scans/' || isd.id");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_imagescanData isd  ");
				query.append(" JOIN xnat_abstractresource abst ON isd.xnat_imagescandata_id=abst.xnat_imagescandata_xnat_imagescandata_id");
				query.append(" JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" WHERE (");
				sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("isd.image_session_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				query.append(" UNION ");
				query.append(starterFields);
				query.append(", 'reconstructions'::TEXT,recon.id,recon.type");
				if (includeURI) {
					query.append(",'/experiments/' || recon.image_session_id");
					query.append(" || '/reconstructions/' || recon.id || '/out'");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_reconstructedimagedata recon");
				query.append(" JOIN recon_out_resource map ON recon.xnat_reconstructedimagedata_id=map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id");
				query.append(" JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
				query.append(" JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" WHERE (");
				sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("image_session_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				query.append(" UNION ");
				query.append(starterFields);
				query.append(", 'assessors'::TEXT,iad.id,xes.singular");
				if (includeURI) {
					query.append(",'/experiments/' || iad.imagesession_id");
					query.append(" || '/assessors/' || iad.id || '/out'");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_imageAssessorData iad");
				query.append(" JOIN img_assessor_out_resource map ON iad.id=map.xnat_imageassessordata_id");
				query.append(" JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
				query.append(" JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" LEFT JOIN xdat_element_security xes ON xme.element_name=xes.element_name");
				query.append(" WHERE (");
				sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("imagesession_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				query.append(" UNION ");
				query.append(starterFields);
				query.append(", 'assessors'::TEXT,iad.id,xes.singular");
				if (includeURI) {
					query.append(",'/experiments/' || iad.imagesession_id");
					query.append(" || '/assessors/' || iad.id");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_imageAssessorData iad");
				query.append(" JOIN xnat_experimentdata_resource map ON iad.id=map.xnat_experimentdata_id");
				query.append(" JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
				query.append(" JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" LEFT JOIN xdat_element_security xes ON xme.element_name=xes.element_name");
				query.append(" WHERE (");
				sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("imagesession_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				query.append(") all_resources");

				if (resourceIDs != null && resourceIDs.size() > 0) {
					int c = 0;
					query.append(" WHERE ");
					for (String resourceID : resourceIDs) {
						if (c++ > 0)
							query.append(" OR ");
						if (org.apache.commons.lang.StringUtils
								.isNumeric(resourceID)) {
							query.append(" (xnat_abstractresource_id=");
							query.append(resourceID);
							query.append(" OR label='");
							query.append(resourceID);
							query.append("')");
						} else if(resourceID.equalsIgnoreCase("NULL")){
							query.append(" label IS NULL");
						}
						else{
							query.append(" label='");
							query.append(resourceID);
							query.append("'");
						}
					}
				}
			} else {
				xmlPath = "xnat:experimentData/resources/resource";
				// resources
				query.append(starterFields);
				query.append(", 'resources'::TEXT AS category, expt.id::TEXT AS cat_id");
				query.append(", ' '::TEXT AS cat_desc");
				if (includeURI) {
					query.append(",'/experiments/' || map.xnat_experimentdata_id");
					query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
				}
				query.append(" FROM xnat_experimentdata_resource map ");
				query.append(" LEFT JOIN xnat_experimentdata expt ON map.xnat_experimentdata_id=expt.id ");
				query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
				query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
				query.append(" WHERE (");
				int sC=0;
				for(XnatExperimentdata expt:expts){
					if(sC++>0)query.append(" OR ");
					query.append("xnat_experimentdata_id='");
					query.append(expt.getId());
					query.append("'");
				}
				query.append(") ");
				if (resourceIDs != null && resourceIDs.size() > 0) {
					int c = 0;
					query.append(" AND ( ");
					for (String resourceID : resourceIDs) {
						if (c++ > 0)
							query.append(" OR ");
						if (org.apache.commons.lang.StringUtils
								.isNumeric(resourceID)) {
							query
									.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
							query.append(resourceID);
							query.append(" OR abst.label='");
							query.append(resourceID);
							query.append("')");
						} else if(resourceID.equalsIgnoreCase("NULL")){
							query.append(" abst.label IS NULL");
						}
						else{
							query.append(" abst.label='");
							query.append(resourceID);
							query.append("'");
						}
					}
					query.append(")");
				}
			}
		} else if (sub != null) {
			security = this.sub;
			parent = this.sub;
			xmlPath = "xnat:subjectData/resources/resource";
			// resources
			query.append(starterFields);
			query
					.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id");
			query.append(", ' '::TEXT AS cat_desc");
			if (includeURI) {
				query.append(",'/projects/' || sub.project");
				query.append(" || '/subjects/' || sub.id");
				query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
			}
			query.append(" FROM xnat_subjectdata_resource map ");
			query.append(" LEFT JOIN xnat_subjectdata sub ON map.xnat_subjectdata_id=sub.id");
			query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
			query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
			query.append(" WHERE xnat_subjectdata_id='");
			query.append(sub.getId());
			query.append("'");
			if (resourceIDs != null && resourceIDs.size() > 0) {
				int c = 0;
				query.append(" AND ( ");
				for (String resourceID : resourceIDs) {
					if (c++ > 0)
						query.append(" OR ");
					if (org.apache.commons.lang.StringUtils
							.isNumeric(resourceID)) {
						query
								.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
						query.append(resourceID);
						query.append(" OR abst.label='");
						query.append(resourceID);
						query.append("')");
					} else if(resourceID.equalsIgnoreCase("NULL")){
						query.append(" abst.label IS NULL");
					}
					else{
						query.append(" abst.label='");
						query.append(resourceID);
						query.append("'");
					}
				}
				query.append(")");
			}
		} else if (proj != null) {
			security = this.proj;
			parent = this.proj;
			xmlPath = "xnat:projectData/resources/resource";
			// resources
			query.append(starterFields);
			query
					.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id");
			query.append(", ' '::TEXT AS cat_desc");
			if (includeURI) {
				query.append(",'/projects/' || map.xnat_projectdata_id");
				query.append(" || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
			}
			query.append(" FROM xnat_projectdata_resource map ");
			query.append(" LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id");
			query.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
			query.append(" WHERE xnat_projectdata_id='");
			query.append(proj.getId());
			query.append("'");
			if (resourceIDs != null && resourceIDs.size() > 0) {
				int c = 0;
				query.append(" AND ( ");
				for (String resourceID : resourceIDs) {
					if (c++ > 0)
						query.append(" OR ");
					if (org.apache.commons.lang.StringUtils
							.isNumeric(resourceID)) {
						query
								.append(" (map.xnat_abstractresource_xnat_abstractresource_id=");
						query.append(resourceID);
						query.append(" OR abst.label='");
						query.append(resourceID);
						query.append("')");
					} else if(resourceID.equalsIgnoreCase("NULL")){
						query.append(" abst.label IS NULL");
					}
					else{
						query.append(" abst.label='");
						query.append(resourceID);
						query.append("'");
					}
				}
				query.append(")");
			}
		} else {
			query.append(starterFields);
			query
					.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id");
			query.append(", ' '::TEXT AS cat_desc");
			query.append(" FROM xnat_abstractresource abst");
			query
					.append(" LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id");
			query.append(" WHERE xnat_abstractresource_id IS NULL");
		}

		return XFTTable.Execute(query.toString(), user.getDBName(), userName);
	}
}
