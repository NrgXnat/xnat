package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.io.ByteArrayInputStream;

/**
 * Created by davidmaffitt on 4/13/17.
 */
public class DiscoverVariablesTreeVisitorTest {

    @Test
    public void testScriptApplicator() throws Exception {
//        final ByteArrayInputStream in = bytes("(0008,103e) := studyDescription\nstudyDescription := subject\n");
        final ByteArrayInputStream in = bytes("studyDescription := subject\n(0008,103e) := studyDescription\n");

        DE6Lexer lexer;

        try {
            ANTLRInputStream ais = new ANTLRInputStream(in);
            lexer = new DE6Lexer(ais);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        TokenStream tokenStream = new CommonTokenStream(lexer);
        DE6Parser   parser      = new DE6Parser(tokenStream);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                String s = "Failed to parse at line " + line + " due to " + msg;
                throw new IllegalStateException(s, e);
            }
        });

        DiscoverVariablesTreeVisitor visitor = new DiscoverVariablesTreeVisitor();
        ParseTree                    tree    = parser.script();

        visitor.visit(tree);

        System.out.println("External Variables");
        for (String name : visitor.getExternalVariables().keySet()) {
            System.out.println(name);
        }
        System.out.println("All Variables");
        for (String name : visitor.getVariables().keySet()) {
            System.out.println(name);
        }

    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}