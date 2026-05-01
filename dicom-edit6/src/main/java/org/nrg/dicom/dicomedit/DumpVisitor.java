/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;

import java.io.PrintStream;

/**
 * Created by drm on 7/29/16.
 */
public class DumpVisitor extends DicomObjectVisitor {
    private DicomObjectI rootDicomObject;
    private PrintStream ps;

    public DumpVisitor(DicomObjectI rootDicomObject, PrintStream ps) {
        this.rootDicomObject = rootDicomObject;
        this.ps = ps;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        ps.println(  tagPath.toString() + " Value = " + rootDicomObject.getString( tagPath));
    }

    public void visitSequenceTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        ps.println( tagPath.toString());
    }
}
