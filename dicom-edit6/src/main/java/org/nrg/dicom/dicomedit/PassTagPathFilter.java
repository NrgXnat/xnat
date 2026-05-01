package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.tags.TagPath;

public class PassTagPathFilter implements TagPathFilter {
    public boolean allow(TagPath tagPath) {
        return true;
    }

    public boolean deny(TagPath tagPath) {
        return false;
    }
}
