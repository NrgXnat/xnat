package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.List;

/**
 * DicomObjectTagVisitor that deletes all orphan private tags.
 * Orphan private tags are private tags in blocks that do not have the required private creator ID tag.
 */
public class DeleteOrphanPvtTagsVisitor extends DicomObjectTagVisitor {
    private List<TagPath> tagPaths;

    public void apply(List<TagPath> tagPaths, DicomObjectI dicomObject) {
        this.tagPaths = tagPaths;
        visit(dicomObject);
    }

    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (tagPath.getLastTag().hasUnknownCreatorID()) {
            dicomObject.delete(dicomElement.tag());
        }
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (tag.hasUnknownCreatorID()) {
            dicomObject.delete(dicomElement.tag());
        } else {
            super.visitSequenceTag(tag, tpContext, dicomElement, dicomObject);
        }
    }
}

