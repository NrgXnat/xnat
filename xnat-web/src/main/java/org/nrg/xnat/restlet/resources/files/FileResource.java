/*
 * web: org.nrg.xnat.restlet.resources.files.FileResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.restlet.resources.ItemResource;
import org.nrg.xnat.restlet.resources.ScanResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class FileResource extends ItemResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ScanResource.class);

	XnatProjectdata proj=null;
	XnatSubjectdata sub=null;
	XnatExperimentdata expt=null;
	XnatImagescandata scan=null;
	XnatReconstructedimagedata recon=null;
	XnatExperimentdata assessed=null;
	String type=null;

	XnatAbstractresource resource=null;
	ItemI parent=null;
	ItemI security=null;
	
	String index=null;
	String filename=null;


	public FileResource(Context context, Request request, Response response) {
		super(context, request, response);

		final UserI user = getUser();

		final String pID = (String) getParameter(request, "PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getXnatProjectdatasById(pID, user, false);
		}

		String subID = (String) getParameter(request, "SUBJECT_ID");
		if (subID != null) {
			if (this.proj != null) {
				sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);
			}

			if (sub == null) {
				sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user, false);
			}
		}

		String assessid = (String) getParameter(request, "ASSESSED_ID");
		if (assessid != null) {
			assessed = XnatImagesessiondata.getXnatImagesessiondatasById(assessid, user, false);

			if (assessed == null) {
				assessed = (XnatImagesessiondata) XnatImagesessiondata.GetExptByProjectIdentifier(proj.getId(), assessid, user, false);
			}
		}

		String exptID = (String) getParameter(request, "EXPT_ID");
		if (exptID != null) {
			expt = XnatImagesessiondata.getXnatImagesessiondatasById(exptID, user, false);

			if (expt == null) {
				expt = (XnatImagesessiondata) XnatImagesessiondata.GetExptByProjectIdentifier(proj.getId(), exptID, user, false);
			}
		}

		String scanID = (String) getParameter(request, "SCAN_ID");
		if (scanID != null && this.assessed != null) {
			CriteriaCollection cc = new CriteriaCollection("AND");
			cc.addClause("xnat:imageScanData/ID", scanID);
			cc.addClause("xnat:imageScanData/image_session_ID", assessed.getId());
			ArrayList<XnatImagescandata> scans = XnatImagescandata.getXnatImagescandatasByField(cc, user, completeDocument);
			if (scans.size() > 0) {
				scan = scans.get(0);
			}
		}

		type = (String) getParameter(request, "TYPE");

		String reconID = (String) getParameter(request, "RECON_ID");
		if (reconID != null) {
			CriteriaCollection cc = new CriteriaCollection("AND");
			cc.addClause("xnat:reconstructedImageData/ID", reconID);
			cc.addClause("xnat:reconstructedImageData/image_session_ID", assessed.getId());
			ArrayList<XnatReconstructedimagedata> scans = XnatReconstructedimagedata.getXnatReconstructedimagedatasByField(cc, user, completeDocument);
			if (scans.size() > 0) {
				recon = scans.get(0);
			}
		}

		String resourceID = (String) getParameter(request, "RESOURCE_ID");
		try {
			Integer.parseInt(resourceID);
		} catch (NumberFormatException e1) {
			//This should be a number, if not something shady is going on.
			AdminUtils.sendAdminEmail(user, "Possible SQL Injection attempt.", "User passed " + resourceID + " as a resource identifier.");
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		}

		index = (String) getParameter(request, "INDEX");
		filename = (String) getParameter(request, "FILENAME");

		String query = "SELECT res.xnat_abstractresource_id,format,description,content,label,uri ";
		if (recon != null) {
			security = this.assessed;
			parent = recon;
			if (type != null) {
				if (type.equals("in")) {
					query += " FROM recon_in_resource map " +
							 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
							 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
					query += " WHERE xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=" + recon.getXnatReconstructedimagedataId();
					query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
				} else {
					query += " FROM recon_out_resource map " +
							 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
							 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
					query += " WHERE xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=" + recon.getXnatReconstructedimagedataId() + "";
					query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
				}
			} else {
				//resources
				query += " FROM recon_out_resource map " +
						 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
						 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
				query += " WHERE xnat_imageassessordata_id='" + expt.getId() + "'";
				query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
			}
		} else if (scan != null) {
			security = this.assessed;
			parent = scan;
			query += " FROM xnat_abstractresource abst" +
					 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
			query += " WHERE xnat_imagescandata_xnat_imagescandata_id=" + scan.getXnatImagescandataId() + "";
			query += " AND abst.xnat_abstractresource_id=" + resourceID;
		} else if (expt != null) {
			security = this.expt;
			parent = this.expt;
			try {
				if (expt.getItem().instanceOf("xnat:imageAssessorData")) {
					security = this.expt;
					parent = this.expt;
					if (type != null) {
						if (type.equals("in")) {
							query += " FROM img_assessor_in_resource map " +
									 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
									 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
							query += " WHERE xnat_imageassessordata_id='" + expt.getId() + "'";
							query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
						} else {
							query += " FROM img_assessor_out_resource map " +
									 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
									 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
							query += " WHERE xnat_imageassessordata_id='" + expt.getId() + "'";
							query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
						}
					} else {
						//resources
						query += " FROM xnat_experimentdata_resource map " +
								 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
								 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
						query += " WHERE xnat_experimentdata_id='" + expt.getId() + "'";
						query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
					}
				} else {
					//resources
					query += " FROM xnat_experimentdata_resource map " +
							 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
							 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
					query += " WHERE xnat_experimentdata_id='" + expt.getId() + "'";
					query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
				}
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			}
		} else if (sub != null) {
			security = this.sub;
			parent = this.sub;
			//resources
			query += " FROM xnat_subjectdata_resource map " +
					 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
					 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
			query += " WHERE xnat_subjectdata_id='" + sub.getId() + "'";
			query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
		} else if (proj != null) {
			security = this.proj;
			parent = this.proj;
			//resources
			query += " FROM xnat_projectdata_resource map " +
					 " LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id" +
					 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
			query += " WHERE xnat_projectdata_id='" + proj.getId() + "'";
			query += " AND map.xnat_abstractresource_xnat_abstractresource_id=" + resourceID;
		} else {
			query += " FROM xnat_abstractresource abst" +
					 " LEFT JOIN xnat_resource res ON abst.xnat_abstractresource_id=res.xnat_abstractresource_id";
			query += " WHERE res.xnat_abstractresource_id IS NULL";
		}

		try {
			XFTTable table = XFTTable.Execute(query, user.getDBName(), userName);
			if (table.size() > 0) {
				resource = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(resourceID, user, false);
				this.getVariants().add(new Variant(MediaType.ALL));
			} else {
				response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid read permissions");
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void handleDelete(){
			if(resource!=null && this.parent!=null && this.security!=null){
				try {
					if(Permissions.canEdit(getUser(),this.security)){
						
						if(proj==null){
							if(parent.getItem().instanceOf("xnat:experimentData")){
								proj = ((XnatExperimentdata)parent).getPrimaryProject(false);
							}else if(security.getItem().instanceOf("xnat:experimentData")){
								proj = ((XnatExperimentdata)security).getPrimaryProject(false);
						}else if(security.getItem().instanceOf("xnat:subjectData")){
							proj = ((XnatSubjectdata)security).getPrimaryProject(false);
						}else if(security.getItem().instanceOf("xnat:projectData")){
							proj = (XnatProjectdata)security;
							}
						}
						
						XnatResourcecatalog catResource=(XnatResourcecatalog)resource;
						
						File catFile = catResource.getCatalogFile(proj.getRootArchivePath());
						
						String parentPath=catFile.getParent();
						
						CatCatalogBean cat=catResource.getCleanCatalog(proj.getRootArchivePath(), false,null,null);
						
						CatEntryI entry = retrieveEntry(cat, index + "/" + filename);
						
						if(entry==null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify specified file.");
							return;
						}
						
						File f = new File(parentPath,entry.getUri());
						
						if(f.exists()){
							FileUtils.DeleteFile(f);
							this.removeEntry(cat, entry);
							try
							{
							    FileWriter fw = new FileWriter(catFile);
								cat.toXML(fw, true);
								fw.close();
							 }catch(Exception e){
							 logger.error("",e);
							 }
						}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"File missing");
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"User account doesn't have permission to modify this session.");
					}
				} catch (Exception e) {
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				}
			}
		}
	
	private boolean removeEntry(CatCatalogI cat,CatEntryI entry)
	{
		for(int i=0;i<cat.getEntries_entry().size();i++){
			CatEntryI e= cat.getEntries_entry().get(i);
			if(e.getId().equals(entry.getId())){
				cat.getEntries_entry().remove(i);
				return true;
			}
		}
		
		for(CatCatalogI subset: cat.getSets_entryset()){
			if(removeEntry(subset,entry)){
				return true;
			}
		}
		
		return false;
	}
	private CatEntryI retrieveEntry(CatCatalogI cat,String id){
		for(CatEntryI entry: cat.getEntries_entry()){
			if(entry.getId().equals(id)){
				return entry;
			}
		}
		
		for(CatCatalogI subset: cat.getSets_entryset()){
			CatEntryI e = retrieveEntry(subset,id);
			if(e!=null)
			{
				return e;
			}
		}
		
		return null;
	}

	@Override
	public Representation represent(Variant variant) {
		MediaType mt = overrideVariant(variant);

		if(resource!=null){
			try {
				if(proj==null){
					if(parent.getItem().instanceOf("xnat:experimentData")){
						proj = ((XnatExperimentdata)parent).getPrimaryProject(false);
					}else if(security.getItem().instanceOf("xnat:experimentData")){
						proj = ((XnatExperimentdata)security).getPrimaryProject(false);
					}
				}
				
				XnatResourcecatalog catResource=(XnatResourcecatalog)resource;
				
				File catFile = catResource.getCatalogFile(proj.getRootArchivePath());
				
				String parentPath=catFile.getParent();
				
				CatCatalogI cat=catResource.getCleanCatalog(proj.getRootArchivePath(), false,null,null);
				
				CatEntryI entry = retrieveEntry(cat, index + "/" + filename);
				
				if(entry==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify specified file.");
				}else{
					File f = new File(parentPath,entry.getUri());
					return representFile(f,mt);
				}
				
			} catch (ElementNotFoundException e) {
	            logger.error("",e);
			}
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified catalog.");
		}

		return null;

	}
}
