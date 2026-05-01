/*
 * DicomEdit: TestTagPathMatching
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import org.nrg.dicom.mizer.tags.TagPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * 8. Tolerated extra whitespace.
 *
 * Created by drm on 2020/05/05.
 */
public class TestTagPathMatching {

    private static final String PUBLIC_TAG = "(001A,001f)\n";
    private static final String PUBLIC_TAG_MATCH_LITERAL = "(001a,001F)";

    private static final String PUBLIC_TAG_DIGIT_WILDCARD = "(001a,001X)";
    private static final String PUBLIC_TAG_DIGIT_WILDCARD_MATCH1 = "(001a,001a)";
    private static final String PUBLIC_TAG_DIGIT_WILDCARD_MATCH2 = "(x01a,001a)";  // should not match
    private static final String PUBLIC_TAG_DIGIT_WILDCARD_MATCH3 = "(001a,001x)";  // should not match
    private static final String PUBLIC_TAG_DIGIT_WILDCARD_MATCH4 = "(001@,0010)";  // should not match

    private static final String PRIVATE_TAG = "(001B,{A private tag.}1f)";
    private static final String PRIVATE_TAG_MATCH_LITERAL = "(001B,{A private tag.}1f)";

    private static final String TOPLEVEL_SEQ_ITEMNUMBER = "(0008,1024)[0]";
    private static final String TOPLEVEL_SEQ_ITEMNUMBER_MATCH1 = "(0008,1024)[0]";
    private static final String TOPLEVEL_SEQ_ITEMNUMBER_MATCH2 = "(0008,1024)";  // should not match: substring.
    private static final String TOPLEVEL_SEQ_ITEMNUMBER_MATCH3 = "(0008,1024)[1]";  // should not match

    private static final String TOPLEVEL_SEQ_ITEMNUMBER_WILDCARD = "(0008,1024)[%]";
    private static final String TOPLEVEL_SEQ_ITEMNUMBER_WILDCARD_MATCH1 = "(0008,1024)[0]";

    private static final String SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM = "(0008,1024)[1]/(0010,0010)";
    private static final String SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM_MATCH1 = "(0008,1024)[1]/(0010,0010)";

    private static final String SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM = "(0008,1024)[1]/(2222,1111)[0]/(0010,0010)\n";
    private static final String SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM_MATCH1 = "(0008,1024)[1]/(2222,1111)[0]/(0010,0010)\n";

    private static final String TAG_IN_SPECIFIC_PVT_SEQ_ITEM = "(0013,{CTP}33)[0]/(0010,0010)";
    private static final String TAG_IN_SPECIFIC_PVT_SEQ_ITEM_MATCH1 = "(0013,{CTP}33)[0]/(0010,0010)";


    private static final String SIMPLE_PUBLIC_TAG = "(0010,0020)\n";
    private static final String SIMPLE_PUBLIC_TAG_HEXDIGITS = "(ABCE,EFab)\n";
    private static final String SIMPLE_PVT_TAG = "(0013,{CTP}00)\n";
    private static final String PVT_CREATOR_ID_WITH_SPACES = "(0019,{Pvt creator id}00)\n";
    private static final String SIMPLE_PVT_TAG_HEXDIGITS = "(cdef,{CTP}AB)\n";
//    private static final String TOPLEVEL_SEQ_ITEMNUMBER = "(0008, 1024)[0]\n";
//    private static final String SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM = "(0008, 1024)[1]/(0010,0010)\n";
//    private static final String SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM = "(0008, 1024)[1]/(2222,1111)[0]/(0010,0010)\n";
//    private static final String SIMPLE_TAG_IN_SPECIFIC_PVT_SEQ_ITEM = "(0013,{CTP}33)[0]/(0010, 0010)\n";
    private static final String SIMPLE_PVT_TAG_IN_SPECIFIC_PVT_SEQ_ITEM = "(0013,{CTP}33)[0]/(0013,{CTP}10)\n";
    private static final String PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION1 = "(0010,000X)\n";
    private static final String PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION2 = "(x010,000X)\n";
    private static final String PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION3 = "(xXx0,XXXX)\n";
    private static final String PUBLIC_TAG_WITH_EVEN_DIGIT_WILDCARD_IN_GROUP = "(001@,0010)\n";
    private static final String PUBLIC_TAG_WITH_ODD_DIGIT_WILDCARD_IN_GROUP = "(001#,0010)\n";    // expect to fail.
    private static final String PVT_TAG_WITH_ODD_DIGIT_WILDCARD_IN_GROUP = "(001#,{STENTOR}10)\n";
    private static final String PVT_TAG_WITH_EVEN_DIGIT_WILDCARD_IN_GROUP = "(001@,{STENTOR}10)\n";  // expect to fail.
    private static final String DIGIT_WILDCARD_PUBLIC_PVT_AMBIGUITY = "(001x,0020)\n";  // expect to fail
    private static final String SIMPLE_TAG_IN_WILDCARD_SEQ_ITEM = "(0008,1024)[%]/(0010,0010)\n";
    private static final String SIMPLE_TAG_IN_NESTED_SEQ_MISSING_ITEMNUMBER = "(0008,1024)/(00a0,0010)/(0010,0010)\n";
    private static final String ALLOWED_EXTRA_WS = "(0008 , 1024) [%] / (00ab , {Foo}10) / (0010 , 0010) \n";
    private static final String PUBLIC_TAG_BAD_EXTRA_WS1 = "( 0008,1024)\n"; // expect to fail.
    private static final String PUBLIC_TAG_BAD_EXTRA_WS2 = "(0008,1024 )\n"; // expect to fail.
    private static final String SEQ_TAG_WILDCARD_ANY_AT_START = "* / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ANY_AT_MIDDLE = "(0008,1024)[%] / * / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ANY_AT_END = "(0008,1024)[%] / (0010,0010) / *\n";
    private static final String SEQ_TAG_WILDCARD_ONE_AT_START = "? / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_AT_MIDDLE = "(0008,1024)[%] / ? / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_AT_END = "(0008,1024)[%] / (0010,0010) / ?\n";

    private static final Logger logger = LoggerFactory.getLogger(TestTagPathMatching.class);

    @Test
    public void testSimplePublicTag() throws IOException {
        TagPath tagPath = parseTagPath( bytes(PUBLIC_TAG));
        TagPath testPath = parseTagPath( bytes(PUBLIC_TAG_MATCH_LITERAL));
        assertTrue( tagPath.isExtendedMatch( testPath));
    }

    @Test
    public void testHexDigitWildcard() throws IOException {
        TagPath tagPath = parseTagPath( bytes(PUBLIC_TAG_DIGIT_WILDCARD));
        TagPath testPath = parseTagPath( bytes(PUBLIC_TAG_DIGIT_WILDCARD_MATCH1));
        assertTrue( tagPath.isExtendedMatch( testPath));

        testPath = parseTagPath( bytes(PUBLIC_TAG_DIGIT_WILDCARD_MATCH2));
        assertFalse( tagPath.isExtendedMatch( testPath));

        testPath = parseTagPath( bytes(PUBLIC_TAG_DIGIT_WILDCARD_MATCH3));
        assertFalse( tagPath.isExtendedMatch( testPath));

        testPath = parseTagPath( bytes(PUBLIC_TAG_DIGIT_WILDCARD_MATCH4));
        assertFalse( tagPath.isExtendedMatch( testPath));
    }

    @Test
    public void testSimplePrivateTag() throws IOException {
        TagPath tagPath = parseTagPath( bytes(PRIVATE_TAG));
        TagPath testPath = parseTagPath( bytes(PRIVATE_TAG_MATCH_LITERAL));
        assertTrue( tagPath.isExtendedMatch( testPath));
    }

    @Test
    public void testTopLevelSequenceItem() throws IOException {
        TagPath tagPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER));
        TagPath testPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER_MATCH1));
        assertTrue( tagPath.isMatch( testPath));

        testPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER_MATCH2));
        assertFalse( tagPath.isMatch( testPath));

        testPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER_MATCH3));
        assertFalse( tagPath.isMatch( testPath));
    }

    @Test
    public void testTopLevelSequenceItemWildcard() throws IOException {
        TagPath tagPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER_WILDCARD));
        TagPath testPath = parseTagPath( bytes(TOPLEVEL_SEQ_ITEMNUMBER_WILDCARD_MATCH1));
        assertTrue( tagPath.isExtendedMatch( testPath));
    }

    @Test
    public void testTagInSpecificSeqItem() throws IOException {
        TagPath tagPath = parseTagPath( bytes(SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM));
        TagPath testPath = parseTagPath( bytes(SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM_MATCH1));
        assertTrue( tagPath.isMatch( testPath));
    }

    @Test
    public void testTagInNestedSpecificSeqItem() throws IOException {
        TagPath tagPath = parseTagPath( bytes(SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM));
        TagPath testPath = parseTagPath( bytes(SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM_MATCH1));
        assertTrue( tagPath.isMatch( testPath));
    }

    @Test
    public void testTagInSpecificPvtSeqItem() throws IOException {
        TagPath tagPath = parseTagPath( bytes(TAG_IN_SPECIFIC_PVT_SEQ_ITEM));
        TagPath testPath = parseTagPath( bytes(TAG_IN_SPECIFIC_PVT_SEQ_ITEM_MATCH1));
        assertTrue( tagPath.isMatch( testPath));
    }

    public TagPath parseTagPath( InputStream in) throws IOException {

        DE6Lexer lexer;
        try {
            ANTLRInputStream ais = new ANTLRInputStream(in);
            lexer = new DE6Lexer( ais);
        }
        finally {
            if( in != null) in.close();
        }

        TokenStream tokenStream = new CommonTokenStream(lexer);
        DE6Parser   parser      = new DE6Parser(tokenStream);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });

        ParseTree tree = parser.tagpath();
        logger.debug("parse ok: " + tree);

        DicomEditParseTreeVisitor visitor = new DicomEditParseTreeVisitor();
        TagPath tagpath = (TagPath) visitor.visitTagpath((DE6Parser.TagpathContext) tree).asObject();
        logger.debug("tagpath: " + tagpath);
        return tagpath;
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
