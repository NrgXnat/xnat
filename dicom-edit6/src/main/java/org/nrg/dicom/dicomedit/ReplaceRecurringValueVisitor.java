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

import java.util.Collection;
import java.util.Map;

/**
 * The goal is to replace the value in any tag if the tag contains a value from a list of known PHI values.
 * Do not replace the tag's value if the tag is in a list of known tagPaths. This is because the these tags are
 * the source of the known PHI and have likely been dealt with separately. Replacing the value of those tags is
 * likely to be inappropriate.
 * <p>
 * Replace value of tags that are not on a list of specified tags but have a value that is on a list of specified values.
 * The default replacement value is the empty string.
 */
public class ReplaceRecurringValueVisitor extends DicomObjectVisitor {
    private Collection<TagPath> knownTagPaths;
    private Collection<String> knownValues;
    private String replacementString;
    private static String DEFAULT_REPLACEMENT_VALUE = "";

    /**
     * Construct the visitor from a Map. Use the default replacement value.
     *
     * @param knownValueMap Provides both the set of tagPaths to ignore, knownValueMap.keySet() and the set of
     *                      values-to-be-replaced, knownValueMap.values();
     */
    public ReplaceRecurringValueVisitor(Map<TagPath, String> knownValueMap) {
        this(knownValueMap, DEFAULT_REPLACEMENT_VALUE);
        this.knownValues = knownValueMap.values();
        this.knownTagPaths = knownValueMap.keySet();
    }

    /**
     * Construct the visitor from a map. Use the supplied replacement value.
     *
     * @param knownValueMap     Provides both the set of tagPaths to ignore, knownValueMap.keySet() and the set of
     *                          values-to-be-replaced, knownValueMap.values();
     * @param replacementString The value replacing old values.
     */
    public ReplaceRecurringValueVisitor(Map<TagPath, String> knownValueMap, String replacementString) {
        this.knownValues = knownValueMap.values();
        this.knownTagPaths = knownValueMap.keySet();
        this.replacementString = replacementString;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (replaceThisTag(tagPath, dicomElement, dicomObject, knownTagPaths, knownValues)) {
            dicomObject.putString(dicomElement.tag(), replacementString);
        }
    }

    protected boolean replaceThisTag(TagPath tp, DicomElementI dicomElement, DicomObjectI dicomObject, Collection<TagPath> knownTagPaths, Collection<String> knownValues) {
        return knownValues.contains(dicomObject.getString(dicomElement.tag()))
                && (!knownTagPaths.contains(tp));
    }

}
