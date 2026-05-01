/*
 * dicom-xnat-util: org.nrg.xnat.FilesTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class FilesTest extends TestCase {

    /**
     * Test method for {@link org.nrg.xnat.Files#toFileNameChars(java.lang.CharSequence)}.
     */
    public void testToFileNameChars() {
        assertEquals("4", Files.toFileNameChars("4"));
        assertEquals("_", Files.toFileNameChars("_"));
        assertEquals(".", Files.toFileNameChars("."));
        assertEquals("_", Files.toFileNameChars("%"));
        assertEquals("_f_o_o_", Files.toFileNameChars(",f:o^o)"));
    }

    /**
     * Test method for {@link org.nrg.xnat.Files#isValidFilename(java.lang.CharSequence)}.
     */
    public void testIsValidFilename() {
        assertTrue(Files.isValidFilename("4"));
        assertTrue(Files.isValidFilename("_"));
        assertTrue(Files.isValidFilename("foo.bar"));
        assertFalse(Files.isValidFilename(""));
        assertFalse(Files.isValidFilename("*w*"));
    }

    /**
     * Test method for {@link org.nrg.xnat.Files#getImageFile(File, String, String)}.
     */
    public void testGetImageFile() throws IOException {
        final File root = File.createTempFile("org.nrg.xnat.FileTest", "root");
        root.delete();
        root.mkdir();
        root.deleteOnExit();

        final File img = Files.getImageFile(root, "1", "foo.dcm");
        assertFalse(img.exists());

        img.getParentFile().mkdirs();
        assertTrue(img.createNewFile());

        img.delete();
    }
}
