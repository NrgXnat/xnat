package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.*;

import java.util.Iterator;

import static org.dcm4che2.util.TagUtils.isPrivateCreatorDataElement;
import static org.dcm4che2.util.TagUtils.isPrivateDataElement;


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
        if (isPrivateDataElement(t)) {
            String pvtCreatorID;
            if (isPrivateCreatorDataElement(t)) {
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
