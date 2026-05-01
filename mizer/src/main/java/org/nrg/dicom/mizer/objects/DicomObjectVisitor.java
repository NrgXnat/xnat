/*
 * DicomEdit: DicomObjectVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.objects;

import org.nrg.dicom.mizer.tags.*;

import java.util.Iterator;

import static org.dcm4che2.util.TagUtils.isPrivateCreatorDataElement;
import static org.dcm4che2.util.TagUtils.isPrivateDataElement;

/**
 * Created by drm on 7/29/16.
 */
public abstract class DicomObjectVisitor {
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

    public void visit( TagPath tpContext, DicomElementI dicomElement, DicomObjectI parentDicomObject) {
        int t = dicomElement.tag();

        Tag tag;
        if (isPrivateDataElement(t)) {
            String pvtCreatorID;
            if (isPrivateCreatorDataElement(t)) {
                pvtCreatorID = parentDicomObject.getString(t);
                tag = new TagPrivateCreator(t, pvtCreatorID);
            } else {
                pvtCreatorID = parentDicomObject.getPrivateCreator(t);
                tag = new TagPrivate(t, pvtCreatorID);
            }
        } else {
            tag = new TagPublic(t);
        }

        if( dicomElement.hasItems() && ( ! dicomElement.isPixelData())) {
            visitSequenceTag( tag, tpContext, dicomElement, parentDicomObject);
        }
        else {
            visitTag( (new TagPath(tpContext)).addTag(tag), dicomElement, parentDicomObject);
        }
    }

    public abstract void visitTag( TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject);

    public void visitSequenceTag( Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI parentDicomObject) {

        for( int i = 0; i < dicomElement.countItems(); i++) {
            Tag tagSequence = new TagSequence( tag, i);
            visit( (new TagPath(tpContext)).addTag(tagSequence), dicomElement.getDicomObject(i));
        }
    }
}
