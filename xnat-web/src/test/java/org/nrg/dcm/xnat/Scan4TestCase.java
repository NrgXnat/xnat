/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.Scan4TestCase
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Contains common functions to configure unit test data.
 */
public class Scan4TestCase {
    private static final ResourceManager RESOURCE_MANAGER = ResourceManager.getInstance();

    private static final String scan4_includes = "1.MR.head_DHead.4.*";

    protected File initializeScan4WorkingCopy() throws IOException {
        return initializeScan4WorkingCopy(scan4_includes);
    }

    protected File initializeScan4WorkingCopy(final String includes) throws IOException {
        final List<Resource> resources = BasicXnatResourceLocator.getResources("classpath:dicom/" + includes);
        assertNotNull(resources);
        assertTrue(resources.size() > 0);

        final FileSet scan4 = new FileSet();
        scan4.setIncludes(includes);
        scan4.setDir(RESOURCE_MANAGER.getTestResourceFile(resources.getFirst().getURL()).getParentFile());

        final File folder = File.createTempFile("org.nrg.dcm.xnat.DICOMScanBuilder", ".test");
        folder.delete();
        folder.mkdir();

        final Project project = new Project();
        project.setBaseDir(folder);

        final Copy copy = new Copy();
        copy.setProject(project);
        copy.setTodir(folder);

        copy.addFileset(scan4);
        copy.execute();

        return folder;
    }

    protected void tearDownWorkingCopy(final File folder) {
        final Project project = new Project();
        project.setBaseDir(folder);
        final Delete delete = new Delete();
        delete.setDir(folder);
        delete.setVerbose(false);
        delete.execute();
    }
}
