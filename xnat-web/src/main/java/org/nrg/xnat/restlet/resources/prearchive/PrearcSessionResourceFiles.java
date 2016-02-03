/*
 * org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourceFiles
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.dcm.Dcm2Jpg;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author tolsen01
 *
 */
public class PrearcSessionResourceFiles extends PrearcScanResourceList {
	private static final String RESOURCE_ID = "RESOURCE_ID";
	private final String resource_id;
	
	public PrearcSessionResourceFiles(Context context, Request request,
			Response response) {
		super(context, request, response);
		resource_id = (String)SecureResource.getParameter(request,RESOURCE_ID);
	}


	
	final static ArrayList<String> columns=Lists.newArrayList("Name","Size","URI");


	@Override
	public Representation getRepresentation(Variant variant) {
		final MediaType mt=overrideVariant(variant);
				
		final PrearcInfo info;
		try {
			info = retrieveSessionBean();
		} catch (ActionException e) {
			setResponseStatus(e);
			return null;
		}
		
		final XnatImagescandataI scan=MergeUtils.getMatchingScanById(scan_id,(List<XnatImagescandataI>)info.session.getScans_scan());
		
		if(scan==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		
		final XnatResourcecatalogI res=(XnatResourcecatalogI)MergeUtils.getMatchingResourceByLabel(resource_id, scan.getFile());
		
		if(res==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		
		final String rootPath=CatalogUtils.getCatalogFile(info.session.getPrearchivepath(), ((XnatResourcecatalogI)res)).getParentFile().getAbsolutePath();
		
		final CatCatalogI catalog=CatalogUtils.getCleanCatalog(info.session.getPrearchivepath(), res, false);
		
		if(StringUtils.isNotEmpty(filepath)){
			final CatEntryI entry=CatalogUtils.getEntryByURI(catalog, filepath);
			File f= CatalogUtils.getFile(entry, rootPath);
			
            if (mt.equals(MediaType.IMAGE_JPEG) && StringUtils.equals(resource_id, "DICOM") && Dcm2Jpg.isDicom(f)) {
                try {
                    return new InputRepresentation(new ByteArrayInputStream(Dcm2Jpg.convert(f)), mt);
                } catch (IOException e) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to convert this file to jpeg : " + e.getMessage());
                    return new StringRepresentation("");
                }
            }
            
			return representFile(f,mt);
		}else{
			boolean prettyPrint=this.isQueryVariableTrue("prettyPrint");
			
			final XFTTable table=new XFTTable();
	        table.initTable(columns);
	        for (final CatEntryI entry: CatalogUtils.getEntriesByFilter(catalog,null)) {
	        	File f=CatalogUtils.getFile(entry, rootPath);
	        	Object[] oarray = new Object[] { f.getName(), (prettyPrint)?CatalogUtils.formatSize(f.length()):f.length(), constructURI(entry.getUri())};
	        	table.insertRow(oarray);
	        }
	        
	        return representTable(table, mt, new Hashtable<String,Object>());
		}
        
	}
			
    private String constructURI(String resource) {
    	String requestPart = this.getHttpServletRequest().getServletPath() + this.getHttpServletRequest().getPathInfo();
    	return requestPart + "/" + resource;
    	
    }
}
