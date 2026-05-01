package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
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
        supportedVersionStrings = Arrays.asList("6.0", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7");
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

    private static final Pattern PATTERN = Pattern.compile("^version \"(?<version>[\\d][\\d.]+)\"$", Pattern.MULTILINE);
}
