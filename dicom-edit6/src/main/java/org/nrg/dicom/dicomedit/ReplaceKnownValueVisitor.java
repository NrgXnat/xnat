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

/**
 * Replace the value in any tag if the tag contains a value from a list of known values.
 * <p>
 * The default replacement value is the empty string.
 */
public class ReplaceKnownValueVisitor extends DicomObjectVisitor {
    private Collection<String> knownValues;
    private String replacementString;
    private static String DEFAULT_REPLACEMENT_VALUE = "";

    /**
     * Construct the visitor from a Map. Use the default replacement value.
     *
     * @param knownValues Provides values-to-be-replaced.
     */
    public ReplaceKnownValueVisitor(Collection<String> knownValues) {
        this(knownValues, DEFAULT_REPLACEMENT_VALUE);
        this.knownValues = knownValues;
    }

    /**
     * Construct the visitor from a map. Use the supplied replacement value.
     *
     * @param knownValues     Provides values-to-be-replaced.
     * @param replacementString The value replacing old values.
     */
    public ReplaceKnownValueVisitor(Collection<String> knownValues, String replacementString) {
        this.knownValues = knownValues;
        this.replacementString = replacementString;
    }

    public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
        if (replaceThisTag(tagPath, dicomElement, dicomObject, knownValues)) {
            dicomObject.putString(dicomElement.tag(), replacementString);
        }
    }

    protected boolean replaceThisTag(TagPath tp, DicomElementI dicomElement, DicomObjectI dicomObject, Collection<String> knownValues) {
        return knownValues.contains(dicomObject.getString(dicomElement.tag()));
    }

}
