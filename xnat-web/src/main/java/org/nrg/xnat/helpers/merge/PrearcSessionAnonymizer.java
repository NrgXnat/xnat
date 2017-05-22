/*
 * web: org.nrg.xnat.helpers.merge.PrearcSessionAnonymizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.dcm4che2.iod.module.macro.Code;
import org.nrg.config.entities.Configuration;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PrearcSessionAnonymizer extends AnonymizerA {
    /**
     * @param session           A session object
     * @param projectId         The project id, eg. xnat_E*
     * @param sessionPrearcPath The location of the root prearchive directory
     */
    public PrearcSessionAnonymizer(final XnatImagesessiondataI session, final String projectId, final String sessionPrearcPath) {
        _prearchivePath = Paths.get(XDAT.getSiteConfigPreferences().getPrearchivePath());
        _sessionPrearcPath = Paths.get(sessionPrearcPath);
        _anonymizer = new ProjectAnonymizer(session, projectId, sessionPrearcPath);
        _session = session;
    }

    @Override
    String getProjectName() {
        return _anonymizer.projectId;
    }

    @Override
    String getLabel() {
        return _anonymizer.getLabel();
    }

    /**
     * Returns the subject string that will be passed into the
     * Anonymize.anonymize function
     */
    @Override
    String getSubject() {
        return _anonymizer.getSubject();
    }

    @SuppressWarnings("unused")
    Long getDBId(String project) {
        return BaseXnatProjectdata.getProjectInfoIdFromStringId(project);
    }

    Configuration getScript() {
        return _anonymizer.getScript();
    }

    boolean isEnabled() {
        return _anonymizer.isEnabled();
    }

    /**
     * A DICOM file doesn't need anonymization if this project's script
     * was the last thing applied to it.
     *
     * Make sure this is only called if this project has a script or it will
     * throw a NullPointerException
     *
     * @param file The file to check for reanonymization.
     *
     * @return Returns true if the file should be reanonymized.
     *
     * @throws IOException When an error occurs accessing the file.
     */
    boolean needsAnonymization(final File file) throws IOException {
        try {
            final Path        relative = _prearchivePath.relativize(_sessionPrearcPath);
            final SessionData session  = PrearcDatabase.getSession(Paths.get("/prearchive/projects", relative.toString()).toString());
            if (session != null) {
                return !session.getPreventAnon();
            }
        } catch (Exception e) {
            logger.error("An error occurred trying to retrieve the prearchive sessions for path " + file.getAbsolutePath(), e);
        }
        final Code[] codes = DicomUtils.getCodes(file);
        if (codes != null && codes.length != 0) {
            final Code          last          = codes[codes.length - 1];
            final Configuration configuration = getScript();
            if (configuration != null && last.getCodeValue().equals(Long.toString(configuration.getId()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve a list of files to anonymize, but if the files have already been anonymized we don't do it again. To
     * that end for each file we check its last code to ensure that it doesn't correspond to the project-specific script
     * that is about to be applied.
     *
     * @return A list of the files that should be anonymized.
     */
    @Override
    public List<File> getFilesToAnonymize() throws IOException {
        List<File> ret = new ArrayList<>();
        // anonymize everything in srcRootPath
        for (final XnatImagescandataI scan : _session.getScans_scan()) {
            for (final XnatAbstractresourceI res : scan.getFile()) {
                if (res instanceof XnatResource) {
                    final XnatResource abs = (XnatResource) res;
                    if (abs.getFormat().equals("DICOM")) {
                        for (final File file : abs.getCorrespondingFiles(_sessionPrearcPath.toString())) {
                            if (needsAnonymization(file)) {
                                logger.debug("Adding file {} to the list of files requiring reanonymization.", file.getPath());
                                ret.add(file);
                            } else {
                                // no anonymization needed so don't include this file in the list.
                                logger.debug("The file {} doesn't require reanonymization.", file.getPath());
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static final Logger logger = LoggerFactory.getLogger(PrearcSessionAnonymizer.class);

    private final Path                  _prearchivePath;
    private final Path                  _sessionPrearcPath;
    private final ProjectAnonymizer     _anonymizer;
    private final XnatImagesessiondataI _session;
}
