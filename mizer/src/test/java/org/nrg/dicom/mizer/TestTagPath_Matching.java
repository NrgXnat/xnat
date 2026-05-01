/*
 * DicomEdit: TestTagPathSyntax
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer;

import org.junit.Test;
import org.nrg.dicom.mizer.tags.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run tests of TagPath matching.
 *
 * Attempts to provide adequate coverage of the following features:
 * 1. Public Tags
 * 2. Private Tags
 * 3. Tag wildcard characters
 *   a. 'x' or 'X' stand for any hex digit
 *   b. '@' stands for any even hex digit
 *   c. '#' stands for any odd hex digit.
 *   The semantics of these wildcards are not tested here but help inform why the syntax is like it is.
 * 4. Sequences of Public and Private Tags nested arbitrarily deep.
 * 5. Sequence tag wild cards: '*' and '?'
 * 6. Sequence items can have a specific item number reference or no reference.
 * 7. Sequence item number wild card: '%'
 *
 * Created by drm on 2020/05/05.
 */
public class TestTagPath_Matching {

    @Test
    public void testSimplePublicTag() {
        // (001A,001f)
        TagPath tp = new TagPath();
        tp.addTag( new TagPublic( "001A", "001f"));

        // (001a,001F)  true
        TagPath test_tp = new TagPath();
        test_tp.addTag(  new TagPublic( "001a", "001F"));
        assertTrue( tp.isMatch( test_tp));
    }

    @Test
    public void testHexDigitWildcard() {
        // (001a,001X)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic( "001a","001X");
        tagPath.addTag( tag);

        // (001a,001a) true
        TagPath testPath = new TagPath();
        tag = new TagPublic( 0x001a001a);
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));

        // (001a,0011)  true
        testPath = new TagPath();
        tag = new TagPublic( "001a","0011");
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));

        // (001a,1011)  false
        testPath = new TagPath();
        tag = new TagPublic( "001a","101a");
        testPath.addTag( tag);
        assertFalse( tagPath.isMatch( testPath));
    }

    @Test
    public void testHexDigitWildcard2() {
        // (xXx0,XXXX)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic( "xXx0","XXXX");
        tagPath.addTag( tag);

        // (0010,001a) true
        TagPath testPath = new TagPath();
        tag = new TagPublic( 0x0010001a);
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));

        // (001a,001a)  false
        testPath = new TagPath();
        tag = new TagPublic( "001a","001a");
        testPath.addTag( tag);
        assertFalse( tagPath.isMatch( testPath));

        // (1110,4321)   true
        testPath = new TagPath();
        tag = new TagPublic( "1110","4321");
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));

    }

    @Test
    public void testEvenDigitWildcard() {
        // (001@,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic( "001@","0010");
        tagPath.addTag( tag);

        // (0010,0010)  true
        TagPath testPath = new TagPath();
        tag = new TagPublic( 0x00100010);
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));

        // (001b,0010)  false
        testPath = new TagPath();
        tag = new TagPublic( 0x001b0010);
        testPath.addTag( tag);
        assertFalse( tagPath.isMatch( testPath));
    }

    @Test
    public void testOddDigitWildcard() {
        // (001#,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic( "001#","0010");
        tagPath.addTag( tag);

        // (0010,0010)  false
        TagPath testPath = new TagPath();
        tag = new TagPublic( 0x00100010);
        testPath.addTag( tag);
        assertFalse( tagPath.isMatch( testPath));

        // (001b,0010)  true
        testPath = new TagPath();
        tag = new TagPublic( 0x001b0010);
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));
    }

    @Test
    public void testSimplePrivateTag() {
        // (001B,{A private tag.}1f)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPrivate( "001B", "A private tag.", "1f");
        tagPath.addTag( tag);

        // (001B,{A private tag.}1f)   true
        TagPath testPath = new TagPath();
        tag = new TagPrivate( "001B", "A private tag.", "1f");
        testPath.addTag( tag);
        assertTrue( tagPath.isMatch( testPath));
    }

    @Test
    public void testTopLevelSequenceItem() {
        // (0008,1024)[0]
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic( 0x00081024);
        Tag tagseq = new TagSequence( tag, "0");
        tagPath.addTag( tagseq);

        // (0008,1024)[0]   true
        TagPath testPath = new TagPath();
        tag = new TagPublic( 0x00081024);
        tagseq = new TagSequence( tag, "0");
        testPath.addTag( tagseq);
        assertTrue( tagPath.isMatch( testPath));

        // (0008,1024)   false
        testPath = new TagPath();
        tag = new TagPublic( 0x00081024);
        testPath.addTag( tag);
        assertFalse( tagPath.isMatch( testPath));

        // (0008,1024)[1]   false
        testPath = new TagPath();
        tag = new TagPublic( 0x00081024);
        tagseq = new TagSequence( tag, "1");
        testPath.addTag( tagseq);
        assertFalse( tagPath.isMatch( testPath));
    }

    @Test
    public void testTopLevelSequenceItemWildcard() {
        // (0008,1024)[%]
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic(0x00081024);
        Tag tagseq = new TagSequence(tag, "%");
        tagPath.addTag(tagseq);

        // (0008,1024)[0]   true
        TagPath testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        assertTrue(tagPath.isMatch(testPath));

        // (0008,1024)[0]/(0010,0010)   false
        testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, 0);
        testPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        tagPath.addTag( tag);
        assertFalse(tagPath.isMatch( testPath));
    }

    @Test
    public void testTagInSpecificSeqItem() {
        // (0008,1024)[1]/(0010,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic(0x00081024);
        Tag tagseq = new TagSequence(tag, "1");
        tagPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        tagPath.addTag( tag);

        // (0008,1024)[1]/(0010,0010)    true
        TagPath testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, "1");
        testPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        testPath.addTag( tag);
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)[0]/(0010,0010)    false
        testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        testPath.addTag( tag);
        assertFalse(tagPath.isMatch( testPath));
    }

    @Test
    public void testTagInNestedSpecificSeqItem() {
        // (0008,1024)[1]/(2222,1111)[0]/(0010,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic(0x00081024);
        Tag tagseq = new TagSequence(tag, "1");
        tagPath.addTag(tagseq);
        tag = new TagPublic(0x22221111);
        tagseq = new TagSequence(tag, "0");
        tagPath.addTag(tagseq);
        tag = new TagPublic(0x00100010);
        tagPath.addTag(tag);

        // (0008,1024)[1]/(2222,1111)[0]/(0010,0010)   true
        TagPath testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, "1");
        testPath.addTag(tagseq);
        tag = new TagPublic(0x22221111);
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        tag = new TagPublic(0x00100010);
        testPath.addTag(tag);
        assertTrue(tagPath.isMatch(testPath));
    }

    @Test
    public void testNestedSequenceMissingItemNumbers() {
        // (0008,1024)/(00a0,0010)/(0010,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPublic(0x00081024);
        Tag tagseq = new TagSequence(tag, null);
        tagPath.addTag(tagseq);
        tag = new TagPublic( 0x00a00010);
        tagseq = new TagSequence(tag, null);
        tagPath.addTag(tagseq);
        tag = new TagPublic(0x00100010);
        tagPath.addTag(tag);

        // (0008,1024)[1]/(2222,1111)[0]/(0010,0010)   true
        TagPath testPath = new TagPath();
        tag = new TagPublic(0x00081024);
        tagseq = new TagSequence(tag, "1");
        testPath.addTag(tagseq);
        tag = new TagPublic( 0x00a00010);
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        tag = new TagPublic(0x00100010);
        testPath.addTag(tag);
        assertTrue(tagPath.isMatch(testPath));
    }

    @Test
    public void testTagInSpecificPvtSeqItem() {
        // (0013,{CTP}33)[0]/(0010,0010)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPrivate( "0013","CTP", "33");
        Tag tagseq = new TagSequence(tag, "0");
        tagPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        tagPath.addTag( tag);

        // (0013,{CTP}33)[0]/(0010,0010)   true
        TagPath testPath = new TagPath();
        tag = new TagPrivate( "0013","CTP", "33");
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        tag = new TagPublic( 0x00100010);
        testPath.addTag( tag);
        assertTrue(tagPath.isMatch( testPath));
    }

    @Test
    public void testPvtTagInSpecificPvtSeqItem(){
        // (0013,{CTP}33)[0]/(0013,{ C.T P. 1}10)
        TagPath tagPath = new TagPath();
        Tag tag = new TagPrivate( "0013","CTP", "33");
        Tag tagseq = new TagSequence(tag, "0");
        tagPath.addTag(tagseq);
        tag = new TagPrivate( "0013"," C.T P. 1", "10");
        tagPath.addTag( tag);

        // (0013,{CTP}33)[0]/(0013,{ C.T P. 1}10)   true
        TagPath testPath = new TagPath();
        tag = new TagPrivate( "0013","CTP", "33");
        tagseq = new TagSequence(tag, "0");
        testPath.addTag(tagseq);
        tag = new TagPrivate( "0013"," C.T P. 1", "10");
        testPath.addTag( tag);
        assertTrue(tagPath.isMatch( testPath));
    }

    @Test
    public void testSeqWildcardAnyAtStart() {
        // * / (0010,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard( "*"));
        tagPath.addTag( new TagPublic( 0x00100010));

        // (0010,0010)    true
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)/(0008,0100)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag( new TagSequence( new TagPublic( 0x00100010), null));
        testPath.addTag( new TagPublic( 0x00080100));
        assertFalse(tagPath.isMatch( testPath));

        // (0013,{CTP}13)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPrivate( 0x00130013, "CTP"), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));
    }

    @Test
    public void testSeqWildcardAnyAtStartAndEnd() {
        // * / (0010,0010) / *
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard( "*"));
        tagPath.addTag( new TagSequence( new TagPublic( 0x00100010), null));
        tagPath.addTag( new TagSequenceWildcard( "*"));
        String regex = tagPath.getRegex();

        // (0010,0010)    true
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)/(0008,0100)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag( new TagSequence( new TagPublic( 0x00100010), null));
        testPath.addTag( new TagPublic( 0x00080100));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)[1]/(0008,0100)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag(  new TagSequence( new TagPublic( 0x00100010), 1));
        testPath.addTag( new TagPublic( 0x00080100));
        assertTrue(tagPath.isMatch( testPath));
    }

    @Test
    public void testSeqWildcardAnyAtStartAndPrivateTags() {
        // * / (0013,{CTP}10)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard("*"));
        tagPath.addTag( new TagPrivate(0x00101010, "CTP"));
        String regex = tagPath.getRegex();

        // (0013,{CTP}10)    true
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPrivate(0x00101010, "CTP"));
        assertTrue(tagPath.isMatch(testPath));

        // (0008,0124) / (0013,{CTP}10)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00080124), null));
        testPath.addTag( new TagPrivate(0x00101010, "CTP"));
        assertTrue(tagPath.isMatch(testPath));
    }

    @Test
    public void testSeqWildcardOneOrMoreAtStart() {
        // + / (0010,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard("+"));
        tagPath.addTag( new TagPublic(0x00100010));
        String regex = tagPath.getRegex();

        // (0010,0010)    false
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic(0x00100010));
        assertFalse(tagPath.isMatch(testPath));

        // (0018,012A) / (0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x0018012A));
        testPath.addTag( new TagPublic(0x00100010));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A) / (0010,0010) / (0018,1234) / (0010,0010)   true
        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x0018012A));
        testPath.addTag( new TagPublic( 0x00100010));
        testPath.addTag( new TagPublic( 0x00181234));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A)[1] / (0010,0010)[2] / (0018,1234)[3] / (0010,0010)   true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x0018012A), 1));
        testPath.addTag( new TagSequence( new TagPublic( 0x00100010), 2));
        testPath.addTag( new TagSequence( new TagPublic( 0x00181234), 3));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A) / (0010,0010) / (0018,1234) / (0010,0010) / (0008,000E)   false
        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x0018012A));
        testPath.addTag( new TagPublic( 0x00100010));
        testPath.addTag( new TagPublic( 0x00181234));
        testPath.addTag( new TagPublic( 0x00100010));
        testPath.addTag( new TagPublic( 0x0008000E));
        assertFalse(tagPath.isMatch(testPath));

        // (0018,012A) / (0010,0010) / (0018,1234) / (0010,0010) / (0008,000E)   false
        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        testPath.addTag( new TagPublic( 0x00181234));
        testPath.addTag( new TagPublic( 0x00100010));
        testPath.addTag( new TagPublic( 0x0008000E));
        assertFalse(tagPath.isMatch(testPath));
    }

    @Test
    public void testPvtTagSeqWildcardOneOrMoreAtStart() {
        // + / (0013, {A. B. 1.}00)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard("+"));
        tagPath.addTag( new TagPrivate(0x00131000, "A. B. 1."));
        String regex = tagPath.getRegex();

        // (0013, {A. B. 1.}00)   false
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPrivate(0x00131000, "A. B. 1."));
        assertFalse(tagPath.isMatch(testPath));

        // (0018,012A) / (0013, {A. B. 1.}00)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x0018012A), null));
        testPath.addTag( new TagPrivate(0x00131000, "A. B. 1."));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A) / (0028,ABCD)/ (0013, {A. B. 1.}00)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x0018012A), null));
        testPath.addTag( new TagSequence( new TagPublic( 0x0028ABCD), null));
        testPath.addTag( new TagPrivate(0x00131000, "A. B. 1."));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A) / (0013, {A. B. 1.}00) / (0018,1234) / (0013, {A. B. 1.}00)   true
        testPath = new TagPath();
        testPath.addTag(  new TagSequence( new TagPublic( 0x0018012A), null));
        testPath.addTag( new TagSequence( new TagPrivate(0x00131000, "A. B. 1."), null));
        testPath.addTag( new TagSequence( new TagPublic( 0x00181234), null));
        testPath.addTag( new TagPrivate(0x00131000, "A. B. 1."));
        assertTrue(tagPath.isMatch(testPath));

        // (0018,012A) / (0013, {A. B. 1.}00) / (0018,1234)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x0018012A), null));
        testPath.addTag( new TagSequence( new TagPrivate(0x00131000, "A. B. 1."), null));
        testPath.addTag( new TagPublic( 0x00181234));
        assertFalse( tagPath.isMatch(testPath));
    }

    @Test
    public void testSeqWildcardOneAtStart() {
        // . / (0010,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequenceWildcard( "."));
        tagPath.addTag( new TagPublic( 0x00100010));
        String regex = tagPath.getRegex();

        // (0010,0010)    false
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        assertFalse(tagPath.isMatch( testPath));

        // (0008,1024)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence(  new TagPublic(0x00081024), null));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0008,1024)/(0010,0010)/(0008,0100)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00081024), null));
        testPath.addTag( new TagSequence( new TagPublic( 0x00100010), null));
        testPath.addTag(  new TagPublic( 0x00080100));
        assertFalse(tagPath.isMatch( testPath));

        // (0013,{CTP}13)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPrivate( 0x00130013, "CTP"), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));
    }

    @Test
    public void testSeqWildcardOneInMiddle() {
        // (0018,9876) / . / (0010,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic( 0x00189876), null));
        tagPath.addTag( new TagSequenceWildcard( "."));
        tagPath.addTag( new TagPublic( 0x00100010));
        String regex = tagPath.getRegex();

        // (0010,0010)    false
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        assertFalse(tagPath.isMatch( testPath));

        // (0018,9876)[1]/(0010,0010)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00189876), "1"));
        testPath.addTag( new TagPublic( 0x00100010));
        assertFalse(tagPath.isMatch( testPath));

        // (0018,9876)[1]/(0010,0010)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00189876), 1));
        testPath.addTag( new TagPublic( 0x00100010));
        assertFalse(tagPath.isMatch( testPath));

        // (0018,9876)/(0010,0010)/(0008,0100)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00189876), null));
        testPath.addTag(  new TagSequence( new TagPublic( 0x00100010), null));
        testPath.addTag( new TagPublic( 0x00080100));
        assertFalse(tagPath.isMatch( testPath));

        // (0018,9876)[1]/(0010,0010)[0]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x00189876), 1));
        testPath.addTag(  new TagSequence( new TagPublic( 0x00100010), 0));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0018,9876)[0]/(0018,0120)[4]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x00189876), 0));
        testPath.addTag(  new TagSequence( new TagPublic( 0x00180120), 4));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0018,9876)[0]/(0013,{C T P .}13)[1]/(0010,0010)    true
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic( 0x00189876), 0));
        testPath.addTag( new TagSequence( new TagPrivate( 0x00130013, "C T P ."), 1));
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue(tagPath.isMatch( testPath));

        // (0013,{CTP}13)[1]/(0010,0010)    false
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPrivate( 0x00130013, "CTP"), "1"));
        testPath.addTag(  new TagPublic( 0x00100010));
        assertFalse(tagPath.isMatch( testPath));
    }

}
