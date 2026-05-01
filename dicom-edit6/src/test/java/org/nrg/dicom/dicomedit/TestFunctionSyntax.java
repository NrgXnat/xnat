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
 * Run tests of function syntax.
 *
 * Syntax only. Doesn't need to make semantic sense.
 *
 * Functions have syntax: ID '[' termlist? ']'
 * ID is
 * where termlist is one or more of comma separated terms
 * where term has syntax of one of String, number, tagpath, function, or ID.
 *
 *
 * Created by drm on 8/8/16.
 */
public class TestFunctionSyntax {

    private static final Logger logger = LoggerFactory.getLogger(( TestConstrainedActionStatementSyntax.class));

    @Test
    public void testEmptyTerm() throws IOException {
        parse( bytes( "foo_func[]\n"));
    }

    @Test
    public void testSingleTermString() throws IOException {
        parse( bytes( "foo_func[\"my string\"]\n"));
    }

    @Test
    public void testSingleTermNumber() throws IOException {
        parse( bytes( "foo_func[666]\n"));
    }

    @Test
    public void testSingleTermTagPath() throws IOException {
        parse( bytes( "foo_func[ (0018,1032)/(0010,0010)]\n"));
    }

    @Test
    public void testSingleTermTagPathWithItemNumber() throws IOException {
        parse( bytes( "foo_func[ (0018,1032)[0]/(0010,0010)]\n"));
    }

    @Test
    public void testSingleTermTagPathWithItemNumberAndPrivateTag() throws IOException {
        parse( bytes( "foo_func[ (0019,{PvtCreator}32)[0]/(0010,0010)]\n"));
    }

    @Test
    public void testSingleTermTagPathWithItemNumberAndPrivateTagWithSpace() throws IOException {
        parse( bytes( "foo_func[ (0019,{Pvt Creator}32)[0]/(0010,0010)]\n"));
    }

    @Test
    public void testSingleTermFunction() throws IOException {
        parse( bytes( "foo_func[ func_two[]]\n"));
    }

    @Test
    public void testSingleTermID() throws IOException {
        parse( bytes( "foo_func[ varName]\n"));
    }

    @Test
    public void testSingleTermNestedFunction() throws IOException {
        parse( bytes( "foo_func[ func_two[ func_3[ varName]]]\n"));
    }

    @Test
    public void testTermListStringString() throws IOException {
        parse( bytes( "foo_func[ \"s1\", \"s2\" ]\n"));
    }

    @Test
    public void testTermListStringTagpath() throws IOException {
        parse( bytes( "foo_func[ \"s1\", (0013,{CTP}10)[%] ]\n"));
    }

    @Test
    public void testTermListTagpathFunction() throws IOException {
        parse( bytes( "foo_func[  (xx13,{CTP}10)[%], func_funky[ var1, var2] ]\n"));
    }

    @Test
    public void testTermListCrazy() throws IOException {
        parse( bytes( "foo_func[  (xx13,{CTP}10)[%]/(aaaa,xxff), func_funky[ var1, \"string\"], varName]\n"));
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

        ParseTree tree = parser.function();
        logger.info("parse ok: " + tree);
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
