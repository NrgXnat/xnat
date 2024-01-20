/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveSeparatePetMrHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;
import static org.nrg.xnat.archive.Operation.Archive;
import static org.nrg.xnat.archive.Operation.Separate;
import static org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler.PARAM_AUTO_ARCHIVE_BLOCKED;

@Handles(Separate)
@Slf4j
public class PrearchiveSeparatePetMrHandler extends AbstractPrearchiveOperationHandler {
    public PrearchiveSeparatePetMrHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider, final DicomInboxImportRequestService importRequestService) {
        super(request, eventService, userProvider, importRequestService);
        autoArchiveBlocked = getBooleanParameter(PARAM_AUTO_ARCHIVE_BLOCKED);
    }

    @Override
    public void execute() throws Exception {
        List<PrearchiveOperationRequest> requests = separate();
        if (requests != null) {
            for (PrearchiveOperationRequest request : requests) {
                PrearcUtils.queuePrearchiveOperation(request);
                // XNAT-6106: HACKITY HACKITY HACK HACK HACK!! Sleep for 1 second between queuing each session.
                // This prevents the same race condition as creating the subject above, except this time with
                // the experiment ID.
                Thread.sleep(1000);
            }
        }
    }

    public List<PrearchiveOperationRequest> separate() throws  Exception {
        final String  project    = getSessionData().getProject();
        final String  timestamp  = getSessionData().getTimestamp();
        final String  folderName = getSessionData().getFolderName();
        final String  subject    = getSessionData().getSubject();
        final List<PrearchiveOperationRequest> requestList = new ArrayList<>();

        if (!getSessionDir().getParentFile().exists()) {
            final String name = getSessionData().getName();
            try {
                log.info("The parent of the indicated session {} could not be found at the indicated location {}", name, getSessionDir().getParentFile().getAbsolutePath());
                PrearcDatabase.unsafeSetStatus(folderName, timestamp, project, PrearcUtils.PrearcStatus._DELETING);
                PrearcDatabase.deleteCacheRow(folderName, timestamp, project);
            } catch (Exception e) {
                log.error("An error occurred attempting to clear the prearchive entry for the session {}, which doesn't exist at the indicated location {}", name, getSessionDir().getParentFile().getAbsolutePath(), e);
            }
        } else if (PrearcDatabase.setStatus(folderName, timestamp, project, PrearcUtils.PrearcStatus.SEPARATING)) {
            final File sessionXml = new File(getSessionDir() + ".xml");
            if (sessionXml.exists()) {
                log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());
                final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
                if (bean instanceof XnatPetmrsessiondataBean) {
                    // XNAT-6106: HACKITY HACKITY HACK HACK HACK!! If the subject for these sessions doesn't
                    // already exist, create it now to prevent race condition where the two separate operations
                    // both try to create the subject with the "next" available subject ID (only it's not "next"
                    // in the case of the one that tries this milliseconds after the first, so there's then a
                    // conflict and stuff explodes.
                    if (shouldCreateSubject(project, subject)) {
                        createSubject(project, subject);
                    }

                    log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
                    final Map<String, SessionData> sessions = PrearcDatabase.separatePetMrSession(folderName, timestamp, project, (XnatPetmrsessiondataBean) bean);
                    if (sessions == null) {
                        log.warn("No sessions returned from separate PET/MR session operation, check your logs for errors.");
                        return requestList;
                    }
                    // Now finish the upload process, including checking for auto-archive.
                    for (final String modality : sessions.keySet()) {
                        final SessionData   sessionData = sessions.get(modality);
                        final PrearcSession session     = new PrearcSession(sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), null, getUser());
                        if (!autoArchiveBlocked && session.isAutoArchive()) {
                            final Map<String, Object> parameters = new HashMap<>();
                            parameters.put(HandlePetMr.SEPARATE_PET_MR, HandlePetMr.Separate.value());
                            final PrearchiveOperationRequest request = new PrearchiveOperationRequest(getUser(), Archive, session.getSessionData(), session.getSessionDir(), parameters);
                            requestList.add(request);
                        }
                    }
                } else {
                    log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
                }
            } else {
                log.warn("Tried to separate a PET/MR session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            }
        }
        return requestList;
    }

    private boolean shouldCreateSubject(final String project, final String idOrLabel) {
        if (StringUtils.isAnyBlank(project, idOrLabel) || StringUtils.equalsIgnoreCase(project, UNASSIGNED)) {
            log.info("Got blank or unassigned project (\"{}\") or blank subject ID/label (\"{}\"). Can't create subject from this so you shouldn't even try.", project, idOrLabel);
            return false;
        }
        final boolean exists = XDAT.getNamedParameterJdbcTemplate().queryForObject(QUERY_SUBJECT_EXISTS_IN_PROJECT, new MapSqlParameterSource("project", project).addValue("idOrLabel", idOrLabel), Boolean.class);
        log.info("Looked in project \"{}\" for subject ID or label \"{}\". Subject {} so I should {} to create subject.", project, idOrLabel, exists ? "exists" : "does not exist", exists ? "should not" : "should");
        return !exists;
    }

    private void createSubject(final String projectId, final String subjectLabel) throws Exception {
        log.info("Creating new subject {} in project {}", subjectLabel, projectId);
        final String subjectId;
        try {
            subjectId = XnatSubjectdata.CreateNewID();
        } catch (Exception e) {
            failed("unable to create new subject ID");
            throw new ServerException("Unable to create new subject ID", e);
        }

        log.debug("Creating subject in project {} with ID {} and label {}", projectId, subjectId, subjectLabel);
        final XnatSubjectdata subject = new XnatSubjectdata(getUser());
        subject.setId(subjectId);
        subject.setProject(projectId);
        subject.setLabel(subjectLabel);

        final PersistentWorkflowI workflow;
        final EventMetaI          eventMeta;

        try {
            workflow = PersistentWorkflowUtils.buildOpenWorkflow(getUser(), XnatSubjectdata.SCHEMA_ELEMENT_NAME, subjectLabel, projectId, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, EventUtils.TRANSFER, "Created to support archiving separated PET/MR session", "Creating new subject " + subjectLabel));
            assert workflow != null;
            workflow.setStepDescription("Creating");
            eventMeta = workflow.buildEvent();
        } catch (PersistentWorkflowUtils.JustificationAbsent e2) {
            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, e2);
        } catch (PersistentWorkflowUtils.EventRequirementAbsent e2) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e2);
        }

        try {
            SaveItemHelper.authorizedSave(subject, getUser(), false, false, eventMeta);
            XDAT.triggerXftItemEvent(subject, CREATE);
            workflow.setStepDescription(PersistentWorkflowUtils.COMPLETE);
            WorkflowUtils.complete(workflow, eventMeta);
            log.info("Successfully create subject {} with ID {} in project {}", subjectLabel, subject.getId(), projectId);
        } catch (Exception e) {
            failed("unable to save new subject " + subject.getId());
            workflow.setStepDescription(PersistentWorkflowUtils.FAILED);
            WorkflowUtils.fail(workflow, eventMeta);
            throw new ServerException("Unable to save new subject " + subject.getId(), e);
        }
    }

    private static final String QUERY_SUBJECT_EXISTS_IN_PROJECT = "SELECT EXISTS(SELECT TRUE FROM xnat_subjectdata s LEFT JOIN xnat_projectparticipant p ON s.id = p.subject_id WHERE s.project = :project AND :idOrLabel IN (s.id, s.label) OR p.project = :project AND :idOrLabel IN (p.subject_id, p.label)) AS exists";
    private final boolean autoArchiveBlocked;
}
