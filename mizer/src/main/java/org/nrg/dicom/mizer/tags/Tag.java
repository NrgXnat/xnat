/*
 * DicomEdit: Tag
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * The abstract base class for DICOM tags.
 * <p>
 * All tags can compute their own regex.
 * A tag might need the contents of a subsequent tag to be able to compute their regex. e.g. TagSequenceWildcard with
 * '+' wild card needs to know how to skip its subsequent tag if it occurs first.
 * <p>
 * This implements Comparable. The need to order tags is for backwards compatability with DE4. Ordering tags is
 * problematic because Tags can contain wild cards which means there order is not unique. This implementation puts
 * them in some rational order.  DE4 is only concerned with single valued tags and they order as expected.
 */
public abstract class Tag implements Comparable<Tag> {

    public static final char VERBATIM_MODE_CHAR = '\"';
    public static final char ITEMNUMBER_WILDCARD_CHAR = '%';
    public static final char ITEMNUMBER_DELIMITER_START_CHAR = '[';
    public static final char ITEMNUMBER_DELIMITER_END_CHAR = ']';
    public static final char SEQUENCE_TAG_SEPARATOR = '/';
    public static final char HEX_DIGIT_WILDCARD_CHAR1 = 'X';
    public static final char HEX_DIGIT_WILDCARD_CHAR2 = 'x';
    public static final char HEX_DIGIT_EVEN_WILDCARD = '@';
    public static final char HEX_DIGIT_ODD_WILDCARD = '#';

    public static final Pattern fourHexDigitsAndWildCards = Pattern.compile("[0-9a-fA-FxX@#]{4}");
    public static final Pattern twoHexDigitsAndWildCards = Pattern.compile("[0-9a-fA-FxX@#]{2}");

    protected Tag _subsequentTag;

    private static final Logger logger = LoggerFactory.getLogger(Tag.class);

    public abstract String getGroup();

    public abstract String getElement();

    /**
     * Return String representation of this Tag consistent with matching with the regex from getRegex().
     *
     * @return String consistent for matching with getRegex().
     */
    public abstract String asString();

    public abstract String getVR();

    public abstract void setVR(String vr);

    /**
     * Return the Regular Expression suitable to match this tag.
     *
     * @return String Regular Expression for matching.
     */
    public abstract String getRegex();

    public abstract boolean isMatch(Tag tag);

    public boolean isSubsequentTagRequired() {
        return false;
    }

    public void setSubsequentTag(Tag tag) {
        _subsequentTag = tag;
    }

    /**
     * Return the integer value of this tag.
     * <p>
     * Careful! Private tags only resolve to a unique integer in the context of a particular dicom object. This
     * returns private tags in the first block, "10ee" and may not be the appropriate mapping in a particular context.
     * Use DicomObject's resolveTag method to do this.
     * <p>
     * Throws NumberFormatException if this tag has wild cards and does not map to a unique integer.
     * Use "isSingular()" first to test this.
     * <p>
     * The tag is parsed as unsigned, so the full 0x00000000 - 0xFFFFFFFF range round-trips through the returned
     * int's bit pattern:
     * 0x7fffffff maps to max int ( 2147483647)
     * 0x80000000 maps to min int (-2147483648)
     * 0xffffffff maps to -1
     *
     * @return The value of the tag as an integer.
     */
    public int asInt() {
        return Integer.parseUnsignedInt(getGroup() + getElement(), 16);
    }

    /**
     * getGroupAsInt
     *
     * @return the integer value of this tag's group.
     * @throws NumberFormatException if this tag does not resolve to a single group due to wild cards.
     */
    public int getGroupAsInt() {
        return Integer.parseUnsignedInt(getGroup(), 16);
    }

    /**
     * getElementAsInt
     *
     * @return the integer value of this tag's element (default block for private tags).
     * @throws NumberFormatException if this tag does not resolve to a single element due to wild cards.
     */
    public int getElementAsInt() {
        return Integer.parseUnsignedInt(getElement(), 16);
    }

    /**
     * Map digit wild cards to their max value for purposes of uniqueness and ordering.
     *
     * @return
     */
    public long asLong() {
        String s = getGroup() + getElement();
        s = s.replaceAll("[xX]", "F");
        s = s.replaceAll("[#]", "F");
        s = s.replaceAll("[@]", "E");
        return Long.parseLong(s, 16);
    }

    /**
     * Return true if this tag does not contain wildcards and thus maps to a unique integer.
     *
     * @return Whether the tag is singular.
     */
    public abstract boolean isSingular();

    public abstract boolean isPrivate();

    public abstract boolean isPrivateCreatorID();

    public boolean hasUnknownCreatorID() {
        return false;
    }

    /**
     * Return true if wild card char is detected in string.
     * <p>
     * TODO: let the parser do this work.
     *
     * @param s String containing the group or element portion of a tag.
     * @return true if the component contains a wild card char.
     */
    public boolean hasWildCard(String s) {
        if (s != null) {
            char[] chars = s.toCharArray();
            for (char c : chars) {
                switch (c) {
                    case ITEMNUMBER_WILDCARD_CHAR:
                    case HEX_DIGIT_WILDCARD_CHAR1:
                    case HEX_DIGIT_WILDCARD_CHAR2:
                    case HEX_DIGIT_EVEN_WILDCARD:
                    case HEX_DIGIT_ODD_WILDCARD:
                        return true;
                    default:
                        break;
                }
            }
        }
        return false;
    }

    public String getValidGroup(String s) {
        if (fourHexDigitsAndWildCards.matcher(s).matches()) {
            return s;
        } else {
            throw new IllegalArgumentException(String.format("Invalid group: '%s'", s));
        }
    }

    public String getValidElement(String s) {
        if (fourHexDigitsAndWildCards.matcher(s).matches()) {
            return s;
        } else if (twoHexDigitsAndWildCards.matcher(s).matches()) {
            return TagPrivate.DEFAULT_ELEMENT_BLOCK + s;
        } else {
            throw new IllegalArgumentException(String.format("Invalid element: '%s'", s));
        }
    }

    /**
     * Adds enough of the the specified padding character to the submitted string to make the resulting string at least
     * as long as the specified minimum width.
     *
     * @param string   The string to be left-padded.
     * @param minWidth The width to which the string should be padded.
     * @param padChar  The character to use to left-pad the resulting string.
     * @return The submitted string along with the required left padding.
     */
    public static String padLeft(String string, int minWidth, char padChar) {
        if (string.length() >= minWidth) {
            return string;
        } else {
            int padSize = minWidth - string.length();
            StringBuilder sb = new StringBuilder(minWidth);
            for (int i = 0; i < padSize; i++) {
                sb.append(padChar);
            }
            sb.append(string);
            return sb.toString();
        }
    }

    protected String computeBlockRegex(String s) {
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        for (char c : chars) {
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    sb.append(c);
                    break;
                case 'a':
                case 'A':
                    sb.append("[aA]");
                    break;
                case 'b':
                case 'B':
                    sb.append("[bB]");
                    break;
                case 'c':
                case 'C':
                    sb.append("[cC]");
                    break;
                case 'd':
                case 'D':
                    sb.append("[dD]");
                    break;
                case 'e':
                case 'E':
                    sb.append("[eE]");
                    break;
                case 'f':
                case 'F':
                    sb.append("[fF]");
                    break;
                case HEX_DIGIT_WILDCARD_CHAR1:
                case HEX_DIGIT_WILDCARD_CHAR2:
                    sb.append("[0-9a-fA-F]");
                    break;
                case HEX_DIGIT_ODD_WILDCARD:
                    sb.append("[13579bdfBDF]");
                    break;
                case HEX_DIGIT_EVEN_WILDCARD:
                    sb.append("[02468aceACE]");
                    break;
                default:
                    String msg = String.format("Unexpected character in tag block: %s", c);
                    logger.error(msg);
                    throw new IllegalArgumentException(msg);
            }
        }
        return sb.toString();
    }

    /**
     * Order tags numerically.
     *
     * @param o
     * @return
     */
    @Override
//    public int compareTo(Tag o) {
//        int c = getGroup().compareTo( o.getGroup());
//        return (c != 0)? c: getElement().compareTo( o.getElement());
//    }
    public int compareTo(Tag o) {
        long delta = this.asLong() - o.asLong();
        if (delta < 0) return -1;
        else if (delta > 0) return 1;
        else return 0;
    }

    public static int getGroup(int tag) {
        return (tag & 0xFFFF0000) >>> 16;
    }

    public static int getPrivateCreatorBlock(int privateCreatorIDTag) {
        return (privateCreatorIDTag & 0x000000FF);
    }

    public static int getPrivateBlock(int privateTag) {
        return (privateTag & 0x0000FF00) >>> 8;
    }

    public static int getTag(int group, int block, int element) {
        int tag = group << 16;
        tag += (block & 0x000000FF) << 8;
        tag += (element & 0x000000FF);
        return tag;
    }

    public static boolean isPrivateDataTag(int tag) {
        return (tag & 0x00010000) != 0;
    }

    public static boolean isPrivateCreatorDataTag(int tag) {
        return (tag & 0x00010000) != 0 && (tag & '\uff00') == 0;
    }

    public static int getPrivateCreatorIDTag(int tag) throws IllegalArgumentException {
        if (Tag.isPrivateCreatorDataTag(tag)) return tag;
        else if (Tag.isPrivateDataTag(tag)) return Tag.getTag(Tag.getGroup(tag), 0, Tag.getPrivateBlock(tag));
        else
            throw new IllegalArgumentException(String.format("Cannot find private-creator-id tag for tag 0x%08X", tag));
    }

}
