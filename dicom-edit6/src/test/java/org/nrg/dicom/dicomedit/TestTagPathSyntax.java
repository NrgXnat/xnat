/*
 * DicomEdit: TestTagPathSyntax
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Run tests of TagPath syntax.
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
 * Created by drm on 8/4/16.
 */
public class TestTagPathSyntax {

    private static final String SIMPLE_PUBLIC_TAG = "(0010,0020)\n";
    private static final String SIMPLE_PUBLIC_TAG_HEXDIGITS = "(ABCE,EFab)\n";
    private static final String SIMPLE_PVT_TAG = "(0013,{CTP}00)\n";
    private static final String PVT_CREATOR_ID_WITH_SPACES = "(0019,{Pvt creator id}00)\n";
    private static final String SIMPLE_PVT_TAG_HEXDIGITS = "(cdef,{CTP}AB)\n";
    private static final String TOPLEVEL_SEQ_ITEMNUMBER = "(0008, 1024)[0]\n";
    private static final String SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM = "(0008, 1024)[1]/(0010,0010)\n";
    private static final String SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM = "(0008, 1024)[1]/(2222,1111)[0]/(0010,0010)\n";
    private static final String SIMPLE_TAG_IN_SPECIFIC_PVT_SEQ_ITEM = "(0013,{CTP}33)[0]/(0010, 0010)\n";
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
    private static final String SEQ_TAG_WILDCARD_ONE_AT_START = ". / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_AT_MIDDLE = "(0008,1024)[%] / . / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_AT_END = "(0008,1024)[%] / (0010,0010) / .\n";
    private static final String SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_START = "+ / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_MIDDLE = "(0008,1024)[%] / + / (0010,0010)\n";
    private static final String SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_END = "(0008,1024)[%] / (0010,0010) / +\n";

    private static final Logger logger = LoggerFactory.getLogger(TestTagPathSyntax.class);

    @Test
    public void testSimplePublicTag() throws IOException {
        parse( bytes( SIMPLE_PUBLIC_TAG));
    }

    @Test
    public void testSimplePublicTagHexDigits() throws IOException {
        parse( bytes( SIMPLE_PUBLIC_TAG_HEXDIGITS));
    }

    @Test
    public void testSimplePvtTag() throws IOException {
        parse( bytes( SIMPLE_PVT_TAG));
    }

    @Test
    public void testPvtCreatorIDWithSpaces() throws IOException {
        parse( bytes( PVT_CREATOR_ID_WITH_SPACES));
    }

    @Test
    public void testSimplePvtTagHexDigits() throws IOException {
        parse( bytes( SIMPLE_PVT_TAG_HEXDIGITS));
    }

    @Test
    public void testTopLevelSeqItemNumber() throws IOException {
        parse( bytes( TOPLEVEL_SEQ_ITEMNUMBER));
    }

    @Test
    public void testSimpleTagInSpecificSeqItem() throws IOException {
        parse( bytes( SIMPLE_TAG_IN_SPECIFIC_SEQ_ITEM));
    }

    @Test
    public void testSimpleTagInNestedSpecificSeqItem() throws IOException {
        parse( bytes( SIMPLE_TAG_IN_NESTED_SPECIFIC_SEQ_ITEM));
    }

    @Test
    public void testSimpleTagSpecificInPvtSeqItem() throws IOException {
        parse( bytes( SIMPLE_TAG_IN_SPECIFIC_PVT_SEQ_ITEM));
    }

    @Test
    public void testSimplePvtTagInSpecificPvtSeqItem() throws IOException {
        parse( bytes( SIMPLE_PVT_TAG_IN_SPECIFIC_PVT_SEQ_ITEM));
    }

    @Test
    public void testPublicTagWithDigitWildcardPosition1() throws IOException {
        parse( bytes( PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION1));
    }

    @Test
    public void testPublicTagWithDigitWildcardPosition2() throws IOException {
        parse( bytes( PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION2));
    }

    @Test
    public void testPublicTagWithDigitWildcardPosition3() throws IOException {
        parse( bytes( PUBLIC_TAG_WITH_DIGIT_WILDCARD_POSITION3));
    }

    @Test
    public void testPublicTagWithEvenDigitWildcardInGroup() throws IOException {
        parse( bytes( PUBLIC_TAG_WITH_EVEN_DIGIT_WILDCARD_IN_GROUP));
    }

    @Test(expected = IllegalStateException.class)
    public void testPublicTagWithOddDigitWildcardInGroup() throws IOException {
        parse( bytes( PUBLIC_TAG_WITH_ODD_DIGIT_WILDCARD_IN_GROUP));
    }

    @Test(expected = IllegalStateException.class)
    public void testPvtTagWithEvenDigitWildcardInGroup() throws IOException {
        parse( bytes( PVT_TAG_WITH_EVEN_DIGIT_WILDCARD_IN_GROUP));
    }

    @Test
    public void testPvtTagWithOddDigitWildcardInGroup() throws IOException {
        parse( bytes( PVT_TAG_WITH_ODD_DIGIT_WILDCARD_IN_GROUP));
    }

    @Test(expected = IllegalStateException.class)
    public void testDigitWildcardPublicPvtAmbiguity() throws IOException {
        parse( bytes( DIGIT_WILDCARD_PUBLIC_PVT_AMBIGUITY));
    }

    @Test
    public void testSimpleTagInWildcardSeqItem() throws IOException {
        parse( bytes( SIMPLE_TAG_IN_WILDCARD_SEQ_ITEM));
    }

    @Test
    public void testSimpleTagInNestedSeqMissingItemnumber() throws IOException {
        parse( bytes( SIMPLE_TAG_IN_NESTED_SEQ_MISSING_ITEMNUMBER));
    }

    @Test
    public void testAllowedExtraWhiteSpace() throws IOException {
        parse( bytes( ALLOWED_EXTRA_WS));
    }

    @Test(expected = IllegalStateException.class)
    public void testPublicTagBadExtraWhiteSpace1() throws IOException {
        parse( bytes( PUBLIC_TAG_BAD_EXTRA_WS1));
    }

    @Test(expected = IllegalStateException.class)
    public void testPublicTagBadExtraWhiteSpace2() throws IOException {
        parse( bytes( PUBLIC_TAG_BAD_EXTRA_WS2));
    }

    @Test
    public void testSeqTagWildcardAnyAtStart() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ANY_AT_START));
    }

    @Test
    public void testSeqTagWildcardAnyAtMiddle() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ANY_AT_MIDDLE));
    }

    @Test
    public void testSeqTagWildcardAnyAtEnd() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ANY_AT_END));
    }

    @Test
    public void testSeqTagWildcardOneAtStart() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_AT_START));
    }

    @Test
    public void testSeqTagWildcardOneAtMiddle() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_AT_MIDDLE));
    }

    @Test
    public void testSeqTagWildcardOneAtEnd() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_AT_END));
    }

    public void testSeqTagWildcardOneMoreAtStart() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_START));
    }

    @Test
    public void testSeqTagWildcardOneMoreAtMiddle() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_MIDDLE));
    }

    @Test
    public void testSeqTagWildcardOneMoreAtEnd() throws IOException {
        parse( bytes( SEQ_TAG_WILDCARD_ONE_OR_MORE_AT_END));
    }

    public void parse( InputStream in) throws IOException {

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
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
