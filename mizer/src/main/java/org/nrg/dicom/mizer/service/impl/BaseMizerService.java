package org.nrg.dicom.mizer.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.DicomObject;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
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
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
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
    public AnonymizationResult anonymize(final DicomObject dicomObject, String project, String subject, String session, String scriptString, final boolean ignoreRejection) throws MizerException {
        final DicomObjectI doi = DicomObjectFactory.newInstance(dicomObject);
        final Properties anonContext = new Properties();
        anonContext.put("project", project);
        anonContext.put("subject", subject);
        anonContext.put("session", session);
        return anonymize(doi, anonContext, scriptString, ignoreRejection);
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
    // changed headers are written to a temporary file in the system's temp directory under
    // in the "anon_backup" directory. The given file is replaced with the
    // temporary file if the anonymization process is successful. The "anon_backup" directory
    // is left in place.
    @Override
    public AnonymizationResult anonymize(final File dicomFile, final MizerContext context) throws MizerException {
        try {
            final Mizer mizer = findMizer(context);
            log.info("Found mizer for versions {}", Joiner.on(", ").join(mizer.getSupportedVersions()));
            final CallOnFile<AnonymizationResult> callOnFile = new AnonymizeCallOnFileWithPixels(dicomFile, mizer, context);
            final File tmpdir = new File(System.getProperty("java.io.tmpdir"), "anon_backup");
            final WorkOnCopyOp<AnonymizationResult> anonymizeOp = new WorkOnCopyOp<>(dicomFile, tmpdir, callOnFile);
            return new TransactionRunner<AnonymizationResult>().runTransaction(anonymizeOp);
        } catch (RollbackException | TransactionException e) {
            throw new MizerException(e);
        }
    }

    @Override
    public List<AnonymizationResult> anonymize(List<File> dicomFiles, String project, String subject, String session, long scriptId, String script, boolean record, boolean ignoreRejection) throws MizerException {
        try {
            List<AnonymizationResult> resultList = new ArrayList<>();
            MizerContextWithScript context = createContext( project, subject, session, scriptId, script, record, ignoreRejection);
            final Mizer mizer = findMizer(context);
            log.info("Found mizer for versions {}", Joiner.on(", ").join(mizer.getSupportedVersions()));
            mizer.setContext( context);
            for( File dicomFile: dicomFiles) {
                final CallOnFile<AnonymizationResult> callOnFile = new AnonymizeCallOnFileWithPixels(dicomFile, mizer, context);
                final File tmpdir = new File(System.getProperty("java.io.tmpdir"), "anon_backup");
                final WorkOnCopyOp<AnonymizationResult> anonymizeOp = new WorkOnCopyOp<>(dicomFile, tmpdir, callOnFile);
                AnonymizationResult result = new TransactionRunner<AnonymizationResult>().runTransaction(anonymizeOp);
                result.setAbsolutePath(dicomFile.getAbsolutePath());
                resultList.add(result);
            }
            mizer.removeContext( context);
            return resultList;
        } catch (RollbackException | TransactionException e) {
            throw new MizerException(e);
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
}
