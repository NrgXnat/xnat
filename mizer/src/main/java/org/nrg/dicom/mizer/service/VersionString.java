package org.nrg.dicom.mizer.service;


import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model version string of format major.minor.fix, e.g. 6.0.1.
 *
 * Fields default to 0 if not supplied in string.
 * This class simplifies the ordering of versioned items.
 *
 */
public class VersionString implements Comparable<VersionString> {
    public VersionString(final int major, final int minor, final int fix) {
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }

    /**
     * Construct VersionString from supplied string.
     *
     *  Strings must have format major.minor.fix, where major.minor.fix are integers.
     *  Valid strings are "4.0.10", "4.0", "4"
     *  Invalid strings are "4.", ".1"
     *
     * @param version the version string
     * @throws IllegalArgumentException if version string is not in the allowed format.
     */
    public VersionString(final String version) {
        final Matcher matcher;
        if (StringUtils.isBlank(version) || !(matcher = VERSION_STRING.matcher(version)).find()) {
            throw new IllegalArgumentException(String.format(INVALID_VERSION, StringUtils.defaultIfBlank(version, "[blank]")));
        }

        major = parse(matcher, "major");
        minor = parse(matcher, "minor");
        fix = parse(matcher, "fix");
    }

    public boolean matches(final String version) {
        try {
            VersionString that = new VersionString( version);
            return this.equals(that);
        }
        catch( IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return fix == 0 ? major + "." + minor : major + "." + minor + "." + fix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionString that = (VersionString) o;

        return major == that.major && minor == that.minor && fix == that.fix;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + fix;
        return result;
    }

    @Override
    public int compareTo(@Nonnull VersionString that) {
        if( this == that) return EQUAL;

        if( this.major < that.major) return BEFORE;
        if( this.major > that.major) return AFTER;

        if( this.minor < that.minor) return BEFORE;
        if( this.minor > that.minor) return AFTER;

        if( this.fix < that.fix) return BEFORE;
        if( this.fix > that.fix) return AFTER;

        return EQUAL;
    }

    private static int parse(final Matcher matcher, final String group) {
        final String text = matcher.group(group);
        return StringUtils.isNotBlank(text) ? Integer.parseInt(text) : 0;
    }

    private static final Pattern VERSION_STRING  = Pattern.compile("(?<major>[\\d]+)(?:\\.(?<minor>[\\d]+)(?:\\.(?<fix>[\\d]+))?)?");
    private static final String  INVALID_VERSION = "Invalid value passed for version: %s. Should be of the form [major].[minor].[fix]. Only the major version is required.";
    private static final int     BEFORE          = -1;
    private static final int     EQUAL           = 0;
    private static final int     AFTER           = 1;

    private final int major;
    private final int minor;
    private final int fix;
}
