/*
 * web: org.nrg.xnat.archive.Rename
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.transaction.TransactionException;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.DBItemCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;

@SuppressWarnings("serial")
@Slf4j
public class Rename  implements Callable<File>{
	enum STEP {PREPARING, PREPARE_SQL, COPY_DIR, ANONYMIZE, EXECUTE_SQL, DELETE_OLD_DIR, COMPLETE}
	private static final String    SUCCESSFUL_RENAMES = "successful_renames";
	private static final String    FAILED_RENAME      = "failed_rename";
	private final ArchivableItem   item;
	private final XnatProjectdata  project;
	
	private final String newLabel,reason;
	private final UserI user;
	
	private final EventUtils.TYPE type;
	
	private STEP step=STEP.PREPARING;
	/**
	 * Only for use in the JUNIT tests
	 */
	@SuppressWarnings("unused")
	public Rename(){
		project = null;
		item = null;
		newLabel = null;
		user = null;
		reason = null;
		type = null;
	}
	
	public Rename(final XnatProjectdata project, final ArchivableItem item, final String newLabel, final UserI user, final String reason, final EventUtils.TYPE type){
		if(item == null){
			throw new IllegalArgumentException();
		}

		if(StringUtils.isBlank(newLabel)) {
			throw new IllegalArgumentException();
		}

		this.project = project;
		this.item = item;
		this.newLabel = newLabel;
		this.user = user;
		this.reason = reason;
		this.type = type;
	}

	/**
	 * Rename the label for the corresponding session and modify the file URIs for the adjusted path.
	 * 
	 * @throws FieldNotFoundException
	 * @throws ProcessingInProgress
	 * @throws DuplicateLabelException
	 * @throws IllegalAccessException
	 * @throws LabelConflictException
	 * @throws FolderConflictException
	 * @throws InvalidArchiveStructure
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public File call() throws FieldNotFoundException, ProcessingInProgress, DuplicateLabelException, IllegalAccessException, LabelConflictException, FolderConflictException, InvalidArchiveStructure, URISyntaxException,Exception{
		final File newSessionDir = item instanceof XnatSubjectdata
								   ? new File(new File(project.getRootArchivePath(), "subjects"), newLabel)
								   : new File(new File(project.getRootArchivePath(), project.getCurrentArc()), newLabel);

		try {
			final String id           = item.getStringProperty("ID");
			final String currentLabel = StringUtils.defaultIfBlank(item.getStringProperty("label"), id);
						
			if(newLabel.equals(currentLabel)){
				throw new DuplicateLabelException();
			}
			
			//confirm if user has permission
			if(!checkPermissions(item, user)){
				throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for project: " + project.getId());
			}

			//confirm if new label is already in use
			if(item instanceof XnatSubjectdata){
				final XnatSubjectdata match=XnatSubjectdata.GetSubjectByProjectIdentifier(project.getId(), newLabel, null, false);
				if(match!=null){
					throw new LabelConflictException();
				}
			}else{
				final XnatExperimentdata match=XnatExperimentdata.GetExptByProjectIdentifier(project.getId(), newLabel, null, false);
				if(match!=null){
					throw new LabelConflictException();
				}
			}
			
			final Collection<? extends PersistentWorkflowI> open=PersistentWorkflowUtils.getOpenWorkflows(user, id);
			if(!open.isEmpty()){		
				throw new ProcessingInProgress(((WrkWorkflowdata)CollectionUtils.get(open, 0)).getPipelineName());
			}
			
			//confirm if new directory already exists w/ stuff in it
			if(newSessionDir.exists() && ArrayUtils.getLength(newSessionDir.list()) > 0){
				throw new FolderConflictException();
			}else{
				newSessionDir.mkdir();
			}
			
			//identify existing directory
			final File oldSessionDir = item.getExpectedCurrentDirectory();
				
			final String message=String.format("Renamed from %s to %s", currentLabel,newLabel);
			
			//add workflow entry    		
			final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, item.getXSIType(), item.getStringProperty("ID"), project.getId(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, type, EventUtils.RENAME, reason, null));
			workflow.setDetails(message);
			EventMetaI c=WorkflowUtils.setStep(workflow, getStep().toString());
			PersistentWorkflowUtils.save(workflow,c);
			
			
			final URI expected=oldSessionDir.toURI();
			final String newArchive=newSessionDir.getAbsolutePath();
			
			final boolean moveFiles=oldSessionDir.exists();
			
			c=this.updateStep(workflow, setStep(STEP.PREPARE_SQL));	
			
			try {
				//Copy files to new location    		
				
				//Generate SQL to update URIs
				final DBItemCache cache=new DBItemCache(user,c);
				generateLabelSQL(item, newLabel, cache, user, c);
				generateURISQL(item, expected, newArchive, cache, user);

				this.updateStep(workflow, setStep(STEP.COPY_DIR));

				if(moveFiles)org.nrg.xft.utils.FileUtils.CopyDir(oldSessionDir, newSessionDir,false);	
				
				if(item instanceof XnatImagesessiondata){
					this.updateStep(workflow, setStep(STEP.ANONYMIZE));
					new ProjectAnonymizer(newLabel,
										  (XnatImagesessiondata) item,
										  project.getId(),
										  ((XnatImagesessiondata) item).getArchivePath(item.getArchiveRootPath())
										  ).call();
				}else if(item instanceof XnatSubjectdata){
					for(final XnatSubjectassessordata expt : ((XnatSubjectdata) item).getExperiments_experiment("xnat:imageSessionData")){
                        try{
                           // re-apply this project's edit script
                           expt.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) expt, newLabel, expt.getProject(), expt.getArchiveRootPath()));
                        }
                        catch (TransactionException e) {
                           throw new AnonException(e);
                        }
                   }
				}
				
				this.updateStep(workflow, setStep(STEP.EXECUTE_SQL));
				//Execute SQL
				executeSQL(cache,user);

				this.updateStep(workflow, setStep(STEP.DELETE_OLD_DIR));
				//if successful, move old directory to cache)
				if(moveFiles)org.nrg.xnat.utils.FileUtils.moveToCache(project.getId(), SUCCESSFUL_RENAMES, oldSessionDir);
				
				//close workflow entry
				workflow.setStepDescription(setStep(STEP.COMPLETE).toString());
				workflow.setStatus(PersistentWorkflowUtils.COMPLETE);
			} catch (final Exception e) {
				if(!getStep().equals(STEP.DELETE_OLD_DIR)){
					try {
						if(moveFiles)org.nrg.xnat.utils.FileUtils.moveToCache(project.getId(), FAILED_RENAME, newSessionDir);
					} catch (IOException e1) {
						log.error("", e1);
					}
					
					//close workflow
					workflow.setStatus(PersistentWorkflowUtils.FAILED);
					
					throw e;
				}else{
					workflow.setStatus(PersistentWorkflowUtils.COMPLETE);
				}
			}finally{
				PersistentWorkflowUtils.save(workflow,c);
			}
		} catch (XFTInitException e) {
			log.error("", e);
		} catch (ElementNotFoundException e) {
			log.error("", e);
		}
		
		return newSessionDir;
	}
	
	public EventMetaI updateStep(final PersistentWorkflowI wrk, final STEP step) throws Exception{
		EventMetaI c=WorkflowUtils.setStep(wrk, step.toString());
		PersistentWorkflowUtils.save(wrk,c);
		return c;
	}
	
	public static boolean checkPermissions(final ArchivableItem i, final UserI user) throws Exception{
		return Permissions.canEdit(user,i);
	}
	
	/**
	 * Generate the SQL update logic for all of the items resources.  
	 * Checks permissions for assessments, if they were modified.
	 * 
	 * @param item       The item
	 * @param expected   The expected URI
	 * @param newArchive The new archive path
	 * @param cache      The item cache
	 * @param user       The user requesting the SQL.

	 * @throws UnsupportedResourceType
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws FieldNotFoundException 
	 * @throws XFTInitException 
	 * @throws ElementNotFoundException 
	 */
	public static void generateURISQL(final ItemI item, final URI expected, final String newArchive, final DBItemCache cache,final UserI user) throws UnsupportedResourceType, SQLException, Exception{
		final SecurityManager sm= SecurityManager.GetInstance();
		//set label and modify URI
		if(item instanceof XnatSubjectdata){
			for(final XnatAbstractresourceI res: ((XnatSubjectdata)item).getResources_resource()){
				modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
			}
		}else{
			for(final XnatAbstractresourceI res: ((XnatExperimentdata)item).getResources_resource()){
				modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
			}
			
			if(item instanceof XnatImagesessiondata){
				for(final XnatImagescandataI scan: ((XnatImagesessiondataI)item).getScans_scan()){
					for(final XnatAbstractresourceI res: scan.getFile()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
				}

				for(final XnatReconstructedimagedataI recon: ((XnatImagesessiondataI)item).getReconstructions_reconstructedimage()){
					for(final XnatAbstractresourceI res: recon.getIn_file()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
					
					for(final XnatAbstractresourceI res: recon.getOut_file()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
				}

				for(final XnatImageassessordataI assess: ((XnatImagesessiondataI)item).getAssessors_assessor()){
					boolean checkdPermissions =false;
					for(final XnatAbstractresourceI res: assess.getResources_resource()){
						if(modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache)){
							if(!checkdPermissions){
								if(checkPermissions((XnatImageassessordata)assess, user)){
									checkdPermissions=true;
								}else{
									throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for assessor in project: " + assess.getProject());
								}
							}
						}
					}
					
					for(final XnatAbstractresourceI res: assess.getIn_file()){
						if(modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache)){
							if(!checkdPermissions){
								if(checkPermissions((XnatImageassessordata)assess, user)){
									checkdPermissions=true;
								}else{
									throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for assessor in project: " + assess.getProject());
								}
							}
						}
					}
					
					for(final XnatAbstractresourceI res: assess.getOut_file()){
						if(modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache)){
							if(!checkdPermissions){
								if(checkPermissions((XnatImageassessordata)assess, user)){
									checkdPermissions=true;
								}else{
									throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for assessor in project: " + assess.getProject());
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Modifies the resource to point to the new path, if the old path is in the expected place.
	 * @param resource   The resource to be modified
	 * @param expected   The expected URI on completion
	 * @param newArchive The new archive location
	 * @param user       The user requesting the modification
	 * @param sm         The security manager
	 * @param cache      The item cache
	 *
	 * @return Returns true if the resource was modified successfully, false otherwise.
	 *
	 * @throws UnsupportedResourceType 
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws FieldNotFoundException 
	 * @throws XFTInitException 
	 * @throws ElementNotFoundException 
	 */
	protected static boolean modifyResource(final XnatAbstractresource resource, final URI expected, final String newArchive,final UserI user, final SecurityManager sm, final DBItemCache cache) throws UnsupportedResourceType, ElementNotFoundException, XFTInitException, FieldNotFoundException, SQLException, Exception{
		final String path=getPath(resource);
		final URI current= new File(path).toURI();
		
		final URI relative=expected.relativize(current);
		
		if(relative.equals(current)){
			//not within expected path
			final File oldSessionDir=new File(expected);
			if(path.replace('\\', '/').contains("/"+oldSessionDir.getName()+"/")){
				//session contains resource which is not in the standard format, but is in a directory with the old label.
				throw new UnsupportedResourceType();
			}else{
			return false;
			}
		}else{
			//properly in place
			setPath(resource,(new File(newArchive,relative.getPath())).getAbsolutePath());
			DBAction.StoreItem(resource.getItem(),user,false,false,false,false,sm,cache);
			
			return true;
		}
	}
	
	/**
	 * Generate update logic for modifying the label of the given item.
	 * @param item     The item being modified
	 * @param newLabel The label to be set
	 * @param cache    The item cache
	 * @param user     The user requesting the modification
	 *
	 * @throws SQLException
	 * @throws Exception
	 */
	protected static void generateLabelSQL(final ArchivableItem item, final String newLabel, final DBItemCache cache, final UserI user, final EventMetaI message) throws SQLException, Exception{
		item.getItem().setProperty("label", newLabel);
		DBAction.StoreItem(item.getItem(), user, false, false, false, false, SecurityManager.GetInstance(), message);
	}
	
	/**
	 * Executes the given cached logic against the database.  
	 * @param cache The item cache
	 * @param user  The user requesting the modification
	 *
	 * @throws Exception
	 */
	protected static void executeSQL(final DBItemCache cache, final UserI user) throws Exception{
		DBAction.executeCache(cache, user, user.getDBName());
	}
	
	/**
	 * Gets the path or URI of the given resource
	 * @param resource The resource
	 * @return The path or URI for the resource
	 * @throws UnsupportedResourceType When the resource is not an {@link XnatResource} or {@link XnatResourceseries}.
	 */
	protected static String getPath(final XnatAbstractresource resource) throws UnsupportedResourceType{
		if(resource instanceof XnatResource){
			return ((XnatResource)resource).getUri();
		}
		if(resource instanceof XnatResourceseries){
			return ((XnatResourceseries)resource).getPath();
		}
		throw new UnsupportedResourceType();
	}
	
	/**
	 * Sets the path or URI of the given resource to the newPath.  
	 * @param resource The resource to be modified
	 * @param newPath  The new path to be set.
	 * @throws UnsupportedResourceType When the resource is not an {@link XnatResource} or {@link XnatResourceseries}.
	 */
	protected static void setPath(final XnatAbstractresource resource, final String newPath) throws UnsupportedResourceType{
		if(resource instanceof XnatResource){
			((XnatResource)resource).setUri(newPath);
		}else if(resource instanceof XnatResourceseries){
			((XnatResourceseries)resource).setPath(newPath);
		}else{
			throw new UnsupportedResourceType();
		}
	}
	
	public STEP getStep() {
		return step;
	}

	public STEP setStep(STEP step) {
		return (this.step = step);
	}

	//EXCEPTION DECLARATIONS
	public class LabelConflictException extends Exception{
		public LabelConflictException(){
			super();
		}
	}
	
	public class DuplicateLabelException extends Exception{
		public DuplicateLabelException(){
			super();
		}
	}
	
	public class FolderConflictException extends Exception{
		public FolderConflictException(){
			super();
		}
	}
	
	public static class UnsupportedResourceType extends Exception{
		public UnsupportedResourceType(){
			super();
		}
	}
	
	public static class ProcessingInProgress extends Exception{
		private final String pipeline_name;
		public ProcessingInProgress(final String s){
			super();
			pipeline_name=s;
		}
		public String getPipeline_name() {
			return pipeline_name;
		}
	}
}
