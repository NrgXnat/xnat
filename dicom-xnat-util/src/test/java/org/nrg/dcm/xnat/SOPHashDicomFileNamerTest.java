/*
 * dicom-xnat-util: org.nrg.dcm.xnat.SOPHashDicomFileNamerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;
import org.nrg.xnat.Files;

import com.google.common.collect.ImmutableMap;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class SOPHashDicomFileNamerTest {
    private static final ImmutableMap<Integer,VR> vrs =
        new ImmutableMap.Builder<Integer,VR>()
        .put(Tag.StudyID, VR.SH)
        .put(Tag.Modality, VR.CS)
        .put(Tag.StudyDescription, VR.LO)
        .put(Tag.SeriesNumber, VR.IS)
        .put(Tag.InstanceNumber, VR.IS)
        .put(Tag.StudyDate, VR.DA)
        .put(Tag.StudyTime, VR.TM)
        .put(Tag.SOPClassUID, VR.UI)
        .put(Tag.SOPInstanceUID, VR.UI)
        .build();

    private static final ImmutableMap<Integer,String> values =
        new ImmutableMap.Builder<Integer,String>()
        .put(Tag.StudyID, "StudyID")
        .put(Tag.Modality, "OT")
        .put(Tag.StudyDescription, "Study Description")
        .put(Tag.SeriesNumber, "1")
        .put(Tag.InstanceNumber, "42")
        .put(Tag.StudyDate, "20101101")
        .put(Tag.StudyTime, "130130.12345")
        .put(Tag.SOPClassUID, "1.2.3.4.5")
        .put(Tag.SOPInstanceUID, "2.4.6.8.10")
        .build();

    private static final String hashString = Long.toString(0xffffffffL &
                                                           ("1.2.3.4.5".hashCode() + 37 * "2.4.6.8.10".hashCode()),
            36);


    @Test
    public void shouldWorkWithFullyPopulatedObject() {
        final DicomObject o = new BasicDicomObject();
        for (final Integer tag : values.keySet()) {
            o.putString(tag, vrs.get(tag), values.get(tag));
        }
        assertEquals("StudyID.OT.Study_Description.1.42.20101101.130130." + hashString + ".dcm",
                new SOPHashDicomFileNamer().makeFileName(o));
    }

    @Test
    public void shouldWorkWithMissingTags() {
        for (final Integer missingTag : values.keySet()) {
            final DicomObject o = new BasicDicomObject();
            for (final Integer tag : values.keySet()) {
                if (!tag.equals(missingTag)) {
                    o.putString(tag, vrs.get(tag), values.get(tag));
                }
            }
            final String name = new SOPHashDicomFileNamer().makeFileName(o);
            assertNotNull(name);
            assertFalse("".equals(name));
            assertEquals(name, Files.toFileNameChars(name));
        }
    }
}
