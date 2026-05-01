package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.Optional;

public interface DicomTagPrognosticator {

    Optional<String> prognosticate( TagPath tp, DicomObjectI dobj);
}
