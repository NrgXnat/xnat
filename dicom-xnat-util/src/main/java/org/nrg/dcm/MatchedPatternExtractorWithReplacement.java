/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;

import java.util.regex.Pattern;


public class MatchedPatternExtractorWithReplacement extends MatchedPatternExtractor {
    private final String target;
    private final String repl;

    public MatchedPatternExtractorWithReplacement(final int tag, final Pattern pattern, final int group,
                                                  final String target, final String repl) {
        super(tag, pattern, group);
        this.target = target;
        this.repl = repl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(final Attributes attributes) {
        String origVal = super.extract(attributes);
        if (origVal == null) {
            return null;
        }
        if (target == null || repl == null) {
            return origVal;
        }
        return origVal.replaceAll(target, repl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " replacing " + target + " with " + repl;
    }
}
