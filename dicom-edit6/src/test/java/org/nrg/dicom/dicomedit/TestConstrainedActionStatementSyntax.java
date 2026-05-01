/*
 * DicomEdit: TestConstrainedActionStatementSyntax
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
 * Run tests of conditional statement syntax.
 *
 * Syntax only. Doesn't need to make semantic sense.
 *
 * Conditional statements have syntax: <value> ('=' | '~') <value> ':' <action>
 * <value> is one of String, number, tagpath, function or ID.
 * <action> is one of assignment, deletion, echo.
 *
 * Created by drm on 8/8/16.
 */
public class TestConstrainedActionStatementSyntax {

    private static final Logger logger = LoggerFactory.getLogger(( TestConstrainedActionStatementSyntax.class));

    @Test
    public void testVariableEqualVariableCondition_VariableAssignedString() throws IOException {
        parse( bytes( "foo = bar ? foo:= \"foobar\"\n"));
    }

    @Test
    public void testVariableEqualVariableCondition_PublicTagAssignedString() throws IOException {
        parse( bytes( "foo = bar ? (0010,0010) := \"foobar\"\n"));
    }

    @Test
    public void testVariableNotEqualVariableCondition_PublicTagAssignedString() throws IOException {
        parse( bytes( "foo != bar ? (0010,0010) := \"foobar\"\n"));
    }

    @Test
    public void testVariableNotMatchesVariableCondition_PublicTagAssignedString() throws IOException {
        parse( bytes( "foo !~ bar ? (0010,0010) := \"foobar\"\n"));
    }

    @Test
    public void testPublicTagEqualPvtTagCondition_PvtTagAssignedFunction() throws IOException {
        parse( bytes( "(2222,1111) = (3333, {pvtCreatorID}22) ? (0013, {CTP}22) := function[ (0020, 000D)]\n"));
    }

    @Test
    public void testFunctionMatchesVariableCondition_PublicTagSequenceDelete() throws IOException {
        parse( bytes( "FUNC[(2222,1111)] ~ var ? - (0080, 1034)/(0010,0010)\n"));
    }

    @Test
    public void testTagSequenceMatchesTagSequenceCondition_TagSequenceDelete() throws IOException {
        parse( bytes( "(1234,4321)[0]/(0019,{GE}22) ~ (0021,{pcid}00)[1]/(0010,0011) ? - (0081, {foo}34)/(0010,0010)[1]/(8456,1111)\n"));
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

        ParseTree tree = parser.statement();
        logger.info("parse ok: " + tree);
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
