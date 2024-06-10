package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.Extractor;
import org.nrg.framework.utilities.SortedSets;

import java.util.SortedSet;

/**
 * Returns the assigned subject for all DICOM objects.
 */
public class FixedSubjectExtractor implements Extractor {
    public FixedSubjectExtractor(final String subjectLabel) {
        _subjectLabel = subjectLabel;
    }

    @Override
    public String extract(final DicomObject o) {
        return _subjectLabel;
    }

    @Override
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(Tag.PatientID);
    }

    private final String _subjectLabel;
}
