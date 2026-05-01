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
public class TagSequenceWildcardPublic extends Tag {

    private String wildcard;
    private String regex;
    private String vr;

    private static final Logger logger = LoggerFactory.getLogger( TagSequenceWildcardPublic.class);

    public TagSequenceWildcardPublic(String wildcard) {
        this.wildcard = wildcard;
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
    public boolean isPrivate() { return false; }

    @Override
    public boolean isPrivateCreatorID() { return false; }

    @Override
    public String getRegex() {
        regex = (regex != null)? regex: computeRegex();
        return regex;
    }

    @Override
    public boolean isMatch( Tag tag) {
        return tag.asString().matches(getRegex());
    }

    private String computeRegex() {
        String s = "";
        switch (wildcard) {
            case "*":
                s = "(/?[0-9a-fA-F]{8}(\\[[0-9]+\\])?/?)*";
                break;
            case "?":
                s = "([0-9a-fA-F]{8}(\\[[0-9]+\\])?)?";
                break;
            case "+":
                s = "([0-9a-fA-F]{8}(\\[[0-9]+\\])?)+";
                break;
            case ".":
                s = "([0-9a-fA-F]{8}(\\[[0-9]+\\])?){1}";
                break;
            default:
                String msg = String.format("Unknown sequence wildcard: %s.", wildcard);
                logger.error( msg);
                throw new IllegalArgumentException( msg);
        }
        return s;
    }
}
