/*
 * web: org.nrg.xnat.restlet.actions.SessionImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.FinishImageUpload;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.merge.SiteWideAnonymizer;
import org.nrg.xnat.helpers.prearchive.*;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.status.ListenerUtils;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.restlet.data.Status;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.nrg.xnat.archive.Operation.Delete;

@ImporterHandler(handler = ImporterHandlerA.SESSION_IMPORTER)
@Slf4j
public class SessionImporter extends ImporterHandlerA implements Callable<List<String>> {
    private final Boolean overrideExceptions;

    private final Boolean allowSessionMerge;

    private final FileWriterWrapperI fw;

    private final Object listenerControl;

    private final UserI user;

    final Map<String, Object> params;

    /**
     * Creates a new session importer instance.
     *
     * @param listenerControl The listener for the import operation.
     * @param user            The user doing the import.
     * @param fileWriter      A file writer.
     * @param params          Import parameters.
     */
    public SessionImporter(final Object listenerControl, final UserI user, final FileWriterWrapperI fileWriter, final Map<String, Object> params) {
        super(listenerControl, user);
        this.listenerControl = getControlString();
        this.user = user;
        this.fw = fileWriter;
        this.params = params;

        String overwriteV = (String) params.remove("overwrite");

        if (overwriteV == null) {
            this.overrideExceptions = false;
            this.allowSessionMerge = false;
        } else {
            if (overwriteV.equalsIgnoreCase(PrearcUtils.APPEND)) {
                this.overrideExceptions = false;
                this.allowSessionMerge = true;
            } else if (overwriteV.equalsIgnoreCase(PrearcUtils.DELETE)) {//leaving this for backwards compatibility... deprecated by 'override' setting
                this.overrideExceptions = true;
                this.allowSessionMerge = true;
            } else if (overwriteV.equalsIgnoreCase("override")) {
                this.overrideExceptions = true;
                this.allowSessionMerge = true;
            } else {
                this.overrideExceptions = false;
                this.allowSessionMerge = true;
            }
        }
    }

    public static List<PrearcSession> importToPrearc(StatusProducer parent, String format, Object listener, UserI user, FileWriterWrapperI fw, Map<String, Object> params, boolean allowSessionMerge, boolean overwriteFiles) throws ActionException {
        //write file
        try {
            final PrearcImporterA     destination    = PrearcImporterA.buildImporter(format, listener, user, fw, params, allowSessionMerge, overwriteFiles);
            final PrearcImporterA     listeners      = ListenerUtils.addListeners(parent, destination);
            final List<PrearcSession> prearcSessions = listeners.call();
            for (final PrearcSession session : prearcSessions) {
                if (PrearcDatabase.getSessionIfExists(session.getFolderName(), session.getTimestamp(), session.getProject()) == null) {
                    final SessionData sessionData = new SessionData();
                    sessionData.setFolderName(session.getFolderName());
                    sessionData.setName(session.getFolderName());
                    sessionData.setProject(session.getProject());
                    sessionData.setTimestamp(session.getTimestamp());
                    sessionData.setStatus(PrearcUtils.PrearcStatus.BUILDING);
                    sessionData.setLastBuiltDate(Calendar.getInstance().getTime());
                    sessionData.setSubject(session.getAdditionalValues().get("subject_ID"));
                    sessionData.setUrl(session.getSessionDir().getAbsolutePath());
                    sessionData.setSource(SessionImporter.class.getSimpleName());
                    sessionData.setPreventAnon(false);
                    sessionData.setPreventAutoCommit(true);
                    PrearcDatabase.addSession(sessionData);
                }
            }
            return prearcSessions;
        } catch (SecurityException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServerException(e.getMessage(), e);
        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e);
        } catch (PrearcImporterA.UnknownPrearcImporterException e) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerException("An error occurred adding the session to the prearchive database", e);
        }
    }

    public static XnatImagesessiondata getExperimentByIdOrLabel(final String project, final String experimentId, final UserI user) {
        if (StringUtils.isBlank(project)) {
            return null;
        }

        final XnatImagesessiondata imageSession = (XnatImagesessiondata) XnatExperimentdata.GetExptByProjectIdentifier(project, experimentId, user, false);
        if (imageSession != null) {
            return imageSession;
        }

        return (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
    }

    @SuppressWarnings("serial")
    public List<String> call() throws ClientException, ServerException {
        try {
            String dest = (String) params.get(RequestUtil.DEST);

            XnatImagesessiondata expt = null;

            final URIManager.DataURIA destination = (!StringUtils.isEmpty(dest)) ? UriParserUtils.parseURI(dest) : null;

            String project = null;

            Map<String, Object> prearcParameters = new HashMap<>(params);

            //check for existing session by URI
            this.processing("Determining destination");
            if (destination != null) {
                if (destination instanceof URIManager.PrearchiveURI) {
                    prearcParameters.putAll(destination.getProps());
                    copyNonEmptyMapValue(prearcParameters, "TIMEZONE");
                    copyNonEmptyMapValue(prearcParameters, "SOURCE");
                } else {
                    project = PrearcImporterHelper.identifyProject(destination.getProps());
                    if (!StringUtils.isEmpty(project)) {
                        prearcParameters.put("project", project);
                    }

                    if (destination.getProps().containsKey(URIManager.SUBJECT_ID)) {
                        prearcParameters.put("subject_ID", destination.getProps().get(URIManager.SUBJECT_ID));
                    }

                    copyNonEmptyMapValue(prearcParameters, "TIMEZONE");
                    copyNonEmptyMapValue(prearcParameters, "SOURCE");

                    String expt_id = (String) destination.getProps().get(URIManager.EXPT_ID);
                    if (!StringUtils.isEmpty(expt_id)) {
                        expt = getExperimentByIdOrLabel(project, expt_id, user);
                    }

                    if (expt == null) {
                        if (!StringUtils.isEmpty(expt_id)) {
                            prearcParameters.put("label", expt_id);
                        }
                    }
                }
            }

            if (expt == null) {
                if (StringUtils.isEmpty(project)) {
                    project = PrearcImporterHelper.identifyProject(prearcParameters);
                }

                //check for existing experiment by params
                if (prearcParameters.containsKey(URIManager.SUBJECT_ID)) {
                    prearcParameters.put("xnat:subjectAssessorData/subject_ID", prearcParameters.get(URIManager.SUBJECT_ID));
                }

                String expt_id    = (String) prearcParameters.get(URIManager.EXPT_ID);
                String expt_label = (String) prearcParameters.get(URIManager.EXPT_LABEL);
                if (!StringUtils.isEmpty(expt_id)) {
                    expt = getExperimentByIdOrLabel(project, expt_id, user);
                }

                if (expt == null && !StringUtils.isEmpty(expt_label)) {
                    expt = getExperimentByIdOrLabel(project, expt_label, user);
                }

                if (expt == null) {
                    if (!StringUtils.isEmpty(expt_label)) {
                        prearcParameters.put("xnat:experimentData/label", expt_label);
                    } else if (!StringUtils.isEmpty(expt_id)) {
                        prearcParameters.put("xnat:experimentData/label", expt_id);
                    }
                }
            }

            //set properties to match existing session
            if (expt != null) {
                prearcParameters.put("xnat:experimentData/project", expt.getProject());
                if (!prearcParameters.containsKey("xnat:subjectAssessorData/subject_ID")) {
                    prearcParameters.put("xnat:subjectAssessorData/subject_ID", expt.getSubjectId());
                }
                prearcParameters.put("xnat:experimentData/label", expt.getLabel());
            }

            //import to prearchive, code allows for merging new files into a pre-existing session directory
            this.processing("Beginning import");
            final List<PrearcSession> sessions = importToPrearc(this, (String) params.remove(PrearcImporterA.PREARC_IMPORTER_ATTR), listenerControl, user, fw, prearcParameters, allowSessionMerge, overrideExceptions);

            if (sessions.size() == 0) {
                throw new ClientException("Upload did not include parsable files for session generation.");
            }

            //if prearc=destination, then return
            if (destination instanceof URIManager.PrearchiveURI) {
                this.completed("Successfully uploaded " + sessions.size() + " sessions to the prearchive.", false);
                resetStatus(sessions);
                List<String> urls = returnURLs(sessions);
                this.completed("Prearchive:" + Joiner.on(";").join(urls), true);
                return urls;
            }

            //if unknown destination, only one session supported
            if (sessions.size() > 1) {
                resetStatus(sessions);
                throw new ClientException("Upload included files for multiple imaging sessions.");
            }

            final PrearcSession session = sessions.get(0);
            session.getAdditionalValues().putAll(params);

            try {
                this.processing("Populating session");
                final FinishImageUpload    finisher           = ListenerUtils.addListeners(this, new FinishImageUpload(listenerControl, user, session, destination, overrideExceptions, allowSessionMerge, true));
                final XnatImagesessiondata imageSession       = new XNATSessionPopulater(user, session.getSessionDir(), session.getProject(), false).populate();
                final SessionData sessionData                 = session.getSessionData();
                final File        sessionDir                  = session.getSessionDir();

                this.processing("Performing anonymization");
                final SiteWideAnonymizer   siteWideAnonymizer = new SiteWideAnonymizer(imageSession, true);
                if (siteWideAnonymizer.call()) {
                    // rebuild XML
                    PrearcUtils.buildSession(sessionData, sessionDir, imageSession.getLabel(), sessionData.getTimestamp(),
                            imageSession.getProject(), imageSession.getSubjectId(), imageSession.getVisit(),
                            imageSession.getProtocol(), (String) session.getAdditionalValues().get("TIMEZONE"),
                            (String) session.getAdditionalValues().get("SOURCE"));
                }
                if (finisher.isAutoArchive()) {
                    this.processing("Archiving");
                    final List<String> urls = Collections.singletonList(finisher.call());
                    if (PrearcDatabase.setStatus(session.getFolderName(), session.getTimestamp(), session.getProject(), PrearcUtils.PrearcStatus.QUEUED_DELETING)) {
                        XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Delete, sessionData, sessionDir));
                    }
                    this.completed("Archive:" + Joiner.on(";").join(urls), true);
                    return urls;
                } else {
                    completed("Successfully uploaded " + sessions.size() + " sessions to the prearchive.", false);
                    resetStatus(sessions);
                    List<String> urls = returnURLs(sessions);
                    this.completed("Prearchive:" + Joiner.on(";").join(urls), true);
                    return urls;
                }
            } catch (Exception e) {
                resetStatus(sessions);
                if (e instanceof ClientException && Status.CLIENT_ERROR_CONFLICT.equals(((ClientException) e).getStatus())) {
                    //if this failed due to a conflict
                    PrearcDatabase.setStatus(session.getSessionDir().getName(), session.getTimestamp(), session.getProject(), PrearcUtils.PrearcStatus.CONFLICT);
                } else {
                    PrearcDatabase.setStatus(session.getSessionDir().getName(), session.getTimestamp(), session.getProject(), PrearcUtils.PrearcStatus.ERROR);
                }
                throw e;
            }

        } catch (ClientException | ServerException e) {
            this.failed(e.getMessage(), true);
            throw e;
        } catch (IOException e) {
            log.error("", e);
            this.failed(e.getMessage(), true);
            throw new ServerException(e.getMessage(), e);
        } catch (SAXException e) {
            log.error("", e);
            this.failed(e.getMessage(), true);
            throw new ClientException(e.getMessage(), e);
        } catch (Throwable e) {
            log.error("", e);
            this.failed(e.getMessage(), true);
            throw new ServerException(e.getMessage(), new Exception());
        }
    }

    private void copyNonEmptyMapValue(final Map<String, Object> prearc_parameters, final String timezone2) {
        String timezone = (String) params.get(timezone2);
        if (!StringUtils.isEmpty(timezone)) {
            prearc_parameters.put(timezone2, timezone);
        }
    }

    public List<String> returnURLs(final List<PrearcSession> sessions) {
        final List<String> urls = new ArrayList<>();
        for (final PrearcSession ps : sessions) {
            urls.add(ps.getUrl());
        }
        return urls;
    }

    public void resetStatus(final List<PrearcSession> prearcSessions) throws ActionException {
        for (final PrearcSession prearcSession : prearcSessions) {
            try {
                final Map<String, Object> session = PrearcUtils.parseURI(prearcSession.getUrl());
                try {
                    PrearcUtils.addSession(user, (String) session.get(URIManager.PROJECT_ID), (String) session.get(PrearcUtils.PREARC_TIMESTAMP), (String) session.get(PrearcUtils.PREARC_SESSION_FOLDER), true);
                } catch (SessionException e) {
                    PrearcUtils.resetStatus(user, (String) session.get(URIManager.PROJECT_ID), (String) session.get(PrearcUtils.PREARC_TIMESTAMP), (String) session.get(PrearcUtils.PREARC_SESSION_FOLDER), true);
                }
            } catch (InvalidPermissionException e) {
                log.error("", e);
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, e);
            } catch (Exception e) {
                log.error("", e);
                throw new ServerException(e);
            }
        }
    }
}
