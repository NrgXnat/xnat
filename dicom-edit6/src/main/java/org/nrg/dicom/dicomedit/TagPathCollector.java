/*
 * DicomEdit: TagPathCollector
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
import java.util.stream.Collectors;

/**
 * Collect the tags in an object, including sequence elements. Neat.
 */
public class TagPathCollector extends DicomObjectTagVisitor {
    private List<TagPath> tagPaths;

    public List<TagPath> getAll(DicomObjectI dicomObject) {
        this.tagPaths = new ArrayList<>();
        visit(dicomObject);
        TagPathFilter filter = new PassTagPathFilter();
        return tagPaths.stream()
                .filter(filter::allow)
                .collect(Collectors.toList());
    }

    public List<TagPath> getMatching(List<TagPath> retainTagPaths, DicomObjectI dicomObject) {
        this.tagPaths = new ArrayList<>();
        visit(dicomObject);
        TagPathFilter filter = new MatchingTagPathFilter(retainTagPaths);
        return tagPaths.stream()
                .filter(filter::allow)
                .collect(Collectors.toList());
    }

    public List<TagPath> getNotMatching(List<TagPath> removeTagPaths, DicomObjectI dicomObject) {
        this.tagPaths = new ArrayList<>();
        visit(dicomObject);
        TagPathFilter filter = new MatchingTagPathFilter(removeTagPaths);
        return tagPaths.stream()
                .filter(filter::deny)
                .collect(Collectors.toList());
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
}
