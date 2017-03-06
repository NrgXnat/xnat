/*
 * DicomDB: org.nrg.dcm.Attributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che2.data.Tag;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class Attributes {
    private Attributes() {}

    public static final DicomAttributeIndex
    SOPClassUID = new FixedDicomAttributeIndex("SOPClassUID", Tag.SOPClassUID),
    TransferSyntaxUID = new FixedDicomAttributeIndex("TransferSyntaxUID", Tag.TransferSyntaxUID),
    StudyInstanceUID = new FixedDicomAttributeIndex("StudyInstanceUID", Tag.StudyInstanceUID),
    SeriesDescription = new FixedDicomAttributeIndex("SeriesDescription", Tag.SeriesDescription),
    SeriesInstanceUID = new FixedDicomAttributeIndex("SeriesInstanceUID", Tag.SeriesInstanceUID),
    SeriesNumber = new FixedDicomAttributeIndex("SeriesNumber", Tag.SeriesNumber),
    SeriesDate = new FixedDicomAttributeIndex("SeriesDate", Tag.SeriesDate),
    SeriesTime = new FixedDicomAttributeIndex("SeriesTime", Tag.SeriesTime),
    AcquisitionDateTime = new FixedDicomAttributeIndex("AcquisitionDateTime", Tag.AcquisitionDateTime),
    AcquisitionDate = new FixedDicomAttributeIndex("AcquisitionDate", Tag.AcquisitionDate),
    AcquisitionTime = new FixedDicomAttributeIndex("AcquisitionTime", Tag.AcquisitionTime),
    Modality = new FixedDicomAttributeIndex("Modality", Tag.Modality),
    PatientComments = new FixedDicomAttributeIndex(Tag.PatientComments),
    StudyComments = new FixedDicomAttributeIndex(Tag.StudyComments),
    StudyDate = new FixedDicomAttributeIndex(Tag.StudyDate),
    StudyDescription = new FixedDicomAttributeIndex(Tag.StudyDescription),
    AccessionNumber = new FixedDicomAttributeIndex(Tag.AccessionNumber),
    PatientID = new FixedDicomAttributeIndex(Tag.PatientID),
    PatientName = new FixedDicomAttributeIndex(Tag.PatientName),
    Rows = new FixedDicomAttributeIndex(Tag.Rows),
    Cols = new FixedDicomAttributeIndex(Tag.Columns),
    EchoNumbers = new FixedDicomAttributeIndex(Tag.EchoNumbers),
    EchoTime = new FixedDicomAttributeIndex(Tag.EchoTime),
    ImageOrientationPatient = new FixedDicomAttributeIndex(Tag.ImageOrientationPatient),
    PixelSpacing = new FixedDicomAttributeIndex(Tag.PixelSpacing),
    SliceThickness = new FixedDicomAttributeIndex(Tag.SliceThickness),
    TimeZoneOffset = new FixedDicomAttributeIndex(Tag.TimezoneOffsetFromUTC);
}
