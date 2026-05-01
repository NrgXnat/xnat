/*
 * DicomEdit: DumpVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Find the values in any tag from the set of supplied tagPaths.
 * Map those values from the tagPath originating the values.
 */
public class FindKnownValueVisitor extends DicomObjectVisitor {
    private List<TagPath> specifiedTagPaths;
    private Map<TagPath, String> knownValueMap;

    /**
     * Construct the visitor from a Collection of TagPaths.
     *
     * @param tagPaths Provides TagPaths to search.
     */
    public FindKnownValueVisitor(List<TagPath> tagPaths) {
        this.specifiedTagPaths = Collections.unmodifiableList(new ArrayList<>(tagPaths));
        this.knownValueMap = new HashMap<>();
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        specifiedTagPaths.forEach(stp -> testTagPath(stp, tagPath, dicomElement, dicomObject));
    }

    protected void testTagPath( TagPath specifiedTagPath, TagPath testTagPath, DicomElementI dicomElement, DicomObjectI dobj) {
        if( specifiedTagPath.isMatch(testTagPath)) {
            knownValueMap.put( testTagPath, dobj.getString(dicomElement.tag()));
        }
    }

    public Map<TagPath,String> getKnownValues( DicomObjectI dicomObject) {
        this.knownValueMap = new HashMap<>();
        this.visit(dicomObject);
        return this.knownValueMap;
    }

}
