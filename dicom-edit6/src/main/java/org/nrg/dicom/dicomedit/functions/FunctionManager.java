/*
 * DicomEdit: FunctionManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provide API to all built-in and custom script functions.
 *
 * Singleton class that builds all script functions and executes them. Functions are addressed by
 * their fully qualified name (namespace and name).
 *
 */
public class FunctionManager {
    private static final Logger logger = LoggerFactory.getLogger(FunctionManager.class);
    private static Map<String,BaseFunction> functions;

    /**
     * Construct the FunctionManager, instantiating built-in functions, then external custom ones.
     *
     * This is private constructor. Use <code>FunctionManager.getInstance()</code> to get the singleton instance.
     */
    private FunctionManager() {
        functions = new HashMap<>();

        try {
            register( new ConcatenateScriptFunction());
            register( new FormatScriptFunction());
            register( new LowercaseScriptFunction());
            register( new UppercaseScriptFunction());
            register( new SubstringScriptFunction());
            register( new ReplaceScriptFunction());
            register( new HashUIDScriptFunction());
            register( new HashUIDListFunction());
            register( new NewUIDScriptFunction());
            register( new GetURLScriptFunction());
            register( new RetainPrivateTagsReplaceFunction());
            register( new LookupFunction());
            register( new ShiftDateByIncrementFunction());
            register( new ShiftDateTimeListByIncrementFunction());
            register( new ShiftDateTimeByIncrementFunction());
            register( new ShiftDateTimeSequenceByIncrementMethod());
            register( new MapUIDFunction());
            register( new MapReferencedUIDsMethod());
            register( new DeleteFunction());
            register( new SetFunction());
            register( new MatchFunction());
            register( new IsMatchFunction());
            register( new IsPresentFunction());
            register( new AlterPixelsFunction());
            register( new RemoveTagsFunction());
            register( new NormalizeStringFunction());
            register( new ScalePatientAgeAndDobFromStudyDateFunction());
            register( new CollectValues());
            register( new BlankValues());
            register( new RejectFunction());
            register( new TrueFunction());
            register( new FalseFunction());

            loadCustomFunctions();
        } catch (IOException e) {
            logger.error( e.getMessage(), e);
        }
    }

    /**
     * register the function or skip it with logged warning if functions fqn is already registered.
     *
     * @param function The function to register.
     */
    protected void register( BaseFunction function) {
        if( functions.containsKey( function.getFQN())) {
            String msg = MessageFormat.format("Skipping pre-existing function: {0}", function.getFQN());
            logger.warn(msg);
        }
        else {
            functions.put( function.getFQN(), function);
            String msg = MessageFormat.format("Registering function: {0}", function.getFQN());
            logger.info(msg);
        }
    }

    public Map<String,Object> getInternalContext(String functionName) {
        return functions.get(functionName).getInternalContext();
    }

    /**
     * Initialization on demand of FunctionManager singleton via "holder" class
     */
    private static final class FunctionManagerHolder {
        static final FunctionManager functionManager = new FunctionManager();
    }

    /**
     * Return the singleton instance of the FunctionManager.
     *
     * @return FunctionManager
     */
    public static FunctionManager getInstance() {
        return FunctionManagerHolder.functionManager;
    }

    /**
     * Execute the function specified by the fqn using the provided values as arguments.
     *
     * @param fqn The fully qualified name of the function. (As used in the script).
     * @param args The list of values passed as arguments to the function.
     * @return The value that results from function's execution
     * @throws ScriptEvaluationException When an error occurs evaluating the script.
     * @see AbstractMizerValue
     */
    public Value execute(String fqn, List<Value> args, Map<String,Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException {

        BaseFunction scriptFunction = functions.get(fqn);

        if( scriptFunction == null) {
            logger.error( "Failed to find function: " + fqn);
            throw new ScriptEvaluationException("Failed to find function: " + fqn);
        }
        return scriptFunction.apply(args, variables, dicomObject);
    }

    /**
     * Discover custom script functions on the classpath and load them.
     *
     * Look for properties files with path matching 'classpath:META-INF/xnat/dicom-edit/*-function.properties'. These
     * files have sufficient info to create new instances of the functions.
     * Uses Spring's resolver @see PathMatchingResourcePatternResolver
     *
     * The appropriate property file will be generated by the DicomEditFunction annotation, so annotating a function
     * is sufficient to inject it.
     *
     * @throws IOException When an error occurs reading a resource.
     */
    protected void loadCustomFunctions() throws IOException {
        logger.debug("loading custom functions ...");
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for(final Resource resource : resolver.getResources("classpath*:META-INF/xnat/dicom-edit/*-function.properties")) {
            logger.debug("Resource: " + resource);
            try (InputStream is = resource.getInputStream()) {
                Properties properties = new Properties();
                properties.load(is);
                BaseFunction function = (AbstractScriptFunction.createInstance(properties));

                register( function);
            }
            catch( Exception e) {
                String msg = MessageFormat.format("Error creating script function from resource: {0}.\nError: {1}", resource, resource.getDescription());
                logger.error(msg, e);
            }
        }
        logger.debug("... done loading custom functions.");
    }
}
