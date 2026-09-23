package org.nrg.dicom.mizer.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.StagingDirectoryResolver;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.TransactionException;
import org.nrg.transaction.TransactionRunner;
import org.nrg.transaction.operations.CallOnFile;
import org.nrg.transaction.operations.WorkOnCopyOp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.dicom.mizer.values.Value.EMPTY_VARIABLES;

/**
 * Basic Anonymization Service Provider
 *
 * Uses the first capable handler to process the request. Handlers are presented in descending max version order.
 * For example,
 * Mizer1 supports versions 4.0, Mizer2 supports versions '7.0, 6.0', Mizer3 supports versions 6.0. Search order will be
 * Mizer2, Mizer3, then Mizer1. Order is indeterminate if two handlers have the same max supported version.
 */
@Slf4j
@Service
public class BaseMizerService implements MizerService {
    @Autowired
    public BaseMizerService(@SuppressWarnings("SpringJavaAutowiringInspection") final List<Mizer> mizers) {
        _mizers = mizers;
        Collections.sort(_mizers);
    }

    @Override
    public List<Mizer> getMizers() {
        return _mizers;
    }

    /**
     * Sets where the anonymized version of a file is staged before it replaces the original. Optional:
     * without one, files are staged under {@code java.io.tmpdir} and replaced by a copy, as they always
     * were. A resolver that stages on the volume the file lives on makes the replacement an atomic rename
     * and saves a full read and write of the file.
     *
     * @param resolver The resolver to use.
     */
    @Autowired(required = false)
    public void setStagingDirectoryResolver(final StagingDirectoryResolver resolver) {
        _stagingDirectories = resolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mizer getMizer( MizerContext context) throws MizerException {
        return findMizer( context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setContext(MizerContext context) {
        try {
            Mizer mizer = findMizer( context);
            if( context instanceof MizerContextWithScript) {
                mizer.setContext( (MizerContextWithScript) context);
                return true;
            }
        } catch (MizerException e) {
            return false;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeContext(MizerContext context) {
        try {
            Mizer mizer = findMizer( context);
            if( context instanceof MizerContextWithScript) {
                mizer.removeContext( (MizerContextWithScript) context);
                return true;
            }
        } catch (MizerException e) {
            return false;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(final List<MizerContext> contexts) throws MizerException {
        return new HashSet<TagPath>() {{
            for (final MizerContext context : contexts) {
                addAll(getScriptTags(context));
            }
        }};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagPath> getScriptTags(MizerContext context) throws MizerException {
        return findMizer(context).getScriptTags(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Variable> getReferencedVariables(final List<MizerContext> contexts) throws MizerException {
        if (contexts == null || contexts.isEmpty()) {
            return EMPTY_VARIABLES;
        }

        final LinkedHashSet<Variable> variables = Sets.newLinkedHashSet();
        for (final MizerContext context : contexts) {
            final Mizer mizer = findMizer(context);
            mizer.getReferencedVariables(context, variables);
        }
        return variables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Variable> getReferencedVariables(final MizerContext context) throws MizerException {
        return getReferencedVariables(Collections.singletonList(context));
    }

    @Override
    public AnonymizationResult anonymize(final DicomObjectI dicomObject, final MizerContext context) throws MizerException {
        final Mizer mizer = findMizer(context);

        if (mizer != null) {
            return mizer.anonymize(dicomObject, context);
        } else {
            throw new MizerException(MessageFormat.format("Failed to find Mizer to anonymize dicom object in context {0}", context));
        }
    }

    @Override
    public AnonymizationResult anonymize(final DicomObjectI dicomObject, final List<MizerContext> contexts) throws MizerException {
        AnonymizationResult finalResult = null;
        for (final MizerContext context : contexts) {
            AnonymizationResult result = anonymize(dicomObject, context);
            finalResult = (finalResult == null)? result: finalResult.merge(result);
        }
        return finalResult;
    }

    @Override
    public AnonymizationResult anonymize(DicomObjectI dicomObject, Properties anonContext, final boolean ignoreRejection) throws MizerException {
        MizerContext context = new MizerContextBase();
        context.add(anonContext);
        context.setIgnoreRejection(ignoreRejection);
        return anonymize(dicomObject, context);
    }

    @Override
    public AnonymizationResult anonymize(final DicomObjectI dicomObject, final Properties anonContext, final String script, final boolean ignoreRejection) throws MizerException {
        final MizerContextWithScript context = new MizerContextWithScript();
        context.setScript(script);
        context.add(anonContext);
        context.setIgnoreRejection(ignoreRejection);
        return anonymize(dicomObject, context);
    }

    /**
     * Called from FileSystemSessionDataModifier
     *
     * @param dicomObject         The DICOM object.
     * @param project      The project.
     * @param subject      The subject.
     * @param session      The session.
     * @param scriptString The script as a string.
     *
     * @throws MizerException When an error occurs processing the script.
     */
    @Override
    public AnonymizationResult anonymize(final DicomObjectI dicomObject, String project, String subject, String session, String scriptString, final boolean ignoreRejection) throws MizerException {
        final Properties anonContext = new Properties();
        anonContext.put("project", project);
        anonContext.put("subject", subject);
        anonContext.put("session", session);
        return anonymize(dicomObject, anonContext, scriptString, ignoreRejection);
    }

    @Override
    public AnonymizationResult anonymize(File dicomFile, String project, String subject, String session, boolean record, boolean ignoreRejection, long scriptId, List<String> script) throws MizerException {
        return anonymize(dicomFile, createContext( project, subject, session, scriptId, script, record, ignoreRejection));
    }

    @Override
    public AnonymizationResult anonymize(File dicomFile, String project, String subject, String session, boolean record, boolean ignoreRejection, long scriptId, InputStream scriptStream) throws MizerException {
        return anonymize(dicomFile, createContext( project, subject, session, scriptId, scriptStream, record, ignoreRejection));
    }

    /**
     * Called from GradualDicomImporter, AnonymizerA,
     *
     * @param dicomFile    The file containing the DICOM to be processed.
     * @param project      The project.
     * @param subject      The subject.
     * @param session      The session.
     * @param record       Whether the deidentification processing should be recorded in the DICOM output.
     * @param scriptId     The ID of the script (only used when <b>record</b> is true)
     * @param scriptString The script as a string.
     *
     * @throws MizerException When an error occurs processing the script.
     */
    @Override
    public AnonymizationResult anonymize(File dicomFile, String project, String subject, String session, boolean record, boolean ignoreRejection, long scriptId, String scriptString) throws MizerException {
        return anonymize(dicomFile, createContext( project, subject, session, scriptId, scriptString, record, ignoreRejection));
    }

    @Override
    public AnonymizationResult anonymize(File dicomFile, String project, String subject, String session, boolean record, boolean ignoreRejection, InputStream script) throws MizerException {
        return anonymize(dicomFile, project, subject, session, record, ignoreRejection, 0L, script);
    }

    @Override
    public AnonymizationResult anonymize(File dicomFile, String project, String subject, String session, boolean record, boolean ignoreRejection, List<String> script) throws MizerException {
        return anonymize(dicomFile, project, subject, session, record, ignoreRejection, 0L, script);
    }

    @Override
    public AnonymizationResult anonymize(File dicomFile, List<MizerContext> contexts) throws MizerException {
        AnonymizationResult finalResult = null;
        for (final MizerContext context : contexts) {
            AnonymizationResult result = anonymize(dicomFile, context);
            finalResult = (finalResult == null)? result: finalResult.merge(result);
        }
        return finalResult;
    }

    /**
     * Anonymize the DICOM File, rollback if error occurs.
     *
     * This is the lowest level function of this type.  Others are convenience methods that call this one.
     *
     * @param dicomFile The given dicom file on the filesystem
     * @param context   {@link MizerContext} additional context.
     *
     * @throws MizerException When an error occurs evaluating the script.
     */
    // The dicom data is read from the given file, but the unchanged pixel data and
    // changed headers are written to a staging file in the directory the staging
    // directory resolver chooses. The given file is replaced with the
    // staging file if the anonymization process is successful.
    @Override
    public AnonymizationResult anonymize(final File dicomFile, final MizerContext context) throws MizerException {
        try {
            final Mizer mizer = findMizer(context);
            log.info("Found mizer for versions {}", Joiner.on(", ").join(mizer.getSupportedVersions()));
            final CallOnFile<AnonymizationResult> callOnFile = new AnonymizeCallOnFileWithPixels(dicomFile, mizer, context);
            final WorkOnCopyOp<AnonymizationResult> anonymizeOp = new WorkOnCopyOp<>(dicomFile, stagingDirectoryFor(dicomFile), callOnFile);
            return new TransactionRunner<AnonymizationResult>().runTransaction(anonymizeOp);
        } catch (RollbackException | TransactionException e) {
            throw new MizerException(e);
        }
    }

    /**
     * All or nothing: every file is anonymized into a staged copy before any is put in its place. A
     * failure on any one discards every staged copy and throws, so the batch is left as it was rather
     * than partly anonymized, and running the script again later can't apply it twice to some of the
     * files. The cost is room on the data volume for the anonymized copy of the whole batch at once,
     * rather than of one file at a time.
     * <p>
     * A rejection isn't a failure: it comes back as a result, with the file untouched, for the caller
     * to delete.
     *
     * @throws MizerException when any file fails to anonymize, in which case no file was changed. The
     *                        exception is only thrown after some files were changed if putting a staged
     *                        copy in place fails, and then it names the file whose copy is preserved.
     */
    @Override
    public List<AnonymizationResult> anonymize(List<File> dicomFiles, String project, String subject, String session, long scriptId, String script, boolean record, boolean ignoreRejection) throws MizerException {
        final List<WorkOnCopyOp<AnonymizationResult>> staged = new ArrayList<>();
        boolean committed = false;
        try {
            List<AnonymizationResult> resultList = new ArrayList<>();
            MizerContextWithScript context = createContext( project, subject, session, scriptId, script, record, ignoreRejection);
            final Mizer mizer = findMizer(context);
            log.info("Found mizer for versions {}", Joiner.on(", ").join(mizer.getSupportedVersions()));
            mizer.setContext( context);
            try {
                for( File dicomFile: dicomFiles) {
                    final CallOnFile<AnonymizationResult> callOnFile = new AnonymizeCallOnFileWithPixels(dicomFile, mizer, context);
                    final WorkOnCopyOp<AnonymizationResult> anonymizeOp = new WorkOnCopyOp<>(dicomFile, stagingDirectoryFor(dicomFile), callOnFile);
                    staged.add(anonymizeOp);
                    AnonymizationResult result = anonymizeOp.stage();
                    result.setAbsolutePath(dicomFile.getAbsolutePath());
                    if (result instanceof AnonymizationResultError) {
                        throw new MizerException("Unable to anonymize " + dicomFile + ", so none of the " + dicomFiles.size()
                                                 + " files in this batch was changed: " + result.getMessage());
                    }
                    resultList.add(result);
                }
                for (final WorkOnCopyOp<AnonymizationResult> anonymizeOp : staged) {
                    anonymizeOp.commit();
                }
                committed = true;
            } finally {
                mizer.removeContext( context);
            }
            return resultList;
        } catch (TransactionException e) {
            throw new MizerException(e);
        } finally {
            if (!committed) {
                discard(staged);
            }
        }
    }

    /**
     * Removes the staged copies still there after a batch didn't finish: all of them when staging
     * failed, the ones not yet in place when putting one in place failed. A copy whose replacement
     * failed is kept, since it may be the only intact version of its file.
     */
    private static void discard(final List<WorkOnCopyOp<AnonymizationResult>> staged) {
        for (final WorkOnCopyOp<AnonymizationResult> anonymizeOp : staged) {
            try {
                anonymizeOp.rollback();
            } catch (RollbackException e) {
                log.warn("Unable to remove a staged anonymization file", e);
            }
        }
    }

    private File stagingDirectoryFor(final File dicomFile) throws MizerException {
        try {
            return _stagingDirectories.resolve(dicomFile);
        } catch (IOException e) {
            throw new MizerException("Unable to determine where to stage the anonymized version of " + dicomFile, e);
        }
    }

    /**
     * Find the first mizer that understands the anonymization context or throw error if none found.
     *
     * @param context {@link MizerContext}
     *
     * @return the first mizer that understands the context or null if none found.
     *
     * @throws MizerException if there are no matching Mizers.
     */
    private Mizer findMizer(final MizerContext context) throws MizerException {
        for (final Mizer mizer : _mizers) {
            if (mizer.understands(context)) {
                return mizer;
            }
        }
        if (context instanceof MizerContextWithScript) {
            final Matcher matcher = INVALID_VERSION_FORMAT.matcher(((MizerContextWithScript) context).getScriptAsString());
            if (matcher.find()) {
                throw new MizerException(String.format(INVALID_VERSION_MESSAGE, matcher.group("expression")));
            }
        }
        throw new MizerException("The Mizer service failed to find a Mizer implementation that knows how to handle your script");
    }

    @Nonnull
    @Override
    public MizerContextWithScript createContext( final String project, final String subject, final String session, final long scriptId, final Object script, final boolean record, final boolean ignoreRejection) throws MizerException {
        final MizerContextWithScript mizerContext = new MizerContextWithScript();
        mizerContext.setScriptId(scriptId);
        mizerContext.setElement("project", project);
        mizerContext.setElement("subject", subject);
        mizerContext.setElement("session", session);
        mizerContext.setIgnoreRejection(ignoreRejection);
        if (script instanceof List) {
            //noinspection unchecked
            mizerContext.setScript((List<String>) script);
        } else if (script instanceof InputStream) {
            mizerContext.setScript((InputStream) script);
        } else {
            mizerContext.setScript(script.toString());
        }
        mizerContext.setRecord(record);
        return mizerContext;
    }

    private static final Pattern   INVALID_VERSION_FORMAT  = Pattern.compile("^.*(?<expression>version\\s*[:]?=\\s*\"[\\d.]+\").*$", Pattern.MULTILINE);
    private static final String    INVALID_VERSION_MESSAGE = "The Mizer service failed to find a Mizer implementation that knows how to handle your"
                                                             + "script, but also found what appears to be a malformed version declaration. The "
                                                             + "version declaration should be use the format 'version \"X.Y\"', where X.Y is a valid "
                                                             + "version such as \"6.1\". The statement in your script is: %s";

    private final List<Mizer> _mizers;

    private StagingDirectoryResolver _stagingDirectories = StagingDirectoryResolver.JAVA_IO_TMPDIR;
}
