/*
 * DicomEdit: PrivateBlock
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.objects;

import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPrivate;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulate the group number, block tag, and creator ID string that constitute a private block.
 *
 * Add the block tag o support vendors that put multiple blocks in the same group with the same creator id. For example
 * Fuji has data like
 * (0029,0012) = "FujiFILM TM"
 * (0029,00E1) = "FujiFILM TM"
 * (0029,1231) = 'some data'
 * (0029,E131) = 'some data'
 *
 * We add the block tag to address these blocks uniquely.  The example defines two private blocks {0x0029, 0x12, "FujiFILM TM"} and
 * {0x0029, 0xE1, "FujiFILM TM"}
 *
 * A block tag value of 0 is used to denote an unknown/unassigned block.
 *
 *
 */
public class PrivateBlock {
    /**
     * item is the tag array of the dataset item.
     * item is int[0] if the block is in the root dataset. If the block is in a sequence item, item will be the
     * tag and item numbers of the containing sequence. For example, the tag (52009230[0]/2005140f[0]/20010010 = Philips Imaging DD 001
     * defines the PrivateBlock with item = {0x52009230,0,2005140f,0}, group = 2005, blockTag = 10, creatorID = "Philips Imaging DD 001"
     */
    private final int[] item;
    /**
     * A DICOM group is an unsigned 16-bit value, so it is held in an int. A short would render groups >= 0x8000
     * negative and sign-extend them into every comparison.
     */
    private final int group;
    private final short blockTag;
    /**
     * creatorID will have value TagPrivate.UNKNOWN_PVT_CREATOR_ID_LABEL if PrivateBlock is created from tagPath.getPrivateBlock() when tagPath
     * is not to a private creator ID.
     */
    private final String creatorID;

    public PrivateBlock( int tag, String creatorID) {
        this(new int[0], tag, creatorID);
    }

    /**
     * Creates a block in the given group with an unassigned block tag.
     *
     * @param group     the group of this private block.
     * @param creatorID the private creator ID.
     * @deprecated the width of the first argument is the only thing separating this from
     * {@link #PrivateBlock(int, String)}, which takes a whole tag rather than a group, so dropping the cast at a
     * call site silently constructs a different block. Use {@code new PrivateBlock(group << 16, creatorID)}, which
     * is equivalent. Retained for binary compatibility.
     */
    @Deprecated
    public PrivateBlock( short group, String creatorID) {
        this.item = new int[0];
        this.group = group & 0xFFFF;
        this.blockTag = 0;
        this.creatorID = creatorID;
    }

    public PrivateBlock( int[] item, int tag, String creatorID) {
        this.item = item;
        this.group = tag >>> 16;
        this.blockTag = getBlockTag( tag);
        this.creatorID = creatorID;
    }

    /**
     * the int[] of the dataset containing this private block.
     * int[] = int[0] if the dataset is top level.
     *
     * @return the int[] of the dataset containing this private block.
     */
    public int[] getItem() {
        return item;
    }

    /**
     * The integer tag of this private block
     * @return
     */
    public int getTag() {
        return (group << 16) + blockTag;
    }

    /**
     * The group of this private block.
     *
     * @return the group as a short.
     * @deprecated a group >= 0x8000 narrows to a negative short, which then sign-extends whenever it is
     * compared against an unsigned group such as {@link org.nrg.dicom.mizer.tags.Tag#getGroupAsInt()}. Use
     * {@link #getGroupAsInt()} instead. Retained for binary compatibility.
     */
    @Deprecated
    public short getGroup() {
        return (short) group;
    }

    /**
     * The group of this private block as an unsigned value in the range 0x0000 - 0xFFFF.
     *
     * @return the group as an int.
     */
    public int getGroupAsInt() {
        return group;
    }

    public short getBlockTag() { return blockTag; }

    public String getCreatorID() {
        return creatorID;
    }

    private short getBlockTag( int tag) {
        short blockTag = (short) ((tag & 0xFF00)>>>8);
        if( blockTag == 0) {
            // tag is the creator id.
            blockTag = (short) (tag & 0x00FF);
        }
        return blockTag;
    }

    public boolean isPvtCreatorForTag( List<Integer> tags) {
        return isPvtCreatorForTag( tags.stream().mapToInt(i->i).toArray());
    }
    public boolean isPvtCreatorForTag( int[] tags) {
        return isSameItem( tags) && isSamePvtBlock( tags) && !TagPrivate.UNKNOWN_PVT_CREATOR_ID_LABEL.equals( creatorID);
    }
    private boolean isSameItem( int[] tags) {
        return (item.length == tags.length - 1)? Arrays.equals( item, Arrays.copyOf( tags, item.length)): false;
    }
    private boolean isSamePvtBlock( int[] tags) {
        if( tags.length > 1) {
            int t = tags[tags.length - 1];
            return isSameGroup( t) && isSameBlock( t);
        }
        return false;
    }
    private boolean isSameGroup( int tag) {
        return group == (tag >>> 16);
    }
    private boolean isSameBlock( int tag) {
        return blockTag == getBlockTag( tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrivateBlock that = (PrivateBlock) o;

        if( ! Arrays.equals( getItem(), that.getItem())) return false;
        if (getGroupAsInt() != that.getGroupAsInt()) return false;
        if (getBlockTag() != that.getBlockTag()) return false;
        return getCreatorID() != null ? getCreatorID().equals(that.getCreatorID()) : that.getCreatorID() == null;

    }

    @Override
    public int hashCode() {
        int result = getGroupAsInt() + getBlockTag() + Arrays.hashCode(item);
        result = 31 * result + (getCreatorID() != null ? getCreatorID().hashCode() : 0);
        return result;
    }

    public static void main( String[] args) {
        PrivateBlock pb = new PrivateBlock( 0x0017E110, "XYZ") ;

        System.out.println("Group = " + Tag.padLeft(Integer.toHexString(pb.getGroupAsInt()), 4, '0'));
        System.out.println("BlockTag = " + Integer.toHexString(pb.getBlockTag()));
    }
}
