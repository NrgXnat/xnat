/*
 * web: org.nrg.xnat.helpers.merge.AnonymizerA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.RejectedInstanceException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xdat.XDAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class AnonymizerA implements Callable<List<AnonymizationResult>> {
    abstract String getSubject();

    abstract String getLabel();

    /**
     * Get the appropriate edit script.
     *
     * @return The configuration containing the anon script.
     */
    abstract Configuration getScript();

    /**
     * Check if editing is enabled.
     *
     * @return Returns true if anonymization is enabled.
     */
    abstract boolean isEnabled();

    /**
     * Sometimes the session passed in isn't associated with a project,
     * for instance if the session is in the prearchive so
     * subclasses must specify how to get the project name.
     *
     * @return The name of the associated project (if any).
     */
    abstract String getProjectName();

    /**
     * Check if reject statements should be ignored (on pre-existing data).
     *
     * @return Returns true if reject statements should be ignored.
     */
    public boolean ignoreRejections(){
        return _ignoreRejections;
    }


    /**
     * Get the list of files that need to be anonymized.
     *
     * @return The list of files to be anonymized.
     *
     * @throws IOException When an error occurs accessing the files.
     */
    abstract List<File> getFilesToAnonymize() throws IOException;

    private List<AnonymizationResult> anonymize(List<File> files, String projectName, String subject, String label, long id, String script, boolean record) throws MizerException {
        if (script != null) {
            if (isEnabled()) {
                final MizerService service = XDAT.getContextService().getBeanSafely(MizerService.class);
                return service.anonymize(files, getProjectName(), getSubject(), getLabel(), id, script, record, ignoreRejections());
            } else {
                // anonymization is disabled.
                if (_log.isDebugEnabled()) {
                    _log.debug("Anonymization is disabled for the script {}, nothing to do.", script.toString());
                }
            }
        } else {
            // this project does not have an anon script
            _log.debug("No anon script found for project {}, nothing to do.", getProjectName());
        }
        return new ArrayList<>();
    }

    @Override
    public List<AnonymizationResult> call() throws Exception {
        if (getScript() == null) {
            String msg = "No anon script found for current configuration, nothing to do.";
            _log.debug(msg);
            return Arrays.asList(new AnonymizationResultNoOp(null, msg));
        }
        if (!isEnabled()) {
            String msg = "Anonymization is not enabled in the current configuration, nothing to do.";
            _log.debug(msg);
            return Arrays.asList(new AnonymizationResultNoOp(null, msg));
        }
        final List<File> files = getFilesToAnonymize();
        if (files.size() == 0) {
            String msg = "Found no files to be anonymized.";
            _log.debug(msg);
            return Arrays.asList(new AnonymizationResultNoOp(null, msg));
        }
        _log.debug("Found {} files to be anonymized.", files.size());
        Configuration script = getScript();
        return anonymize( files, getProjectName(), getSubject(), getLabel(), script.getId(), script.getContents(), true);
    }

    private static final Logger _log = LoggerFactory.getLogger(AnonymizerA.class);

    protected boolean _ignoreRejections;


}
