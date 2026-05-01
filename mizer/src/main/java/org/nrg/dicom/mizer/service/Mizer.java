package org.nrg.dicom.mizer.service;

import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Set;

/**
 * Mizer Interface models the  Mizer Service Handlers.
 *
 * {@link MizerService} finds a suitable Mizer to handle anonymization in a given {@link MizerContext}
 */
public interface Mizer extends Comparable<Mizer> {
    /**
     * The DICOM standard and DCM4CHE differ on the max. allowable lengths of the tags described above. In the DCM4CHE
     * implementation all the tags are set to 8 bytes (64 chars) which is why all values written to the history tag are
     * checked for length against the DICOM standard conforming lengths below.
     */
    int MeaningLength    = 64;
    int VersionLength    = 16;
    int DesignatorLength = 16;
    int ValueLength      = 16;

    /**
     * Anonymize the specified DICOM object in the given context. Excpetions are not thrown.
     * Users must examine the returned {@link AnonymizationResult} for status.
     *
     * @param dobj    {@link DicomObjectI} the DICOM object to be transformed in-place.
     * @param context {@link MizerContext} the context to be applied to the transformation.
     *
     * @return {@link AnonymizationResult} containing the processed Dicom and related messages
     * @throws {@link MizerException} for non-anonymization related problems.
     */
    AnonymizationResult anonymize(DicomObjectI dobj, MizerContext context) throws MizerException;

    void setContext(MizerContextWithScript context) throws MizerException;

    void removeContext( MizerContextWithScript context);

    /**
     * Return true if this Mizer can successfully process the supplied context.
     *
     * Used by {@link MizerService} to find a suitable Mizer to handle this context. The Mizer examines the context
     * and returns true to tell the {@link MizerService} that this handler is capable of handling the context. If
     * this Mizer is selected, the {@link MizerService} will call this Mizer's {@link Mizer#anonymize(DicomObjectI,
     * MizerContext)}
     * method.
     *
     * @param context {@link MizerContext} to check for compatibility.
     *
     * @return true if this Mizer can process this context.
     *
     * @throws MizerException if shit hits the fan.
     */
    boolean understands(MizerContext context) throws MizerException;

    /**
     * Return the list of version strings supported by this Mizer.
     *
     * The {@link MizerContext} presented to this Mizer can include these strings to aid in matching Mizers to
     * {@link MizerContext}.
     *
     * @return the  list of the {@link VersionString version strings} supported by this mizer implementation.
     */
    List<VersionString> getSupportedVersions();

    /**
     * Indicates the preferred version supported by this mizer.
     *
     * Return the single preferred version supported by this Mizer.  An implementation of {@link MizerService} may use
     * this
     * to help match Mizer handlers to {@link MizerContext}.
     *
     * @return The latest version supported by this mizer implementation.
     */
    VersionString getMaxSupportedVersion();

    /**
     * Returns a set containing all of the DICOM tags referenced in the list of contexts.
     *
     * @param contexts The list of {@link MizerContext} to evaluate.
     *
     * @return All of the DICOM tags referenced in the submitted contexts.
     *
     * @throws MizerException When an error occurs evaluating the submitted contexts.
     */
    Set<TagPath> getScriptTags(final List<MizerContext> contexts) throws MizerException;

    /**
     * Returns a set containing all of the DICOM tags referenced in the submitted context.
     *
     * @param context The {@link MizerContext} to evaluate.
     *
     * @return All of the DICOM tags referenced in the submitted contexts.
     *
     * @throws MizerException When an error occurs evaluating the submitted contexts.
     */
    Set<TagPath> getScriptTags(final MizerContext context) throws MizerException;

    /**
     * Returns a set containing all of the variables referenced in the submitted contexts.
     *
     * @param context   The {@link MizerContext} to evaluate.
     * @param variables A set of variables that can be accumulated across multiple contexts.

     * @throws MizerContextException When an error occurs evaluating the submitted contexts.
     */
    void getReferencedVariables(final MizerContext context, final Set<Variable> variables) throws MizerContextException;

    /**
     * Aggregates the context with the submitted set of variables.
     *
     * @param context   The Mizer context.
     * @param variables The variables to aggregate.
     */
    void aggregate(MizerContext context, Set<Variable> variables) throws MizerContextException;
}
