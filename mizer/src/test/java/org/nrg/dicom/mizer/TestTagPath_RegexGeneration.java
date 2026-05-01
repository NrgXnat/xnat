package org.nrg.dicom.mizer;

import org.junit.Test;
import org.nrg.dicom.mizer.tags.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Do TagPaths generate the regular expressions we think they should?
 *
 */
public class TestTagPath_RegexGeneration {

    @Test
    public void testSimplePublicTag() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0010", "0020");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "00100020", regex);
    }

    @Test
    public void testSimplePublicTagHex() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0a10", "0F20");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "0[aA]100[fF]20", regex);
    }

    @Test
    public void testPublicTagWildCard() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0010", "00x0");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "001000[0-9a-fA-F]0", regex);
    }

    @Test
    public void testPublicTagEvenWildCard() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "001@", "0020");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "001[02468aceACE]0020", regex);
    }

    @Test
    public void testPublicTagOddWildCard() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "001#", "A020");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "001[13579bdfBDF][aA]020", regex);
    }

    @Test
    public void testSimplePrivateTag() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPrivate( 0x00191000, "P. C. ID");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "0019\"\\QP. C. ID\\E\"[0-9a-fA-F]{2}00", regex);
    }

    @Test
    public void testPrivateTagWithWildcards() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPrivate( "0019", "P. C. ID", "00XX");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "0019\"\\QP. C. ID\\E\"[0-9a-fA-F]{2}[0-9a-fA-F][0-9a-fA-F]", regex);
    }

    @Test
    public void testSequenceItem() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0010", "0020");
        Tag tagSequence = new TagSequence( tag, 0);
        tp.addTag( tagSequence);
        String regex = tp.getRegex();
        assertEquals( "00100020\\[0\\]", regex);
    }

    @Test
    public void testSequenceItemWildcard() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0010", "0020");
        Tag tagSequence = new TagSequence( tag, "%");
        tp.addTag( tagSequence);
        String regex = tp.getRegex();
        assertEquals( "00100020(\\[[0-9]+\\])?+", regex);
    }

    @Test
    public void testSequence() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagPublic( "0018", "0124");
        Tag tagSequence = new TagSequence( tag, 3);
        tp.addTag( tagSequence);
        tag = new TagPublic( "0008", "0100");
        tp.addTag( tag);
        String regex = tp.getRegex();
        assertEquals( "00180124\\[3\\]/00080100", regex);
    }

    @Test
    public void testSequenceWildcardSplat() throws IOException {
        TagPath tp = new TagPath();
        Tag tag = new TagSequenceWildcard( "*");
        tp.addTag( tag);
        String regex = tp.getRegex();
//        assertEquals( "([0-9a-fA-F]{8}(\\[[0-9]+\\])?/?)*", regex);
        assertEquals( "[\\s\\S]*", regex);
    }

}
