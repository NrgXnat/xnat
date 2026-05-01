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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Run tests of private tag syntax.
 *
 * Syntax only. Doesn't need to make semantic sense.
 *
 * Conditional statements have syntax: <value> ('=' | '~') <value> ':' <action>
 * <value> is one of String, number, tagpath, function or ID.
 * <action> is one of assignment, deletion, echo.
 *
 * Created by drm on 8/8/16.
 */
public class TestPrivateTagSyntax {

    private static final Logger logger = LoggerFactory.getLogger(( TestPrivateTagSyntax.class));

    @Test
    public void testSimpleTag() throws IOException {
        parse( bytes( "(0013, {creator}00)"));
    }

    @Test
    public void testCreatorNameWithSpaces() throws IOException {
        parse( bytes( "(0013, {crea tor}00)"));
    }

    @Test
    public void testCreatorNameWithHyphen() throws IOException {
        parse( bytes( "(0013, {crea-tor}00)"));
    }

    @Test
    public void testCreatorNameWithUnderscore() throws IOException {
        parse( bytes( "(0013, {crea_tor}00)"));
    }

    @Test
    public void testCreatorNameWithDot() throws IOException {
        parse( bytes( "(0013, {creator .}00)"));
    }

    @Test
    public void testVariousRealCreatorNames() throws IOException {
        parse( bytes( "(01F7,{ELSCINT1}9B)"));
        parse( bytes( "(0053,{GEHC_CT_ADVAPP_001}40)"));
        parse( bytes( "(0053,{GEHC_CT_ADVAPP_001}41)"));
        parse( bytes( "(0053,{GEHC_CT_ADVAPP_001}42)"));
        parse( bytes( "(0053,{GEHC_CT_ADVAPP_001}43)"));
        parse( bytes( "(7005,{TOSHIBA_MEC_CT3}0B)"));
    }

    @Test
    public void testCreatorNameWithUID() throws IOException {
        parse( bytes( "(0013, {creator 1.2.3}00)"));
    }

    @Test
    public void testEvenGroup() throws IOException {
        try {
            parse(bytes("(0012, {creator}00)"));
            fail("Failed to throw expected exception.");
        }
        catch( Exception e) {
            assertTrue( e.getMessage().contains("failed to parse"));
        }
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

        ParseTree tree = parser.pvt_tag();
        logger.info("parse ok: " + tree);
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
