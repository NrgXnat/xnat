/*
 * org.nrg.xnat.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/13/14 5:34 PM
 */
package org.nrg.xnat.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xft.XFT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileUtils {

    public static void moveToCache(final String project, final String subdir, final File src) throws IOException {
        // should include a timestamp in folder name
        if (src.exists()) {
            final File cache = (StringUtils.isBlank(subdir)) ? new File(XFT.GetCachePath(), project) : new File(new File(XFT.GetCachePath(), project), subdir);

            final File dest = new File(cache, org.nrg.xft.utils.FileUtils.renameWTimestamp(src.getName(), Calendar.getInstance().getTime()));

            org.nrg.xft.utils.FileUtils.MoveDir(src, dest, false);
        }
    }

    public static File buildCachepath(String project, final String subdir, final String destName) {
        if (project == null)
            project = "Unknown";
        final File cache = (StringUtils.isEmpty(subdir)) ? new File(XFT.GetCachePath(), project) : new File(new File(XFT.GetCachePath(), project), subdir);

        return new File(cache, org.nrg.xft.utils.FileUtils.renameWTimestamp(destName, Calendar.getInstance().getTime()));
    }

    /**
     * This attempts to retrieve the XNAT version from a combination of the tags
     * and tip.txt files in the {@link XFT#GetConfDir() default configuration folder}.
     * Failing that, it will use the VERSION file. The tags file is copied in from
     * the .hgtags file in the Mercurial repository, while the tip.txt file is generated
     * by calling the Mercurial tip function. This works for installations that are built
     * from source in a connected HG repository.
     *
     * @return The current version of XNAT as a String.
     */
    public static String getXNATVersion() throws IOException {
        if (VERSION == null) {
            // The CHANGESET_PATTERN is just a convenient static object to synchronize on.
            synchronized (MUTEX) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Version information not found, extracted and caching");
                }

                final String location = XFT.GetConfDir();

                if (StringUtils.isEmpty(location)) {
                    throw new IOException("Can't look for version in empty location.");
                }

                // First try to get the tags file at the indicated location.
                File tags = new File(location + File.separator + "tags");

                // If that doesn't exist...
                if (!tags.exists()) {
                    // Get the value from the VERSION file
                    return VERSION = getSimpleVersion();
                }

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(tags));

                    // Go through each line until we get to the end (readLine()
                    // returns null).
                    String current, last = null;
                    while ((current = reader.readLine()) != null) {
                        // Cache each non-null line.
                        last = current;
                    }
                    reader.close();

                    // If the last non-null line was empty, then we don't know
                    // what's going on.
                    if (StringUtils.isBlank(last)) {
                        return VERSION = getSimpleVersion();
                    }

                    // Split on the space, the last line should be something like
                    // "123456789abcdef0 1.5.0"
                    String[] components = last.split(" ");

                    // If it didn't meet that criteria, we don't know what's going on.
                    if (components.length != 2) {
                        return VERSION = getSimpleVersion();
                    }

                    // If we got back a two-element array, the second element should be the version as indicated by the
                    // HG tag. Use that as the default VERSION value. We'll see if there's any reason to override it.
                    final String tag = VERSION = components[1];

                    // Interpret version containing the '-' character as non-release, e.g. snapshot or RC
                    if (tag.contains("-")) {
                        // Then try to get more info from the tip.txt file.
                        final File tip = new File(location + File.separator + "tip.txt");
                        if (tip.exists()) {
                            reader = new BufferedReader(new FileReader(tip));
                            String changeset = "", date = "";
                            while ((current = reader.readLine()) != null) {
                                if (current.contains("changeset:")) {
                                    changeset = current.split(":")[2];
                                } else if (current.contains("date:")) {
                                    date = current.split(":\\s+")[1];
                                }
                            }
                            VERSION = String.format("%s-%s %s", tag, changeset, new SimpleDateFormat("yyyyMMddHHmmss").format(new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(date)));
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Error reading file at the indicated location: " + location, e);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found version information: " + VERSION);
        }

        return VERSION;
    }

    @SafeVarargs
    public static <T extends String> File buildCacheSubDir(T... directories) {
        File last = new File(XFT.GetCachePath());

        for (final String dir : directories) {
            if (!StringUtils.isEmpty(dir)) {
                last = new File(last, dir);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found cache sub-directory: " + last.getAbsolutePath());
        }

        return last;
    }

    private static String getSimpleVersion() throws IOException {
        File version = new File(XFT.GetConfDir() + File.separator + "VERSION");
        if (!version.exists()) {
            logger.error("Can't find the VERSION file at the indicated location: " + XFT.GetConfDir());
            return "Unknown";
        }
        // It's pretty simple, just read it and spit it back out.
        return (new BufferedReader(new FileReader(version))).readLine();
    }

    private static final Log logger = LogFactory.getLog(FileUtils.class);
    private static final Object MUTEX = new Object();
    private static String VERSION;
}
