/*
 * web: org.nrg.xnat.archive.PrearcImporterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.PrearcImporter;
import org.nrg.xft.utils.predicates.ProjectAccessPredicate;

import java.io.File;
import java.util.Arrays;

import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

public final class PrearcImporterFactory {
    public static PrearcImporterFactory getFactory() { return INSTANCE; }

    /**
     * Builds a PrearchiveImporter for the given project, source and destination directories, and (optionally) data files.
     *
     * @param project Name of the project; replaced by null if equal to the {@link ProjectAccessPredicate#UNASSIGNED
     *                unassigned prearchive name}
     * @param toDir   Destination directory
     * @param fromDir Source directory
     * @param files   Explicit list of files to be imported (optional)
     */
    public PrearcImporter getPrearcImporter(final String project, final File toDir, final File fromDir, final File... files) {
        return new PrearcImporter(StringUtils.equalsIgnoreCase(project, UNASSIGNED) ? null : project,
                                  toDir, fromDir, ArrayUtils.isNotEmpty(files) ? files : null,
                                  _buildXMLfromIMAcmd, _buildXMLfromIMAenv);
    }

    private PrearcImporterFactory(final String[] buildXMLfromIMAcmd, final String[] buildXMLfromIMAenv) {
        _buildXMLfromIMAcmd = Arrays.copyOf(buildXMLfromIMAcmd, buildXMLfromIMAcmd.length);
        _buildXMLfromIMAenv = Arrays.copyOf(buildXMLfromIMAenv, buildXMLfromIMAenv.length);
    }

    // TODO: these should be configurable
    private static final String[]              COMMAND  = {"archiveIma"};
    private static final String[]              ENV      = {"LD_LIBRARY_PATH=/usr/local/lib", "PATH=/usr/bin:/data/cninds01/data2/arc-tools"};
    private static final PrearcImporterFactory INSTANCE = new PrearcImporterFactory(COMMAND, ENV);

    private final String[] _buildXMLfromIMAcmd;
    private final String[] _buildXMLfromIMAenv;
}
