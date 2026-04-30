/*
 * mizer: org.nrg.dicom.mizer.service.impl.test.TestMizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.service.impl.test;

import org.dcm4che3.data.Tag;
import org.nrg.dicom.mizer.exceptions.MizerContextException;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.VersionString;
import org.nrg.dicom.mizer.service.impl.AbstractMizer;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.*;

//@Component
public class TestMizer extends AbstractMizer {

    private static final List<VersionString> supportedVersions = Arrays.asList(
            new VersionString("6.0"),
            new VersionString("6.1"));
    public static final Set<Variable> REFERENCED_VARIABLES = new HashSet<>(Arrays.asList(
            new BasicVariable("project"),
            new BasicVariable("subject"),
            new BasicVariable("session")));
    public static final Set<TagPath> REFERENCED_TAG_PATHS = new HashSet<>(Arrays.asList(
            new TagPath().addTag(new TagPublic(0x00100010)),
            new TagPath().addTag(new TagPublic(Tag.PixelData - 1))
    ));
    public static final String RESULT_MESSAGE = "from TestMizer.";
    public static final String CODE_MEANING = "Test Mizer";
    public static final String CODE_DESIGNATOR = "Test";
    public static final String CODE_VERSION = "1.0";

    public TestMizer() {
        super(supportedVersions);
    }

    @Override
    public boolean understands(MizerContext context) throws MizerException {
        return super.understands(context);
    }

    @Override
    public void aggregate(final MizerContext context, final Set<Variable> variables) throws MizerContextException {

    }

    @Override
    protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) throws MizerException {
        return new AnonymizationResultSuccess(dicomObject, RESULT_MESSAGE);
    }

    @Override
    protected String getMeaning() {
        return CODE_MEANING;
    }

    @Override
    protected String getSchemeDesignator() {
        return CODE_DESIGNATOR;
    }

    @Override
    protected String getSchemeVersion() {
        return CODE_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<org.nrg.dicom.mizer.tags.TagPath> getScriptTags(final List<MizerContext> contexts) {
        return new HashSet<org.nrg.dicom.mizer.tags.TagPath>() {{
            for (final MizerContext context : contexts) {
                addAll(getScriptTags(context));
            }
        }};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<org.nrg.dicom.mizer.tags.TagPath> getScriptTags(final MizerContext context) {
        return REFERENCED_TAG_PATHS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getReferencedVariables(final MizerContext context, final Set<Variable> variables) throws MizerContextException {
        variables.addAll(REFERENCED_VARIABLES);
    }

    @Override
    public void setContext(MizerContextWithScript context) {
    }

    @Override
    public void removeContext(MizerContextWithScript context) {
    }
}
