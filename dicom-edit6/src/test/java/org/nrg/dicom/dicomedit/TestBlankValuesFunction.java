package org.nrg.dicom.dicomedit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;

import static org.junit.Assert.*;
import static org.nrg.dicom.dicomedit.TestUtils.*;

public class TestBlankValuesFunction {

    @Test()
    public void testBlankList() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "AccessionNumber","");
        TestTag t2 = new TestTag( 0x00300102, "PatientName", "");
        TestTag t3 = new TestTag( 0x00300103, "AccessionNumber", "");
        TestTag t4 = new TestTag( 0x00300104, "Accession");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));

        final String script =
                "myValues := {\"PatientName\", \"AccessionNumber\"}\n" +
                        "blankValues[myValues]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
        assertEquals( t4.postValue, dobj.getString( t4.tag));
    }

    @Test()
    public void testBlankMultiArgs() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "AccessionNumber","");
        TestTag t2 = new TestTag( 0x00300102, "PatientName", "");
        TestTag t3 = new TestTag( 0x00300103, "AccessionNumber", "");
        TestTag t4 = new TestTag( 0x00300104, "Accession");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));

        final String script = "blankValues[\"PatientName\", \"AccessionNumber\"]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
        assertEquals( t4.postValue, dobj.getString( t4.tag));
    }

    @Test()
    public void testBlankIntegerArg() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "12345","");
        TestTag t2 = new TestTag( 0x00300102, "PatientName", "");
        TestTag t3 = new TestTag( 0x00300103, "12345", "");
        TestTag t4 = new TestTag( 0x00300104, "Accession");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));

        final String script = "blankValues[\"PatientName\", 12345]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
        assertEquals( t4.postValue, dobj.getString( t4.tag));
    }

    @Test()
    public void testBlankValuesInSequences() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "AccessionNumber","");
        TestTag t2 = new TestTag( 0x00300102, "PatientName", "");
        TestTag t3 = new TestTag( 0x00300103, "AccessionNumber", "");
        TestSeqTag t4_0_t1 = new TestSeqTag( new int[]{0x00420010,0,0x00360001}, "t4_0_t1");
        TestSeqTag t4_0_t2 = new TestSeqTag( new int[]{0x00420010,0,0x00360002}, "AccessionNumber", "");
        TestSeqTag t4_1_t1 = new TestSeqTag( new int[]{0x00420010,1,0x00360001}, "t4_1_t1");
        TestSeqTag t4_1_t2 = new TestSeqTag( new int[]{0x00420010,1,0x00360002}, "PatientName", "");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4_0_t1);
        put( dobj, t4_0_t2);
        put( dobj, t4_1_t1);
        put( dobj, t4_1_t2);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4_0_t1.initialValue, dobj.getString( t4_0_t1.tag));
        assertEquals( t4_0_t2.initialValue, dobj.getString( t4_0_t2.tag));
        assertEquals( t4_1_t1.initialValue, dobj.getString( t4_1_t1.tag));
        assertEquals( t4_1_t2.initialValue, dobj.getString( t4_1_t2.tag));

        final String script = "blankValues[\"PatientName\", \"AccessionNumber\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
        assertEquals( t4_0_t1.postValue, dobj.getString( t4_0_t1.tag));
        assertTrue( StringUtils.isEmpty( dobj.getString( t4_0_t2.tag)));
        assertEquals( t4_1_t1.postValue, dobj.getString( t4_1_t1.tag));
        assertTrue( StringUtils.isEmpty( dobj.getString( t4_1_t2.tag)));
    }

    @Test()
    public void testWildcard() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "AccessionNumber","");
        TestTag t2 = new TestTag( 0x00089201, "PatientName", "");
        TestTag t3 = new TestTag( 0x00089203, "AccessionNumber", "");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));

        final String script = "blankValues[(0008,920X)]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
    }

    @Test()
    public void testMixedArgTypes() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "PatientName","");
        TestTag t1 = new TestTag( 0x00080050, "AccessionNumber","");
        TestTag t2 = new TestTag( 0x00089201, "PatientName", "");
        TestTag t3 = new TestTag( 0x00089203, "AccessionNumber", "");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));

        final String script = "snafu := 666\n"
        + "blankValues[(0008,920X), \"foo\", snafu, 13]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t1.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t2.tag)));
        assertTrue( StringUtils.isEmpty( dobj.getString( t3.tag)));
    }

    @Test
    public void testVariableArgWithString() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00189020, "SomeValue","");

        put( dobj, t0);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));

        final String script = "tag_to_remove := \"SomeValue\"\n" +
                "blankValues[tag_to_remove]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
    }

    @Test
    public void testVariableArgWithTagPath() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00189020, "SomeValue","");

        put( dobj, t0);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));

        final String script = "tag_to_remove := (0018,9020)\n" +
                "blankValues[tag_to_remove]";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( StringUtils.isEmpty( dobj.getString( t0.tag)));
    }

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File watermelon = _resourceManager.getTestResourceFile("dicom/IM_0001");

}
