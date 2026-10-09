/*
 * DicomEdit: ScriptApplicator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.RejectedInstanceException;
import org.nrg.dicom.mizer.objects.*;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * The entry point into the library.
 * <p>
 * The applicator parses the script on creation. Dicom objects are modified by calling the applicator's apply methods.
 */
public class BaseScriptApplicator implements ScriptApplicatorI {
    private static final Logger logger = LoggerFactory.getLogger(BaseScriptApplicator.class);
    private final DE6Script script;
    private final DicomEditParseTreeVisitor visitor;

    /**
     * Construct the applicator from the specified context.
     *
     * @param context
     * @throws MizerException
     */
    private BaseScriptApplicator(MizerContextWithScript context) throws MizerException {
        script = new DE6Script.Builder().statements(context.getScript()).build();
        visitor = new DicomEditParseTreeVisitor(script);

        // script may contain functions with "hidden" parameters. They can be manipulated programmatically
        // as variables.
        for (final String name : this.getExternalVariableNames()) {
            final String value = (String) context.getElement(name);
            if (value != null) {
                this.setVariable(name, value);
            }
        }
    }

    /**
     * Construct the applicator with the specified anon-script.
     *
     * @param script
     */
    private BaseScriptApplicator(DE6Script script) {
        this.script = script;
        visitor = new DicomEditParseTreeVisitor(script);
    }

    public static BaseScriptApplicator getInstance(DE6Script script) throws MizerException {
        return new BaseScriptApplicator(script);
    }

    /**
     * Must take a step backward to extract statements from script for context just to recreate the script from
     * context. TODO: MizerContextWithScript should delegaet to an AbstractMizerScript field for script storage.
     *
     * @param script
     * @param ignoreRejections
     * @return
     * @throws MizerException
     */
    public static BaseScriptApplicator getInstance(DE6Script script, boolean ignoreRejections) throws MizerException {
        MizerContextWithScript context = new MizerContextWithScript(script.getStatements());
        context.setIgnoreRejection(ignoreRejections);
        return new BaseScriptApplicator(context);
    }

    public static BaseScriptApplicator getInstance(InputStream inputStream) throws MizerException {
        return getInstance(inputStream, false);
    }

    public static BaseScriptApplicator getInstance(InputStream inputStream, boolean ignoreRejections) throws MizerException {
        MizerContextWithScript context = new MizerContextWithScript(inputStream);
        context.setIgnoreRejection(ignoreRejections);
        return getInstance(context);
    }

    public static BaseScriptApplicator getInstance(MizerContextWithScript context) throws MizerException {
        return new BaseScriptApplicator(context);
    }

    /**
     * Apply this script to the specified DICOM object file.
     *
     * @param file {@link java.io.File}  The DICOM object file.
     * <p>
     * If the script redacts pixels, the returned object's pixel data is a reference into a scratch
     * file that it reads from when written. The caller owns that file: call
     * {@link org.nrg.dicom.mizer.objects.DicomObjectI#releaseScratchFiles()} once the object has
     * been written out, or it is left behind, one per redacted object.
     *
     * @return {@link AnonymizationResult} encapsulates the modified DicomObject and processing details. Users must
     * examine this object for status of the anon process.
     * @throws MizerException When an error occurs creating a DICOM object from the submitted file.
     */
    @Override
    public AnonymizationResult apply(File file) throws MizerException {
        logger.info("Applying script to file: " + file);
        DicomObjectI dicomObject = DicomObjectFactory.newInstance(file);
        return apply(dicomObject);
    }

    /**
     * Apply this script to the specified DICOM object input stream.
     *
     * @param is {@link InputStream}  The DICOM object input stream.
     * <p>
     * If the script redacts pixels, the returned object's pixel data is a reference into a scratch
     * file that it reads from when written. The caller owns that file: call
     * {@link org.nrg.dicom.mizer.objects.DicomObjectI#releaseScratchFiles()} once the object has
     * been written out, or it is left behind, one per redacted object.
     *
     * @return {@link AnonymizationResult} encapsulates the modified DicomObject and processing details. Users must
     * examine this object for status of the anon process.
     * @throws MizerException When an error occurs creating a DICOM object from the specified stream.
     */
    @Override
    public AnonymizationResult apply(InputStream is) throws MizerException {
        logger.debug("Applying script to stream.");
        DicomObjectI dicomObject = DicomObjectFactory.newInstance(is);
        return apply(dicomObject);
    }

    /**
     * Apply this script to the specified DICOM object.
     * This modifies the supplied object.
     * No exceptions are thrown. Users must check the {@link AnonymizationResult} for status.
     *
     * @param dicomObject The DICOM object.
     * @return {@link AnonymizationResult} encapsulates the modified DicomObject and processing details.
     */
    @Override
    public AnonymizationResult apply(DicomObjectI dicomObject) {

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Applying script to Dicom object: {}", dicomObject);
            }
            visitor.setDicomObject(dicomObject);
            visitor.visit(script.getParseTree());
            if (logger.isTraceEnabled()) {
                logger.trace("Edited Dicom object: {}", dicomObject);
            }
            return new AnonymizationResultSuccess(dicomObject);
        } catch (RejectedInstanceException re) {
            return new AnonymizationResultReject(dicomObject, re.getMessage());
        } catch (Exception e) {
            logger.error("Failed to apply script", e);
            return new AnonymizationResultError(visitor.getDicomObject(),
                    e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        }
    }

    @Override
    public Set<String> getVariableNames() {
        return script.getVariables().keySet();
    }

    @Override
    public Set<String> getExternalVariableNames() {
        return script.getExternalVariables().keySet();
    }

    /**
     * Return the {@link Variable} with the specified name.
     *
     * @param name The name of the variable.
     * @return {@link Variable} the value of the named variable, null if it does not exist.
     */
    @Override
    public Variable getVariable(String name) {
        return visitor.getVariable(name);
    }

    /**
     * Return the {@link AbstractMizerValue} of the defined variable with the specified name.
     *
     * @param name The name of the variable.
     * @return {@link AbstractMizerValue} the value of the named variable, null if the variable is not defined.
     */
    @Override
    public Value getValue(final String name) {
        final Variable variable = visitor.getVariable(name);
        return (variable == null) ? null : variable.getValue();
    }

    /**
     * Maximum tag value potentially required for or modified by this operation.
     * <p>
     * Punt for now and assume everything up to Pixel Data is allowed.
     *
     * @return The value of the top tag.
     */
    @Override
    public long getTopTag() {
        return 0x7FE00010 - 1;
    }

    /**
     * Apply the script to the provided DICOM object.
     * <p>
     * This is for backwards compatibility with anonymize package.
     *
     * @param matchFile   TODO:  What is this? Ignored for now.
     * @param dicomObject The DICOM object to be processed.
     * @throws MizerException When an error occurs creating a DICOM object from the submitted file.
     */
    @Override
    public void apply(final File matchFile, final DicomObjectI dicomObject) throws MizerException {
        apply(dicomObject);
    }

    /**
     * Return a map with variables.
     * <p>
     * For compatibility with anonymize package.
     *
     * @return The variables for the script application.
     */
    @Override
    public Map<String, Variable> getVariables() {
        return visitor.getVariables();
    }

    /**
     * Set a variable.
     * <p>
     * TODO: will want a "Variable" version too?
     *
     * @param name  The name of the variable to set.
     * @param value The value to set for the variable.
     */
    @Override
    public void setVariable(final String name, final String value) {
        visitor.setVariable(name, new ConstantValue(value));
    }

}
