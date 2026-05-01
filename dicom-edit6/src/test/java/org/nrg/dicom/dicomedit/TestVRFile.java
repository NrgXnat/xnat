/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.*;

import static org.junit.Assert.*;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * Run tests of retainPrivateTags function.
 *
 */
public class TestVRFile {

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File file = _resourceManager.getTestResourceFile("dicom/vr.dcm");
    private static final File priv = _resourceManager.getTestResourceFile("dicom/priv-vr.dcm");
    @Test
    @Ignore
    public void test() throws MizerException {

        String script = "(0008,0418) := normalizeString[(0008,0418)]\n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(file);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(0x00100010));
    }

    @Test
    @Ignore
    public void testPriv() throws MizerException {

        String script = "(0055,{REST TEST}15) := normalizeString[(0055,{REST TEST}15)]\n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(priv);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(0x00100010));
    }


}
