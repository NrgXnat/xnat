/*
 * web: org.nrg.xnat.archive.Rename
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.nrg.transaction.TransactionException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatReconstructedimagedataI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourceseries;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
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

@SuppressWarnings("serial")
public class Rename  implements Callable<File>{
	enum STEP{PREPARING,PREPARE_SQL,COPY_DIR,ANONYMIZE,EXECUTE_SQL,DELETE_OLD_DIR,COMPLETE};
	static org.apache.log4j.Logger logger = Logger.getLogger(Rename.class);
	private static final String SUCCESSFUL_RENAMES = "successful_renames";
	private static final String FAILED_RENAME = "failed_rename";
	private final ArchivableItem i;
	private final XnatProjectdata proj;
	
	private final String newLabel,reason;
	private final UserI user;
	
	private final EventUtils.TYPE type;
	
	private STEP step=STEP.PREPARING;
	/**
	 * Only for use in the JUNIT tests
	 */
	public Rename(){
		proj=null;
		i=null;
		newLabel=null;
		user=null;
		reason=null;
		type=null;
	}
	
	public Rename(final XnatProjectdata p,final ArchivableItem e, final String lbl, final UserI u, final String reason, final EventUtils.TYPE type){
		proj=p;
		i=e;
		newLabel=lbl;
		user=u;
		this.reason=reason;
		this.type=type;
		
		if(i==null){
			throw new NullPointerException();
		}
		
		if(newLabel==null){
			throw new NullPointerException();
		}
		
		if(StringUtils.isEmpty(newLabel)){
			throw new IllegalArgumentException();
		}
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
		final File newSessionDir;
		if(i instanceof XnatSubjectdata){
			newSessionDir= FileUtils.getFile(proj.getRootArchivePath(),proj.getCurrentArc(),"subjects",newLabel);
		}else{
			newSessionDir = new File(new File(proj.getRootArchivePath(),proj.getCurrentArc()),newLabel);
		}
		
		try {
			final String id=i.getStringProperty("ID");
			final String lbl=i.getStringProperty("label");
			final String current_label=(StringUtils.isEmpty(lbl))?id:lbl;	
						
			if(newLabel.equals(current_label)){
				throw new DuplicateLabelException();
			}
			
			//confirm if user has permission
			if(!checkPermissions(i, user)){
				throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for project: " + proj.getId());
			}

			//confirm if new label is already in use
			if(i instanceof XnatSubjectdata){
				final XnatSubjectdata match=XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), newLabel,null, false);
				if(match!=null){
					throw new LabelConflictException();
				}
			}else{
				final XnatExperimentdata match=XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), newLabel,null, false);
				if(match!=null){
					throw new LabelConflictException();
				}
			}
			
			final Collection<? extends PersistentWorkflowI> open=PersistentWorkflowUtils.getOpenWorkflows(user, id);
			if(!open.isEmpty()){		
				throw new ProcessingInProgress(((WrkWorkflowdata)CollectionUtils.get(open, 0)).getPipelineName());
			}
			
			//confirm if new directory already exists w/ stuff in it
			if(newSessionDir.exists() && newSessionDir.list() != null && newSessionDir.list().length > 0){		
				throw new FolderConflictException();
			}else{
				newSessionDir.mkdir();
			}
			
			//identify existing directory
			final File oldSessionDir = i.getExpectedCurrentDirectory();
				
			final String message=String.format("Renamed from %s to %s", current_label,newLabel);
			
			//add workflow entry    		
			final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, i.getXSIType(), i.getStringProperty("ID"), proj.getId(),EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, type, EventUtils.RENAME, reason,null));
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
				generateLabelSQL(i, newLabel, cache, user,c);
				generateURISQL(i, expected, newArchive, cache, user);

				this.updateStep(workflow, setStep(STEP.COPY_DIR));

				if(moveFiles)org.nrg.xft.utils.FileUtils.CopyDir(oldSessionDir, newSessionDir,false);	
				
				if(i instanceof XnatImagesessiondata){
					this.updateStep(workflow, setStep(STEP.ANONYMIZE));
					new ProjectAnonymizer(newLabel,
										 (XnatImagesessiondata) i, 
										  proj.getId(), 
										  ((XnatImagesessiondata) i).getArchivePath(((XnatImagesessiondata) i).getArchiveRootPath())
										  ).call();
				}else if(i instanceof XnatSubjectdata){
					for(final XnatSubjectassessordata expt : ((XnatSubjectdata)i).getExperiments_experiment("xnat:imageSessionData")){
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
				if(moveFiles)org.nrg.xnat.utils.FileUtils.moveToCache(proj.getId(),SUCCESSFUL_RENAMES,oldSessionDir);
				
				//close workflow entry
				workflow.setStepDescription(setStep(STEP.COMPLETE).toString());
				workflow.setStatus(PersistentWorkflowUtils.COMPLETE);
			} catch (final Exception e) {
				if(!getStep().equals(STEP.DELETE_OLD_DIR)){
					try {
						if(moveFiles)org.nrg.xnat.utils.FileUtils.moveToCache(proj.getId(),FAILED_RENAME,newSessionDir);
					} catch (IOException e1) {
						logger.error("", e1);
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
			logger.error("", e);
		} catch (ElementNotFoundException e) {
			logger.error("", e);
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
	 * @param current_label
	 * @return
	 * @throws UnsupportedResourceType 
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws FieldNotFoundException 
	 * @throws XFTInitException 
	 * @throws ElementNotFoundException 
	 */
	public static void generateURISQL(final ItemI i, final URI expected, final String newArchive, final DBItemCache cache,final UserI user) throws UnsupportedResourceType, SQLException, Exception{
		final SecurityManager sm= SecurityManager.GetInstance();
		//set label and modify URI
		if(i instanceof XnatSubjectdata){
			for(final XnatAbstractresourceI res: ((XnatSubjectdata)i).getResources_resource()){
				modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
			}
		}else{
			for(final XnatAbstractresourceI res: ((XnatExperimentdata)i).getResources_resource()){
				modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
			}
			
			if(i instanceof XnatImagesessiondata){
				for(final XnatImagescandataI scan: ((XnatImagesessiondataI)i).getScans_scan()){
					for(final XnatAbstractresourceI res: scan.getFile()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
				}

				for(final XnatReconstructedimagedataI recon: ((XnatImagesessiondataI)i).getReconstructions_reconstructedimage()){
					for(final XnatAbstractresourceI res: recon.getIn_file()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
					
					for(final XnatAbstractresourceI res: recon.getOut_file()){
						modifyResource((XnatAbstractresource)res,expected,newArchive,user,sm,cache);
					}
				}

				for(final XnatImageassessordataI assess: ((XnatImagesessiondataI)i).getAssessors_assessor()){
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
	 * @param res
	 * @param expected
	 * @return
	 * @throws UnsupportedResourceType 
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws FieldNotFoundException 
	 * @throws XFTInitException 
	 * @throws ElementNotFoundException 
	 */
	protected static boolean modifyResource(final XnatAbstractresource res, final URI expected, final String newArchive,final UserI user, final SecurityManager sm, final DBItemCache cache) throws UnsupportedResourceType, ElementNotFoundException, XFTInitException, FieldNotFoundException, SQLException, Exception{
		final String path=getPath(res);
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
			setPath(res,(new File(newArchive,relative.getPath())).getAbsolutePath());
			DBAction.StoreItem(res.getItem(),user,false,false,false,false,sm,cache);
			
			return true;
		}
	}
	
	/**
	 * Generate update logic for modifying the label of the given item.
	 * @param i
	 * @param newLabel
	 * @param cache
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	protected static void generateLabelSQL(final ArchivableItem i, final String newLabel, final DBItemCache cache,final UserI user,EventMetaI message) throws SQLException, Exception{
		i.getItem().setProperty("label", newLabel);

		DBAction.StoreItem(i.getItem(),user,false,false,false,false,SecurityManager.GetInstance(),message);
	}
	
	/**
	 * Executes the given cached logic against the database.  
	 * @param cache
	 * @return
	 * @throws Exception 
	 */
	protected static void executeSQL(final DBItemCache cache, final UserI user) throws Exception{
		DBAction.executeCache(cache, user, user.getDBName());
	}
	
	/**
	 * Gets the path or URI of the given resource
	 * @param res
	 * @return
	 * @throws UnsupportedResourceType
	 */
	protected static String getPath(final XnatAbstractresource res) throws UnsupportedResourceType{
		if(res instanceof XnatResource){
			return ((XnatResource)res).getUri();
		}else if(res instanceof XnatResourceseries){
			return ((XnatResourceseries)res).getPath();
		}else{
			throw new UnsupportedResourceType();
		}
	}
	
	/**
	 * Sets the path or URI of the given resource to the newPath.  
	 * @param res
	 * @param newPath
	 * @throws UnsupportedResourceType
	 */
	protected static void setPath(final XnatAbstractresource res, final String newPath) throws UnsupportedResourceType{
		if(res instanceof XnatResource){
			((XnatResource)res).setUri(newPath);
		}else if(res instanceof XnatResourceseries){
			((XnatResourceseries)res).setPath(newPath);
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
