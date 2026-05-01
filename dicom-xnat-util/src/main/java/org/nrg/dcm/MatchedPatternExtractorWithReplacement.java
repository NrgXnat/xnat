/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.base.Strings;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.framework.utilities.SortedSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractorWithReplacement extends MatchedPatternExtractor {
    private final String target;
    private final String repl;

    public MatchedPatternExtractorWithReplacement(final int tag, final Pattern pattern, final int group,
                                                  final String target, final String repl) {
        super(tag, pattern, group);
        this.target = target;
        this.repl = repl;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.Extractor#extract(org.dcm4che2.data.DicomObject)
     */
    public String extract(final DicomObject o) {
        String origVal = super.extract(o);
        if (origVal == null) {
            return null;
        }
        if (target == null || repl == null) {
            return origVal;
        }
        return origVal.replaceAll(target, repl);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + " replacing " + target + " with " + repl;
    }
}
