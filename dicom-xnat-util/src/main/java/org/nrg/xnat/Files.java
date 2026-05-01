/*
 * dicom-xnat-util: org.nrg.xnat.Files
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class Files {
    private Files() {}

    private static final String nonFileCharRE = "[^\\w.-]";
    private static final String fileNameRE = "[\\w.-]+";

    private final static Pattern nonFileCharsPattern = Pattern.compile(nonFileCharRE);
    private static final Pattern fileNamePattern = Pattern.compile(fileNameRE);


    public static String toFileNameChars(final CharSequence s) {
        return null == s ? null : nonFileCharsPattern.matcher(s).replaceAll("_");
    }

    public static boolean isValidFilename(final CharSequence s) {
        return null != s && fileNamePattern.matcher(s).matches();
    }

    private final static String FORMAT_NAME = "DICOM";
    private final static String SCANS_DIR_NAME = "SCANS";
    private final static String RESOURCES_DIR_NAME = "RESOURCES";

    /**
     * Makes a File record for the provided named image file in the indicated scan,
     * using the canonical XNAT directory structure.
     * @param sessionDirectory root directory for the containing session
     * @param scan name of the scan containing the image file; if null, file will be placed in RESOURCES
     * @param name name of the image file
     * @return image File in the proper location
     */
    public static File getImageFile(final File sessionDirectory, final String scan, final String name) {
        final File dataDir;
        if (null == scan) {
            final File resourcesDir = new File(sessionDirectory, RESOURCES_DIR_NAME);
            dataDir = new File(resourcesDir, FORMAT_NAME);
        } else {
            final File scansDir = new File(sessionDirectory, SCANS_DIR_NAME);
            final File scanDir = new File(scansDir, scan);
            dataDir = new File(scanDir, FORMAT_NAME);
        }
        return new File(dataDir, name);
    }
}
