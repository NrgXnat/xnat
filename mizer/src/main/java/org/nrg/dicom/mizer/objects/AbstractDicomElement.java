package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;

import java.io.IOException;
import java.util.List;

public abstract class AbstractDicomElement implements DicomElementI {
    protected final Attributes attrs;
    protected final int tag;
    protected final VR vr;

    public AbstractDicomElement(Attributes attrs, int tag) {
        this.attrs = attrs;
        this.tag = tag;
        this.vr = attrs.getVR(tag);
    }

    @Override
    public int tag() {
        return tag;
    }

    @Override
    public byte[] getBytes() {
        try {
            return this.attrs.getBytes(tag);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public String tagString() {
        return Integer.toHexString(tag);
    }

    @Override
    public boolean hasItems() {
        VR vr = attrs.getVR(tag);
        if (vr != VR.SQ) {
            return false;
        }
        Sequence seq = attrs.getSequence(tag);
        return seq != null && !seq.isEmpty();
    }

    @Override
    public int countItems() {
        VR vr = attrs.getVR(tag);
        if (vr != VR.SQ) {
            return 0;
        }
        Sequence seq = attrs.getSequence(tag);
        return seq != null ? seq.size() : 0;
    }

    @Override
    public DicomObjectI getDicomObject(int i) {
        List<Attributes> sequence = attrs.getSequence(tag);
        if (sequence != null && i < sequence.size()) {
            return new DicomObjectFactory.MizerDicomObject(sequence.get(i));
        }
        return null;
    }

    @Override
    public void removeItem(int i) {
        List<Attributes> sequence = attrs.getSequence(tag);
        if (sequence != null && i < sequence.size()) {
            sequence.remove(i);
        }
    }

    @Override
    public boolean isUID() {
        if (this.vr != null) {
            return vr == VR.UI;
        }
        return VR.UI == ElementDictionary.getStandardElementDictionary().vrOf(tag);
    }

    @Override
    public String getVRAsString() {
        if (this.vr != null) {
            return this.vr.toString();
        }
        VR vr2 = ElementDictionary.getStandardElementDictionary().vrOf(tag);
        return vr2 != null ? vr2.toString() : "UN";
    }

    @Override
    public VR getVR() {
        return ElementDictionary.getStandardElementDictionary().vrOf(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueAsString() {
        if (VR.SQ.equals(vr)) {
            return null;
        }
        if ("UN".equals(getVRAsString())) {
            return attrs.getString(tag);
        }
        final String[] values = attrs.getStrings(tag);
        return null == values ? null : String.join("\\", values);
     }

    @Override
    public String[] getStrings() {
        String[] result = attrs.getStrings(tag);
        return result == null? new String[0]: result;
    }

    @Override
    public SpecificCharacterSet getSpecificCharacterSet() {
        return this.attrs.getSpecificCharacterSet();
    }
}
