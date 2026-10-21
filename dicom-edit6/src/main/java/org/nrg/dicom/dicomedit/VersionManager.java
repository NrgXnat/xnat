package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class to evaluate script files.
 */
public class VersionManager {

    private static final Logger logger = LoggerFactory.getLogger(VersionManager.class);
    private static VersionManager versionManager;
    private        List<String>   supportedVersionStrings;

    private VersionManager() {
        supportedVersionStrings = supportedVersionStrings(readLibraryVersion());
    }

    /**
     * The script versions a library of the given version accepts: 6.0 to 6.7, which predate the
     * rule, then every minor from 6.10 up to the library's own. 6.8 and 6.9 were library releases
     * only and were never language versions. A patch release adds nothing, and a version that cannot
     * be read accepts up to 6.10.
     *
     * @param libraryVersion the dicom-edit6 version, such as 6.11.0 or 6.11.0-SNAPSHOT.
     *
     * @return the accepted version strings, oldest first.
     */
    static List<String> supportedVersionStrings(final String libraryVersion) {
        final List<String> versions = new ArrayList<>(Arrays.asList("6.0", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7"));
        final Matcher      matcher  = LIBRARY_VERSION.matcher(StringUtils.defaultString(libraryVersion));
        final int          minor    = matcher.find() ? Math.max(Integer.parseInt(matcher.group(1)), FIRST_DERIVED_MINOR) : FIRST_DERIVED_MINOR;
        for (int each = FIRST_DERIVED_MINOR; each <= minor; each++) {
            versions.add("6." + each);
        }
        return Collections.unmodifiableList(versions);
    }

    private static String readLibraryVersion() {
        try (InputStream in = VersionManager.class.getResourceAsStream(LIBRARY_VERSION_RESOURCE)) {
            if (in == null) {
                logger.warn("{} is missing, so script versions are accepted only up to 6.{}", LIBRARY_VERSION_RESOURCE, FIRST_DERIVED_MINOR);
                return null;
            }
            final Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("version");
        } catch (IOException e) {
            logger.warn("Could not read {}, so script versions are accepted only up to 6.{}", LIBRARY_VERSION_RESOURCE, FIRST_DERIVED_MINOR, e);
            return null;
        }
    }

    /**
     * Get the singleton instance of the VersionManager.
     *
     * @return version manager.
     */
    public static VersionManager getInstance() {
        if (versionManager == null) {
            versionManager = new VersionManager();
        }
        return versionManager;
    }

    public List<String> getSupportedVersionStrings() {
        return supportedVersionStrings;
    }

    /**
     * Scan the scriptFile and determine if its supported.
     *
     * Use readVersionString() to extract version identifier and isKnownVersion() to evaluate the identifier.
     *
     * @param scriptFile The file containing the script to evaluate.
     *
     * @return true if file is supported or false if not supported or if an error occured scanning the script file.
     */
    public boolean isSupportedScript(File scriptFile) {
        try {
            return isKnownVersion(readVersionString(scriptFile));
        } catch (IOException e) {
            logger.error("Failed to determine if script is supported: " + scriptFile);
            logger.error("Reason: " + e.getMessage());
            return false;
        }
    }

    /**
     * Scan the script and determine if its supported.
     *
     * Use readVersionString() to extract version identifier and isKnownVersion() to evaluate the identifier.
     *
     * @param script The script to evaluate.
     *
     * @return true if the script is supported or false if not supported or if an error occurred scanning the script file.
     */
    public boolean isSupportedScript(final String script) {
        return isKnownVersion(readVersionString(script));
    }

    /**
     * Check the version string against a list of known versions.
     *
     * Version strings are assumed to have the form 6.0, i.e. &lt;major&gt;.&lt;minor&gt;.
     *
     * @param versionString The version string to evaluate.
     *
     * @return true if known string, false if not.
     */
    protected boolean isKnownVersion(final String versionString) {
        return StringUtils.isNotBlank(versionString) && supportedVersionStrings.contains(versionString);
    }

    /**
     * Parse the version string out of the script file.
     *
     * Assumes the script file contains a line with format 'version "some-string"'. The version string is some-string
     * with the double-quotes removed.
     *
     * @param scriptFile The file containing the script to evaluate.
     *
     * @return the version string or null if it is not found.
     *
     * @throws IOException if the scriptFile is not found or if an IO error occurs.
     */
    protected String readVersionString(File scriptFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String version = readVersionString(line);
                if (StringUtils.isNotBlank(version)) {
                    return version;
                }
            }
        }
        return null;
    }

    /**
     * Parse the version string out of the submitted string. Note that this can be a full script or just a single line.
     *
     * Assumes the string contains a line with format 'version "some-string"'. The version string is some-string
     * with the double-quotes removed.
     *
     * @param script The script to evaluate.
     *
     * @return the version string or null if it is not found.
     */
    protected String readVersionString(final String script) {
        final Matcher matcher = PATTERN.matcher(script);
        return matcher.find() ? matcher.group("version") : null;
    }

    private static final String  LIBRARY_VERSION_RESOURCE = "dicomedit-version.properties";
    private static final Pattern LIBRARY_VERSION          = Pattern.compile("^6\\.(\\d+)");
    private static final int     FIRST_DERIVED_MINOR      = 10;
    private static final Pattern PATTERN = Pattern.compile("^version \"(?<version>[\\d][\\d.]+)\"$", Pattern.MULTILINE);
}
