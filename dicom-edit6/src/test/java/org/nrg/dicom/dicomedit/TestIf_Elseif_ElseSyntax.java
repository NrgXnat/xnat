/*
 * DicomEdit: TestFunctionSyntax
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
 * Run tests of if-elsif-else syntax.
 *
 * Syntax only. Doesn't need to make semantic sense.
 *
 * These statements have syntax: if(condition) {statements+} (elseif (condition) { statements+})* (else {statements+})?
 * - required if fragment, zero or more elseif fragments, optional else fragment
 */
public class TestIf_Elseif_ElseSyntax {

    private static final Logger logger = LoggerFactory.getLogger(( TestConstrainedActionStatementSyntax.class));

    @Test
    public void testIfTerm() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\"){(0010,0010) := bar}\n"));
    }

    @Test
    public void testIfTermNewLineFormatting() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\")\n" +
                "{\n" +
                "(0010,0010) := bar\n" +
                "}\n"));
    }

    @Test
    public void testIf_Else() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\") {\n" +
                "(0010,0010) := bar\n" +
                "}\n" +
                "else {\n" +
                "(0010,0010) := bar\n" +
                "}\n"));
    }

    @Test
    public void testIf_One_Elseif() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\") {\n" +
                "(0010,0010) := bar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"snafu\"){\n" +
                "(0010,0010) := fubar\n" +
                "}\n"));
    }

    @Test
    public void testIf_Two_Elseif() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\") {\n" +
                "(0010,0010) := bar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"snafu\"){\n" +
                "(0010,0010) := fubar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"yuk\"){\n" +
                "(0010,0010) := \"666\"\n" +
                "}\n"));
    }

    @Test
    public void testIf_Two_Elseif_Else() throws IOException {
        parse( bytes( "if ((0010,0010) = \"foo\") {\n" +
                "(0010,0010) := bar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"snafu\"){\n" +
                "(0010,0010) := fubar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"yuk\"){\n" +
                "(0010,0010) := \"666\"\n" +
                "}\n" +
                "else {\n" +
                "(0010,0010) := \"667\"\n" +
                "}\n"));
    }

    @Test
    public void testIf_Two_Elseif_Else_With_PrivateTags() throws IOException {
        parse( bytes( "if ((0019,{SIEMENS}10) = \"foo\") {\n" +
                "(0019,{SIEMENS}10) := bar\n" +
                "}\n" +
                "elseif ((0010,0010) = \"snafu\"){\n" +
                "(0010,0010) := fubar\n" +
                "}\n" +
                "elseif ((0019,{Foo bar}10) = \"yuk\"){\n" +
                "(0019,{SIEMENS}20) := \"666\"\n" +
                "}\n" +
                "else {\n" +
                "(0019,{x y}10) := (0019,{SIEMENS}10)\n" +
                "}\n"));
    }

    @Test
    public void testEmptyBlock() throws IOException {
        parse(bytes("if ((0010,0010) = \"foo\"){ }\n"));
    }

    @Test
    public void testNewLineBlock() throws IOException {
        parse(bytes("if ((0010,0010) = \"foo\"){ \n }\n"));
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

        ParseTree tree = parser.if_elseif_else();
        logger.info("parse ok: " + tree);
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
