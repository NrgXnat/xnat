/*
 * DicomEdit: TagSequenceWildcard
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by drm on 8/5/16.
 */
public class TagSequenceWildcard extends Tag {

    private String wildcard;
    private String regex;
    private String vr;

    private static final Logger logger = LoggerFactory.getLogger( TagSequenceWildcard.class);

    public TagSequenceWildcard( String wildcard) {
        super();
        this.wildcard = wildcard;
    }

    @Override
    public boolean isSubsequentTagRequired() {
        boolean b;
        switch (wildcard) {
            case "+":
                b = true;
                break;
            default:
                b = false;
                break;
        }
        return b;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getElement() {
        return null;
    }

    @Override
    public String asString() {
        return wildcard;
    }

    @Override
    public String getVR() {
        return vr;
    }

    @Override
    public void setVR(String vr) {
        this.vr = vr;
    }

    @Override
    public int asInt() {
        return -1;
    }

    @Override
    public boolean isSingular() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isPrivateCreatorID() {
        return false;
    }

    @Override
    public String getRegex() {
        regex = (regex != null)? regex: computeRegex();
        return regex;
    }

    @Override
    public boolean isMatch( Tag tag) {
        return true;
    }

    private String computeRegex() {
        String s = "";
        switch (wildcard) {
            case "*":
//                s = "(/?[^/]*/?)*";
                s = "[\\s\\S]*";
                break;
            case "?":
                s = "nertz.";
                break;
            case "+":
                String t = _subsequentTag.getRegex();
                s = "(?!" + t + ")([^/]*/)*";
                break;
            case ".":
                s = "(/?[^/]+/){1}";
                break;
            default:
                String msg = String.format("Unknown sequence wildcard: %s.", wildcard);
                logger.error( msg);
                throw new IllegalArgumentException( msg);
        }
        return s;
    }

    /**
     * integer less than (before) wild card.
     *
     * @param thatTag
     * @return
     */
    @Override
    public int compareTo( Tag thatTag) {
        return (thatTag instanceof TagSequenceWildcard)? 0: -1;
    }
}
