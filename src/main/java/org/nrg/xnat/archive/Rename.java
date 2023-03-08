/*
 * web: org.nrg.xnat.archive.Rename
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.transaction.TransactionException;
import org.nrg.xdat.XDAT;
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
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"serial", "RedundantThrows"})
@Slf4j
public class Rename implements Callable<File>{
    public static final String OLD_LABEL = "oldLabel";
    public static final String OLD_PATH  = "oldPath";
    public static final String NEW_LABEL = "newLabel";
    public static final String NEW_PATH  = "newPath";

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
	 * @return The file object for the new session folder.
	 * 
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 * @throws ProcessingInProgress When the session to be renamed is already being processed.
	 * @throws DuplicateLabelException When the new label is the same as the current label.
	 * @throws IllegalAccessException When the user doesn't have permission to operate on a subject, experiment, or resource.
	 * @throws LabelConflictException When the new label replicates an existing label.
	 * @throws FolderConflictException When a folder with the same name as the new label already exists and contains files or folders.
	 * @throws InvalidArchiveStructure When a folder can't be located in the expected location.
	 * @throws URISyntaxException When the URI for an item is malformed or invalid.
	 * @throws Exception When an unexpected error.
	 */
	public File call() throws FieldNotFoundException, ProcessingInProgress, DuplicateLabelException, IllegalAccessException, LabelConflictException, FolderConflictException, InvalidArchiveStructure, URISyntaxException, Exception {
		final boolean isSubject = item instanceof XnatSubjectdata;
		final File newSessionDir = isSubject
								   ? FileUtils.getFile(project.getRootArchivePath(), project.getCurrentArc(), "subjects", newLabel)
								   : new File(new File(project.getRootArchivePath(), project.getCurrentArc()), newLabel);

		try {
			final String id           = item.getStringProperty("ID");
			final String currentLabel = StringUtils.defaultIfBlank(item.getStringProperty("label"), id);

			if (newLabel.equals(currentLabel)) {
				throw new DuplicateLabelException();
			}

			//Confirm user has permission
			final String projectId = project.getId();
			if (!checkPermissions(item, user)) {
				throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for project: " + projectId);
			}

			//Confirm new label not already in use
			final ArchivableItem match = isSubject
										 ? XnatSubjectdata.GetSubjectByProjectIdentifier(projectId, newLabel, null, false)
										 : XnatExperimentdata.GetExptByProjectIdentifier(projectId, newLabel, null, false);
			if (match != null) {
				throw new LabelConflictException();
			}

			//Confirm processing not running
			final Collection<? extends PersistentWorkflowI> open = PersistentWorkflowUtils.getOpenWorkflows(user, id);
			if (!open.isEmpty()) {
				throw new ProcessingInProgress(((WrkWorkflowdata) CollectionUtils.get(open, 0)).getPipelineName());
			}

			//Confirm new directory doesn't exist w/ stuff in it
			if (newSessionDir.exists() && ArrayUtils.getLength(newSessionDir.list()) > 0) {
				throw new FolderConflictException();
			}
			newSessionDir.mkdir();

			final String message = String.format("Renamed from %s to %s", currentLabel, newLabel);

			//Add workflow entry
			final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, item.getXSIType(), item.getStringProperty("ID"), projectId, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, type, EventUtils.RENAME, reason, null));
			assert workflow != null;
			workflow.setDetails(message);
			EventMetaI eventMeta = WorkflowUtils.setStep(workflow, getStep().toString());
			PersistentWorkflowUtils.save(workflow, eventMeta);

			//Variables
			final File    oldSessionDir = item.getExpectedCurrentDirectory();
			final URI     expected      = oldSessionDir.toURI();
			final String  newArchive    = newSessionDir.getAbsolutePath();
			final boolean moveFiles     = oldSessionDir.exists();

			//Generate SQL to update URIs
			eventMeta = updateStep(workflow, setStep(STEP.PREPARE_SQL));
			final DBItemCache cache = new DBItemCache(user, eventMeta);
			generateLabelSQL(item, newLabel, user, eventMeta);
			generateURISQL(item, expected, newArchive, cache, user);

			//Copy files to new location
			eventMeta = updateStep(workflow, setStep(STEP.COPY_DIR));
			if (moveFiles) {
				FileUtils.copyDirectory(oldSessionDir, newSessionDir);
			}

			try {
				//Apply anonymization. If an anonymization script exists, this will pull all files into local filesystem,
				//where they'll be updated and later pushed to remote on cleanup.
				if (DefaultAnonUtils.getService().isProjectScriptEnabled(projectId)) {
					eventMeta = updateStep(workflow, setStep(STEP.ANONYMIZE));
					if (item instanceof XnatImagesessiondata) {
						new ProjectAnonymizer(newLabel, (XnatImagesessiondata) item, projectId, ((XnatImagesessiondata) item).getArchivePath(item.getArchiveRootPath())).call();
					} else if (isSubject) {
						for (final XnatSubjectassessordata expt : ((XnatSubjectdata) item).getExperiments_experiment("xnat:imageSessionData")) {
							try {
								// re-apply this project's edit script
								expt.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) expt, newLabel, expt.getProject(), expt.getArchiveRootPath()));
							} catch (TransactionException e) {
								throw new AnonException(e);
							}
						}
					}
				}

				//Execute SQL
				updateStep(workflow, setStep(STEP.EXECUTE_SQL));
				executeSQL(cache, user);

				//If successful, move old directory to cache
				updateStep(workflow, setStep(STEP.DELETE_OLD_DIR));
				if (moveFiles) {
					org.nrg.xnat.utils.FileUtils.moveToCache(projectId, SUCCESSFUL_RENAMES, oldSessionDir);
				}

				//Close workflow entry
				workflow.setStepDescription(setStep(STEP.COMPLETE).toString());
				workflow.setStatus(PersistentWorkflowUtils.COMPLETE);

                // Fire event so other functions can adjust as needed.
                XDAT.triggerXftItemEvent(item.getXSIType(), id, EventUtils.RENAME, ImmutableMap.of(OLD_LABEL, currentLabel, OLD_PATH, oldSessionDir.getAbsoluteFile().toPath(), NEW_LABEL, newLabel, NEW_PATH, newSessionDir.getAbsoluteFile().toPath()));
			} catch (final Exception e) {
				if (!getStep().equals(STEP.DELETE_OLD_DIR)) {
					try {
						if (moveFiles) {
							org.nrg.xnat.utils.FileUtils.moveToCache(projectId, FAILED_RENAME, newSessionDir);
						}
					} catch (IOException e1) {
						log.error("Issue caching rename failure", e1);
					}

					//Fail workflow
					workflow.setStatus(PersistentWorkflowUtils.FAILED);

					throw e;
				} else {
					workflow.setStatus(PersistentWorkflowUtils.COMPLETE);
				}
			} finally {
				PersistentWorkflowUtils.save(workflow, eventMeta);
			}
		} catch (XFTInitException e) {
			log.error("XFT failed to initialize properly.", e);
		} catch (ElementNotFoundException e) {
			log.error("Element not found while trying to process item of type {}", e.ELEMENT, e);
		}

		return newSessionDir;
	}
	
	public EventMetaI updateStep(final PersistentWorkflowI workflow, final STEP step) throws Exception{
		final EventMetaI eventMeta = WorkflowUtils.setStep(workflow, step.toString());
		PersistentWorkflowUtils.save(workflow, eventMeta);
		return eventMeta;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkPermissions(final ArchivableItem item, final UserI user) throws Exception{
		return Permissions.canEdit(user,item);
	}
	
	/**
	 * Generate the SQL update logic for all the items resources.
	 * Checks permissions for assessments, if they were modified.
	 * 
	 * @param item       The item
	 * @param expected   The expected URI
	 * @param newArchive The new archive path
	 * @param cache      The item cache
	 * @param user       The user requesting the SQL.

	 * @throws UnsupportedResourceType Indicates that the particular resource type is not supported.
	 * @throws Exception When an unexpected error.
	 * @throws SQLException When an error occurs trying to execute an SQL query.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws ElementNotFoundException When an XFT data-type element can't be found in the database.
	 */
	public static void generateURISQL(final ItemI item, final URI expected, final String newArchive, final DBItemCache cache, final UserI user) throws UnsupportedResourceType, SQLException, Exception {
		final SecurityManager sm = SecurityManager.GetInstance();
		//set label and modify URI
		if (item instanceof XnatSubjectdata) {
			for (final XnatAbstractresourceI res : ((XnatSubjectdata) item).getResources_resource()) {
				modifyResource((XnatAbstractresource) res, expected, newArchive, user, sm, cache);
			}
		} else {
			for (final XnatAbstractresourceI res : ((XnatExperimentdata) item).getResources_resource()) {
				modifyResource((XnatAbstractresource) res, expected, newArchive, user, sm, cache);
			}

			if (item instanceof XnatImagesessiondata) {
				for (final XnatImagescandataI scan : ((XnatImagesessiondataI) item).getScans_scan()) {
					for (final XnatAbstractresourceI res : scan.getFile()) {
						modifyResource((XnatAbstractresource) res, expected, newArchive, user, sm, cache);
					}
				}

				for (final XnatReconstructedimagedataI recon : ((XnatImagesessiondataI) item).getReconstructions_reconstructedimage()) {
					for (final XnatAbstractresourceI res : recon.getIn_file()) {
						modifyResource((XnatAbstractresource) res, expected, newArchive, user, sm, cache);
					}

					for (final XnatAbstractresourceI res : recon.getOut_file()) {
						modifyResource((XnatAbstractresource) res, expected, newArchive, user, sm, cache);
					}
				}

				for (final XnatImageassessordataI assessor : ((XnatImagesessiondataI) item).getAssessors_assessor()) {
					final AtomicBoolean checkedPermissions = new AtomicBoolean();
					for (final XnatAbstractresourceI resource : Stream.of(assessor.getResources_resource(), assessor.getIn_file(), assessor.getOut_file()).flatMap(Collection::stream).collect(Collectors.toList())) {
						if (modifyResource((XnatAbstractresource) resource, expected, newArchive, user, sm, cache)) {
							if (!checkedPermissions.get()) {
								if (!checkPermissions((XnatImageassessordata) assessor, user)) {
									throw new org.nrg.xdat.exceptions.IllegalAccessException("Invalid Edit permissions for assessor in project: " + assessor.getProject());
								}
								checkedPermissions.set(true);
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
	 * @param securityManager         The security manager
	 * @param cache      The item cache
	 *
	 * @return Returns true if the resource was modified successfully, false otherwise.
	 *
	 * @throws UnsupportedResourceType Indicates that the particular resource type is not supported.
	 * @throws SQLException When an error occurs trying to execute an SQL query.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 * @throws ElementNotFoundException When an XFT data-type element can't be found in the database.
	 * @throws Exception An unexpected error occurred.
	 */
	protected static boolean modifyResource(final XnatAbstractresource resource, final URI expected, final String newArchive, final UserI user, final SecurityManager securityManager, final DBItemCache cache) throws UnsupportedResourceType, ElementNotFoundException, XFTInitException, FieldNotFoundException, SQLException, Exception{
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
			DBAction.StoreItem(resource.getItem(),user,false,false,false,false,securityManager,cache);
			
			return true;
		}
	}
	
	/**
	 * Generate update logic for modifying the label of the given item.
	 * @param item     The item being modified
	 * @param newLabel The label to be set
	 * @param user     The user requesting the modification
	 *
	 * @throws SQLException When an error occurs trying to execute an SQL query.
	 * @throws Exception When an unexpected error.
	 */
	protected static void generateLabelSQL(final ArchivableItem item, final String newLabel, final UserI user, final EventMetaI message) throws SQLException, Exception{
		item.getItem().setProperty("label", newLabel);
		DBAction.StoreItem(item.getItem(), user, false, false, false, false, SecurityManager.GetInstance(), message);
	}
	
	/**
	 * Executes the given cached logic against the database.  
	 * @param cache The item cache
	 * @param user  The user requesting the modification
	 *
	 * @throws Exception When an unexpected error.
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

	public STEP setStep(final STEP step) {
		return (this.step = step);
	}

	//EXCEPTION DECLARATIONS
	public static class LabelConflictException extends Exception{
		public LabelConflictException(){
			super();
		}
	}
	
	public static class DuplicateLabelException extends Exception{
		public DuplicateLabelException(){
			super();
		}
	}
	
	public static class FolderConflictException extends Exception{
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
		private final String _pipelineName;
		public ProcessingInProgress(final String pipelineName){
			super();
			_pipelineName =pipelineName;
		}
		public String getPipelineName() {
			return _pipelineName;
		}
	}
}
