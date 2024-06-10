/*
 * web: org.nrg.xnat.archive.DicomInboxImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xdat.om.XnatProjectdata;
import org.restlet.data.Status;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xnat.restlet.actions.SessionImporter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

@Slf4j
@ImporterHandler(handler = ImporterHandlerA.DICOM_INBOX_IMPORTER, allowCallsWithoutFiles = true, callPartialUriWrap = false)
public final class DicomInboxImporter extends ImporterHandlerA {
	
    private static final String[] ARCHIVE_EXTENSIONS = { ".zip", ".tgz", ".tar", ".tar.gz." }; 
    private static final String CLEANUP_PARAMETER = "cleanupAfterImport";
    //private static final org.apache.commons.logging.Log _logger = org.apache.commons.logging.LogFactory.getLog(DicomInboxImporter.class);

    public DicomInboxImporter(final Object listener, final UserI user, @SuppressWarnings("unused") final FileWriterWrapperI writer, final Map<String, Object> parameters) {
        super(listener, user);
        _service = XDAT.getContextService().getBean(DicomInboxImportRequestService.class);
        _user = user;
        _parameters = parameters;
        _sessionPath = null;
    }
    

    private File handleInboxArchiveFile(final File pFile) throws ClientException {
    	final String pFn = pFile.getName();
    	final String lFn = pFn.toLowerCase();
    	if (!hasArchiveExtension(lFn)) {
	    	throw new ClientException("User inbox file " + pFn + " does not have an archive extension - " + Arrays.toString(ARCHIVE_EXTENSIONS) +
	    			".  It will be skipped. The path must be to either a directory or an archive file.");
    	}
    	final File parent = pFile.getParentFile();
    	// Create a subdirectory for the file and move it there.
    	String              dirName = pFn.split(Pattern.quote("."))[0];
    	final AtomicBoolean exists  = new AtomicBoolean(true);
    	while (exists.get()) {
    		final File subDir = new File(parent, dirName);
    		if (!subDir.exists()) {
    			exists.set(false);
    			if (!subDir.mkdir()) {
    				throw new ClientException("Couldn't create subdirectory for archive file - " + subDir.getAbsolutePath());
    			}
    			try {
					ZipI zipper = null;
					if (lFn.endsWith(".zip")) {
						zipper = new ZipUtils();
					} else if (lFn.endsWith("gz")) {
						zipper = new TarUtils();
						zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
					} else if (lFn.endsWith("tar")) {
						zipper = new TarUtils();
					} 
					if (zipper != null) {
						zipper.extract(pFile, subDir.getCanonicalPath(), false);
						// The deleteZip option above only works on JVM exit.  Let's clean it up here.
						// TODO:  Is there a good Executor service to use here?
						if (_cleanupAfterImport) {
							FileUtils.deleteQuietly(pFile);
						}
						return subDir;
					} else {
						throw new ClientException("Couldn't obtain zipper for archive file - " + pFn);
					}
				} catch (IOException e) {
					throw new ClientException("Couldn't unzip archive file - " + pFn);
				}
    		}
    		dirName = dirName + "_1";
    	}
    	return null;
	}
    
	private boolean hasArchiveExtension(final String lowCaseFileName) {
    	return StringUtils.endsWithAny(lowCaseFileName, ARCHIVE_EXTENSIONS);
	}    
    
    /**
     * Processes the folder specified by the session or path parameter, importing all of the files located in the folder
     * and its subfolders.
     *
     * @return A list of all of the files that were imported into XNAT.
     */
    @Override
    public List<String> call() throws ClientException {

		final boolean hasPathParameter    = _parameters.containsKey("path");

		if (_parameters.containsKey(CLEANUP_PARAMETER)) {
			_cleanupAfterImport = Boolean.valueOf(_parameters.get(CLEANUP_PARAMETER).toString());
		}

		if (!hasPathParameter) {
			//You must specify the path parameter specifying a full path to the session data to be imported.
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
					"You must specify a valid path to a file or directory. It must be under the main inbox directory within a subdirectory whose name matches the ID of a project you have edit access to.");
		}

		String inboxPath = XDAT.getSiteConfigPreferences().getInboxPath();
		final String parameter = String.valueOf(_parameters.get("path"));
		String normalizedPath = parameter==null? "": parameter;
		normalizedPath = Paths.get(normalizedPath).normalize().toString();
		if(!normalizedPath.startsWith(inboxPath)){
			//Specified directory is not within inbox directory.
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a valid path to a file or directory. It must be under the main inbox directory within a subdirectory whose name matches the ID of a project you have edit access to.");
		}
		String pathFromRequest = normalizedPath;
		String projectFromRequest = _parameters.get("PROJECT_ID")==null?"":_parameters.get("PROJECT_ID").toString();
		pathFromRequest = pathFromRequest.replaceFirst("^"+inboxPath, "");
		pathFromRequest = pathFromRequest.replaceFirst("^\\\\", "");
		pathFromRequest = pathFromRequest.replaceFirst("^/", "");
		int backslashIndex = pathFromRequest.indexOf('\\');
		int forwardSlashIndex = pathFromRequest.indexOf('/');
		int slashIndex = ((backslashIndex<forwardSlashIndex&&backslashIndex!=-1)?backslashIndex:forwardSlashIndex);
		String inboxSubdirectory = pathFromRequest;
		if(slashIndex>-1) {
			inboxSubdirectory = pathFromRequest.substring(0, slashIndex);
		}

		try {
			_sessionPath = (Paths.get(normalizedPath)).toFile();
		}
		catch(Exception ignored){

		}
		if (!_sessionPath.exists()) {
			//No session folder or archive file was found at the specified path
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a valid path to a file or directory. It must be under the main inbox directory within a subdirectory whose name matches the ID of a project you have edit access to.");
		}

		XnatProjectdata inboxProject = XnatProjectdata.getXnatProjectdatasById(inboxSubdirectory, _user, false);
		XnatProjectdata destinationProject = XnatProjectdata.getXnatProjectdatasById(projectFromRequest, _user, false);

		//Make sure _user has access to both the project whose subdirectory the data is in right now, as well as the destination project.
		if(inboxProject == null || !Permissions.canEditProject(_user, inboxSubdirectory)){
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a valid path to a file or directory. It must be under the main inbox directory within a subdirectory whose name matches the ID of a project you have edit access to.");
		}
		if(destinationProject == null || !Permissions.canEditProject(_user, projectFromRequest)){
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You do not have permission to edit the specified destination project.");
		}

		if (_sessionPath.isFile()) {
			_sessionPath = handleInboxArchiveFile(_sessionPath);
		}
		else if (_sessionPath.isDirectory()){
			String[] filesInIt = _sessionPath.list();
			if(filesInIt==null || filesInIt.length==0){
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Specified directory does not contain any files.");
			}
		}
		if (_sessionPath == null) {
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to process supplied archive file");
		}
		// If the call specifies an experiment label...
		if (_parameters.containsKey(URIManager.EXPT_LABEL)) {
			// Remove the session parameter so that it doesn't cause the final imported session to be renamed.
			_parameters.remove("session");

			String expt_label = (String)_parameters.get(URIManager.EXPT_LABEL);
			if(SessionImporter.getExperimentByIdOrLabel(projectFromRequest, expt_label, _user)!=null){
				throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Experiment with that label already exists in that project.");
			}
		}
		else if(_parameters.containsKey("session")){
			String sess = (String)_parameters.get("session");
			if(SessionImporter.getExperimentByIdOrLabel(projectFromRequest, sess, _user)!=null){
				throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already exists in that project.");
			}
			_parameters.put(URIManager.EXPT_LABEL, sess);
		}

		final DicomInboxImportRequest request = new DicomInboxImportRequest();
		request.setUsername(_user.getUsername());
		request.setSessionPath(_sessionPath.getAbsolutePath());
		request.setCleanupAfterImport(_cleanupAfterImport);
        request.setParametersFromObjectMap(_parameters);
        XDAT.sendJmsRequest(_service.create(request));

        log.info("Created and queued import request {} for the inbox session located at {}.", request.getId(), _sessionPath.getAbsolutePath());
        try {
            return Collections.singletonList(new URL(XDAT.getSiteConfigurationProperty("siteUrl") + "/xapi/dicom/" + request.getId()).getPath());
        } catch (MalformedURLException | ConfigServiceException e) {
            log.error("An error occurred trying to retrieve the site URL when composing DICOM inbox import request response, panicking.", e);
            return Collections.singletonList("/xapi/dicom/" + request.getId());
        }
    }

    private final DicomInboxImportRequestService _service;
    private final UserI                          _user;
    private final Map<String, Object>            _parameters;
    private boolean						         _cleanupAfterImport = true;
    private File                                 _sessionPath;
}
