package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;

import java.io.IOException;

public class Dcm4cheConvert {

    private Dcm4cheConvert() {

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

    /**
     * The two halves produced by {@link #extractFmiFromDataset(Attributes)}: the file meta information and the
     * dataset it was taken out of. {@link #onlyDataset} is the very instance that was passed in, not a copy.
     */
    public static class SplitAttributes {
        public final Attributes fmi;
        public final Attributes onlyDataset;

        public SplitAttributes(Attributes fmi, Attributes onlyDataset) {
            this.fmi = fmi;
            this.onlyDataset = onlyDataset;
        }
    }

    /**
     * Moves the file meta information (every group 0002 element) out of {@code dataset} into a separate
     * {@link Attributes} so the two can be handed to
     * {@link org.dcm4che3.io.DicomOutputStream#writeDataset(Attributes, Attributes)}, which requires them apart.
     *
     * <p><strong>This method mutates its argument.</strong> On return, {@code dataset} no longer contains any
     * group 0002 element, and {@link SplitAttributes#onlyDataset} is that same instance rather than a copy.
     * Callers that need the FMI in the dataset afterwards must restore it themselves, typically with
     * {@code dataset.addAll(split.fmi)} once the write has finished. The dataset is not copied on purpose:
     * this runs once per received instance on the DICOM ingest path and pixel data would otherwise be duplicated.
     *
     * <p>If {@code dataset} carries no group 0002 elements, an Explicit VR Little Endian FMI is created for it.
     * In both cases Media Storage SOP Class UID (0002,0002) and Media Storage SOP Instance UID (0002,0003) are
     * set from the dataset's SOP Class UID and SOP Instance UID.
     */
    public static SplitAttributes extractFmiFromDataset(Attributes dataset) {
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

    /**
     * @deprecated The name hid the fact that the input is modified in place. Use
     * {@link #extractFmiFromDataset(Attributes)}, which does exactly the same thing and documents it.
     * Scheduled for removal after 1.10.2.
     */
    @Deprecated
    public static SplitAttributes splitFmiAndDataset(Attributes dataset) {
        return extractFmiFromDataset(dataset);
    }

    /**
     * Merges the file meta information into the dataset, so processors and scripts see a complete
     * object, synthesizing the meta group from the dataset first when the source carried none -- a
     * DIMSE stream has no meta group -- or what it carried names no transfer syntax.
     *
     * @param dataset        the dataset to merge into.
     * @param fmi            the file meta information the source carried, or null.
     * @param transferSyntax the transfer syntax to synthesize a meta group with when needed.
     *
     * @return the meta group that was merged.
     */
    public static Attributes mergeFileMetaInformation(final Attributes dataset, final Attributes fmi, final String transferSyntax) {
        final Attributes merged = fmi == null || !fmi.contains(Tag.TransferSyntaxUID)
                                  ? dataset.createFileMetaInformation(transferSyntax)
                                  : fmi;
        dataset.addAll(merged);
        return merged;
    }

}