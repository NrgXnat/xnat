/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

@Slf4j
@ImporterHandler(handler = ImporterHandlerA.DICOM_INBOX_IMPORTER, allowCallsWithoutFiles = true)
public final class DicomInboxImporter extends ImporterHandlerA {
	
    private static final String[] ARCHIVE_EXTENSIONS = { ".zip", ".tgz", ".tar", ".tar.gz." }; 
    private static final String CLEANUP_PARAMETER = "cleanupAfterImport";
    //private static final org.apache.commons.logging.Log _logger = org.apache.commons.logging.LogFactory.getLog(DicomInboxImporter.class);

    public DicomInboxImporter(final Object listener, final UserI user, @SuppressWarnings("unused") final FileWriterWrapperI writer, final Map<String, Object> parameters) throws ClientException, ConfigServiceException {
        super(listener, user);

        final boolean hasPathParameter    = parameters.containsKey("path");
        
        if (parameters.containsKey(CLEANUP_PARAMETER)) {
        	_cleanupAfterImport = Boolean.valueOf(parameters.get(CLEANUP_PARAMETER).toString());
        }

        if (!hasPathParameter) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                                      "You must specify the path parameter specifying a full path to the session data to be imported.");
        }

        final String parameter = String.valueOf(parameters.get("path"));
        _sessionPath = (Paths.get(parameter)).toFile();
        if (!_sessionPath.exists()) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "No session folder or archive file was found at the specified path: " + parameter);
        }
        if (_sessionPath.isFile()) {
        	_sessionPath = handleInboxArchiveFile(_sessionPath);
        }
        if (_sessionPath == null) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to process supplied archive file");
        }
        // If the call specifies an experiment label...
        if (parameters.containsKey(URIManager.EXPT_LABEL)) {
            // Remove the session parameter so that it doesn't cause the final imported session to be renamed.
            parameters.remove("session");
        }

		String inboxPath = XDAT.getSiteConfigPreferences().getInboxPath();
		String pathFromRequest = parameters.get("path")==null?"":parameters.get("path").toString();
		String projectFromRequest = parameters.get("PROJECT_ID")==null?"":parameters.get("PROJECT_ID").toString();
		pathFromRequest = pathFromRequest.replaceFirst("^"+inboxPath, "");
		pathFromRequest = pathFromRequest.replaceFirst("^\\\\", "");
		pathFromRequest = pathFromRequest.replaceFirst("^/", "");
		int backslashIndex = pathFromRequest.indexOf('\\');
		int forwardslashIndex = pathFromRequest.indexOf('/');
		int slashIndex = ((backslashIndex<forwardslashIndex&&backslashIndex!=-1)?backslashIndex:forwardslashIndex);
		String inboxSubdirectory = pathFromRequest;
		if(slashIndex>-1) {
			inboxSubdirectory = pathFromRequest.substring(0, slashIndex);
		}

		XnatProjectdata inboxProject = XnatProjectdata.getXnatProjectdatasById(inboxSubdirectory, user, false);
		XnatProjectdata destinationProject = XnatProjectdata.getXnatProjectdatasById(projectFromRequest, user, false);

		//Make sure user has access to both the project whose subdirectory the data is in right now, as well as the destination project.
		if(destinationProject == null || !Permissions.canEditProject(user, projectFromRequest)){
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You do not have permission to edit the specified destination project.");
		}
		if(inboxProject == null || !Permissions.canEditProject(user, inboxSubdirectory)){
			throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "No session folder or archive file was found at the specified path: " + parameter);
		}

        _service = XDAT.getContextService().getBean(DicomInboxImportRequestService.class);
        _user = user;
        _parameters = parameters;
    }
    

    private File handleInboxArchiveFile(final File pFile) throws ClientException {
    	final String pFn = pFile.getName();
    	final String lFn = pFn.toLowerCase();
    	if (!hasArchiveExtension(lFn)) {
	    	throw new ClientException("User inbox file " + pFn + " does not have an archive extension - " + ARCHIVE_EXTENSIONS +
	    			".  It will be skipped.");
    	}
    	final File parent = pFile.getParentFile();
    	// Create a subdirectory for the file and move it there.
    	String dirName = pFn.split(Pattern.quote("."))[0];
    	boolean exists = true;
    	while (exists) {
    		final File subDir = new File(parent, dirName);
    		if (!subDir.exists()) {
    			exists = false;
    			if (!subDir.mkdir()) {
    				throw new ClientException("Couldn't create subdiretory for archive file - " + subDir.getAbsolutePath());
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
		for (final String ext : Arrays.asList(ARCHIVE_EXTENSIONS)) {
			if (lowCaseFileName.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}    
    
    /**
     * Processes the folder specified by the session or path parameter, importing all of the files located in the folder
     * and its subfolders.
     *
     * @return A list of all of the files that were imported into XNAT.
     */
    @Override
    public List<String> call() {
        final DicomInboxImportRequest request = DicomInboxImportRequest.builder()
                                                                       .username(_user.getUsername())
                                                                       .sessionPath(_sessionPath.getAbsolutePath())
                                                                       .cleanupAfterImport(_cleanupAfterImport)
                                                                       .build();
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
