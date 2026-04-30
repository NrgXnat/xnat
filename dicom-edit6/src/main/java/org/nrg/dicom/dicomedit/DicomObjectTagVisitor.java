package org.nrg.dicom.dicomedit;

import org.dcm4che3.util.TagUtils;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivate;
import org.nrg.dicom.mizer.tags.TagPrivateCreator;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.tags.TagSequence;

import java.util.Iterator;

public abstract class DicomObjectTagVisitor {
    public void visit( DicomObjectI dicomObject) {
        visit(new TagPath(), dicomObject);
    }

    public void visit(TagPath tpContext, DicomObjectI dicomObject) {
        Iterator<DicomElementI> iterator = dicomObject.iterator();
        DicomElementI de;
        while( iterator.hasNext()) {
            de = iterator.next();
            visit( tpContext, de, dicomObject);
        }
    }

    public void visit( TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
        int t = dicomElement.tag();

        Tag tag;
        if (TagUtils.isPrivateTag(t) || TagUtils.isPrivateGroup(t)) {
            String pvtCreatorID;
            if (TagUtils.isPrivateCreator(t)) {
                pvtCreatorID = dicomObject.getString(t);
                tag = new TagPrivateCreator(t, pvtCreatorID);
            } else {
                pvtCreatorID = dicomObject.getPrivateCreator(t);
                tag = new TagPrivate(t, pvtCreatorID);
            }
        } else {
            tag = new TagPublic(t);
        }

        if( dicomElement.hasItems() && ( ! dicomElement.isPixelData())) {
            visitSequenceTag( tag, tpContext, dicomElement, dicomObject);
        }
        else {
            visitTag((new TagPath(tpContext)).addTag(tag), dicomElement, dicomObject);
        }
    }

    public abstract void visitTag( TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject);

    public void visitSequenceTag( Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {

        for( int i = 0; i < dicomElement.countItems(); i++) {
            Tag tagSequence = new TagSequence( tag, i);
            visit( (new TagPath(tpContext)).addTag(tagSequence), dicomElement.getDicomObject(i));
        }
    }

}
