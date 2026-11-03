package org.nrg.xnat.restlet.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CachedUploadTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Path cache;
    private Path folder;

    @Before
    public void setUp() throws IOException {
        cache = temp.newFolder("cache").toPath();
        folder = Files.createDirectory(cache.resolve("upload1"));
    }

    @Test
    public void deletesTheUploadAndItsFolder() throws IOException {
        final Path file = Files.writeString(folder.resolve("study.zip"), "zip");

        new CachedUpload(cache, folder, file).delete();

        assertFalse(Files.exists(file));
        assertFalse(Files.exists(folder));
        assertTrue(Files.isDirectory(cache));
    }

    @Test
    public void keepsTheFolderWhenItHoldsOtherFiles() throws IOException {
        final Path file = Files.writeString(folder.resolve("study.zip"), "zip");
        final Path other = Files.writeString(folder.resolve("other.zip"), "zip");

        new CachedUpload(cache, folder, file).delete();

        assertFalse(Files.exists(file));
        assertTrue(Files.exists(other));
    }

    @Test
    public void deletesTheFolderWithTheLastOfItsUploads() throws IOException {
        // The image uploader imports each file of one upload folder on its own.
        final Path first = Files.writeString(folder.resolve("first.zip"), "zip");
        final Path second = Files.writeString(folder.resolve("second.zip"), "zip");

        new CachedUpload(cache, folder, first).delete();
        assertTrue(Files.isDirectory(folder));

        new CachedUpload(cache, folder, second).delete();
        assertFalse(Files.exists(folder));

        // A second cleanup of a folder another import already removed does nothing.
        new CachedUpload(cache, folder, second).delete();
        assertTrue(Files.isDirectory(cache));
    }

    @Test
    public void deletesTheSubfoldersItEmpties() throws IOException {
        final Path file = Files.writeString(Files.createDirectories(folder.resolve("a/b")).resolve("study.zip"), "zip");

        new CachedUpload(cache, folder, file).delete();

        assertFalse(Files.exists(folder));
        assertTrue(Files.isDirectory(cache));
    }

    @Test
    public void deletesTheFolderWhenTheUploadIsAlreadyGone() throws IOException {
        // Some importers move their upload out of the cache.
        new CachedUpload(cache, folder, folder.resolve("study.zip")).delete();

        assertFalse(Files.exists(folder));
    }

    @Test
    public void deletesNothingOutsideTheCacheFolder() throws IOException {
        final Path outside = Files.writeString(temp.getRoot().toPath().resolve("study.zip"), "zip");
        final Path sibling = Files.writeString(Files.createDirectory(cache.resolve("upload2")).resolve("study.zip"), "zip");

        new CachedUpload(cache, folder, folder.resolve("../../study.zip")).delete();
        new CachedUpload(cache, folder, folder.resolve("../upload2/study.zip")).delete();
        new CachedUpload(cache, cache.resolve(".."), outside).delete();
        new CachedUpload(cache, cache, cache.resolve("upload2/study.zip")).delete();

        assertTrue(Files.exists(outside));
        assertTrue(Files.exists(sibling));
        assertTrue(Files.isDirectory(folder));
    }
}
