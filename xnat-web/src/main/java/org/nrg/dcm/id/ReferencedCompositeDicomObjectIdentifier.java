/*
 * web: org.nrg.dcm.id.ReferencedCompositeDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;

public class ReferencedCompositeDicomObjectIdentifier extends CompositeDicomObjectIdentifier {
    public ReferencedCompositeDicomObjectIdentifier(final DicomProjectIdentifier projectID,
                                                    final Extractor subjectExtractor,
                                                    final Extractor sessionExtractor,
                                                    final Extractor aaExtractor) {
        this(null, projectID, subjectExtractor, sessionExtractor, aaExtractor);
    }

    public ReferencedCompositeDicomObjectIdentifier(final String name,
                                                    final DicomProjectIdentifier projectID,
                                                    final Extractor subjectExtractor,
                                                    final Extractor sessionExtractor,
                                                    final Extractor aaExtractor) {
        super(name, projectID, subjectExtractor, sessionExtractor, aaExtractor);

        _subjectExtractor = subjectExtractor;
        _sessionExtractor = sessionExtractor;

        referenceExtractors(subjectExtractor);
        referenceExtractors(sessionExtractor);
        referenceExtractors(aaExtractor);
    }

    private void referenceExtractors(final Extractor extractor) {
        if (extractor instanceof IdentifierReferencingExtractor) {
            ((IdentifierReferencingExtractor) extractor).setIdentifier(this);
        }
        if (extractor instanceof SubjectReferencingExtractor) {
            ((SubjectReferencingExtractor) extractor).setSubjectExtractor(_subjectExtractor);
        }
        if (extractor instanceof SessionReferencingExtractor) {
            ((SessionReferencingExtractor) extractor).setSessionExtractor(_sessionExtractor);
        }
    }

    private final Extractor _subjectExtractor;
    private final Extractor _sessionExtractor;
}
