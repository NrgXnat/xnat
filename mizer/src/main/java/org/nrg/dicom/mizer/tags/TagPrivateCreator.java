/*
 * DicomEdit: TagPrivateCreator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

/**
 * Implements a container for private tag creators.
 */
public class TagPrivateCreator extends TagPrivate {

    public TagPrivateCreator( String group, String pvtCreatorID, String element) {
        super( group, pvtCreatorID, element);
    }

    public TagPrivateCreator( int tag, String pvtCreatorID) {
        super( tag, pvtCreatorID);
    }

    public int getPrivateBlock() {
        return Tag.getPrivateCreatorBlock( getElementAsInt());
    }

    public int getPvtCreatorIDTag() {
        return asInt();
    }

    @Override
    public boolean isPrivateCreatorID() {
        return true;
    }

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

}
