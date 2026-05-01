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
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;

import java.io.PrintStream;

public class DumpDicomPvtTagVisitor extends DicomObjectTagVisitor {
    private PrintStream ps;

    public DumpDicomPvtTagVisitor( PrintStream ps) {
        this.ps = ps;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if( tagPath.isPrivate()) {
            ps.println(tagPath + " Value = " + dicomObject.getString(dicomElement.tag()));
        }
    }
    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI parentDicomObject) {
//        ps.println("Seq Tag " + tag + " in " + tpContext);
        super.visitSequenceTag(tag, tpContext, dicomElement, parentDicomObject);
    }
}
