package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.*;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFindKnownValueVisitor {

    @Test()
    public void testWildcardAndSequenceTags() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00100010, "t0");
        TestTag t1 = new TestTag( 0x00080050, "t1");
        TestTag t2 = new TestTag( 0x00300102, "t2");
        TestTag t3 = new TestTag( 0x00300103, "t3");
        TestTag t4 = new TestTag( 0x00100020, "t4");

        TestSeqTag s1_0_1 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "s1_0_1");
        TestSeqTag s1_0_2 = new TestSeqTag(new int[]{0x00420010,0,0x00100020}, "s1_0_2");
        TestSeqTag s1_1_1 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "s1_1_1");
        TestSeqTag s1_1_2 = new TestSeqTag(new int[]{0x00420010,1,0x00100020}, "s1_1_2");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);
        put( dobj, s1_0_1);
        put( dobj, s1_0_2);
        put( dobj, s1_1_1);
        put( dobj, s1_1_2);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));
        assertEquals( s1_0_1.initialValue, dobj.getString( s1_0_1.tag));
        assertEquals( s1_0_2.initialValue, dobj.getString( s1_0_2.tag));
        assertEquals( s1_1_1.initialValue, dobj.getString( s1_1_1.tag));
        assertEquals( s1_1_2.initialValue, dobj.getString( s1_1_2.tag));

        Map<TagPath, String> expectedKnownValueMap = new HashMap<>();
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0010,0010)"), t0.initialValue );
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0008,0050)"), t1.initialValue );
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0030,0102)"), t2.initialValue );
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0030,0103)"), t3.initialValue );
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0042,0010)[0]/(0010,0010)"), s1_0_1.initialValue );
        expectedKnownValueMap.put(TagPathFactory.createDE6Instance("(0042,0010)[1]/(0010,0010)"), s1_1_1.initialValue );

        List<TagPath> specifiedTagPaths = Arrays.asList(
                TagPathFactory.createDE6Instance("(0010,0010)"),
                TagPathFactory.createDE6Instance("(0008,0050)"),
                TagPathFactory.createDE6Instance("(0030,010X)"),
                TagPathFactory.createDE6Instance("(0042,0010)/(0010,0010)")
        );
        FindKnownValueVisitor visitor = new FindKnownValueVisitor(specifiedTagPaths);
        Map<TagPath,String> actualKnownValueMap = visitor.getKnownValues(dobj);

        assertTrue( areEqual( expectedKnownValueMap, actualKnownValueMap));
    }

    private boolean areEqual(Map<TagPath, String> first, Map<TagPath, String> second) {
        if (first.size() != second.size()) {
            return false;
        }

        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }
}
