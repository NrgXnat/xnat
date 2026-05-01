/*
 * DicomEdit: TagPath
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.PrivateBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The primary model for addressing attributes in DICOM objects.
 * A collection of Tags specifying a unique DICOM attribute, or a set of attriubtes if the TagPath contains
 * wildcards.
 *
 */
public class TagPath  implements Comparable<TagPath> {
    private final List<Tag> tags;
    private String path;
    private String regex;
    private static final Logger logger = LoggerFactory.getLogger(TagPath.class);

    public TagPath( TagPath path) {
        this( path.getTags());
    }

    public TagPath() {
        this( new ArrayList<Tag>());
    }

    public TagPath( List<Tag> tags) {
//        this.tags = tags;
        this.tags = new ArrayList<>(tags);
        this.path = null;
        this.regex = null;
    }

    public TagPath(Tag tag) {
        this();
        tags.add(tag);
    }

    public TagPath addTag(Tag tag) {
        path = null;
        regex = null;
        tags.add( tag);
        return this;
    }

    public void addTags( List<Tag> tags) {
        path = null;
        regex = null;
        this.tags.addAll( tags);
    }

    public List<Tag> getTags() {
        return tags;
    }
    public Tag getTag(int i) { return (i < tags.size())? tags.get(i): null;}

    public Tag getLastTag() {
        return (tags.size() > 0)? tags.get(tags.size()-1): null;
    }

    public TagPath getParentTagPath() {
        return (tags.size() < 2) ? new TagPath(): new TagPath( tags.subList(0, tags.size() -1));
    }

    /**
     * Return the tag path as an array of integers.
     *
     * [[sequence-tag, item-number] ...] tag
     * Private tags are in the default block.  It is expected that the  private tags will be resolved correctly
     * in the context of a specific dicom object.
     *
     * @return tag path as int array.
     */
    public int[] getTagsAsArray() {
        List<Integer> tagList = new ArrayList<>();

        for( Tag t: tags) {
            if( t instanceof TagSequence) {
                TagSequence ts = (TagSequence) t;
                tagList.add( ts.getTag().asInt());
                tagList.add( ts.getItemNumberAsInt());
            }
            else {
                tagList.add( t.asInt());
            }
        }

        int[] array = copyListToArray( tagList);
        return array;
    }

    /**
     * convert a list of Integers into an int array.
     *
     * This can be done more concisely and generically with 1.8 features.
     * {@code
     *     int[] array = integerList.stream().mapToInt(i->i).toArray();
     * }
     *
     * @param integerList The list of integer objects to convert.
     * @return  int array.
     */
    static public int[] copyListToArray( List<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        for( int i = 0; i < integerList.size(); i++) {
            intArray[i] = integerList.get(i);
        }
        return intArray;
    }

    /**
     * Return a String representation of this TagPath.
     *
     * @return
     */
    public String getPath() {
        if( path == null) {
            StringBuilder sb = new StringBuilder();
            for (Tag t : tags) {
                sb.append( t.asString()).append(Tag.SEQUENCE_TAG_SEPARATOR);
            }
            sb.setLength(Math.max(sb.length() - 1, 0));
            path = sb.toString();
        }
        return path;
    }

    public int size() {
        return tags.size();
    }
    public boolean isEmpty() { return tags.isEmpty();}

    /**
     * Return a Regular Expression that can be used to match this tagPath against the string
     * representations of other tagPaths.  This regex is used by the isMatch method.
     *
     * @return
     */
    public String getRegex() {
        if (regex == null) regex = computeRegex();
        return regex;
    }

    public boolean isMatch(TagPath testpath) {
        return tagCompare(testpath.getPath(), false);
    }

    public boolean isMatch(Tag testpath) {
        return tagCompare(testpath.asString(), false);
    }

    public boolean isExtendedMatch(TagPath testpath) {
        return tagCompare(testpath.getPath(), true);
    }

    private boolean tagCompare(String tagpath, boolean addStar) {
        // this is usually comparing 2 8-char strings (i.e dicom tag).
        // sometimes the tagpath is more then one tagpath contactenated together
        // sometimes the regex already has a ".*" on the end (but often it doesn't)
        // sometimes the regex has a special char in it that makes it more then 8 chars
        // But it often needs to do actual regex
        // FYI, switching to equals or contains when possible changed a 26 second upload to 18 seconds.
        if (regex == null) {
            regex = computeRegex();
        }
        boolean endsWithStar = regex.endsWith(".*");
        int tagLength = tagpath.length();
        int regexLength = regex.length();

        String localRegex = regex;
        if(regexLength == 10 && endsWithStar) {
            localRegex = regex.substring(0,8);
        }

        if(isNumeric(localRegex)){
            if (tagLength == 8 && localRegex.length() == 8) {
                return tagpath.equals(localRegex);
            }else if(tagLength>8 && localRegex.length() == 8 && addStar) {
                return tagpath.indexOf(localRegex) == 0;
            }
        }

        if(addStar){
            regex = endsWithStar ? regex: regex + ".*";
        }
        return tagpath.matches(regex);
    }

    private boolean isNumeric(String tagpath) {
        final char[] array = tagpath.toCharArray();
        for (final char value : array) {
            if (value >= '0' && value <= '9' || value >= 'a' && value <= 'f' || value >= 'A' && value <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * TagPath is singular if all the Tags are singular and the last Tag is not a sequence.
     *
     * @return true if this TagPath is singular.
     */
    public boolean isSingular() {
        boolean singular = true;
        for( Tag t: tags) {
            if ( ! t.isSingular()) {
                singular = false;
                break;
            }
        }
        return singular;
    }

    private boolean isLastTagSequence() {
        return ( tags.isEmpty())? false: TagSequence.class.isInstance(tags.get(tags.size() - 1));
    }

    /**
     * TagPath is private if any of the tags is private.
     *
     * @return true if any of the tags are private.
     */
    public boolean isPrivate() {
        return tags.stream().anyMatch(Tag::isPrivate);
    }

    /**
     * TagPath is a Private Creator ID if the last tag is an instance of TagPrivateCreator.
     *
     * @return
     */
    public boolean isPrivateCreatorID() {
        return !tags.isEmpty() && tags.get(tags.size() - 1) instanceof TagPrivateCreator;
    }

    /**
     * TagPath is a Sequence if the last tag is an instance of TagSequence.
     *
     * @return
     */
    public boolean isSequence() {
        return !tags.isEmpty() && tags.get(tags.size() - 1) instanceof TagSequence;
    }

    /**
     * isInPrivateBlock
     *
     * @param pvtCreatorIDTagPath private-creator-id tag defining private block.
     * @return true if this tagPath is in the private block.
     */
    public boolean isInPrivateBlock( TagPath pvtCreatorIDTagPath) {
        if( this.isPrivate() && pvtCreatorIDTagPath.isPrivateCreatorID() && this.isInSameItem( pvtCreatorIDTagPath)) {
            TagPrivate thisLastTag = (TagPrivate) getLastTag();
            TagPrivateCreator otherLastTag = (TagPrivateCreator) pvtCreatorIDTagPath.getLastTag();
            if (thisLastTag.isInPrivateCreatorBlock( otherLastTag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test the tagPath for missing creator ID.
     * @return true if any Tag in tagPath is missing its creator ID.
     */
    public boolean hasNullPvtCreatorID() {
        return this.getTags().stream().anyMatch(Tag::hasUnknownCreatorID);
    }

    public boolean isInSameItem( TagPath tagPath) {
        return tagPath.getParentTagPath().equals( getParentTagPath());
    }

    public TagPath getExpectedPvtCreatorID() {
        final Tag lastTag = getLastTag();
        if (!(lastTag instanceof TagPrivate)) {
            return null;
        }
        final TagPath path = new TagPath();
        path.addTags(getParentTagPath().getTags());
        path.addTag(((TagPrivate) getLastTag()).getExpectedCreatorIDTag());
        return path;
    }

    private String computeRegex() {
        StringBuilder sb = new StringBuilder();
        Iterator<Tag> iterator = tags.iterator();
        Tag thisTag = null, nextTag = null;
        while(iterator.hasNext()) {
            thisTag = (thisTag == null)? iterator.next(): nextTag;
            nextTag = (iterator.hasNext())? iterator.next(): null;

            if( thisTag.isSubsequentTagRequired()) {
                thisTag.setSubsequentTag( nextTag);
            }

            sb.append( thisTag.getRegex());
            // TagSequenceWildCards emit the trailing '/'.
            if( ! ((thisTag instanceof TagSequenceWildcard) || nextTag instanceof TagSequenceWildcard)) {
                sb.append("/");
            }

        }
        if( nextTag != null) {
            sb.append( nextTag.getRegex());
        }
        else {
            if( sb.charAt( sb.length()-1) == '/') {
                sb.setLength(Math.max(sb.length() - 1, 0));
            }
        }
        return sb.toString();
    }

    /**
     * convert this TagPath into a PrivateBlock.
     *
     * @return optional PrivateBlock containing this tagPath.
     */
    public Optional<PrivateBlock> getPrivateBlock() {
        TagPrivate tagPrivate = null;
        TagPath pvtBlockPath = new TagPath();
        for( Tag t: this.getTags()) {
            pvtBlockPath.addTag( t);
            if( TagPrivate.class.isInstance( t)) {
                tagPrivate = (TagPrivate) t;
            }
        }
        PrivateBlock pb = null;
        if( tagPrivate != null) {
            pb = new PrivateBlock( pvtBlockPath.getParentTagPath().getTagsAsArray(), tagPrivate.asInt(), tagPrivate.getPvtCreatorID());
        }
        return Optional.ofNullable(pb);
    }

    @Override
    public String toString() {
        return getPath();
    }

    /**
     * Return a String representation of the specified integer tag array.
     *
     * The array is assumed to be tag1, itemnumber1, tag2, itemnumber2, tag3, ....
     * and format is tag1[itemnumber1]/tag2[itemnumber2]/tag3
     *
     * @param ia the tagpath as int array.
     * @return tagpath-formated string represention of int array.
     */
    public static String toString( int[] ia) {
        StringBuilder sb = new StringBuilder();

        for( int i = 0; i < ia.length; i++) {
            if( (i % 2) == 0) {
                sb.append( Integer.toHexString(ia[i]));
            }
            else {
                sb.append("[").append( Integer.toHexString(ia[i])).append("]");
            }
            sb.append("/");
        }
        sb.deleteCharAt( sb.length()-1);

        return sb.toString();
    }

    public Boolean isContainedIn(DicomObjectI dicomObjectI) {
        return null;
    }

    public boolean contains( Tag tag) {
        return tags.stream().anyMatch(t -> t.isMatch(tag));
    }

    @Override
    public int compareTo(TagPath o) {
        if( tags.size() <= o.getTags().size()) {
            int c = 0;
            for( int i = 0; i < tags.size(); i++) {
                c = tags.get(i).compareTo( o.getTags().get(i));
                if( c != 0) {
                    return c;
                }
            }
            return (tags.size() < o.getTags().size())? -1: 0;
        }
        else {
            return -1 * o.compareTo( this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagPath)) return false;
        TagPath tagPath = (TagPath) o;
        return tags.equals(tagPath.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

}
