/*
 * DicomEdit: DeleteEmptyPrivateBlocksVisitor
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
 * Delete all private creator ID tags for blocks that are empty of any tags.
 */
public class DeleteEmptyPrivateBlocksVisitor extends DicomObjectVisitor {
    private List<PrivateBlock> pvtBlockList;

    @Override
    public void visit(DicomObjectI dicomObject) {
        findPrivateBlocks(dicomObject);
        for (PrivateBlock pb : pvtBlockList) {
            if (isEmpty(pb, dicomObject)) {
                deletePvtBlockCreatorID(pb, dicomObject);
            }
        }
    }

    protected boolean isEmpty(PrivateBlock pb, DicomObjectI dicomObject) {
        PrivateTagCounter visitor = new PrivateTagCounter(pb);
        visitor.visit(dicomObject.getItem(pb.getItem()));
        return visitor.isEmpty();
    }

    protected void deletePvtBlockCreatorID(PrivateBlock pb, DicomObjectI dicomObject) {
        DicomObjectI dobj = dicomObject.getItem(pb.getItem());
        dobj.delete(pb.getTag());
    }

    protected void findPrivateBlocks(DicomObjectI dicomObject) {
        pvtBlockList = new ArrayList<>();
        super.visit(dicomObject);
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (tagPath.isPrivate()) {
            PrivateBlock pb = tagPath.getPrivateBlock().orElse(null);
            if (pb != null) {
                if (!pvtBlockList.contains(pb)) {
                    pvtBlockList.add(pb);
                }
            }
        }
    }

    /**
     * Count the private tags with specified creator ID.
     * Does not recurse into sequence items.
     */
    private class PrivateTagCounter extends DicomObjectVisitor {
        private PrivateBlock _pb;
        private int pvtTagCount = 0;

        public PrivateTagCounter(PrivateBlock pb) {
            _pb = pb;
        }

        @Override
        public void visit(DicomObjectI dicomObject) {
            pvtTagCount = 0;
            super.visit(dicomObject);
        }

        public int getPvtTagCount() {
            return pvtTagCount;
        }

        public boolean isEmpty() {
            return pvtTagCount == 0;
        }

        @Override
        public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if (isPvtTagInBlock(tagPath.getLastTag(), dicomObject)) {
                pvtTagCount++;
            }
        }

        @Override
        public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if (isPvtTagInBlock(tag, dicomObject)) {
                pvtTagCount++;
            }
        }

        public boolean isPvtTagInBlock(Tag tag, DicomObjectI dicomObject) {
            if (tag.isPrivate() && !tag.isPrivateCreatorID()) {
                return isSameGroup(tag) && isSameCreatorID(tag, dicomObject);
            }
            return false;
        }

        public boolean isSameGroup(Tag tag) {
            return _pb.getGroup() == tag.getGroupAsInt();
        }

        public boolean isSameCreatorID(Tag tag, DicomObjectI dicomObject) {
            return _pb.getCreatorID().equals(dicomObject.getPrivateCreator(tag.asInt()));
        }
    }
}
