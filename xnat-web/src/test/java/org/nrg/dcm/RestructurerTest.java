/*
 * PrearcImporter: org.nrg.dcm.RestructurerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.core.io.Resource;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class RestructurerTest {
    private final static File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    private final Project _antProject = new Project();
    private final File _testDataDir;

    public RestructurerTest() throws IOException {
        final List<Resource> resources = BasicXnatResourceLocator.getResources("classpath:dicom/*.dcm");
        assertNotNull(resources);

        _antProject.setBaseDir(_testDataDir = resources.get(0).getFile().getParentFile());
    }

    /**
     * Test method for {@link org.nrg.dcm.Restructurer#Restructurer(java.io.File, java.io.File)}.
     */
    @Test
    public void testRestructurerFileFile() throws IOException {
        // Make a working copy of the data.
        final Copy copy = new Copy();
        copy.setProject(_antProject);
        final FileSet fs = new FileSet();
        fs.setDir(_testDataDir);
        fs.setExcludes("*.xml,*.log");
        copy.addFileset(fs);
        final File copyDir = File.createTempFile("test-data", "copy");
        copyDir.delete();
        copy.setTodir(copyDir);
        copy.execute();

        final File   o1     = new File(tmpDir, "r1");
        final Delete delete = new Delete();
        delete.setProject(_antProject);
        delete.setDir(o1);
        delete.execute();

        assertFalse(o1.exists());
        final Restructurer r1 = new Restructurer(copyDir, o1);
        r1.run();
        final String[] remaining = copyDir.list();
        assertNull(remaining);    // if this fails, verify that the original directory contained only DICOM.
        copyDir.delete();
        assertFalse(copyDir.exists());    // should have moved all files

        // check for exactly one session directory
        final String[] sessionFiles = o1.list();
        assertNotNull(sessionFiles);
        assertEquals(1, sessionFiles.length);
        final File sessionDir = new File(o1, "Sample_ID");
        assertTrue(sessionDir.isDirectory());

        // containing three scans
        final File rawDir = new File(sessionDir, Restructurer.SCANS_DIR);
        assertNotNull(rawDir);
        assertTrue(rawDir.isDirectory());
        final String[] list = rawDir.list();
        assertNotNull(list);
        assertEquals(3, list.length);

        // total of 528 .dcm files
        int          fileCount = 0;
        final File[] files     = rawDir.listFiles();
        assertNotNull(files);
        for (final File scanDir : files) {
            final File dicomDir = new File(scanDir, "DICOM");
            final String[] matching = dicomDir.list(new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    return name.endsWith(".dcm");
                }
            });
            fileCount += matching != null ? matching.length : 0;
        }
        assertEquals(27, fileCount);

        assertNotNull(r1);
        assertEquals(1, r1.getSessions().size());
        for (final File studyDir : r1) {
            assertEquals(o1, studyDir.getParentFile());
        }

        delete.setDir(o1);
        delete.execute();
    }

    /**
     * Test method for {@link org.nrg.dcm.Restructurer#iterator()}.
     */
    @Test
    public final void testIterator() {
        // fail("Not yet implemented"); // TODO
    }
}
