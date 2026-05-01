/*
 * mizer: org.nrg.dicom.mizer.scripts.AnonScript
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.scripts;

import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

/**
 * The model for a generic anon script.
 */
public interface MizerScript {
    /**
     * Anon scripts are versioned.
     *
     * @return Return a string representing the version of this script.
     */
    VersionString getVersionString();

    /**
     * Return an inputStream to the sciipt's contents.
     *
     * @return an inputStream to the contents of the script.
     * @throws UnsupportedEncodingException
     */
    InputStream getInputStream() throws UnsupportedEncodingException;

    /**
     * Return the script as a string.
     *
     * @return the script as a string.
     */
    String asString();

    /**
     * Return the set of all DICOM tags referenced in this script.
     *
     * @return the set of all DICOM tags referenced in this script.
     */
    Set<TagPath> getReferencedTags();

    /**
     * Return all of the variables referenced in the script, keyed by the variable names.
     *
     * @return The set of all referenced variables
     */
    Map<String, Variable> getVariables();

    /**
     * Return all of the variables referenced in the script whose value must be set from outside the script, keyed by
     * the variable names.
     *
     * @return The set of variables that must be set externally.
     */
    Map<String, Variable> getExternalVariables();
}
