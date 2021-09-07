/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResourceFiles
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ServerException;
import org.nrg.dcm.Dcm2Jpg;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author tolsen01
 *
 */
public class PrearcSessionResourceCatalogFiles extends PrearcSessionResourceCatalog {
	public PrearcSessionResourceCatalogFiles(Context context, Request request,
                                             Response response) {
		super(context, request, response);
	}

	final static ArrayList<String> columns=Lists.newArrayList("Name","Size","URI");

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void handlePut() {
		final PrearcInfo info;
		try {
			final List<FileWriterWrapperI> writers = getFileWriters();
			if (writers == null || writers.isEmpty()) {
				final String method = getRequest().getMethod().toString();
				final long   size   = getRequest().getEntity().getAvailableSize();
				if (size == 0) {
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " to this service, but didn't provide any data (found request entity size of 0). Please check the format of your service request.");
				} else {
					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " a payload of " + CatalogUtils.formatSize(size) + " to this service, but didn't provide any data. If you think you sent data to upload, you can try to " + method + " with the query-string parameter inbody=true or use multipart/form-data encoding.");
				}
				return;
			}

			info = retrieveSessionBean();
			XnatResourcecatalogI res = (XnatResourcecatalogI) MergeUtils.getMatchingResourceByLabel(resourceId, info.session.getResources_resource());
			boolean isNew = res == null;
			if (isNew) {
				// create resource
				res = new XnatResourcecatalogBean();
				res.setLabel(resourceId);
				res.setUri(Paths.get("RESOURCES", resourceId, resourceId + "_catalog.xml")
						.toString());
				info.session.addResources_resource(res);
			}

			final String project = info.session.getProject();
			CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(info.session.getPrearchivepath(), res, project);
			final Map<String, CatalogUtils.CatalogMapEntry> catalogMap = CatalogUtils.buildCatalogMap(catalogData);

			// copy file(s) to location
			Path catalogUri = Paths.get(info.session.getPrearchivepath(), res.getUri());
			Path catalogPath = catalogUri.getParent();
			Files.createDirectories(catalogPath);
			boolean rewriteCatalog = false;
			for (final FileWriterWrapperI fileWriter : writers) {
				String relPath = fileWriter.getName();
				fileWriter.write(catalogPath.resolve(relPath).toFile());
				if (!catalogMap.containsKey(relPath)) {
					// if we already reference the file, copying it into place is all we need to do
					CatEntryBean entry = new CatEntryBean();
					entry.setId(relPath);
					entry.setUri(relPath);
					catalogData.catBean.addEntries_entry(entry);
					rewriteCatalog = true;
				}
			}

			if (rewriteCatalog) {
				// refresh catalog itself
				CatalogUtils.writeCatalogToFile(catalogData);
			}

			if (isNew) {
				// rebuild session
				try (FileWriter fw = new FileWriter(info.sessionXML)) {
					info.session.toXML(fw);

					PrearcUtils.log(project, timestamp, session, new Exception("Added resource " + resourceId));
					this.getResponse().setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					logger.error("Failed to update session xml", e);
					PrearcUtils.log(project, timestamp, session, e);
					throw new ServerException(Status.SERVER_ERROR_INTERNAL, "Failed to update session xml.", e);
				}
			}
		} catch (ActionException e) {
			setResponseStatus(e);
		} catch (Exception e) {
			logger.error("Issue adding resource", e);
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, e.getMessage());
		}
	}

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
		
		final XnatResourcecatalogI res = (XnatResourcecatalogI)MergeUtils.getMatchingResourceByLabel(resourceId, info.session.getResources_resource());
		
		if(res==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}

		final String project = info.session.getProject();
		final CatalogUtils.CatalogData catalogData;
		try {
			catalogData = CatalogUtils.CatalogData.getOrCreateAndClean(info.session.getPrearchivepath(), res, false, project);
		} catch (ServerException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}

		final String rootPath = catalogData.catPath;
		final CatCatalogI catalog = catalogData.catBean;
		
		if (StringUtils.isNotEmpty(filepath)) {
			final CatEntryI entry = CatalogUtils.getEntryByURI(catalog, filepath);
			File f = CatalogUtils.getFile(entry, rootPath, project);
			if (f == null) return null;
			return representFile(f,mt);
		}else{
			boolean prettyPrint=this.isQueryVariableTrue("prettyPrint");
			
			final XFTTable table=new XFTTable();
	        table.initTable(columns);
	        for (final CatEntryI entry: CatalogUtils.getEntriesByFilter(catalog,null)) {
	        	File f = CatalogUtils.getFile(entry, rootPath, project);
	        	if (f == null) continue;
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
