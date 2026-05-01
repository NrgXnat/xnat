package org.nrg.dicom.mizer.service;

import org.dcm4che2.data.DicomObject;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The Mizer Service interface (Mizer as in anonyMIZER).
 *
 * At your service for all things anonymized.
 *
 * Implementations of MizerService manage multiple instances of {@link Mizer}, which are the service handlers. The
 * MizerService chooses a particular handler based on the {@link MizerContext} supplied with the request.
 *
 */
public interface MizerService {

    /**
     * Anonymize the DICOM object in-place in the given context. Exceptions are not thrown. Users
     * must examine the returned {@link AnonymizationResult} for the status of the operation.
     *
     * @param dobj {@link DicomObjectI} the DICOM object being anonyimized.
     * @param context {@link MizerContext} supplies additional context to the {@link Mizer}.
     * @return {@link AnonymizationResult} containing the processed Dicom and related messages
     */
    AnonymizationResult anonymize(DicomObjectI dobj, MizerContext context) throws MizerException;
    AnonymizationResult anonymize(DicomObjectI dobj, List<MizerContext> contexts) throws MizerException;

    AnonymizationResult anonymize(DicomObjectI dobj, Properties anonContext, boolean ignoreRejection) throws MizerException;
    AnonymizationResult anonymize(DicomObjectI dobj, Properties anonContext, String script, boolean ignoreRejection) throws MizerException;

    /**
     * Called from FileSystemSessionDataModifier
     * @param dicomFile
     * @param project
     * @param subject
     * @param session
     * @param script
     * @return {@link AnonymizationResult} containing the processed Dicom and related messages
     */
    // leak dcm4che2
    AnonymizationResult anonymize(DicomObject dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      String script,
                                      boolean ignoreRejection) throws MizerException;

    /**
     * Anonymize the dicom file by applying the given edit script.
     *
     * All exceptions throws by this function should be considered fatal.
     *
     * The caller should then make sure to delete the dicom file
     * from the filesystem.
     *
     * NOTE: The record and scriptId arguments indicate whether to record the application of this
     * script in the DICOM header and what the ID of the script is. For that reason if "record" is
     * false, the scriptId isn't checked and allowed to be null. If "record" is true, the "scriptId"
     * cannot be null and results in a runtime exception.
     *
     * This method which accepts the script as a file (and not a string from the database) so most of the intended usage will
     * not require recording to the DICOM header but the functionality is here just in case.
     *
     * This is really janky, but Java doesn't have pattern-matching on tuples and wrapping "record" and
     * "scriptId" into an object makes this function more opaque and harder to use.
     *
     * @param dicomFile The DICOM file
     * @param project   The project to which this file belongs
     * @param subject   The subject to which this file belongs
     * @param session   The session to which this file belongs
     * @param record    A boolean indicating whether to record the application of these scripts in the DICOM header
     * @param ignoreRejection    A boolean indicating whether to fail when a reject clause is hit or ignore it
     * @param scriptId  The database ID of the submitted script. Only required if the "record" flag is true.
     * @param scriptStream    The script to be applied to the given DICOM file
     * @return {@link AnonymizationResult} containing the processed Dicom and related messages
     */
    AnonymizationResult anonymize(File dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      boolean record,
                                      boolean ignoreRejection,
                                      long scriptId,
                                      InputStream scriptStream) throws MizerException;

    AnonymizationResult anonymize(File dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      boolean record,
                                      boolean ignoreRejection,
                                      long scriptId,
                                      String scriptString) throws MizerException;

    AnonymizationResult anonymize(File dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      boolean record,
                                      boolean ignoreRejection,
                                      long scriptId,
                                      List<String> script) throws MizerException;

    AnonymizationResult anonymize(File dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      boolean record,
                                      boolean ignoreRejection,
                                      InputStream scriptStream) throws MizerException;

    AnonymizationResult anonymize(File dicomFile,
                                      String project,
                                      String subject,
                                      String session,
                                      boolean record,
                                      boolean ignoreRejection,
                                      List<String> script) throws MizerException;

    AnonymizationResult anonymize(final File dicomFile, final List<MizerContext> scripts) throws MizerException;

    AnonymizationResult anonymize(final File dicomFile, final MizerContext script) throws MizerException;

    /**
     * Anonymize the list of files with the same context.
     */
    List<AnonymizationResult> anonymize(List<File> dicomFiles, String project, String subject, String session, long scriptId, String script, boolean record, boolean ignoreRejection) throws MizerException;

    List<Mizer> getMizers() throws MizerException;

    /**
     *  Set a context that the service will reuse on every encounter.
     *  Some Mizers need to retain state between encounters and can not reset.
     *
     *  If there are multiple Mizers that can handle this context, the service will choose one and will use it
     *  consistently for all encounters.  Which Mizer that is chosen is service-implementation specific.
     *
     *  Care must be taken to explicitly remove set contexts to avoid a memory leak.
     *
     * @param context
     * @return true if service has at least one Mizer that can set this context, false otherwise.
     */
    boolean setContext( MizerContext context);

    /**
     * Remove a context (see setContext).
     *
     * @param context
     * @return true if the context was found and removed, false otherwise.
     */
    boolean removeContext( MizerContext context);

    /**
     * Return the Mizer that the service will use the provided context.
     * You shouldn't have to access explicit Mizers. The service should do that for you.
     *
     * If the service has multiple Mizers, the one chosen is service-implementation specific.
     *
     * @param context
     * @return the Mizer that will handle this context, null if none.
     */
    Mizer getMizer( MizerContext context) throws MizerException;

    /**
     * Returns a set containing all of the DICOM tags referenced in the list of scripts. The mizer service determines
     * the appropriate {@link Mizer mizer implementation} to call for each script and delegates the evaluation and
     * extraction to that mizer's {@link Mizer#getScriptTags(List)} method.
     *
     * @param contexts The scripts to evaluate.
     *
     * @return All of the DICOM tags referenced in the submitted scripts.
     *
     * @throws MizerException When an error occurs evaluating the submitted script.
     */
    Set<TagPath> getScriptTags(final List<MizerContext> contexts) throws MizerException;

    /**
     * Returns a set containing all of the DICOM tags referenced in the submitted script.  The mizer service determines
     * the appropriate {@link Mizer mizer implementation} to call for the script and delegates the evaluation and
     * extraction to that mizer's {@link Mizer#getScriptTags(MizerContext)} method.
     *
     * @param context The script to evaluate.
     *
     * @return All of the DICOM tags referenced in the submitted script.
     *
     * @throws MizerException When an error occurs evaluating the submitted script.
     */
    Set<TagPath> getScriptTags(final MizerContext context) throws MizerException;

    /**
     * Returns a set containing all of the variables referenced in the submitted scripts.
     *
     * @param contexts The scripts to evaluate.
     *
     * @return All of the variables referenced in the submitted scripts.
     *
     * @throws MizerContextException When an error occurs evaluating the submitted scripts.
     */
    Set<Variable> getReferencedVariables(final List<MizerContext> contexts) throws MizerException;

    /**
     * Returns a set containing all of the variables referenced in the submitted script. The mizer service determines
     * the appropriate {@link Mizer mizer implementation} to call for the script and delegates the evaluation and
     * extraction to that mizer's {@link Mizer#getScriptTags(MizerContext)} method.
     *
     * @param context The script to evaluate.
     *
     * @return All of the variables referenced in the submitted script.
     *
     * @throws MizerContextException When an error occurs evaluating the submitted script.
     */
    Set<Variable> getReferencedVariables(final MizerContext context) throws MizerException;

    /**
     * Convenience method to encapsulate the anon context.
     *
     * @param project
     * @param subject
     * @param session
     * @param scriptId
     * @param script
     * @param record
     * @return
     * @throws MizerException
     */
    MizerContextWithScript createContext( final String project, final String subject, final String session, final long scriptId, final Object script, final boolean record, final boolean ignoreRejection) throws MizerException;

}
