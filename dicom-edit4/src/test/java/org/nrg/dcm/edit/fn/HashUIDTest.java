/*
 * DicomEdit: org.nrg.dcm.edit.fn.HashUIDTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.util.UIDUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.values.SingleTagValue;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.UIDValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class HashUIDTest {
    private static final File DICOM_FILE = ResourceManager.getInstance().getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Test
    public void testApplyValidity() throws ScriptEvaluationException {
        final Value  value    = new ConstantValue("foo");
        final Value  uidValue = new HashUID().apply(singletonList(value));
        final String uid      = uidValue.on(Collections.<Integer, String>emptyMap());
        assertTrue(UIDUtils.isValid(uid));
        assertTrue(uid.startsWith("2.25."));
    }

    @Test
    public void testApplyReproducibility() throws ScriptEvaluationException {
        final Value  value1    = new ConstantValue("bar");
        final Value  value2    = new ConstantValue("bar");
        final Value  uidValue1 = new HashUID().apply(singletonList(value1));
        final Value  uidValue2 = new HashUID().apply(singletonList(value2));
        final String uid1      = uidValue1.on(Collections.<Integer, String>emptyMap());
        final String uid2      = uidValue2.on(Collections.<Integer, String>emptyMap());
        assertEquals(uid1, uid2);
    }

    @Test
    public void testApplyUniqueness() throws ScriptEvaluationException {
        final List<? extends Value> values = Lists.newArrayList(new ConstantValue(UID.MRImageStorage),
                                                                new ConstantValue(UID.CTImageStorage),
                                                                new ConstantValue(UID.ExplicitVRBigEndian),
                                                                new ConstantValue(UID.JPEG2000));
        final List<String> uids = Lists.transform(values, new Function<Value, String>() {
            public String apply(final Value value) {
                try {
                    return new HashUID().apply(singletonList(value)).on(Collections.<Integer, String>emptyMap());
                } catch (ScriptEvaluationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertEquals(values.size(), uids.size());
        assertEquals(uids.size(), Sets.newLinkedHashSet(uids).size());
    }

    @Test
    public void testApplyToEmptyString() throws ScriptEvaluationException {
        final Value  value    = new ConstantValue("");
        final Value  uidValue = new HashUID().apply(singletonList(value));
        final String uid      = uidValue.on(Collections.<Integer, String>emptyMap());
        assertTrue(UIDUtils.isValid(uid));
        assertTrue(uid.startsWith("2.25."));
    }

    @Test
    public void testApplyToDicomObject() throws IOException, MizerException {
        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(DICOM_FILE);
        final Value        value1      = new SingleTagValue(Tag.StudyInstanceUID);
        final Value        value2      = new SingleTagValue(Tag.SeriesInstanceUID);
        final Value        value3      = new SingleTagValue(Tag.SOPInstanceUID);
        final Value        uidValue1   = new HashUID().apply(singletonList(value1));
        final Value        uidValue2   = new HashUID().apply(singletonList(value2));
        final Value        uidValue3   = new HashUID().apply(singletonList(value3));
        final String       uid1        = uidValue1.on(dicomObject);
        final String       uid2        = uidValue2.on(dicomObject);
        final String       uid3        = uidValue3.on(dicomObject);
        assertTrue(UIDUtils.isValid(uid1));
        assertTrue(UIDUtils.isValid(uid2));
        assertTrue(UIDUtils.isValid(uid3));
        assertFalse(uid1.equals(uid2));
        assertFalse(uid2.equals(uid3));
        assertFalse(uid3.equals(uid1));
    }

    @Test
    public void testApplyToNullValue() throws ScriptEvaluationException {
        final Value value    = new UIDValue(new ConstantValue(null));
        final Value uidValue = new HashUID().apply(singletonList(value));
        assertNotNull(uidValue);
        final String uid = uidValue.on(Collections.<Integer, String>emptyMap());
        assertNull(uid);
    }
}
