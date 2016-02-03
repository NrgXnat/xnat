/*
 * org.nrg.xnat.archive.PrearcImporterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.archive;

import org.nrg.PrearcImporter;
import org.nrg.xnat.ajax.Prearchive;

import java.io.File;

public final class PrearcImporterFactory {
    // TODO: these should be configurable
    private final static String[] command = {"archiveIma"};
    private final static String[] env = {
	"LD_LIBRARY_PATH=/usr/local/lib",
	"PATH=/usr/bin:/data/cninds01/data2/arc-tools"
    };
    
    private final String[] buildXMLfromIMAcmd, buildXMLfromIMAenv;
    
    private PrearcImporterFactory(final String[] buildXMLfromIMAcmd, final String[] buildXMLfromIMAenv) {
	this.buildXMLfromIMAcmd = new String[buildXMLfromIMAcmd.length];
	System.arraycopy(buildXMLfromIMAcmd, 0, this.buildXMLfromIMAcmd, 0, buildXMLfromIMAcmd.length);
	this.buildXMLfromIMAenv = new String[buildXMLfromIMAenv.length];
	System.arraycopy(buildXMLfromIMAenv, 0, this.buildXMLfromIMAenv, 0, buildXMLfromIMAenv.length);
    }
    
    private static PrearcImporterFactory instance = new PrearcImporterFactory(command, env);
    
    public static PrearcImporterFactory getFactory() { return instance; }
    
    /**
     * Builds a PrearchiveImporter for the given project, source and destination directories, and (optionally) data files.
     * @param project Name of the project; if equal to the unassigned prearchive name ("Unassigned"), replaced by null
     * @param toDir Destination directory
     * @param fromDir Source directory
     * @param files Explicit list of files to be imported (optional)
     */
    public PrearcImporter getPrearcImporter(final String project, final File toDir, final File fromDir, final File...files) {
	return new PrearcImporter(Prearchive.COMMON.equals(project) ? null : project,
		toDir, fromDir, (null == files || 0 == files.length) ? null : files,
		buildXMLfromIMAcmd, buildXMLfromIMAenv);
    }
}
