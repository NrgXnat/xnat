/*
 * DicomEdit: TagChildrenCollector
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

public class TagChildrenCollector extends DicomObjectTagVisitor {
    private List<TagPath> children = new ArrayList<>();
    private Tag tagToMatch;

    public List<TagPath> apply(Tag tag, DicomObjectI dicomObject) {
        children = new ArrayList<>();
        this.tagToMatch = tag;
        visit(dicomObject);
        return children;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (tagToMatch.isMatch(tagPath.getLastTag())) {
            children.add(tagPath);
        }
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (tagToMatch.isMatch(tag)) {
            children.add(new TagPath(tpContext).addTag(tag));
        }
        super.visitSequenceTag(tag, tpContext, dicomElement, dicomObject);
    }

    public List<TagPath> getChildren() {
        return children;
    }
}
