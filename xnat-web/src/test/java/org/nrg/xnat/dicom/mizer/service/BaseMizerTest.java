/*
 * mizer: org.nrg.dicom.mizer.service.impl.BaseMizerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.dicom.mizer.service;

import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.utils.TestFileUtils;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.io.IOException;

public class BaseMizerTest {
    public static final int TAG_PROJECT = 0x00081030;
    public static final int TAG_SUBJECT = 0x00100010;
    public static final int TAG_SESSION = 0x00100020;

    protected DicomObjectI getTestDicomObject() throws IOException, MizerException {
        final File anonTestFile = TestFileUtils.copyTestFileToTemp(TEST_FILE);
        return DicomObjectFactory.newInstance(anonTestFile);
    }

    protected static final File   TEST_FILE     = ResourceManager.getInstance().getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm");

    protected static final String PROJECT = "my_project";
    protected static final String SUBJECT = "my_subject";
    protected static final String SESSION = "my_session";

    protected static final String SET_STD_ATTRS_DE4 = "(0008,1030) := project\n(0010,0010) := subject\n(0010,0020) := session\n";
    protected static final String SET_STD_ATTRS_DE6 = "version \"6.1\"\n" + SET_STD_ATTRS_DE4;

    protected static final String S_USE_VAR_DE6 = "version \"6.1\"\n(0008,103e) := studyDescription\nstudyDescription := project\n";
    protected static final String S_USE_VAR_DE4 = "(0008,103e) := studyDescription\nstudyDescription := project\n";
    protected static final String S_INIT_VAR    = "studyDescription := \"var init\"\n";
}
