/*
 * DicomEdit: PvtBlockLocator
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
import org.nrg.dicom.mizer.objects.PrivateBlock;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all private creator ID tags as PrivateBlock objects. Include sequence contents if recurse is true;
 */
public class PvtBlockLocator extends DicomObjectVisitor {
    private List<PrivateBlock> pvtBlockList = new ArrayList<>();
    private boolean recurse;

    @Override
    public void visit(DicomObjectI dicomObject) {
        pvtBlockList = new ArrayList<>();
        super.visit(dicomObject);
    }

    public List<PrivateBlock> getPvtBlockList(DicomObjectI dicomObject, boolean recurse) {
        this.recurse = recurse;
        visit(dicomObject);
        return pvtBlockList;
    }

    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        addPrivateBlock(tagPath);
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI parentDicomObject) {
        addPrivateBlock(new TagPath(tpContext).addTag(tag));
        if (recurse) {
            super.visitSequenceTag(tag, tpContext, dicomElement, parentDicomObject);
        }
    }

    private void addPrivateBlock(TagPath tagPath) {
        if (tagPath.isPrivate()) {
            PrivateBlock pb = tagPath.getPrivateBlock().orElse(null);
            if (pb != null && !pvtBlockList.contains(pb)) {
                pvtBlockList.add(pb);
            }
        }
    }
}
