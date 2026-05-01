/*
 * DicomEdit: org.nrg.dcm.edit.TagPatternTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.junit.Test;

import com.google.common.collect.Lists;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.test.workers.resources.ResourceManager;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class TagPatternTest {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File            _sample          = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Test
    public void testExact() {
        final TagPattern tp = new TagPattern(0x00100020);
        assertTrue(tp.apply(0x00100020));
        assertFalse(tp.apply(0x00100000));
        assertFalse(tp.apply(0x00000020));
    }

    @Test
    public void testApplyInt() throws ScriptEvaluationException {
        final TagPattern tp1 = new TagPattern("XXX#,XXXX");
        assertTrue(tp1.apply(0x00010000));
        assertTrue(tp1.apply(0x00010001));
        assertFalse(tp1.apply(0x00000000));
        assertFalse(tp1.apply(0x00000001));
        assertTrue(tp1.apply(0x00190010));
        assertFalse(tp1.apply(0x00080005));

        final TagPattern tp2 = new TagPattern("001@,XXXX");
        assertTrue(tp2.apply(0x00101234));
        assertTrue(tp2.apply(0x00120000));
        assertTrue(tp2.apply(0x0014ffff));
        assertTrue(tp2.apply(0x0016fffe));
        assertTrue(tp2.apply(0x001e0101));
        assertFalse(tp2.apply(0x00110000));
        assertFalse(tp2.apply(0x10101234));
        assertFalse(tp2.apply(0x00200000));

        final TagPattern tp3 = new TagPattern("FFFC,FFFC");
        assertTrue(tp3.apply(0xfffcfffc));
    }

    @Test
    public void testApplyToNullInt() throws ScriptEvaluationException {
        final TagPattern tp = new TagPattern("1234,5678");
        assertFalse(tp.apply((Integer) null));
    }

    @Test
    public void testApplyDicomObject() throws ScriptEvaluationException, IOException {
        final DicomObject o   = DicomUtils.read(_sample);
        final TagPattern  tp1 = new TagPattern("0010,0020");    // extant tag
        assertEquals(Lists.newArrayList(0x00100020L), tp1.apply(o));

        final TagPattern tp2 = new TagPattern("0010,0028");    // nonexistent tag
        assertEquals(Lists.newArrayList(0x00100028L), tp2.apply(o));

        final TagPattern tp3 = new TagPattern("0010,00@0");    // a few even tags
        assertEquals(Lists.newArrayList(0x00100020L, 0x00100040L),
                     tp3.apply(o));

        final TagPattern tp4 = new TagPattern("0010,00#0");    // a few odd tags
        assertEquals(Lists.newArrayList(0x00100010L, 0x00100030L),
                     tp4.apply(o));

    }

    @Test
    public void testGetTopTag() throws ScriptEvaluationException {
        final TagPattern tp1 = new TagPattern("0010,0020");    // exact tag
        assertEquals(0x00100020, tp1.getTopTag());
        final TagPattern tp2 = new TagPattern("0010,00@0");    // even tag
        assertEquals(0x001000e0, tp2.getTopTag());
        final TagPattern tp3 = new TagPattern("0010,00#0");    // odd tag
        assertEquals(0x001000f0, tp3.getTopTag());
        final TagPattern tp4 = new TagPattern("00X0,00#0");    // any+even tag
        assertEquals(0x00f000f0, tp4.getTopTag());
        final TagPattern tp5 = new TagPattern("XXX#,XXXX"); // private tag
        final long       top = tp5.getTopTag();
        assertEquals(0xffffffffL, top);
    }
}
