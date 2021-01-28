/*
 * web: org.nrg.xnat.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import static org.nrg.xft.utils.FileUtils.MoveDir;
import static org.nrg.xft.utils.FileUtils.renameWTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class FileUtils {
    public static List<String> nodeToList(final JsonNode node) {
        return node.isArray()
               ? StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).collect(Collectors.toList())
               : (Collections.singletonList(node.isTextual()
                                            ? node.asText()
                                            : node.toString()));
    }

    public static void moveToCache(final String project, final String subDir, final File src) throws IOException {
        // should include a timestamp in folder name
        if (src.exists()) {
            final File cache = (StringUtils.isBlank(subDir)) ? new File(XDAT.getSiteConfigPreferences().getCachePath(), project) : new File(new File(XDAT.getSiteConfigPreferences().getCachePath(), project), subDir);

            final File dest = new File(cache, renameWTimestamp(src.getName()));

            MoveDir(src, dest, false);
        }
    }

    public static File buildCachepath(final String project, final String subDir, final String destName) {
        final Path root = Paths.get(XDAT.getSiteConfigPreferences().getCachePath(), StringUtils.defaultIfBlank(project, "Unknown"));
        return (StringUtils.isEmpty(subDir) ? root : root.resolve(subDir)).resolve(renameWTimestamp(destName)).toFile();
    }

    /**
     * This attempts to retrieve the XNAT version from a combination of the tags
     * and tip.txt files in the {@link XFT#GetConfDir() default configuration folder}.
     * Failing that, it will use the VERSION file.
     *
     * @return The current version of XNAT as a String.
     */
    public static String getXNATVersion() throws IOException {
        if (VERSION == null) {
            // The CHANGESET_PATTERN is just a convenient static object to synchronize on.
            synchronized (MUTEX) {
                log.debug("Version information not found, extracted and caching");

                final String location = XFT.GetConfDir();
                if (StringUtils.isEmpty(location)) {
                    throw new IOException("Can't look for version in empty location.");
                }

                // First try to get the tags file at the indicated location.
                final File tags = Paths.get(location, "tags").toFile();
                // If that doesn't exist...
                if (!tags.exists()) {
                    // Get the value from the VERSION file
                    return VERSION = getSimpleVersion();
                }

                try (final ReversedLinesFileReader tagsReader = new ReversedLinesFileReader(tags, Charset.defaultCharset())){
                    final String last = tagsReader.readLine();

                    // If the last non-null line was empty, then we don't know
                    // what's going on.
                    if (StringUtils.isBlank(last)) {
                        return VERSION = getSimpleVersion();
                    }

                    // Split on the space, the last line should be something like
                    // "123456789abcdef0 1.5.0"
                    final String[] components = last.split(" ");

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
                            final Pair<String, String> changesetAndDate = getChangesetAndDate(tip);
                            VERSION = String.format("%s-%s %s", tag, changesetAndDate.getKey(), new SimpleDateFormat("yyyyMMddHHmmss").format(new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(changesetAndDate.getValue())));
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Error reading file at the indicated location: " + location, e);
                }
            }
        }

        log.debug("Found version information: {}", VERSION);
        return VERSION;
    }

    @SafeVarargs
    public static <T extends String> File buildCacheSubDir(T... directories) {
        final File subDir = Paths.get(XDAT.getSiteConfigPreferences().getCachePath(), directories).toFile();
        log.debug("Found cache sub-directory: {}", subDir.getAbsolutePath());
        return subDir;
    }

    private static String getSimpleVersion() throws IOException {
        final File version = Paths.get(XFT.GetConfDir(), "VERSION").toFile();
        if (!version.exists()) {
            log.error("Can't find the VERSION file at the indicated location: {}", XFT.GetConfDir());
            return "Unknown";
        }
        // It's pretty simple, just read it and spit it back out.
        final List<String> lines = IOUtils.readLines(new BufferedReader(new FileReader(version)));
        return lines.isEmpty() ? "" : lines.get(0);
    }

    private static Pair<String, String> getChangesetAndDate(final File file) {
        try (final ReversedLinesFileReader reader = new ReversedLinesFileReader(file, Charset.defaultCharset())) {
            String current, changeset = "", date = "";
            while ((current = reader.readLine()) != null && StringUtils.isAnyBlank(changeset, date)) {
                if (current.contains("changeset:")) {
                    changeset = current.split(":")[2];
                } else if (current.contains("date:")) {
                    date = current.split(":\\s+")[1];
                }
            }
            return Pair.of(changeset, date);
        } catch (IOException e) {
            log.error("An error occurred trying to read changeset and date from file {}", file, e);
            return ImmutablePair.nullPair();
        }
    }

    private static final Object MUTEX = new Object();

    private static String VERSION;
}
