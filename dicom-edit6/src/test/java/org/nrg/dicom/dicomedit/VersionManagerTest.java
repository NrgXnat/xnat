package org.nrg.dicom.dicomedit;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void theBuiltLibraryAcceptsItsOwnMajorMinor() {
        // Set by the test task from the project version, so a resource the build failed to fill in
        // fails here instead of quietly accepting fewer versions.
        final String   library     = System.getProperty("dicomedit.libraryVersion");
        final String[] parts       = library.split("[.-]");
        final String   ownVersion  = parts[0] + "." + parts[1];
        final List<String> accepted = VersionManager.getInstance().getSupportedVersionStrings();
        assertEquals(ownVersion, accepted.get(accepted.size() - 1));
        assertTrue(accepted.contains("6.10"));
    }

    private static List<String> concat(final String... derived) {
        final List<String> versions = new java.util.ArrayList<>(BEFORE_THE_RULE);
        versions.addAll(Arrays.asList(derived));
        return versions;
    }
}
