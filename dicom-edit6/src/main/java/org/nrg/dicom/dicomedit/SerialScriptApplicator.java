/*
 * DicomEdit: ScriptApplicator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.dicomedit.functions.LookupManager;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.RejectedInstanceException;
import org.nrg.dicom.mizer.objects.*;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Apply an ordered list of scripts serially.
 * <p>
 * The applicator parses the script on creation. Dicom objects are modified by calling the applicator's apply methods.
 */
public class SerialScriptApplicator implements ScriptApplicatorI {
    private static final Logger logger = LoggerFactory.getLogger(SerialScriptApplicator.class);
    private final List<DE6Script> scripts = new ArrayList<>();
    private final DicomEditParseTreeVisitor visitor;

    /**
     * Construct the applicator from the specified list of anon scripts.
     * <p>
     * Scripts will be applied in order supplied.
     *
     * @param scripts List of DE6 anon scripts.
     */
    public SerialScriptApplicator(List<DE6Script> scripts) {
        this.scripts.addAll(scripts);
        visitor = new DicomEditParseTreeVisitor(scripts.toArray(new DE6Script[0]));
    }

    /**
     * Apply this script to the specified DICOM object file.
     *
     * @param file {@link File}  The DICOM object file.
     * @return {@link DicomObjectI} the modified DicomObject.
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
     * @return {@link DicomObjectI} the modified DicomObject.
     * @throws MizerException When an error occurs creating a DICOM object from the submitted file.
     */
    @Override
    public AnonymizationResult apply(InputStream is) throws MizerException {
        logger.debug("Applying script to stream.");
        DicomObjectI dicomObject = DicomObjectFactory.newInstance(is);
        return apply(dicomObject);
    }

    /**
     * Apply this script to the specified DICOM object.
     *
     * @param dicomObject The DICOM object.
     * @return {@link DicomObjectI} the modified DicomObject.
     */
    @Override
    public AnonymizationResult apply(DicomObjectI dicomObject) {

        logger.trace("Applying scripts to Dicom object: " + dicomObject);
        visitor.setDicomObject(dicomObject);
        AnonymizationResult globalResult = new AnonymizationResultSuccess(dicomObject);
        int i = 0;
        for (DE6Script script : scripts) {
            logger.trace("Apply script {} of {}", ++i, scripts.size());
            LookupManager.setLookupTable(script.getLookupTable());
            visitor.visit(script.getParseTree());
            globalResult = globalResult.merge(apply(visitor, script));
        }
        logger.trace("Edited Dicom object: " + dicomObject);
        return globalResult;

    }

    private AnonymizationResult apply(DicomEditParseTreeVisitor visitor, DE6Script script) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Applying script {}", script);
            }
            visitor.visit(script.getParseTree());
            return new AnonymizationResultSuccess(visitor.getDicomObject());
        } catch (RejectedInstanceException re) {
            return new AnonymizationResultReject(visitor.getDicomObject(), re.getMessage());
        } catch (Exception e) {
            logger.error("Failed to apply script", e);
            return new AnonymizationResultError(visitor.getDicomObject(),
                    e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        }
    }

    @Override
    public Set<String> getVariableNames() {
        Set<String> set = new HashSet<>();

        for (DE6Script s : scripts) {
            set.addAll(s.getVariables().keySet());
        }
        return set;
    }

    @Override
    public Set<String> getExternalVariableNames() {
        Set<String> set = new HashSet<>();

        for (DE6Script s : scripts) {
            set.addAll(s.getExternalVariables().keySet());
        }
        return set;
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
     * Apply the script to the provided dcm4che2 dicom object.
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
     * For compatibility with anonymize package which hardcodes use of dcm4che2.
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
