/*
 * DicomEdit: TagPathCompleteCollector
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

import java.util.ArrayList;
import java.util.List;

public class TagPathCompleteCollector extends DicomObjectTagVisitor {
    private List<TagPath> tagPaths = new ArrayList<>();

    public List<TagPath> apply(DicomObjectI dicomObject) {
        tagPaths = new ArrayList<>();
        visit(dicomObject);
        return tagPaths;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        tagPaths.add(tagPath);
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
        tagPaths.add(new TagPath(tpContext).addTag(tag));
        super.visitSequenceTag(tag, tpContext, dicomElement, dicomObject);
    }

    public List<TagPath> getTagPaths() {
        return tagPaths;
    }

    public int getTagCount() {
        return tagPaths.size();
    }

}
