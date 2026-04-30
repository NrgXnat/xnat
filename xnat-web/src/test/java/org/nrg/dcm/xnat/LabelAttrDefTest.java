/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.LabelAttrDefTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.DicomAttributeIndex;

import static org.nrg.dcm.NamedAttributes.StudyDescription;

import junit.framework.TestCase;


public class LabelAttrDefTest extends TestCase {
    private String getValText(final XnatAttrDef def, Map<DicomAttributeIndex,String> m) throws ExtAttrException {
        final Iterable<ExtAttrValue> vs = AbstractExtAttrDef.foldl(def, Collections.singletonList(m));
        final Iterator<ExtAttrValue> vsi = vs.iterator();
        assertTrue(vsi.hasNext());
        final ExtAttrValue v = vsi.next();
        assertFalse(vsi.hasNext());
        return v.getText();
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.LabelAttrDef#convertText(java.util.Map)}.
     */
    public void testConvertTextMapOfIntegerString() throws Exception {
        final XnatAttrDef def = LabelAttrDef.wrap(new XnatAttrDef.Text("test", StudyDescription));

        assertEquals("value_with_underscores", getValText(def, Collections.singletonMap(StudyDescription, "value_with_underscores")));
        assertEquals("value-with-hyphens", getValText(def, Collections.singletonMap(StudyDescription, "value-with-hyphens")));
        assertEquals("mixed-value_01", getValText(def, Collections.singletonMap(StudyDescription, "mixed-value_01")));
        assertEquals("____remap____invalid____chars____", getValText(def,
                Collections.singletonMap(StudyDescription, "!@#$remap%^&*invalid()+=chars{}[]")));
    }
}
