package org.nrg.dicom.mizer;

import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.VR;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static class TestTag {
        public int tag;
        public String initialValue, postValue;
        public String encodedValue;

        public TestTag(int tag, String initialValue, String postValue, String encodedValue) {
            this.tag = tag;
            this.initialValue = initialValue;
            this.postValue = postValue;
            this.encodedValue = encodedValue;
        }

        public TestTag(int tag, String initialValue, String postValue) {
            this(tag, initialValue, postValue, null);
        }

        public TestTag(int tag, String value) {
            this(tag, value, value);
        }

        public String valueFrom(DicomObjectI dobj) {
            return dobj.getString(tag);
        }
    }

    public static class TestTagVM {
        public int tag;
        public String[] initialValues, postValues;

        public TestTagVM(int tag, String[] initialValues, String[] postValues) {
            this.tag = tag;
            this.initialValues = initialValues;
            this.postValues = postValues;
        }

        public TestTagVM(int tag, String[] values) {
            this(tag, values, values);
        }

        public String[] valueFrom(DicomObjectI dobj) {
            return dobj.getStrings(tag);
        }
    }

    public static class TestSeqTag {
        public int[] tag;
        public String initialValue, postValue;

        public TestSeqTag(int[] tag, String initialValue, String postValue) {
            this.tag = tag;
            this.initialValue = initialValue;
            this.postValue = postValue;
        }

        public TestSeqTag(int[] tag, String value) {
            this(tag, value, value);
        }

        public String valueFrom(DicomObjectI dobj) {
            return dobj.getString(tag);
        }
    }

    public static class TestSeqTagVM {
        public int[] tag;
        public String[] initialValues, postValues;

        public TestSeqTagVM(int[] tag, String[] initialValues, String[] postValues) {
            this.tag = tag;
            this.initialValues = initialValues;
            this.postValues = postValues;
        }

        public TestSeqTagVM(int[] tag, String[] values) {
            this(tag, values, values);
        }

        public String[] valueFrom(DicomObjectI dobj) {
            return dobj.getStrings(tag);
        }
    }

    public static void put(DicomObjectI dobj, TestTag tag) {
        dobj.putString(tag.tag, tag.initialValue);
    }

    public static void put(DicomObjectI dobj, TestTagVM tag) {
        dobj.putStrings(tag.tag, tag.initialValues);
    }

    public static void put(DicomObjectI dobj, TestSeqTag tag) {
        if (isEven(tag.tag.length)) {
            createEmptyItem(tag.tag, dobj);
        } else {
            dobj.putString(tag.tag, tag.initialValue);
        }
    }

    private static void createEmptyItem(int[] tagArray, DicomObjectI dobj) {
        if (isEven(tagArray.length)) {
            int[] oddArray = new int[tagArray.length + 1];
            System.arraycopy(tagArray, 0, oddArray, 0, tagArray.length);
            oddArray[oddArray.length - 1] = 0x00100010;
            dobj.putString(oddArray, "ignored");
            dobj.delete(oddArray);
        }
    }

    private static boolean isEven(int i) {
        return (i | 1) > i;
    }

    public static void put(DicomObjectI dobj, TestSeqTagVM tag) {
        dobj.putStrings(tag.tag, tag.initialValues);
    }

    public static String encodeHex(String string) {
        return VR.OB.toString(string.getBytes(StandardCharsets.UTF_8), false, new SpecificCharacterSet("ISO 8859-1"));
    }

    public static String encodeHex(byte[] bytes) {
        return VR.OB.toString(bytes, false, new SpecificCharacterSet("ISO 8859-1"));
    }

}
