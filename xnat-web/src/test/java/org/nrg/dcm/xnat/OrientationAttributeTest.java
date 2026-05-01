/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.OrientationAttributeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.junit.Assert.*;
import static org.nrg.dcm.DicomAttributes.IMAGE_ORIENTATION_PATIENT;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.DicomAttributeIndex;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class OrientationAttributeTest {
    private String getValText(final XnatAttrDef def, Map<DicomAttributeIndex,String> m) throws ExtAttrException {
        final Iterable<ExtAttrValue> vs = AbstractExtAttrDef.foldl(def, Collections.singletonList(m));
        final Iterator<ExtAttrValue> vsi = vs.iterator();
        assertTrue(vsi.hasNext());
        final ExtAttrValue v = vsi.next();
        assertFalse(vsi.hasNext());
        return v.getText();
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.OrientationAttribute#convertText(java.util.Map)}.
     */
    @Test
    public void testConvertTextMapOfDicomAttributeIndexString() throws Exception {
        final XnatAttrDef orientDef = new OrientationAttribute("orientation");
        assertEquals("Sag", getValText(orientDef, Collections.singletonMap(IMAGE_ORIENTATION_PATIENT, "0\\1\\0\\0\\0\\1")));
        assertEquals("Cor", getValText(orientDef, Collections.singletonMap(IMAGE_ORIENTATION_PATIENT, "0\\0\\1\\1\\0\\0")));
        assertEquals("Tra", getValText(orientDef, Collections.singletonMap(IMAGE_ORIENTATION_PATIENT, "1\\0\\0\\0\\1\\0")));
    }
}
