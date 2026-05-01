/*
 * DicomEdit: TagPrivate
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
import java.util.regex.Pattern;

/**
 * Implements a container for private tags.
 */
public class TagPrivate extends Tag {

    private String group;
    private String element;
    private boolean singular;
    private final String pvtCreatorID;
    private String vr;

    private String regex;

    private static final Logger logger = LoggerFactory.getLogger( TagPrivate.class);

    public final static String DEFAULT_ELEMENT_BLOCK = "10";
    public final static String DEFAULT_PRIVATE_CREATOR_ELEMENT = "0010";
    // Give a Creator ID to tags with unknown creator ID to avoid using null.
    public final static String UNKNOWN_PVT_CREATOR_ID_LABEL = "NULL";

    public TagPrivate( String group, String pvtCreatorID, String element) {
        this.group = getValidGroup( group);
        this.element = getValidElement( element);
        this.singular = ! (hasWildCard( group) || hasWildCard( element) );
        this.pvtCreatorID = (pvtCreatorID != null)? pvtCreatorID: UNKNOWN_PVT_CREATOR_ID_LABEL;
        if( pvtCreatorID == null) {
            String msg = String.format("Private tag with null creator ID. group = '%s', element = '%s'", group, element);
            logger.warn( msg);
        }
    }

    public TagPrivate( int tag, String pvtCreatorID) {
        String s = padLeft( Integer.toHexString( tag), 8, '0');
        this.group = s.substring(0,4);
        this.element = s.substring(4,8);
        this.singular = true;
        this.pvtCreatorID = (pvtCreatorID != null)? pvtCreatorID: UNKNOWN_PVT_CREATOR_ID_LABEL;
        if( pvtCreatorID == null) {
            String msg = String.format("Private tag with null creator ID. tag = '%08X'.", tag);
            logger.warn( msg);
        }
    }

    public String getGroup() {
        return group;
    }

    /**
     * getPrivateBock
     *
     * @return return the block of this private tag.
     * @throws NumberFormatException if this tag is not singular.
     */
    public int getPrivateBlock() {
        return Tag.getPrivateBlock( getElementAsInt());
    }

    public String getPvtCreatorID() {
        return pvtCreatorID;
    }

    public int getPvtCreatorIDTag() {
        return Integer.parseInt( group + DEFAULT_PRIVATE_CREATOR_ELEMENT, 16);
    }

    public String getElement() {
        return element;
    }

    @Override
    public String asString() {
        return group + "\"" + pvtCreatorID + "\"" + element;
    }

    @Override
    public String getVR() {
        return vr;
    }

    @Override
    public void setVR(String vr) {
        this.vr = vr;
    }

    public boolean isSingular() { return singular;}

    public boolean isPrivate() { return true; }

    public boolean isPrivateCreatorID() { return false; }

    /**
     * isInPrivateBlock
     *
     * @param group private-element group
     * @param block private-element block.
     * @return true if this tag is singular and in the same private block (group and block) of the specified tag.
     */
    public boolean isInPrivateBlock( int group, int block) {
        return getGroupAsInt() == group && getPrivateBlock() == block;
    }

    public boolean isInPrivateBlock( TagPrivate pvtCreatorIDtag) {
        return isInPrivateBlock( pvtCreatorIDtag.getGroupAsInt(), pvtCreatorIDtag.getPrivateBlock());
    }

    public boolean isInPrivateCreatorBlock( TagPrivateCreator pvtCreatorIDtag) {
        return isInPrivateBlock( pvtCreatorIDtag.getGroupAsInt(), pvtCreatorIDtag.getPrivateBlock());
    }

    /**
     * Return the integer value of this tag.
     *
     * Throws NumberFormatException if this tag has wild cards and does not map to a unique integer.
     *
     * This will always return the tag in block 10, i.e. gggg,10ee.  The actual tag value can only be resolved in the context
     * of the specific dicom object that contains this tag.
     * Use DicomObject's resolveTag method to do this.
     *
     * @return Returns the tag value as an integer.
     */
    public int asInt() { return Integer.parseInt( group + element, 16);}

    public String toString() {
        return asString();
    }

    @Override
    public boolean hasUnknownCreatorID() {
        return UNKNOWN_PVT_CREATOR_ID_LABEL.equals( pvtCreatorID);
    }

    @Override
    public boolean isMatch( Tag tag) {
        return tag.asString().matches(getRegex());
    }

    @Override
    public String getRegex() {
        regex = (regex != null)? regex: computeBlockRegex( group) + computePvtCreatorRegex( pvtCreatorID) + computePvtElementRegex( element);
        return regex;
    }

    private String computePvtCreatorRegex(String s) {
        if( pvtCreatorID == null) {
            throw new IllegalArgumentException(String.format("Null pvtCreatorID with group '%s', element '%s'", group, element));
        }
        // return "\\Q" + pvtCreatorID + "\\E";
        return "\"" + Pattern.quote(  pvtCreatorID) + "\"";
    }

    private String computePvtElementRegex(String s) {
        StringBuilder sb = new StringBuilder( computeBlockRegex( s));
        if( ! hasUnknownCreatorID()) {
            sb.replace(0,2, "[0-9a-fA-F]{2}");
        }
        return sb.toString();
    }

    public TagPrivateCreator getExpectedCreatorIDTag() {
        return new TagPrivateCreator( Tag.getTag( getGroupAsInt(), 0, getPrivateBlock()), getPvtCreatorID() );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagPrivate)) return false;
        TagPrivate that = (TagPrivate) o;
        return isSingular() == that.isSingular()
                && getGroup().equals(that.getGroup())
                && getElement().equals(that.getElement())
                && Objects.equals(getPvtCreatorID(), that.getPvtCreatorID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getElement(), isSingular(), getPvtCreatorID());
    }
}
