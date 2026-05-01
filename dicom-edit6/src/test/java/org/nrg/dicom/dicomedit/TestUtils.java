package org.nrg.dicom.dicomedit;

import org.nrg.dicom.dicomedit.pixeledit.impl.PixelEditTestParams;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;

import java.io.*;
import java.net.URISyntaxException;

public class TestUtils {
    public static class TestTag {
        int tag;
        String initialValue, postValue;
        public TestTag( int tag, String initialValue, String postValue) {
            this.tag = tag;
            this.initialValue = initialValue;
            this.postValue = postValue;
        }
        public TestTag( int tag, String value) {
            this( tag, value, value);
        }
        public String valueFrom( DicomObjectI dobj) {
            return dobj.getString( tag);
        }
    }
    public static class TestSeqTag {
        int[] tag;
        String initialValue, postValue;
        public TestSeqTag( int[] tag, String initialValue, String postValue) {
            this.tag = tag;
            this.initialValue = initialValue;
            this.postValue = postValue;
        }
        public TestSeqTag( int[] tag, String value) {
            this(tag, value, value);
        }
        public String valueFrom( DicomObjectI dobj) {
            return dobj.getString( tag);
        }
    }

    public static void put( DicomObjectI dobj, TestTag tag) {
        dobj.putString( tag.tag, tag.initialValue);
    }

    public static void put( DicomObjectI dobj, TestSeqTag tag) {
        dobj.putString( tag.tag, tag.initialValue);
    }

    public static ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    public static InputStream resourceFileInputStream(final String resourceFile) throws IOException, URISyntaxException {
        ClassLoader loader = PixelEditTestParams.class.getClassLoader();
        File file = new File(loader.getResource(resourceFile).toURI());
        return new FileInputStream( file);
    }

}
