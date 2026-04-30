package org.nrg.dicom.mizer.objects;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Dcm4cheConvert {

    private Dcm4cheConvert() {

    }

    public static DicomObject toDcm4che2DicomObject(Attributes attrs) throws Exception {
        String sopClassUID = attrs.getString(Tag.SOPClassUID);
        String sopInstanceUID = attrs.getString(Tag.SOPInstanceUID);

        if (sopClassUID == null) {
            attrs.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        }
        if (sopInstanceUID == null) {
            attrs.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        }

        // Step 1: Serialize dcm4che3 Attributes into byte[]
        if (attrs.getString(Tag.TransferSyntaxUID) == null) {
            attrs.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DicomOutputStream dos = new DicomOutputStream(baos, UID.ExplicitVRLittleEndian)) {
            SplitAttributes split = splitFmiAndDataset(attrs);
            dos.writeDataset(split.fmi, split.onlyDataset);
            attrs.addAll(split.fmi);
        }

        // Step 2: Read back as dcm4che2 DicomObject, Not include pixelData
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (org.dcm4che2.io.DicomInputStream dis = new org.dcm4che2.io.DicomInputStream(bais)) {
            dis.setHandler(new StopTagInputHandler(Tag.PixelData));
            return dis.readDicomObject();
        }
    }

    public static Attributes toDcm4che3Attributes(DicomObject dicomObject) throws Exception {
        String sopClassUID = dicomObject.getString(org.dcm4che2.data.Tag.SOPClassUID);
        String sopInstanceUID = dicomObject.getString(org.dcm4che2.data.Tag.SOPInstanceUID);
        if (sopClassUID == null) {
            sopClassUID = UID.SecondaryCaptureImageStorage;
            dicomObject.putString(org.dcm4che2.data.Tag.SOPClassUID, org.dcm4che2.data.VR.UI, sopClassUID);
        }
        if (sopInstanceUID == null) {
            sopInstanceUID = UIDUtils.createUID();
            dicomObject.putString(org.dcm4che2.data.Tag.SOPInstanceUID, org.dcm4che2.data.VR.UI, sopInstanceUID);
        }

        dicomObject.initFileMetaInformation(UID.ImplicitVRLittleEndian);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (org.dcm4che2.io.DicomOutputStream dos = new org.dcm4che2.io.DicomOutputStream(baos)) {
            dos.writeDicomFile(dicomObject);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (DicomInputStream dis = new DicomInputStream(bais)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            Attributes fmi = dis.readFileMetaInformation();
            Attributes dataset = dis.readDataset();
            if (fmi != null) {
                dataset.addAll(fmi);
            }
            return dataset;
        }
    }

    public static byte[] getNestedBytes(Attributes root, int[] tagPath) {
        Attributes current = getNestedAttribute(root, tagPath);
        if (current == null) {
            return new byte[0];
        }
        int finalTag = tagPath[tagPath.length - 1];
        try {
            return current.getBytes(finalTag);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static String[] getNestedStrings(Attributes root, int[] tagPath) {
        Attributes current = getNestedAttribute(root, tagPath);
        if (current == null) {
            return new String[0];
        }
        int finalTag = tagPath[tagPath.length - 1];
        return current.getStrings(finalTag);
    }


    private static boolean isItemIndex(int tag) {
        return tag < 0x00080000;
    }

    public static void setNestedString(Attributes root, int[] tagPath, VR vr, String...values) {
        if (tagPath.length < 1) return;

        Attributes current = getOrCreateAttributes(root, tagPath);

        int finalTag = tagPath[tagPath.length - 1];
        current.setString(finalTag, vr, values);
    }

    public static void setNestedBytes(Attributes attrs, int[] tagPath, VR vr, byte[] value) {
        Attributes current = getOrCreateAttributes(attrs, tagPath);
        current.setBytes(tagPath[tagPath.length - 1], vr, value);
    }

    public static void removeNestedTag(Attributes attrs, int[] tagPath) {
        if (tagPath == null || tagPath.length < 1) return;

        Attributes current = attrs;
        for (int i = 0; i < tagPath.length - 2; i += 2) {
            int sequenceTag = tagPath[i];
            int itemIndex = tagPath[i + 1];
            Sequence seq = current.getSequence(sequenceTag);
            if (seq == null || seq.size() <= itemIndex) return;
            current = seq.get(itemIndex);
        }

        current.remove(tagPath[tagPath.length - 1]);
    }

    public static Attributes getOrCreateAttributes(Attributes root, int[] tagPath) {
        Attributes current = root;
        for (int i = 0; i < tagPath.length - 1; i++) {
            int tag = tagPath[i];

            if ((i + 1) < tagPath.length && isItemIndex(tagPath[i + 1])) {
                int itemIndex = tagPath[i + 1];
                Sequence seq = current.getSequence(tag);
                if (seq == null) {
                    seq = current.newSequence(tag, itemIndex + 1);
                }
                while (seq.size() <= itemIndex) {
                    seq.add(new Attributes());
                }
                current = seq.get(itemIndex);
                i++;
            } else {
                throw new IllegalArgumentException("The path contains non-sequence levels and the tagPath cannot be resolved.");
            }
        }
        return current;
    }

    public static Attributes getNestedAttribute(Attributes attrs, int[] tagPath) {
        Attributes current = attrs;
        for (int i = 0; i < tagPath.length - 1; i++) {
            int tag = tagPath[i];

            Sequence seq = current.getSequence(tag);
            if (seq == null || seq.isEmpty()) {
                return null;
            }

            i++;
            if (tagPath[i] >= seq.size()) {
                return null;
            }
            current = seq.get(tagPath[i]);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public static class SplitAttributes {
        public final Attributes fmi;
        public final Attributes onlyDataset;

        public SplitAttributes(Attributes fmi, Attributes onlyDataset) {
            this.fmi = fmi;
            this.onlyDataset = onlyDataset;
        }
    }

    public static SplitAttributes splitFmiAndDataset(Attributes dataset) {
        Attributes fmi = new Attributes();
        Attributes onlyDataset = dataset;

        for (int tag : dataset.tags()) {
            int group = tag >>> 16;
            VR vr = dataset.getVR(tag);
            Object value = dataset.getValue(tag);

            if (group == 0x0002) {
                fmi.setValue(tag, vr, value);
                onlyDataset.remove(tag);
            }
        }
        if (fmi.isEmpty()) {
            fmi = dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian);
        }
        fmi.setString(0x00020002, VR.UI, dataset.getString(0x00080016));
        fmi.setString(0x00020003, VR.UI, dataset.getString(0x00080018));

        return new SplitAttributes(fmi, onlyDataset);
    }

}