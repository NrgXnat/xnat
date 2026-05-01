package org.nrg.dicom.mizer.service.impl;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.*;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.variables.Variable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Base Mizer implementation.
 *
 * Provides implementation of standard mizer operations valid across versions.
 */
public abstract class AbstractMizer implements Mizer {
    /**
     * Creates an instance of the mizer class with the submitted version strings.
     *
     * @param versions The versions of the DicomEdit language supported by this mizer implementation.
     */
    protected AbstractMizer(final List<VersionString> versions) {
        _versions = ImmutableList.copyOf(versions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void aggregate(final MizerContext context, final Set<Variable> variables) throws MizerContextException;

    /**
     * Performs the implementation-specific work required to anonymize the given DICOM object.
     *
     * @param dicomObject The {@link DicomObjectI DICOM object} to be processed.
     * @param context     The anonymization context.
     *
     * @throws MizerException When an error occurs anonymizing the object.
     */
    protected abstract AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) throws MizerException;

    /**
     * Provides the meaning for the de-identification code sequence for the mizer implementation.
     *
     * @return The mizer's de-identification code sequence meaning.
     */
    protected abstract String getMeaning();

    /**
     * Provides the scheme designator for the de-identification code sequence for the mizer implementation.
     *
     * @return The mizer's de-identification code sequence scheme designator.
     */
    protected abstract String getSchemeDesignator();

    /**
     * Provides the scheme version for the de-identification code sequence for the mizer implementation.
     *
     * @return The mizer's de-identification code sequence scheme version.
     */
    protected abstract String getSchemeVersion();

    @Override
    public abstract void setContext(MizerContextWithScript script) throws MizerException;

    @Override
    public abstract void removeContext( MizerContextWithScript script);

    /**
     * {@inheritDoc}
     */
    @Override
    public AnonymizationResult anonymize(final DicomObjectI dicomObject, final MizerContext context) throws MizerException {
        if (!(context instanceof MizerContextWithScript)) {
            throw new MizerException("Anonymization failed: DE4Mizer requires a script in the context.");
        }

        final AnonymizationResult result = anonymizeImpl(dicomObject, (MizerContextWithScript) context);

        if (context.isRecord() && context.getScriptId() > 0) {
            addRecord(dicomObject, context.getScriptId());
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean understands(final MizerContext context) throws MizerException {
        if (context instanceof MizerContextWithScript) {
            final MizerContextWithScript scriptContext = (MizerContextWithScript) context;
            final String                 scriptVersion = parseVersionString(scriptContext.getScript());
            if (StringUtils.isBlank(scriptVersion)) {
                return getMaxSupportedVersion().toString().startsWith("4.");
            } else {
                for (final VersionString supportedVersion : getSupportedVersions()) {
                    if (supportedVersion.matches(scriptVersion)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VersionString> getSupportedVersions() {
        return _versions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionString getMaxSupportedVersion() {
        return Collections.max(getSupportedVersions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nonnull final Mizer that) {
        return getMaxSupportedVersion().compareTo(that.getMaxSupportedVersion());
    }

    /**
     * Add a record indicating that the application of the script with the given ID was applied to the DICOM file to the
     * DICOM header. Each particular Mizer implementation should implement this method to call the {@link
     * #addRecord(DicomObjectI, long, String, String, String)} version of this method with values for the string
     * parameters appropriate for the particular mizer.
     *
     * @param dicomObject The DICOM object to add.
     * @param scriptId    The ID to record the DICOM object.
     *
     * @throws MizerException When an error occurs adding the record.
     */
    protected void addRecord(final DicomObjectI dicomObject, final long scriptId) throws MizerException {
        addRecord(dicomObject, scriptId, getMeaning(), getSchemeDesignator(), getSchemeVersion());
    }

    /**
     * Add a record to the DICOM header of the given DICOM object indicating that it has been edited.
     *
     * @param dicomObject      The DICOM object to add.
     * @param scriptId         The ID to record the DICOM object.
     * @param meaning          The meaning to associate with the DICOM object record.
     * @param schemeDesignator The designator to associate with the DICOM object record.
     * @param schemeVersion    The version to associate with the DICOM object record.
     *
     * @throws MizerException When an error occurs adding the record.
     */
    protected void addRecord(final DicomObjectI dicomObject, final long scriptId, final String meaning, final String schemeDesignator, final String schemeVersion) throws MizerException {

        final String sUniqueId = Long.toString(scriptId);
        if (!validateSizes(sUniqueId, StringUtils.defaultIfBlank(meaning, getMeaning()), StringUtils.defaultIfBlank(schemeDesignator, getSchemeDesignator()), StringUtils.defaultIfBlank(schemeVersion, getSchemeVersion()))) {
            throw new MizerException("One of the arguments: " + sUniqueId + "," + meaning + "," + schemeDesignator + "," + schemeVersion + " " + "exceeds the size fixed by the DICOM standard.");
        }

        if (dicomObject.getDcm4che2Object().get(Tag.DeidentificationMethodCodeSequence) == null) {
            dicomObject.getDcm4che2Object().putSequence(Tag.DeidentificationMethodCodeSequence);
        }

        final SequenceDicomElement record = (SequenceDicomElement) dicomObject.getDcm4che2Object().get(Tag.DeidentificationMethodCodeSequence);
        DicomObject item = new BasicDicomObject();
        item.putString( Tag.CodeValue, VR.SH, sUniqueId);
        item.putString( Tag.CodeMeaning, VR.LO, meaning);
        item.putString( Tag.CodingSchemeDesignator, VR.SH, schemeDesignator);
        item.putString( Tag.CodingSchemeVersion, VR.SH, schemeVersion);
        record.addDicomObject( item);

    }

    /**
     * Parse the version string out of the script file.
     *
     * This method is called on the List of string statements.
     * Assumes the script file contains a line with format 'version "some-string"'. The version string is some-string
     * with the double-quotes removed.
     *
     * @return the version string or the empty string if it is not found.
     */
    private static String parseVersionString(List<String> statements) {
        for(final String statement: statements) {
            if( statement.startsWith("version")) {
                return statement.split("\\s")[1].replaceAll("\"", "");
            }
        }
        return "";
    }

    /**
     * Ensure that the given code tags are within the size limits specified by the DICOM standard.
     *
     * @param uniqueId         The DICOM object ID to validate.
     * @param meaning          The meaning to validate.
     * @param schemeDesignator The designator to validate.
     * @param schemeVersion    The version to validate.
     *
     * @return true if all values are valid, false otherwise.
     */
    private static boolean validateSizes(final String uniqueId, final String meaning, final String schemeDesignator, final String schemeVersion) {
        return uniqueId.length() <= ValueLength &&
               schemeDesignator.length() <= DesignatorLength &&
               meaning.length() <= MeaningLength &&
               schemeVersion.length() <= VersionLength;
    }

    private final ImmutableList<VersionString> _versions;
}
