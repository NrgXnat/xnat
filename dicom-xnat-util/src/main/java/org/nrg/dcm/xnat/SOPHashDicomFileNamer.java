/*
 * dicom-xnat-util: org.nrg.dcm.xnat.SOPHashDicomFileNamer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collection;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.xnat.Files;
import org.nrg.xnat.Labels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * FileNamer that uses an alphanumeric hash of the SOP Class and SOP Instance UIDs
 * to construct filenames. This provides better separation between files with very
 * similar metadata.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class SOPHashDicomFileNamer implements DicomFileNamer {
    private static final String FIELD_SEPARATOR = ".";
    private static final String SUFFIX = ".dcm";
    private final Logger logger = LoggerFactory.getLogger(SOPHashDicomFileNamer.class);

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomFileNamer#makeFileName(org.dcm4che2.data.DicomObject)
     */
    public String makeFileName(final DicomObject o) {
        final List<String> components = Lists.newArrayList();

        final String studyID = o.getString(Tag.StudyID, "NULL");
        if (logger.isTraceEnabled()) {
            logger.trace("Study {} @{} {} - {}:{}", new String[] {
                    o.getString(Tag.StudyDescription),
                    o.getString(Tag.StudyDate), o.getString(Tag.StudyTime),
                    o.getString(Tag.SeriesNumber), o.getString(Tag.InstanceNumber)
            });
        }

        addIfNotNull(components,
                o.getString(Tag.PatientName, studyID),
                Labels.toLabelChars(o.getString(Tag.Modality)),
                Labels.toLabelChars(o.getString(Tag.StudyDescription)),
                Labels.toLabelChars(o.getString(Tag.SeriesNumber)),
                Labels.toLabelChars(o.getString(Tag.InstanceNumber)));

        final String studyDate = o.getString(Tag.StudyDate);
        if (null != studyDate) {
            components.add(studyDate.replace(".", ""));
        }

        final String studyTime = o.getString(Tag.StudyTime);
        if (null != studyTime) {
            String fixedTime = studyTime.replace(":", "");    // fix ACR-NEMA standard 300-style times
            final int msecStart = studyTime.indexOf(".");
            if (msecStart >= 6) {
                fixedTime = fixedTime.substring(0, msecStart);
            }
            components.add(fixedTime);
        }

        final String sopClassUID = o.getString(Tag.SOPClassUID);
        final String instanceUID = o.getString(Tag.SOPInstanceUID);

        int hash = (null == sopClassUID) ? 0 : sopClassUID.hashCode();
        if (null != instanceUID) {
            hash = 37 * instanceUID.hashCode() + hash;
        }
        components.add(Long.toString(hash & 0xffffffffl, 36));

        final Joiner joiner = Joiner.on(FIELD_SEPARATOR);
        final StringBuilder fileName = joiner.appendTo(new StringBuilder(), components);
        fileName.append(SUFFIX);
        return Files.toFileNameChars(fileName.toString());         
    }

    private <T> Collection<T> addIfNotNull(final Collection<T> coll, final T...ts) {
        for (final T t : ts) {
            if (null != t) {
                coll.add(t);
            }
        }
        return coll;
    }
}
