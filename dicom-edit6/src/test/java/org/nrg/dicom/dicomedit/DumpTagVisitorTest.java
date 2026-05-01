package org.nrg.dicom.dicomedit;

import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class DumpTagVisitorTest {

    @Test
    public void test() {
        TestTag p1 = new TestTag(0x00090012, "p1");
        TestSeqTag s1 = new TestSeqTag(new int[]{0x00091200, 0, 0x00230020}, "s1");
        TestSeqTag s1_s1 = new TestSeqTag(new int[]{0x00091200, 0, 0x000232015, 0, 0x00230020}, "s1_s1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString(p1.tag, p1.value);
        dobj.putString(s1.tag, s1.value);
        dobj.putString(s1_s1.tag, s1_s1.value);

        DumpDicomTagVisitor dumpDicomTagVisitor = new DumpDicomTagVisitor(System.out);
        dumpDicomTagVisitor.visit(dobj);

        assertTrue(true);

    }

    public class TestTag {
        int tag;
        String value;

        public TestTag(int tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public String valueFrom(DicomObjectI dobj) {
            return dobj.getString(tag);
        }
    }

    public class TestSeqTag {
        int[] tag;
        String value;

        public TestSeqTag(int[] tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public String valueFrom(DicomObjectI dobj) {
            return dobj.getString(tag);
        }
    }

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File de11 = _resourceManager.getTestResourceFile("dicom/IM_0001");

    /**
     * Useful tool for dumping a file and seeing tags as int[].
     *
     * @throws MizerException
     * @throws IOException
     */
    @Test
    @Ignore
    public void dumpOne() throws MizerException, IOException {
        final DicomObjectI dobj = DicomObjectFactory.newInstance(de11);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try (PrintStream os = new PrintStream( "/tmp/dumpone.txt")) {
        try (PrintStream os = new PrintStream(baos)) {
            DicomObjectTagVisitor visitor = new DumpDicomPvtTagVisitor(os);
            visitor.visit(dobj);
        }
        baos.toString().split("\\R");
    }
}