/*
 * DicomEdit: PrintVisitor
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
public class PrintVisitor extends DicomObjectVisitor {

    private final PrintStream ps;

    public PrintVisitor(PrintStream ps) {
        this.ps = ps;
    }
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        ps.println( "Tag: " + tagPath.toString() + " Value = " + dicomObject.getString(tagPath.getTags().get(tagPath.size()-1).asInt()));
    }

    public void visitSequenceTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        ps.println( "SequenceTag: " + tagPath.toString());
    }
}
