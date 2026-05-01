package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Create TagPath instances utilizing the DE6 and TagPath grammars.
 *
 * DE6 grammar tag paths can address multiple attributes (wild cards) and address private tags through their
 * private creator IDs.
 *
 * TagPath grammar tag paths address single attributes with a direct notation.
 *
 */
public class TagPathFactory  {

    public static TagPath createDE6Instance(final String string) {

        ParseTree parseTree = createDE6ParseTree( string);
        DicomEditParseTreeVisitor visitor = new DicomEditParseTreeVisitor();
        Value v = visitor.visit( parseTree);
        return (TagPath) v.asObject();
    }

    public static int[] createTagPathInstance(final String tagPathString) {

        TagPathParseTreeVisitor tpptv = new TagPathParseTreeVisitor();
        List<Integer> integerList = tpptv.getTagPath( createTagPathParseTree( tagPathString));
        int[] ints = integerList.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .toArray();
        return ints;
    }

    protected static ParseTree createDE6ParseTree( String string) throws ScriptEvaluationRuntimeException {
        logger.trace("Parsing '{}' for DE6 tagpath syntax.", string);
        try (final InputStream input = new ByteArrayInputStream( string.getBytes())) {
            final DE6Parser parser = new DE6Parser(new CommonTokenStream(new DE6Lexer(CharStreams.fromStream(input))));
            parser.addErrorListener(DE6ScriptParser.ERROR_LISTENER);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            return parser.tagpath();
        } catch (Exception e) {
            throw new ScriptEvaluationRuntimeException("Failed creating DE6 Tagpath: " + string, e);
        }
    }

    protected static TagPathParser.TagpathContext createTagPathParseTree(String tagPathString) {
        logger.debug("Parsing TagPath tagpath syntax.");
        final TagPathParser parser = new TagPathParser(new CommonTokenStream(new TagPathLexer(CharStreams.fromString(tagPathString))));
        parser.addErrorListener(DE6ScriptParser.ERROR_LISTENER);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        return parser.tagpath();
    }

    private static final Logger logger = LoggerFactory.getLogger(TagPathFactory.class);
}