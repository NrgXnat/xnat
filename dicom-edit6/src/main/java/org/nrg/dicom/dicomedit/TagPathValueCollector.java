/*
 * DicomEdit: TagPathValueCollector
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collect the tags in an object, including sequence elements.
 */
public class TagPathValueCollector extends DicomObjectTagVisitor {
    private Map<TagPath, String> tagPathMap;
    private static final int PIXEL_DATA = 0x7FE00010;

    public Map<TagPath, String> getAll(DicomObjectI dicomObject) {
        this.tagPathMap = new HashMap<>();
        visit(dicomObject);
        TagPathFilter filter = new PassTagPathFilter();

        return tagPathMap.entrySet()
                .stream().filter(e -> filter.allow(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TagPath, String> getMatching(List<TagPath> retainTagPaths, DicomObjectI dicomObject) {
        this.tagPathMap = new HashMap<>();
        visit(dicomObject);
        TagPathFilter filter = new MatchingTagPathFilter(retainTagPaths);

        return tagPathMap.entrySet()
                .stream().filter(e -> filter.allow(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<TagPath, String> getNotMatching(List<TagPath> removeTagPaths, DicomObjectI dicomObject) {
        this.tagPathMap = new HashMap<>();
        visit(dicomObject);
        TagPathFilter filter = new MatchingTagPathFilter(removeTagPaths);

        return tagPathMap.entrySet()
                .stream().filter(e -> filter.deny(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (dicomElement.tag() != PIXEL_DATA) {
            tagPath.getLastTag().setVR(dicomElement.getVRAsString());
            tagPathMap.put(tagPath, dicomObject.getString(dicomElement.tag()));
        }
    }

    @Override
    public void visitSequenceTag(Tag tag, TagPath tpContext, DicomElementI dicomElement, DicomObjectI dicomObject) {
//        tagPathMap.put( new TagPath(tpContext).addTag(tag),"foo");
        super.visitSequenceTag(tag, tpContext, dicomElement, dicomObject);
    }

    public Map<TagPath, String> getTagPathMap() {
        return tagPathMap;
    }

}
