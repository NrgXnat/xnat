package org.nrg.dicom.dicomedit.datetime;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

public interface OffsetDateTimeFactory {
    OffsetDateTime dateTime(DicomDateTime dicomDateTime);
    OffsetTime time(DicomDateTime dicomDateTime);
}
