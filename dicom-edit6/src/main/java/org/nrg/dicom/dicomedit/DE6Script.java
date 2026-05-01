package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.tree.ParseTree;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.scripts.AbstractMizerScript;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model of DicomEdit version 6 script.
 * <p>
 * Currently stored as a list of String statements.
 * <p>
 * Uses the DE6Lexer and DE6Parser to create ParseTree of a script and used repeatedly by
 * a {@link BaseScriptApplicator}
 */
public class DE6Script extends AbstractMizerScript {

    private static final Logger logger = LoggerFactory.getLogger(DE6Script.class);
    private final DE6ScriptParser _scriptParser = new DE6ScriptParser();
    private final ParseTree _tree;
    private final LookupTable _lookupTable;

    private DE6Script(Builder builder) throws MizerException {
        addStatements(builder.statements);
        _lookupTable = builder.lookupTable;
        _scriptParser.appendProcessors(builder.processors);
        _tree = _scriptParser.parse(getStatements());
        initialize();
    }

    protected ParseTree getParseTree() {
        return _tree;
    }

    public LookupTable getLookupTable() {
        return _lookupTable;
    }

    public List<ScriptDirectiveProcessor> getProcessors() {
        return _scriptParser.getProcessors();
    }

    @Override
    protected void initialize() {
        final DiscoverVariablesTreeVisitor variablesTreeVisitor = new DiscoverVariablesTreeVisitor();
        variablesTreeVisitor.visit(_tree);
        final DiscoverReferencedTagsTreeVisitor tagsTreeVisitor = new DiscoverReferencedTagsTreeVisitor();
        tagsTreeVisitor.visit(_tree);
        final Map<String, Variable> externalVariables = variablesTreeVisitor.getExternalVariables();

        setEntities(variablesTreeVisitor.getVariables(), variablesTreeVisitor.getExternalVariables(), tagsTreeVisitor.getReferenedTags());
    }

    public static class Builder {
        private final List<String> statements = new ArrayList<>();
        private final List<ScriptDirectiveProcessor> processors = new ArrayList<>();
        private LookupTable lookupTable = new LookupTable();

        public Builder() {
        }

        public Builder statement(String statement) {
            statements.add(statement);
            return this;
        }

        public Builder statements(List<String> statements) {
            this.statements.addAll(statements);
            return this;
        }

        public Builder statements(Reader reader) throws IOException {
            statements.addAll(readLines(reader));
            return this;
        }

        public Builder processor(ScriptDirectiveProcessor processor) {
            processors.add(processor);
            return this;
        }

        public Builder lookupTable(LookupTable lookupTable) {
            this.lookupTable = lookupTable;
            return this;
        }

        public Builder lookupTable(Reader reader) throws MizerException {
            try {
                this.lookupTable = new LookupTable(reader);
                return this;
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        public Builder lookupTable(File file) throws MizerException {
            try {
                this.lookupTable = new LookupTable(file);
                return this;
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        public Builder lookupTable(InputStream inputStream) throws MizerException {
            try {
                this.lookupTable = new LookupTable(inputStream);
                return this;
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        public Builder statements(InputStream inputStream) throws MizerException {
            try {
                statements.addAll(readLines(inputStream));
                return this;
            } catch (IOException e) {
                throw new MizerException(e);
            }
        }

        protected List<String> readLines(InputStream is) throws IOException {
            return readLines(new InputStreamReader(is));
        }

        protected List<String> readLines(Reader reader) throws IOException {
            try (BufferedReader br = new BufferedReader(reader)) {
                return br.lines().collect(Collectors.toList());
            }
        }

        public DE6Script build() throws MizerException {
            return new DE6Script(this);
        }
    }

}