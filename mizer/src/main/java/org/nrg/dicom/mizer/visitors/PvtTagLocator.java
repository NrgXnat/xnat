/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.visitors;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Find all private creator ID tags as PrivateBlock objects.
 */
public class PvtTagLocator extends DicomObjectVisitor {
    private List<List<Integer>> pvtTagList = new ArrayList<>();
    private boolean includeCreatorIDs = false;

    @Override
    public void visit(DicomObjectI dicomObject) {
        pvtTagList = new ArrayList<>();
        super.visit( dicomObject);
    }

    public List<List<Integer>> getPvtTagList( DicomObjectI dicomObject, boolean includeCreatorIDs) {
        visit( dicomObject);
        this.includeCreatorIDs = includeCreatorIDs;
        return pvtTagList;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if( tagPath.isPrivate() && isTagIncluded( tagPath)) {
            int[] tagArray = tagPath.getTagsAsArray();
            if (!pvtTagList.contains( tagArray)) {
                pvtTagList.add( Arrays.stream(tagArray).boxed().collect(Collectors.toList()));
            }
        }
    }

    private boolean isTagIncluded( TagPath tagPath) {
        boolean tc = tagPath.isPrivateCreatorID();
        return (tc && includeCreatorIDs) || (!tc && !includeCreatorIDs);
    }

}
