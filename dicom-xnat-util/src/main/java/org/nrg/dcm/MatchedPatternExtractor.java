/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.regex.Pattern;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractor extends MatchedPatternExtractorWithLengthLimit {

    public MatchedPatternExtractor(final int tag, final Pattern pattern, final int group) {
        super( tag, pattern, group, -1);
    }
}
