package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.nrg.dicom.dicomedit.TestUtils.*;

public class TestNormalizeStringFunction {
    @Test
    public void basicTagsList() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00080080, "Søme Institution", "S_me Institution");
        TestTag t2 = new TestTag(0x00100010, "Påti´ntName", "P_ti_ntName");

        put(dobj, t1);
        put(dobj, t2);

        assertEquals(t1.initialValue, dobj.getString(t1.tag));
        assertEquals(t2.initialValue, dobj.getString(t2.tag));

        String script = "(0008,0080) := normalizeString[(0008,0080)]\n"
                + "(0010,0010) := normalizeString[(0010,0010)]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(t1.postValue, dobj.getString(t1.tag));
        assertEquals(t2.postValue, dobj.getString(t2.tag));
    }

    @Test
    public void suppliedReplacementString() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00080080, "Søme Institution", "S_foo_me Institution");
        TestTag t2 = new TestTag(0x00100010, "Påti´ntName", "P_ti_ntName");

        put(dobj, t1);
        put(dobj, t2);

        assertEquals(t1.initialValue, dobj.getString(t1.tag));
        assertEquals(t2.initialValue, dobj.getString(t2.tag));

        String script = "(0008,0080) := normalizeString[(0008,0080), \"_foo_\"]\n"
                + "(0010,0010) := normalizeString[(0010,0010)]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(t1.postValue, dobj.getString(t1.tag));
        assertEquals(t2.postValue, dobj.getString(t2.tag));
    }

    /**
     * Does not insert the tag if the value is null.
     *
     * @throws MizerException
     */
    @Test
    public void missingTag() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00080080, "Some Institution");

//        put( dobj, t1);

        assertFalse(dobj.contains(t1.tag));

        String script = "(0008,0080) := normalizeString[(0008,0080)]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse(dobj.contains(t1.tag));
    }

}
