package org.nrg.dicom.mizer.visitors;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete tags matching tagPathToDelete
 *
 * This will remove an entire sequence tag.
 * If a deletion happens to remove the last tag within a sequence item, the empty item will not be removed.
 *
 */
public class DeleteDicomObjectVisitor extends DicomObjectVisitor {
    private TagPath tagPathToDelete;
    private static int PIXELDATA = 0x7FE00010;
    private static final Logger logger = LoggerFactory.getLogger(DicomObjectFactory.class);

    public void setTagPathToDelete(TagPath tagPathToDelete) {
        this.tagPathToDelete = tagPathToDelete;
    }

    @Override
    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        logger.debug("Testing {} against {} with regex {}", tagPath, tagPathToDelete, tagPathToDelete.getRegex());
        // don't delete private creator ID tags here. Delete "empty" tags at end if desired.
        if( ! tagPath.isPrivateCreatorID() && tagPathToDelete.isExtendedMatch(tagPath)) {
            logger.debug("Delete tagPath = {}", tagPath);
            dicomObject.delete(dicomElement.tag());
        }
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tagPath, DicomElementI dicomElement, DicomObjectI parentDicomObject) {
        if( PIXELDATA != tag.asInt()) {
            TagPath thisTagPath = new TagPath( tagPath).addTag( tag);
            if( tagPathToDelete.isMatch( thisTagPath)) {
                parentDicomObject.delete( dicomElement.tag());
            } else {
                super.visitSequenceTag(tag, tagPath, dicomElement, parentDicomObject);
            }
        }
    }

}
