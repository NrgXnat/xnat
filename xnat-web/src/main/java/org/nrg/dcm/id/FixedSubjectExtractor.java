package org.nrg.dcm.id;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(final Attributes unused) {
        return _subjectLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        return SortedSets.singleton(Tag.PatientID);
    }

    private final String _subjectLabel;
}
