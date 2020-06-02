/*
 * web: org.nrg.xnat.archive.PrearcSessionArchiver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.JustificationAbsent;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.event.archive.ArchiveStatusProducer;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.SessionMergingConfigMapper;
import org.nrg.xnat.helpers.merge.MergePrearcToArchiveSession;
import org.nrg.xnat.helpers.merge.MergeSessionsA.SaveHandlerI;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.TriggerPipelines;
import org.nrg.xnat.services.archive.ApplyPluginLabelingService;
import org.nrg.xnat.services.archive.ApplyPluginValidationService;
import org.nrg.xnat.status.ListenerUtils;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;
import org.xml.sax.SAXException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

@Slf4j
public class PrearcSessionArchiver extends ArchiveStatusProducer implements Callable<String> {

    public static final String MERGED = "Merged";
    
    public static final String MERGED_UID = "Merged UIDs";

    private static final String TRIGGER_PIPELINES = "triggerPipelines";

    public static final String PRE_EXISTS = "Session already exists, retry with overwrite enabled";

    public static final String SUBJECT_MOD = "Invalid modification of session subject via archive process.";

    public static final String PROJ_MOD = "Invalid modification of session project via archive process.";

    public static final String LABEL_MOD = "Invalid modification of session label via archive process.";

    public static final String UID_MOD = "Invalid modification of session UID via archive process.";

    public static final String MODALITY_MOD = "Invalid modification of session modality via archive process.  Data may require a manual merge.";

    public static final String LABEL2 = "label";

    public static final String PARAM_SESSION = "session";
    public static final String PARAM_SUBJECT = "subject";

    protected       XnatImagesessiondata src;
    protected final UserI                user;
    protected final String               project;
    protected final Map<String, Object>  params;

    protected final PrearcSession prearcSession;

    private final boolean overrideExceptions;//as of 1.6.2 this is being used to override any potential overridable exception
    private final boolean allowSessionMerge;//should process proceed if the session already exists
    private final boolean overwriteFiles;//should process proceed if the same file is uploaded again
    private final boolean waitFor;

    private ApplyPluginValidationService applyPluginValidationService;
    private ApplyPluginLabelingService applyPluginLabelingService;

    private boolean needsScanIdCorrection = false;
    private DicomFilterService _filterService;

    protected PrearcSessionArchiver(final Object control, final XnatImagesessiondata src, final PrearcSession prearcSession, final UserI user, final String project, final Map<String, Object> params, final Boolean overrideExceptions, final Boolean allowSessionMerge, final Boolean waitFor, final Boolean overwriteFiles) {
        super(control);
        this.src = src;
        this.user = user;
        this.project = project;
        this.params = params;
        this.overrideExceptions = (overrideExceptions == null) ? false : overrideExceptions;
        this.allowSessionMerge = (allowSessionMerge == null) ? false : allowSessionMerge;
        this.overwriteFiles = (overwriteFiles == null) ? false : overwriteFiles;
        this.prearcSession = prearcSession;
        this.waitFor = waitFor;
    }

    public PrearcSessionArchiver(final Object control,
                                 final PrearcSession session,
                                 final UserI user,
                                 final Map<String, Object> params,
                                 boolean overrideExceptions,
                                 final boolean allowSessionMerge,
                                 final boolean waitFor,
                                 final Boolean overwriteFiles)
            throws IOException, SAXException {
        this(control,
                (new XNATSessionPopulater(user,
                        session.getSessionDir(),
                        session.getProject(),
                        false)).populate(),
                session,
                user,
                session.getProject(),
                params,
                overrideExceptions,
                allowSessionMerge,
                waitFor,
                overwriteFiles);
    }

    public XnatImagesessiondata getSrc() {
        return src;
    }

    public File getSrcDIR() {
        return prearcSession.getSessionDir();
    }


    public XnatImagesessiondata retrieveExistingExpt() {
        XnatImagesessiondata existing = null;

        //review existing sessions
        if (XNATUtils.hasValue(src.getId())) {
            existing = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(src.getId(), user, false);
        }

        if (existing == null) {
            existing = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(project, src.getLabel(), user, false);
        }

        return existing;
    }

    /**
     * Determine an appropriate session label.
     *
     * @throws ClientException When an error occurs on the client side.
     */
    protected void fixSessionLabel() throws ClientException {
        String label = (String) params.get(PARAM_SESSION);

        if (StringUtils.isEmpty(label)) {
            label = (String) params.get(src.getXSIType() + "/label");
        }

        if (StringUtils.isEmpty(label)) {
            label = (String) params.get(URIManager.EXPT_LABEL);
        }

        if (StringUtils.isEmpty(label)) {
            label = (String) params.get(LABEL2);
        }

        if (StringUtils.isEmpty(label) && !SessionData.UPLOADER.equals(params.get(URIManager.SOURCE))) {
            label = getApplyPluginLabelingService().label(src, params, user);
        }

        //the previous code allows the value in the session xml to be overridden by passed parameters.
        //if they aren't there, then it should default to the xml label
        if (StringUtils.isEmpty(label) && !StringUtils.isEmpty(src.getLabel())) {
            label = src.getLabel();
        }

        if (StringUtils.isEmpty(label)) {
            label = prearcSession.getFolderName();
        }

        if (StringUtils.isEmpty(label)) {
            label = src.getId();
        }

        if (StringUtils.isNotEmpty(label)) {
            src.setLabel(XnatImagesessiondata.cleanValue(label));
        }

        if (!XNATUtils.hasValue(src.getLabel())) {
            failed("unable to deduce session label");
            throw new ClientException("unable to deduce session label");
        }
    }

    public static XnatSubjectdata retrieveMatchingSubject(final String id, final String project, final UserI user) {
        if (StringUtils.isNotBlank(project)) {
            // XNAT-2865 - Perform case insensitive search for subject
            final XnatSubjectdata subject = XnatSubjectdata.GetSubjectByProjectIdentifierCaseInsensitive(project, id, user, false);
            if (subject != null) {
                return subject;
            }
        }
        return XnatSubjectdata.getXnatSubjectdatasById(id, user, false);
    }

    /**
     * Ensure that the subject label and ID are set in the session --
     * by deriving and setting them, if necessary.
     *
     * @throws ClientException When an error occurs on the client side.
     * @throws ServerException When an error occurs on the server.
     */
    protected void fixSubject(EventMetaI c, boolean allowNewSubject) throws ClientException, ServerException {
        final String rawSubjectId = StringUtils.firstNonBlank((String) params.get(PARAM_SUBJECT), (String) params.get(URIManager.SUBJECT_ID), src.getSubjectId(), src.getDcmpatientname());

        if (rawSubjectId == null) {
            failed("Unable to identify subject.");
            throw new ClientException("Unable to identify subject.");
        }

        final String subjectId = XnatImagesessiondata.cleanValue(rawSubjectId);
        processing("looking for subject " + subjectId);

        log.debug("Trying to get or create the subject with ID {}", subjectId);
        getOrCreateSubject(subjectId, allowNewSubject, c);
    }

    /**
     * Retrieves the archive session directory for the given session.
     *
     * @return archive session directory
     *
     * @throws UnknownPrimaryProjectException When the primary project can't be found.
     * @throws ServerException When an error occurs on the server.
     */
    protected File getArcSessionDir() throws ServerException, UnknownPrimaryProjectException {
        final File currentArcDir;
        try {
            final String path = src.getCurrentArchiveFolder();
            currentArcDir = (null == path) ? null : new File(path);
        } catch (InvalidArchiveStructure e) {
            throw new ServerException("couldn't get archive folder for " + src, e);
        }
        final String sessDirName = src.getArchiveDirectoryName();
        final File relativeSessionDir;
        if (null == currentArcDir) {
            relativeSessionDir = new File(sessDirName);
        } else {
            relativeSessionDir = new File(currentArcDir, sessDirName);
        }

        final File rootArchiveDir = new File(src.getPrimaryProject(false).getRootArchivePath());

        return new File(rootArchiveDir, relativeSessionDir.getPath());
    }


    /**
     * Verify that the session isn't already in the transfer pipeline.
     *
     * @throws ClientException When an error occurs on the client side.
     */
    protected void preventConcurrentArchiving(final String id, final UserI user) throws ClientException {
        if (!overrideExceptions) {//allow overriding of this behavior via the overwrite parameter
            final Collection<? extends PersistentWorkflowI> workflows = PersistentWorkflowUtils.getOpenWorkflows(user, id);
            if (!workflows.isEmpty()) {
                this.failed("Session processing in progress:" + ((WrkWorkflowdata) CollectionUtils.get(workflows, 0)).getOnlyPipelineName());
                throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session processing may already be in progress: " + ((WrkWorkflowdata) CollectionUtils.get(workflows, 0)).getOnlyPipelineName() + ".  Concurrent modification is discouraged.", new Exception());
            }
        }
    }

    /**
     * Updates the prearchive session XML, if possible. Errors here are logged but not
     * otherwise handled; messing up the prearchive session XML is not a disaster.
     *
     * @param prearcSessionPath path of session directory in prearchive
     */
    protected void updatePrearchiveSessionXML(final String prearcSessionPath, final XnatImagesessiondata newSession) {
        final File prearcSessionDir = new File(prearcSessionPath);
        try (final FileWriter prearcXML = new FileWriter(prearcSessionDir.getPath() + ".xml")) {
            log.debug("Preparing to update prearchive XML for {}", newSession);
            ((XFTItem) newSession.getItem().clone()).toXML(prearcXML, false);
        } catch (RuntimeException e) {
            log.error("unable to update prearchive session XML", e);
            warning("updated prearchive session XML could not be written: " + e.getMessage());
        } catch (SAXException e) {
            log.error("attempted to write invalid updated prearchive session XML", e);
            warning("updated prearchive session XML is invalid: " + e.getMessage());
        } catch (FileNotFoundException e) {
            log.error("unable to update prearchive session XML", e);
            warning("prearchive session XML not found, cannot update");
        } catch (IOException e) {
            log.error("error updating prearchive session XML", e);
            warning("could not update prearchive session XML: " + e.getMessage());
        }
    }


    /**
     * This method will allow users to pass xml path as parameters.  The values supplied will be copied into the loaded session.
     *
     * @throws ClientException When an error occurs on the client side.
     */
    protected void populateAdditionalFields() throws ClientException {
        //prepare params by removing non xml path names
        final Map<String, Object> cleaned = XMLPathShortcuts.identifyUsableFields(params, XMLPathShortcuts.EXPERIMENT_DATA, false);
        XFTItem i = src.getItem();

        if (cleaned.size() > 0) {
            try {
                i.setProperties(cleaned, true);
                i.removeEmptyItems();
            } catch (Exception e) {
                failed("unable to map parameters to valid xml path: " + e.getMessage());
                throw new ClientException("unable to map parameters to valid xml path: ", e);
            }
        }
        src = (XnatImagesessiondata) BaseElement.GetGeneratedItem(i);

        // copy project into scan
        for (XnatImagescandataI scan : src.getScans_scan()) {
            scan.setProject(project);
        }
    }

    public void checkForConflicts(final XnatImagesessiondata src, final File srcDIR, final XnatImagesessiondata existing, final File destDIR) throws ClientException {
        if (existing != null) {
            if (!allowSessionMerge) {
                failed(PRE_EXISTS);
                throw new ClientException(Status.CLIENT_ERROR_CONFLICT, PRE_EXISTS, new Exception());
            }

            if (!StringUtils.equals(src.getLabel(), existing.getLabel())) {
                this.failed(LABEL_MOD);
                throw new ClientException(Status.CLIENT_ERROR_CONFLICT, LABEL_MOD, new Exception());
            }

            if (!StringUtils.equals(existing.getProject(), src.getProject())) {
                failed(PROJ_MOD);
                throw new ClientException(Status.CLIENT_ERROR_CONFLICT, PROJ_MOD, new Exception());
            }

            if (!StringUtils.equals(existing.getSubjectId(), src.getSubjectId())) {
                String subjectId = existing.getLabel();
                String newError = SUBJECT_MOD + ": " + subjectId + " Already Exists for another Subject";
                failed(newError);
                throw new ClientException(Status.CLIENT_ERROR_CONFLICT, newError, new Exception());
            }

            if (!overrideExceptions) {
                if (StringUtils.isNotEmpty(existing.getUid()) && StringUtils.isNotEmpty(src.getUid())) {
                    if (!StringUtils.equals(existing.getUid(), src.getUid())) {
                    	SessionMergingConfigMapper mapper=new SessionMergingConfigMapper();
                    	if(!mapper.getUidModSetting(existing.getProject())){
                    		failed(UID_MOD);
                    		throw new ClientException(Status.CLIENT_ERROR_CONFLICT, UID_MOD, new Exception());
                    	}
                    }
                }
            }

            for (final XnatImagescandataI newScan : src.getScans_scan()) {
                XnatImagescandataI match = MergeUtils.getMatchingScanById(newScan.getId(), existing.getScans_scan());//match by ID
                if (match != null) {
                    if (StringUtils.equals(match.getUid(), newScan.getUid())) {
                        //noinspection ConstantConditions
                        if (!allowSessionMerge) {
                            throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan (" + match.getId() + ") with the same UID and number.", new Exception());
                        }
                    } else if (StringUtils.isNotEmpty(match.getUid())) {
                        //noinspection ConstantConditions
                        if (!allowSessionMerge) {
                            throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan (" + match.getId() + ") with the same number, but a different UID.", new Exception());
                        } else {
                            needsScanIdCorrection = true;
                        }
                    }
                }

                XnatImagescandataI match2 = MergeUtils.getMatchingScanByUID(newScan, existing.getScans_scan());//match by UID
                if (match2 != null) {
                    if (match == null || !StringUtils.equals(match.getId(), newScan.getId())) {
                        if (!overrideExceptions) {
                            throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan with the same UID, but a different number (" + match2.getId() + ").", new Exception());
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public String call() throws ClientException, ServerException {
        try {
            this.lock(this.prearcSession.getUrl());
        } catch (LockedItemException e3) {
            throw new ClientException(Status.CLIENT_ERROR_LOCKED, "Duplicate archive attempt. Prearchive session already archiving.", e3);
        }

        try {
            if (StringUtils.isEmpty(project)) {
                failed("unable to identify destination project");
                throw new ClientException("unable to identify destination project", new Exception());
            }
            populateAdditionalFields();

            fixSessionLabel();

            final XnatImagesessiondata existing = retrieveExistingExpt();

            if (existing == null) {
                try {
                    if (StringUtils.isBlank(src.getId())) {
                        src.setId(XnatExperimentdata.CreateNewID());
                    }
                } catch (Exception e) {
                    failed("unable to create new session ID");
                    throw new ServerException("unable to create new session ID", e);
                }
            } else {
                try {
                    lock(existing.getId());
                } catch (LockedItemException e3) {
                    throw new ClientException(Status.CLIENT_ERROR_LOCKED, "Duplicate archive attempt.  Destination session in use.", e3);
                }
                src.setId(existing.getId());
                preventConcurrentArchiving(existing.getId(), user);
            }

            final PersistentWorkflowI workflow;
            final EventMetaI c;

            PersistentWorkflowI workflow2 = null;
            try {
                String justification = (String) params.get(EventUtils.EVENT_REASON);
                if (justification == null) {
                    justification = "standard upload";
                }
                workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, ((existing==null)?src.getItem():existing.getItem()),EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getType((String)params.get(EventUtils.EVENT_TYPE),EventUtils.TYPE.WEB_SERVICE), (existing==null)?EventUtils.TRANSFER:MERGED, justification, (String)params.get(EventUtils.EVENT_COMMENT)));
                if (workflow == null) {
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL, "Unable to create workflow");
                }
			    workflow.setStepDescription("Validating");
                c = workflow.buildEvent();
                
				if(existing!=null){
					if(!StringUtils.equals(existing.getUid(),src.getUid())){
						workflow2 = PersistentWorkflowUtils.buildOpenWorkflow(user, existing.getItem(),EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getType((String)params.get(EventUtils.EVENT_TYPE),EventUtils.TYPE.WEB_SERVICE), MERGED_UID, justification, (String)params.get(EventUtils.EVENT_COMMENT)));
						workflow2.setStepDescription("Validating");
					}
				}
            } catch (JustificationAbsent e2) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, e2);
            } catch (EventRequirementAbsent e2) {
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e2);
            }

            try{
                fixSubject(c, true);
            }
            catch(Exception e){
                try {
                    Thread.sleep(10000);
                }catch(InterruptedException ignored){
                }
                fixSubject(c, true);
            }

            try {
                try {
                    preArchive(user, src, params, existing);
                } catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        throw e.getCause();
                    } else {
                        throw e;
                    }
                }
                
                processing("validating loaded data");
                validateSession();

                final File arcSessionDir = getArcSessionDir();

                if (existing != null)
                    checkForConflicts(src, this.prearcSession.getSessionDir(), existing, arcSessionDir);


                if (!overrideExceptions) {
                    validateDicomFiles();
                }

                if (XDAT.getBoolSiteConfigurationProperty("verifyComplianceInPrearcSessionReview", false)) {
                    verifyCompliance();
                }

                try {
                    getApplyPluginValidationService().validate(src, params, user);
                } catch (ClientException e) {
                    // ServerException exceptions ought to be thrown always, ClientException only if overrideExceptions=false
                    if (!overrideExceptions) {
                        throw e;
                    }
                }

                if (arcSessionDir.exists()) {
                    processing("merging files data with existing session");
                } else {
                    processing("archiving session");
                }

                final boolean shouldForceQuarantine;
                shouldForceQuarantine = params.containsKey(ViewManager.QUARANTINE) && params.get(ViewManager.QUARANTINE).toString().equalsIgnoreCase("true");

                if (needsScanIdCorrection) {
                    correctScanID(existing);
                }

                SaveHandlerI<XnatImagesessiondata> saveImpl = new SaveHandlerI<XnatImagesessiondata>() {
                    public void save(XnatImagesessiondata merged) throws Exception {
                        if (SaveItemHelper.authorizedSave(merged, user, false, false, c)) {
                            final Date inserted = merged.getItem().getInsertDate();
                            final Date lastModified = merged.getItem().getLastModified();
                            XDAT.triggerXftItemEvent(merged, lastModified == null || lastModified.getTime() - inserted.getTime() == 0 ? CREATE : UPDATE);
                            Users.clearCache(user);
                            try {
                                MaterializedView.deleteByUser(user);
                            } catch (Exception e) {
                                log.error("", e);
                            }

                            try {
                                if (shouldForceQuarantine) {
                                    src.quarantine(user);
                                } else {
                                    // A bunch of null checks here because, under certain race or heavy load conditions,
                                    // one or more of these values can come back null.
                                    final XnatProjectdata project = src.getPrimaryProject(false);
                                    if (project != null) {
                                        final ArcProject arcProject = project.getArcSpecification();
                                        if (arcProject != null) {
                                            final Integer quarantineCode = arcProject.getQuarantineCode();
                                            if (quarantineCode != null) {
                                                if (quarantineCode.equals(1)) {
                                                    src.quarantine(user);
                                                }
                                            } else {
                                                log.debug("Got arcProject {} for project {} associated with session {}, but the quarantine code was null", arcProject.getArcProjectId(), project.getId(), src.getLabel());
                                            }
                                        } else {
                                            log.debug("Didn't find arcProject for project {}", project.getId());
                                        }
                                    } else {
                                        log.debug("Couldn't get primary project for session {}.", src.getLabel());
                                    }
                                }
                            } catch (Exception e) {
                                log.error("", e);
                            }
                        }
                    }
                };

				MergePrearcToArchiveSession mergePrearcToArchiveSession= new MergePrearcToArchiveSession(src.getPrearchivePath(),
						 this.prearcSession,
						 src,
						 src.getPrearchivepath(),
						 arcSessionDir,
						 existing,
						 arcSessionDir.getAbsolutePath(),
						 allowSessionMerge, 
						 (overrideExceptions)?overrideExceptions:overwriteFiles,
						 saveImpl,user,workflow.buildEvent());
				
				ListenerUtils.addListeners(this, mergePrearcToArchiveSession).call();
				XnatImagesessiondata merged=mergePrearcToArchiveSession.getMerged();

                FileUtils.DeleteFile(new File(this.prearcSession.getSessionDir().getAbsolutePath() + ".xml"));
                FileUtils.DeleteFile(this.prearcSession.getSessionDir());
                File timestampedDir = new File(this.prearcSession.getSessionDir().getParent());
                File projectDir = timestampedDir.getParentFile();
                final File[] timestampedDirFiles = timestampedDir.listFiles();
                if (timestampedDirFiles == null || timestampedDirFiles.length == 0) {
                    FileUtils.DeleteFile(timestampedDir);
                }
                final File[] projectDirFiles = projectDir.listFiles();
                if (projectDirFiles == null || projectDirFiles.length == 0) {
                    // to keep things tidy, also direct the project-level dir if it's empty
                    FileUtils.DeleteFile(projectDir);
                }

                try {
                    workflow.setStepDescription(PersistentWorkflowUtils.COMPLETE);
                    WorkflowUtils.complete(workflow, workflow.buildEvent());
					if(workflow2!=null){
						workflow2.setStepDescription(PersistentWorkflowUtils.COMPLETE);
						WorkflowUtils.complete(workflow2, workflow2.buildEvent());
					}

                } catch (Exception e1) {
                    log.error("", e1);
                }

                postArchive(user, merged, params);

                String triggerPipelines = (String) params.get(TRIGGER_PIPELINES);
                //if triggerPipelines!=false
                if ((BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(triggerPipelines)))) {
                    TriggerPipelines tp = new TriggerPipelines(merged, false, user, waitFor);
                    tp.call();
                }
            } catch (ServerException | ClientException e) {
                throw e;
            } catch (Throwable e) {
                log.error("", e);
                throw new ServerException(e.getMessage(), new Exception(e));
            }
        } finally {
            unlock();
        }

        final String url = buildURI(project, src);

        completed("archiving operation complete");
        return url;
    }

    protected ApplyPluginValidationService getApplyPluginValidationService() {
        if (applyPluginValidationService != null) {
            return applyPluginValidationService;
        }
        synchronized (this) {
            if (applyPluginValidationService == null) {
                applyPluginValidationService = XDAT.getContextService().getBean(ApplyPluginValidationService.class);
            }
            return applyPluginValidationService;
        }
    }

    protected ApplyPluginLabelingService getApplyPluginLabelingService() {
        if (applyPluginLabelingService != null) {
            return applyPluginLabelingService;
        }
        synchronized (this) {
            if (applyPluginLabelingService == null) {
                applyPluginLabelingService = XDAT.getContextService().getBean(ApplyPluginLabelingService.class);
            }
            return applyPluginLabelingService;
        }
    }

    /**
     * This code is used by this class and PrearcSessionValidator to confirm that all scan data is compliant according to the SeriesImportFilters
     *
     * @throws ClientException When an error occurs on the client side.
     */
    protected void verifyCompliance() throws ClientException {
        final SeriesImportFilter siteWide = getDicomFilterService().getSeriesImportFilter();
        final SeriesImportFilter projectSpecific = StringUtils.isNotEmpty(project)
                ? getDicomFilterService().getSeriesImportFilter(project)
                : null;

        for (final XnatImagescandataI scan : src.getScans_scan()) {
            if (siteWide != null && siteWide.isEnabled() && !siteWide.shouldIncludeDicomObject(convertScanToMap(scan))) {
                fail(22, String.format("Scan %1$s is non-compliant with this server's DICOM whitelist/blacklist.", scan.getId()));
            }
            if (projectSpecific != null && projectSpecific.isEnabled() && !projectSpecific.shouldIncludeDicomObject(convertScanToMap(scan))) {
                fail(22, String.format("Scan %1$s is non-compliant with this project's DICOM whitelist/blacklist.", scan.getId()));
            }
        }
    }

    private Map<String, String> convertScanToMap(final XnatImagescandataI scan) {
        final Map<String, String> map = new HashMap<>();
        map.put("SeriesDescription", scan.getSeriesDescription());
        map.put("Modality", scan.getModality());
        map.put("Manufacturer", scan.getScanner_manufacturer());
        map.put("ManufacturerModelName", scan.getScanner_model());
        map.put("StationName", scan.getScanner());
        map.put("AcquisitionStartCondition", scan.getCondition());
        map.put("EncapsulatedDocument", scan.getDocumentation());
        map.put("TextComments", scan.getNote());
        map.put("OperatorsName", scan.getOperator());
        map.put("QualityControlImage", scan.getQuality());
        map.put("DataType", scan.getType());
        map.put("StartAcquisitionDateTime", safeToString(scan.getStarttime()));
        map.put("SeriesNumber", safeToString(scan.getXnatImagescandataId()));
        return map;
    }

    private String safeToString(final Object object) {
        return object == null ? "" : object.toString();
    }

    /**
     * This code is used by this class and PrearcSessionValidator to confirm that there are no un-referenced or unexpected files in the prearchive content
     *
     * @throws ClientException When an error occurs on the client side.
     */
    protected void validateDicomFiles() throws ClientException {
        //validate files to confirm DICOM contents
        for (final XnatImagescandataI scan : src.getScans_scan()) {
            for (final XnatAbstractresourceI resource : scan.getFile()) {
                if (resource instanceof XnatResourcecatalogI) {
                    final File catalogFile = CatalogUtils.getCatalogFile(project, src.getPrearchivepath(), (XnatResourcecatalogI) resource);
                    if (catalogFile == null || !catalogFile.exists()) {
                        warn(21, "Expected a catalog file, however it was missing.");
                    }

                    if (catalogFile != null) {
                        final List<String> unreferenced = CatalogUtils.getUnreferencedFiles(catalogFile.getParentFile(), project);
                        if (unreferenced.size() > 0) {
                            warn(20, String.format("Scan %1$s has %2$s non-%3$s (or non-parsable %3$s) files", scan.getId(), unreferenced.size(), resource.getLabel()));
                        }
                    }

                    if (StringUtils.equals(resource.getLabel(), "DICOM") && catalogFile != null) {
                        //check for entries that aren't DICOM entries or don't have a UID stored
                        final CatCatalogI catalog = CatalogUtils.getCatalog(catalogFile, project);
                        if (catalog != null) {
                            final Collection<CatEntryI> nonDicom = CatalogUtils.getEntriesByFilter(catalog, new CatalogUtils.CatEntryFilterI() {
                                @Override
                                public boolean accept(CatEntryI entry) {
                                    return ((!(entry instanceof CatDcmentryI)) || StringUtils.isEmpty(((CatDcmentryI) entry).getUid()));
                                }
                            });

                            if (!nonDicom.isEmpty()) {
                                warn(20, String.format("Scan %1$s has %2$s non-DICOM (or non-parsable DICOM) files", scan.getId(), nonDicom.size()));
                            }
                        } else {
                            log.warn("No catalog found for the file {}", catalogFile);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to compare new scans to existing scans.  Matching scan IDs (with different UIDs) will have a _1 added to them.
     *
     * @param existing An existing session to correct.
     *
     * @throws ServerException When an error occurs on the server.
     */
    private void correctScanID(final XnatImagesessiondata existing) throws ServerException {
        for (final XnatImagescandataI newScan : src.getScans_scan()) {
            //find matching scan by UID
            final XnatImagescandataI match2 = MergeUtils.getMatchingScanByUID(newScan, existing.getScans_scan());//match by UID
            if (match2 != null) {
                if (!StringUtils.equals(match2.getId(), newScan.getId())) {
                    //this UID has been mapped to a different scan ID
                    //update the prearc session to match
                    processing("Renaming scan " + newScan.getId() + " to " + match2.getId() + " due to UID match.");
                    moveScan(newScan, match2.getId());
                }
                //scan with matching UID is done (whether their ID's matched or not)
                continue;
            }

            //build modality code via parsing of the xsi:type.  modality code matches first 2 characters after the : for xnat types.  Otherwise, leave it empty.
            //this is a bit of a hack.  It would be better to have an official mapping
            String modalityCode = (newScan.getXSIType().startsWith("xnat:")) ? newScan.getXSIType().substring(5, 7).toUpperCase() : "";
            if ("PE".equals(modalityCode)) {
                modalityCode = "PT";//this works for everything but PET, which gets called PE instead of PT, so we correct it.
            }

            String scan_id = newScan.getId();
            int count = 1;
            boolean needsMove = false;
            //make sure there aren't any matches by ID.  if there aren't needsMove stays false.  And, it identifies a good scan_id to use in the process.
            while (MergeUtils.getMatchingScanById(scan_id, existing.getScans_scan()) != null) {
                scan_id = newScan.getId() + "-" + modalityCode + count++;
                needsMove = true;
            }

            if (needsMove) {
                //the scan id conflicted with a pre-existing one, so we have to rename this one.
                processing("Renaming scan " + newScan.getId() + " to " + scan_id + " due to ID conflict.");
                moveScan(newScan, scan_id);
            }
        }
    }

    /**
     * Used to move a scan to a different scan ID within the prearchive, prior to transfer
     *
     * @param newScan The new scan to move to.
     * @param scan_id The new scan ID.
     *
     * @throws ServerException When an error occurs on the server.
     */
    private void moveScan(XnatImagescandataI newScan, String scan_id) throws ServerException {
        /*
         * SCANS\1\scan_1_catalog.xml
         */
        final String oldScanCatalogPath = ((XnatResourcecatalog) newScan.getFile().get(0)).getUri();
        final File catalog = new File(src.getPrearchivepath(), oldScanCatalogPath);
        final String oldScanFolderPath = "SCANS/" + newScan.getId();
        final String newScanFolderPath = "SCANS/" + scan_id;
        final String newScanCatalogPath = newScanFolderPath + "/DICOM/" + catalog.getName();
        final String prearcPath = getSrcDIR().getAbsolutePath();

        //confirm expected structure
        if (!catalog.exists()) {
            throw new ServerException("Non-standard prearchive structure- failed scan rename.");
        }


        if (oldScanCatalogPath.startsWith(oldScanFolderPath)) {
            File oldFolder = new File(src.getPrearchivepath(), oldScanFolderPath);

            //confirm expected structure
            if (!oldFolder.exists()) {
                throw new ServerException("Non-standard prearchive structure- failed scan rename.");
            }

            //move the directory
            try {
                FileUtils.MoveDir(oldFolder, new File(src.getPrearchivepath(), newScanFolderPath), false);
            } catch (IOException e) {
                throw new ServerException(e);
            }

            //fix the file path
            XnatResourcecatalog cat = (XnatResourcecatalog) newScan.getFile().get(0);
            cat.setUri(newScanCatalogPath);

            //fix the scan ID
            newScan.setId(scan_id);
        } else {
            throw new ServerException("Non-standard prearchive structure- failed scan rename.");
        }

        try {
            updatePrearchiveSessionXML(prearcPath, src);
        } catch (Throwable e) {
            throw new ServerException(e);
        }
    }

    public interface PostArchiveAction {
        Boolean execute(UserI user, XnatImagesessiondata src, Map<String, Object> params);
    }
    
	public interface PreArchiveAction {
		public Boolean execute(UserI user, XnatImagesessiondata src, Map<String,Object> params, XnatImagesessiondata existing) throws ServerException,ClientException;
	}

    private void postArchive(UserI user, XnatImagesessiondata src, Map<String, Object> params) {
        try {
            List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.postArchive");

            if (classes != null && classes.size() > 0) {
                for (Class<?> clazz : classes) {
                    if (PostArchiveAction.class.isAssignableFrom(clazz)) {
                        PostArchiveAction action = (PostArchiveAction) clazz.newInstance();
                        Boolean result = action.execute(user, src, params);
                        log.debug("Ran post-archive action class: {}. Result was {}", clazz.getSimpleName(), result == null ? "false" : result.toString());
                    } else {
                        log.info("Found class in postArchive action package that's not a valid post-archive action class: {}", clazz.getSimpleName());
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
    
    private void preArchive(UserI user, XnatImagesessiondata src, Map<String,Object> params, XnatImagesessiondata existing){
		List<Class<?>> classes;
	     try {
	    	 classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.preArchive");

	    	 if(classes!=null && classes.size()>0){
				 for(Class<?> clazz: classes){
					 PreArchiveAction action=(PreArchiveAction)clazz.newInstance();
					 action.execute(user,src,params,existing);
				 }
			 }
	     } catch (Exception exception) {
	         throw new RuntimeException(exception);
	     }
	}

    public void validateSession() throws ServerException {
        if (StringUtils.isBlank(src.getId())) {
            try {
                src.setId(XnatExperimentdata.CreateNewID());
            } catch (Exception e) {
                throw new ServerException("unable to create new session ID", e);
            }
        }

        try {
            final ValidationResults validation = src.validate();
            if (null != validation && !validation.isValid()) {
                throw new ValidationException(validation);
            }
        } catch (Exception e) {
            failed("unable to perform session validation: " + e.getMessage());
            throw new ServerException(e.getMessage(), e);
        }
    }

    public static String buildURI(final String project, final XnatImagesessiondata session) {
        final StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("/archive/projects/").append(project);
        uriBuilder.append("/subjects/");
        final XnatSubjectdata subjectData = session.getSubjectData();
        if (XNATUtils.hasValue(subjectData.getLabel())) {
            uriBuilder.append(subjectData.getLabel());
        } else {
            uriBuilder.append(subjectData.getId());
        }
        uriBuilder.append("/experiments/").append(session.getLabel());
        return uriBuilder.toString();
    }

    private synchronized void getOrCreateSubject(final String subjectLabel, final boolean allowNewSubject, final EventMetaI c) throws ServerException {
        log.debug("Trying to get or create the subject {} in project {}", subjectLabel, project);
        final XnatSubjectdata existing = retrieveMatchingSubject(subjectLabel, project, user);
        if (existing != null) {
            src.setSubjectId(existing.getId());
            processing("matches existing subject " + subjectLabel);
            return;
        }

        if (!allowNewSubject) {
            log.debug("Subject {} does not exist in project {}, but not allowed to create new subjects, sorry", subjectLabel, project);
            return;
        }

        log.debug("Subject {} does not exist in project {}, creating", subjectLabel, project);
        processing("Creating new subject in project " + project + " with label " + subjectLabel);
        final XnatSubjectdata subject = new XnatSubjectdata(user);
        subject.setProject(project);
        subject.setLabel(subjectLabel);

        final String subjectId;
        try {
            subjectId = XnatSubjectdata.CreateNewID();
        } catch (Exception e) {
            failed("Unable to create new subject ID");
            throw new ServerException("Unable to create new subject ID", e);
        }

        subject.setId(subjectId);
        log.debug("Subject {} in project {} now has ID {}", subjectLabel, project, subjectId);

        try {
            SaveItemHelper.authorizedSave(subject, user, false, false, c);
            XDAT.triggerXftItemEvent(subject, CREATE);
            log.info("Successfully create subject {} with ID {} in project {}", subjectLabel, subjectId, project);
        } catch (Exception e) {
            failed("unable to save new subject " + subjectId);
            throw new ServerException("Unable to save new subject " + subjectId, e);
        }

        processing("Created new subject " + subjectLabel + " in project " + project + " with ID " + subjectId);
        src.setSubjectId(subjectId);
    }

    /************************************
     * We used to use the workflow table to implement locking of the destination session when merging.
     * We did so by saving the workflow entry towards the beginning of the archive process (the call method), and checking to see
     * if there were any others open.
     * However, Dan (and Jenny) requested that we only save the workflow entry if the job completes. So, that approach won't work anymore.
     * <p/>
     * We should prevent multiple processes from archiving the same prearchived session at the same time.
     * And, we should prevent multiple sessions from being merged into a single archived session at the same time.
     * <p/>
     * So, for now, I'll add a static List to track locked prearchived sessions and archived sessions.
     */
    //tracks all of the strings locked by any archiver
    private static List<String> GLOBAL_LOCKS = Lists.newArrayList();

    private static synchronized void requestLock(String id) throws LockedItemException {
        if (GLOBAL_LOCKS.contains(id)) {
            throw new LockedItemException();
        }

        //free to go
        GLOBAL_LOCKS.add(id);
    }

    private static synchronized void releaseLock(String id) {
        GLOBAL_LOCKS.remove(id);
    }

    //used to track the strings that have been locked for this particular archiver instance
    private List<String> local_locks = Lists.newArrayList();

    private void lock(String s) throws LockedItemException {
        PrearcSessionArchiver.requestLock(s);
        local_locks.add(s);
    }

    private void unlock() {
        for (String lock : local_locks) {
            PrearcSessionArchiver.releaseLock(lock);
        }
    }

    private static class LockedItemException extends Exception {
        private static final long serialVersionUID = 1L;

    }

    //the following methods are overridden in PrearcSessionValidator
    //PrearcSessionValidator tries to validate whether the PrearcSessionArchiver would work (and if not what would break it)
    //ideally, the PrearcSessionValidator would use the exact same code as the Archiver.  But, the Archiver code is sometimes incompatible
    //with the validator (validator wants to collect all failure reasons, archiver fails on first one).
    //however, were possible, they should use the same code.  In those situations the Archiver should use these methods to trigger its exceptions.
    //then Validator can just change the way those exceptions are handled, by changing the implementation of these methods.
    //ideally all of the Validator would work this way, but that requires large scale refactoring of PrearcSessionArchiver (which predated the Validator by several years).

    protected void fail(int code, String msg) throws ClientException {
        failed(msg);
        throw new ClientException(msg);
    }

    protected void warn(int code, String msg) throws ClientException {
        failed(msg);
        throw new ClientException(msg);
    }

    protected void conflict(int code, String msg) throws ClientException {
        failed(msg);
        throw new ClientException(Status.CLIENT_ERROR_CONFLICT, msg, new Exception());
    }
    

    private DicomFilterService getDicomFilterService() {
        if (_filterService == null) {
            synchronized (log) {
                _filterService = XDAT.getContextService().getBean(DicomFilterService.class);
            }
        }
        return _filterService;
    }

}
