/*
 * dicom-xnat-util: org.nrg.dcm.xnat.SOPHashDicomFileNamer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.xnat.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * FileNamer that uses an alphanumeric hash of the SOP Class and SOP Instance UIDs
 * to construct filenames. This provides better separation between files with very
 * similar metadata.
 */
public final class SOPHashDicomFileNamer implements DicomFileNamer {
    private static final String FIELD_SEPARATOR = ".";
    private static final String SUFFIX = ".dcm";
    private final Logger logger = LoggerFactory.getLogger(SOPHashDicomFileNamer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String makeFileName(final Attributes attributes) {
        final List<String> components = new ArrayList<>();

        String studyID = attributes.getString(Tag.StudyID);
        studyID = studyID == null ? "NULL" : studyID;
        String patientName = attributes.getString(Tag.PatientName);
        patientName = patientName == null ? studyID : patientName;
        if (logger.isTraceEnabled()) {
            logger.trace("Study {} @{} {} - {}:{}", new String[] {
                    attributes.getString(Tag.StudyDescription),
                    attributes.getString(Tag.StudyDate), attributes.getString(Tag.StudyTime),
                    attributes.getString(Tag.SeriesNumber), attributes.getString(Tag.InstanceNumber)
            });
        }

        components.add(patientName);
        Stream.of(Tag.Modality, Tag.StudyDescription, Tag.SeriesNumber, Tag.InstanceNumber)
                .map(attributes::getString)
                .filter(Objects::nonNull)
                .forEach(components::add);

        final String studyDate = attributes.getString(Tag.StudyDate);
        if (null != studyDate) {
            components.add(studyDate.replace(".", ""));
        }

        final String studyTime = attributes.getString(Tag.StudyTime);
        if (null != studyTime) {
            String fixedTime = studyTime.replace(":", "");    // fix ACR-NEMA standard 300-style times
            final int msecStart = studyTime.indexOf(".");
            if (msecStart >= 6) {
                fixedTime = fixedTime.substring(0, msecStart);
            }
            components.add(fixedTime);
        }

        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        final String instanceUID = attributes.getString(Tag.SOPInstanceUID);

        int hash = (null == sopClassUID) ? 0 : sopClassUID.hashCode();
        if (null != instanceUID) {
            hash = 37 * instanceUID.hashCode() + hash;
        }
        components.add(Long.toString(hash & 0xffffffffL, 36));

        return Files.toFileNameChars(String.join(FIELD_SEPARATOR, components) + SUFFIX);
    }
}
