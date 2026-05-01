package org.nrg.dicom.dicomedit;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create ParseTree for DE6Scripts.
 *
 * Provides a place to extend the DE6 language by providing hooks for pre-processing of script files.
 *
 */
@Slf4j
public class DE6ScriptParser {
    public static final BaseErrorListener ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer,
                                final Object offendingSymbol,
                                final int line,
                                final int position,
                                final String message,
                                final RecognitionException e) {
            final String errorMessage = MessageFormat.format("Failed to parse at {0}:{1} due to {2}", line, position, message);
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    };

    private final List<ScriptDirectiveProcessor> _processors;

    public DE6ScriptParser() {
        _processors = new ArrayList<>();
        _processors.add( new ScriptProcessorRemoveEscNewline());
        _processors.add( new ScriptProcessorRemoveEndLineComments());
    }

    public ParseTree parse(final List<String> statements) throws MizerException {
        return createParseTree( process( statements));
    }
    protected List<String> process( List<String> statements) throws MizerException {
        List<String> lines = statements;
        for( ScriptDirectiveProcessor processor: _processors) {
            lines = processor.process( lines);
        }
        return lines;
    }

    public String asString(List<String> lines) {
        return (String)lines.stream().map(this::mustEndWithNewline).collect(Collectors.joining(""));
    }

    private String mustEndWithNewline(String line) {
        return line.endsWith("\n") ? line : line + "\n";
    }

    public InputStream getInputStream(String string) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(string.getBytes("UTF-8"));
    }

    protected ParseTree createParseTree(List<String> lines) throws MizerException {
        log.debug("Parsing DE6 script syntax.");
        try (final InputStream input = getInputStream(asString(lines))) {
            final DE6Parser parser = new DE6Parser(new CommonTokenStream(new DE6Lexer(CharStreams.fromStream(input))));
            parser.addErrorListener(ERROR_LISTENER);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            return parser.script();
        } catch (Exception e) {
            throw new MizerException("Failed creating DE6Parser.", e);
        }
    }

    public List<ScriptDirectiveProcessor> getProcessors() {
        return _processors;
    }

    public void appendProcessor( ScriptDirectiveProcessor processor) { _processors.add( processor); }
    public void appendProcessors( List<ScriptDirectiveProcessor> processors) { _processors.addAll( processors); }

}
