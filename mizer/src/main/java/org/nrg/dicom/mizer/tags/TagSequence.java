/*
 * DicomEdit: TagSequence
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

import java.util.Objects;

/**
 * Model Tags of type sequence (VR == SQ).
 *
 *
 */
public class TagSequence extends Tag {

    private final Tag tag;
    private final String itemNumber;
    private final boolean singular;
    private String regex;
    private String vr;

    public TagSequence( Tag tag, String itemNumber) {
        this.tag = tag;
        this.itemNumber = (itemNumber != null)? itemNumber: String.valueOf(Tag.ITEMNUMBER_WILDCARD_CHAR);
        this.singular = ( ! hasWildCard(itemNumber)) && tag.isSingular();
    }

    public TagSequence( Tag tag, int itemNumber) {
        this.tag = tag;
        this.singular = true;
        this.itemNumber = Integer.toString(itemNumber);
    }

    public Tag getTag() {
        return tag;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    /**
     * Throws NumberFormat exception if the item number is a wild card and does not map to a unique integer.
     *
     * @return Returns the item number as an integer.
     */
    public int getItemNumberAsInt() {
        return Integer.parseInt( itemNumber);
    }

    @Override
    public String getGroup() {
        return tag.getGroup();
    }

    @Override
    public String getElement() {
        return tag.getElement();
    }

    @Override
    public String asString() {
        String item = "";
        if( itemNumber != null) item = "[" + itemNumber + "]";
        return tag.asString() + item;
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
    public boolean isPrivate() { return tag.isPrivate();}

    @Override
    public boolean isPrivateCreatorID() { return tag.isPrivateCreatorID();}

    protected boolean isItemWild() {
        return String.valueOf(Tag.ITEMNUMBER_WILDCARD_CHAR).equals( itemNumber);
    }

    /**
     * integer less than (before) wild card.
     *
     * @param thatTag
     * @return
     */
    @Override
    public int compareTo( Tag thatTag) {
        int c = super.compareTo( thatTag);
        if( c == 0 && thatTag instanceof TagSequence) {
            TagSequence thatSeqTag = (TagSequence) thatTag;
            if( isItemWild()) {
                c = ( thatSeqTag.isItemWild())? 0: 1;
            }
            else {
                c = ( thatSeqTag.isItemWild())? -1: this.getItemNumberAsInt() - thatSeqTag.getItemNumberAsInt();
            }
        }
        return c;
    }

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
        String itemNumberRegex = "";
        String item = ( itemNumber != null)? itemNumber: "%";
        switch (item) {
            case "%":
                itemNumberRegex = "(\\[[0-9]+\\])?+";
                break;
            default:
                itemNumberRegex = "\\[" + item + "\\]";
                break;
        }
        return tag.getRegex() + itemNumberRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagSequence that = (TagSequence) o;
        return isSingular() == that.isSingular() && getTag().equals(that.getTag()) && getItemNumber().equals(that.getItemNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTag(), getItemNumber(), isSingular());
    }
}
