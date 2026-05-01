/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.visitors;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.objects.PrivateBlock;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all private creator ID tags as PrivateBlock objects.
 */
public class PvtBlockLocator extends DicomObjectVisitor {
    private List<PrivateBlock> pvtBlockList = new ArrayList<>();

    @Override
    public void visit(DicomObjectI dicomObject) {
        pvtBlockList = new ArrayList<>();
        super.visit( dicomObject);
    }

    public List<PrivateBlock> getPvtBlockList( DicomObjectI dicomObject) {
        visit( dicomObject);
        return pvtBlockList;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if( tagPath.isPrivate()) {
            PrivateBlock pb = tagPath.getPrivateBlock().orElse(null);
            if( pb != null && ! pvtBlockList.contains( pb)) {
                    pvtBlockList.add(pb);
            }
        }
    }

}
