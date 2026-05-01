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

import static org.junit.Assert.fail;

/**
 * Run tests of list syntax.
 * <p>
 * Syntax only. Doesn't need to make semantic sense.
 */
public class TestListSyntax {

    private static final Logger logger = LoggerFactory.getLogger((TestListSyntax.class));

    @Test()
    public void testSingleItemList() throws IOException {
        try {
            parse(bytes("{\"foo\"}"));
            parse(bytes("{ (0010,0010)}"));
            parse(bytes("{ (0010,0010)/(0008,1234)}"));
            parse(bytes("{ (0010,0010)[3]/(0008,1234)}"));
            parse(bytes("{0}"));
            parse(bytes("{0.5}"));
        } catch (IllegalStateException e) {
            fail("Parse error: " + e);
        }
    }

    @Test
    public void testMultiItemList() throws IOException {
        try {
            parse(bytes("{\"foo\", 3.14, 0, (0010,0010)/(0008,1234)}"));
        } catch (IllegalStateException e) {
            fail("Parse error: " + e);
        }
    }

    @Test
    public void testMultiItemMultiLineList() throws IOException {
        try {
            parse(bytes("{\"foo\", \\\n3.14, \\\n0, (0010,0010)/(0008,1234)}"));
        } catch (IllegalStateException e) {
            fail("Parse error: " + e);
        }
    }

    @Test
    public void testEmptyList() throws IOException {
        try {
            parse(bytes("{}"));
            parse(bytes("{  }"));
            // the newline must be escaped if the list is on multiple lines.
            parse(bytes("{ \\\n }"));
        } catch (IllegalStateException e) {
            fail("Parse error: " + e);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testBadSyntaxMissingCommas() throws IOException {
        String list = "{ // comment 1. \n" +
                "foo bar snafu\n" +
                "}";
        parse(bytes(list));
    }

    public void parse(InputStream in) throws IOException {

        DE6Lexer lexer;
        try {
            ANTLRInputStream ais = new ANTLRInputStream(in);
            lexer = new DE6Lexer(ais);
        } finally {
            if (in != null) in.close();
        }

        TokenStream tokenStream = new CommonTokenStream(lexer);
        DE6Parser parser = new DE6Parser(tokenStream);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });

        ParseTree tree = parser.list();
        logger.info("parse ok: " + tree);
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
