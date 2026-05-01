/*
 * DicomEdit: TagPublic
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Implements a container for public tags.
 */
public class TagPublic extends Tag {

    private final String group;
    private final String element;
    private final boolean singular;
    private String regex;
    private String vr;

    private static final Logger logger = LoggerFactory.getLogger(TagPublic.class);

    public TagPublic(String group, String element) {
        this.group = getValidGroup(group);
        this.element = getValidElement(element);
        singular = !((hasWildCard(group) || hasWildCard(element)));
    }

    public TagPublic(int tag) {
        String s = padLeft(Integer.toHexString(tag), 8, '0');
        this.group = s.substring(0, 4);
        this.element = s.substring(4, 8);
        this.singular = true;
    }

    public String getGroup() {
        return group;
    }

    public String getElement() {
        return element;
    }

    @Override
    public String asString() {
        return group + element;
    }

    @Override
    public String getVR() {
        return vr;
    }

    @Override
    public void setVR(String vr) {
        this.vr = vr;
    }

    public String toString() {
        return asString();
    }

    @Override
    public boolean isSingular() {
        return singular;
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
        regex = (regex != null) ? regex : computeBlockRegex(group) + computeBlockRegex(element);
        return regex;
    }

    @Override
    public boolean isMatch(Tag tag) {
        return tag.asString().matches(getRegex());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagPublic tagPublic = (TagPublic) o;
        return isSingular() == tagPublic.isSingular() && getGroup().equals(tagPublic.getGroup()) && getElement().equals(tagPublic.getElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getElement(), isSingular());
    }
}
