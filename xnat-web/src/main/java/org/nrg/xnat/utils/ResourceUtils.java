/*
 * org.nrg.xnat.utils.ResourceUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/9/13 5:05 PM
 */
package org.nrg.xnat.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.restlet.data.Status;

import java.io.File;

public class ResourceUtils {
	
	/**
	 * Refresh values in resource catalog 
     * @param resource                The resource to be updated. In spite of the name, this will also work with
     *                                {@link XnatAbstractresource} objects as well, populating the persisted file
     *                                statistics from the actual data on disk.
     * @param projectPath             The location of the archive path for the project to be refreshed.
     * @param checksums               Indicates whether checksums should be calculated for catalog refresh operations.
     * @param removeMissingFiles      Indicates whether catalog entries for files that no longer exist should be
     *                                removed.
     * @param addUnreferencedFiles    Indicates whether entries should be added for files that are in the archive but
     *                                are not already in the catalog.
     * @param user                    The user who requested the refresh operation.
	 * @param now                     Event metadata for the operation.
	 * @return true if the catalog was modified
	 * @throws Exception Failures stem from the save operation.
	 */
	public static boolean refreshResourceCatalog(final XnatAbstractresource resource, final String projectPath, final boolean populateStats, final boolean checksums, final boolean removeMissingFiles, final boolean addUnreferencedFiles, final UserI user, final EventMetaI now) throws Exception {
		if(resource instanceof XnatResourcecatalog){
            final XnatResourcecatalog catRes=(XnatResourcecatalog)resource;
			
			final CatCatalogBean cat=CatalogUtils.getCatalog(projectPath, catRes);
			final File catFile=CatalogUtils.getCatalogFile(projectPath, catRes);
			
			if(cat!=null){
				boolean modified=false;
				
				if(addUnreferencedFiles){//check for files in the proper resource directory, but not referenced from the existing xml
					if(CatalogUtils.addUnreferencedFiles(catFile, cat, (UserI)user, now.getEventId())){
						modified=true;
					}
				}
				
				if(CatalogUtils.formalizeCatalog(cat, catFile.getParent(), user, now, checksums, removeMissingFiles)){
					modified=true;
				}
				
				if(modified){
					CatalogUtils.writeCatalogToFile(cat, catFile,checksums);
				}
				
				// populate (or repopulate) the file stats --- THIS SHOULD BE DONE AFTER modifications to the catalog xml
	            if (populateStats || modified) {
	                if (CatalogUtils.populateStats(resource, projectPath) || modified) {
                        if(resource.save(user, false, false, now)) {
                            modified = true;
                        }
                    }
	            }
	            
	            return modified;
			}
		} else if (populateStats) {
            if (CatalogUtils.populateStats(resource, projectPath)) {
                return resource.save(user, false, false, now);
            }
        }
		
		return false;
	}
	
	/**
	 * Refresh all of the catalogs for a given archivable item.
	 * @param resourceURI             The URI of the resource to be refreshed.
	 * @param user                    The user who requested the refresh operation.
	 * @param details                 Any details about the refresh to be added to the workflow.
	 * @param checksums               Indicates whether checksums should be calculated for catalog refresh operations.
	 * @param removeMissingFiles      Indicates whether catalog entries for files that no longer exist should be
     *                                removed.
	 * @param addUnreferencedFiles    Indicates whether entries should be added for files that are in the archive but
     *                                are not already in the catalog.
	 * @throws ActionException
	 */
	public static void refreshResourceCatalog(final ArchiveItemURI resourceURI,final UserI user,final EventDetails details, final boolean populateStats, boolean checksums, boolean removeMissingFiles, boolean addUnreferencedFiles) throws ActionException{
		try {
			if(resourceURI instanceof ResourceURII){//if we are referencing a specific catalog, make sure it doesn't actually reference an individual file.
				if(StringUtils.isNotEmpty(((ResourceURII)resourceURI).getResourceFilePath()) && !((ResourceURII)resourceURI).getResourceFilePath().equals("/")){
					throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,new Exception("This operation cannot be performed directly on a file URL"));
				}
			}
			
			final ArchivableItem security=resourceURI.getSecurityItem();
			if(!Permissions.canEdit(user,security)){
				throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized attempt to add a file to "+ resourceURI.getUri()));
			}
			
			final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, resourceURI.getSecurityItem().getItem(), details);
			
			for(XnatAbstractresourceI res:resourceURI.getResources(true)){
                final String archiveRootPath = resourceURI.getSecurityItem().getArchiveRootPath();
                refreshResourceCatalog((XnatAbstractresource)res, archiveRootPath, populateStats, checksums, removeMissingFiles, addUnreferencedFiles, user, wrk.buildEvent());
			}
			
			WorkflowUtils.complete(wrk, wrk.buildEvent());
		} catch (InvalidItemException e) {
			throw new ServerException(e);
		} catch (UnknownPrimaryProjectException e) {
			throw new ServerException(e);
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}
}
