package org.nrg.dicom.dicomedit;

import org.junit.Assume;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Pins how the accepted script versions follow the library version.
 */
public class VersionManagerTest {
    private static final List<String> BEFORE_THE_RULE = Arrays.asList("6.0", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7");

    @Test
    public void acceptsEveryMinorFromSixTenUpToTheLibrarysOwn() {
        assertEquals(concat("6.10"), VersionManager.supportedVersionStrings("6.10.0-SNAPSHOT"));
        assertEquals(concat("6.10", "6.11", "6.12"), VersionManager.supportedVersionStrings("6.12.0"));
    }

    @Test
    public void aPatchReleaseAddsNothing() {
        assertEquals(VersionManager.supportedVersionStrings("6.11.0"), VersionManager.supportedVersionStrings("6.11.3"));
    }

    @Test
    public void anUnreadableVersionAcceptsUpToSixTen() {
        assertEquals(concat("6.10"), VersionManager.supportedVersionStrings(null));
        assertEquals(concat("6.10"), VersionManager.supportedVersionStrings("${version}"));
    }

    @Test
    public void theBuiltLibraryAcceptsItsOwnMajorMinor() throws Exception {
        // The test task passes the project version in. Checked against the resource itself: at 6.10
        // an unfilled resource would still accept the right versions, through the fallback.
        final String library = System.getProperty("dicomedit.libraryVersion");
        Assume.assumeNotNull(library);
        final Properties properties = new Properties();
        try (InputStream in = VersionManager.class.getResourceAsStream("dicomedit-version.properties")) {
            assertNotNull("dicomedit-version.properties is missing", in);
            properties.load(in);
        }
        assertEquals("dicomedit-version.properties was not filled in by the build", library, properties.getProperty("version"));

        final String[]     parts    = library.split("[.-]");
        final List<String> accepted = VersionManager.getInstance().getSupportedVersionStrings();
        assertEquals(parts[0] + "." + parts[1], accepted.get(accepted.size() - 1));
    }

    private static List<String> concat(final String... derived) {
        final List<String> versions = new java.util.ArrayList<>(BEFORE_THE_RULE);
        versions.addAll(Arrays.asList(derived));
        return versions;
    }
}
