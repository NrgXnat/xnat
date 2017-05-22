package org.nrg.test.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides convenience methods for working with test files.
 */
public class TestFileUtils {
    private TestFileUtils() {
        // Prevent instantiation
    }

    public static File copyTestFileToTemp(final File source) throws IOException {
        final Path testFolder = Files.createTempDirectory("test-");
        testFolder.toFile().deleteOnExit();
        _log.debug("Created temporary folder {}, set to delete on exit.", testFolder.toAbsolutePath().toString());
        final File testFile      = Files.createTempFile(testFolder, "copy-of-" + source.getName(), "").toFile();
        testFile.deleteOnExit();
        FileUtils.copyFile(source, testFile);
        _log.debug("Copied source file {} to temporary file {}, set to delete on exit.", source.getAbsolutePath(), testFile.getAbsolutePath());
        return testFile;
    }

    private static final Logger _log = LoggerFactory.getLogger(TestFileUtils.class);
}
