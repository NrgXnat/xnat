/*
 * DicomEdit: org.nrg.dcm.edit.ScriptApplicator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.dcm4che2.io.DicomOutputStream;
import org.nrg.dcm.edit.fn.*;
import org.nrg.dcm.edit.gen.UIDGenerator;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;
import org.nrg.framework.io.PresuffixFileMapper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;

import static org.nrg.dcm.edit.Statement.getActions;
import static org.nrg.dcm.edit.annotations.DicomEdit4Function.FUNCTION_CLASS;
import static org.nrg.dcm.edit.annotations.DicomEdit4Function.FUNCTION_NAME;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class ScriptApplicator {
    public ScriptApplicator(final InputStream script, final Map<String, Object> context, final Map<String, ScriptFunction> functions, final Map<String, ValueGenerator> generators) throws IOException, ScriptEvaluationException {
        if (null == script) {
            _statements = Collections.emptyList();
            _context = Collections.emptyMap();
            _parser = null;
        } else {
            _context = context;
            try {
                final EditDCMLexer lexer = new EditDCMLexer(new ANTLRInputStream(script));
                final EditDCMParser parser = new EditDCMParser(new CommonTokenStream(lexer));
                final EditDCMParser.script_return sr = parser.script();  // start rule method

                _log.trace("parsing to AST");
                final CommonTree ast = (CommonTree) sr.getTree();
                if (null == ast) {
                    _log.trace("unparseable");
                    _parser = null;
                    _statements = Lists.newArrayList();
                } else {
                    _parser = new EditDCMTreeParser(new CommonTreeNodeStream(ast));
                    _parser.setFunction(Substring.name, new Substring());
                    _parser.setFunction(Format.name, new Format());
                    _parser.setFunction(Lowercase.name, new Lowercase());
                    _parser.setFunction(Uppercase.name, new Uppercase());
                    _parser.setFunction(GetURL.name, new GetURL());
                    _parser.setFunction(Replace.name, new Replace());
                    _parser.setFunction(Match.name, new Match());
                    _parser.setFunction(UrlEncode.name, new UrlEncode());
                    _parser.setFunction(HashUID.name, new HashUID());

                    for (final Map.Entry<String, ScriptFunction> me : locateFunctions(functions).entrySet()) {
                        _log.trace("adding function {}", me);
                        _parser.setFunction(me.getKey(), me.getValue());
                    }

                    for (final Map.Entry<String, ValueGenerator> me : generators.entrySet()) {
                        _log.trace("adding generator {}", me);
                        _parser.setGenerator(me.getKey(), me.getValue());
                    }
                    if (!generators.containsKey(UIDGenerator.name)) {
                        _log.trace("adding generator {}", UIDGenerator.name);
                        _parser.setGenerator(UIDGenerator.name, new UIDGenerator());
                    }
                    _log.trace("ready to parse");
                    _statements = _parser.script();
                    _log.trace("{}", _statements);
                }
            } catch (RecognitionException e) {
                throw new ScriptEvaluationException("error parsing script", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ScriptApplicator(final InputStream script, final Map<String, Object> context) throws IOException, ScriptEvaluationException {
        this(script, context,
             context.containsKey("functions") ? (Map<String, ScriptFunction>) context.get("functions") : new HashMap<String, ScriptFunction>(),
             context.containsKey("generators") ? (Map<String, ValueGenerator>) context.get("generators") : new HashMap<String, ValueGenerator>());
    }

    public ScriptApplicator(final InputStream script) throws IOException, ScriptEvaluationException {
        this(script, new HashMap<String, Object>(), new HashMap<String, ScriptFunction>(), new HashMap<String, ValueGenerator>());
    }

    public ScriptApplicator() {
        _statements = Collections.emptyList();
        _context = Collections.emptyMap();
        _parser = null;
    }

    public int getTopTag() {
        final SortedSet<Long> tags = getScriptTags();
        return tags.last().intValue();
    }

    public SortedSet<Long> getScriptTags() {
        final SortedSet<Long> tags = Sets.newTreeSet();
        for (final Statement s : _statements) {
            tags.addAll(s.getScriptTags());
        }
        return tags;
    }

    public Variable getVariable(final String label) {
        return null == _parser ? null : _parser.getVariable(label);
    }

    public Map<String, Variable> getVariables() {
        return null == _parser ? Collections.<String, Variable>emptyMap() : _parser.getVariables();
    }

    public List<Variable> getSortedVariables(final Collection<String> excluding) {
        final Map<Variable, Collection<Variable>> graph = Maps.newLinkedHashMap();
        for (final Variable variable : getVariables().values()) {
            if (!excluding.contains(variable.getName())) {
                final Value                initialValue           = variable.getInitialValue();
                final Collection<Variable> dependencies = Lists.newArrayList();
                if (null != initialValue) {
                    for (final Variable dv : initialValue.getVariables()) {
                        if (!excluding.contains(dv.getName())) {
                            dependencies.add(dv);
                        }
                    }
                }
                graph.put(variable, dependencies);
            }
        }

        // Don't let externally- but not internally-defined variables get
        // in the way of our dependency resolution.
        for (final Collection<Variable> dependencies : graph.values()) {
            for (final Iterator<Variable> i = dependencies.iterator(); i.hasNext(); ) {
                if (!graph.containsKey(i.next())) {
                    i.remove();
                }
            }
        }

        _log.trace("sorting variable dependencies: {}", graph);
        return GraphUtils.topologicalSort(graph);
    }

    public List<Variable> getSortedVariables(final String[] excluding) {
        return getSortedVariables(Arrays.asList(excluding));
    }

    public List<Variable> getSortedVariables() {
        return this.getSortedVariables(Collections.<String>emptyList());
    }

    public List<Statement> getStatements() {
        return _statements;
    }

    public DicomObjectI apply(final File f, final DicomObjectI dicomObject)
            throws AttributeException, ScriptEvaluationException {
        final List<Action> actions = getActions(_statements, f, dicomObject);
        // In order to ensure the semantics of multiple matching operations
        // (i.e., that the first match applies), we apply the operations
        // in reverse order.
        Collections.reverse(actions);
        for (final Action action : actions) {
            action.apply();
        }
        return dicomObject;
    }

    public DicomObjectI apply(final File f) throws IOException, AttributeException, MizerException {
        return apply(f, DicomObjectFactory.newInstance(f));
    }

    public void setGenerator(final String label, final ValueGenerator generator) {
        if (null != _parser) {
            _parser.setGenerator(label, generator);
        }
    }

    public void setFunction(final String label, final ScriptFunction function) {
        if (null != _parser) {
            _parser.setFunction(label, function);
        }
    }

    /**
     * Unify the provided variable with matching variables in this environment.
     * If a similarly-named variable already exists in this environment, replace
     * that one with the provided v and return v. Otherwise, do not add v to the
     * environment and return null.
     * If v has no initial value set, but the previous variable does, v's initial
     * value is set to that of the previous variable.
     *
     * @param variable Variable that should replace any similarly-named variables in
     *          this parser's environment.
     *
     * @return v if a similarly-named variable already existed here, null otherwise
     */
    public Variable unify(final Variable variable) {
        final Variable parsedVariable = _parser.unify(variable);
        for (final Statement statement : _statements) {
            final Operation operation = statement.getOperation();
            if (operation != null) {
                operation.replace(variable);
            }
        }
        return parsedVariable;
    }

    public static void main(final String args[]) throws Exception {
        final InputStream in;
        if (0 == args.length || "-".equals(args[0])) {
            in = System.in;
        } else {
            in = new FileInputStream(args[0]);
        }
        final ScriptApplicator applicator;
        try {
            applicator = new ScriptApplicator(in);
        } finally {
            in.close();
        }

        final Function<File, File> mapper = new PresuffixFileMapper("-mod");

        for (int i = 1; i < args.length; i++) {
            final File file = new File(args[i]);
            final File applied = mapper.apply(file);
            if (applied != null) {
                try (final DicomOutputStream out = new DicomOutputStream(new FileOutputStream(applied))) {
                    out.writeDicomFile(applicator.apply(file).getDcm4che2Object());
                }
            }
        }
    }

    /**
     * Locates all script functions found in DicomEdit 4 properties files.
     *
     * @param existing Any manually configured functions.
     *
     * @return A map of functions by function name and implementation.
     */
    private Map<String, ScriptFunction> locateFunctions(final Map<String, ScriptFunction> existing) {
        final Map<String, ScriptFunction> functions = existing == null ? new HashMap<String, ScriptFunction>() : new HashMap<>(existing);
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath:META-INF/xnat/dicom-edit4/*-function.properties")) {
                _log.debug("Resource: " + resource);
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                try {
                    final String name = properties.getProperty(FUNCTION_NAME);
                    final String className = properties.getProperty(FUNCTION_CLASS);
                    final Class<? extends ScriptFunction> clazz = Class.forName(className).asSubclass(ScriptFunction.class);
                    try {
                        final Constructor<? extends ScriptFunction> constructor = clazz.getDeclaredConstructor(Map.class);
                        try {
                            final ScriptFunction function = constructor.newInstance(_context);
                            functions.put(name, function);
                            _log.debug("Created instance of script function {} using the context constructor", className);
                        } catch (InvocationTargetException e) {
                            _log.error("An error occurred invoking the context constructor of the class " + className, e);
                        }
                    } catch (NoSuchMethodException e) {
                        final ScriptFunction function = clazz.newInstance();
                        functions.put(name, function);
                        _log.debug("Created instance of script function {} using the default constructor", className);
                    }
                } catch (ClassNotFoundException e) {
                    _log.error(MessageFormat.format("Error creating script function from resource: {0}. Couldn't find specified class: {1}", resource, properties.getProperty(FUNCTION_CLASS)), e);
                } catch (IllegalAccessException | InstantiationException e) {
                    _log.error(MessageFormat.format("Error creating script function from resource: {0}. Couldn't access specified class: {1}", resource, properties.getProperty(FUNCTION_CLASS)), e);
                }
            }
        } catch (FileNotFoundException e) {
            _log.info("Didn't find any properties files matching the DicomEdit 4 function definition specification, proceeding without dynamic function loading.");
        } catch (IOException e) {
            _log.warn("An error occurred trying to retrieve the available DicomEdit 4 function definitions.", e);
        }
        return functions;
    }

    private static final Logger _log = LoggerFactory.getLogger(ScriptApplicator.class);

    private final EditDCMTreeParser   _parser;
    private final List<Statement>     _statements;
    private final Map<String, Object> _context;
}
