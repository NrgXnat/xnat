/*
 * web: org.nrg.dcm.id.ClassicDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.ContainedAssignmentExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.TextExtractor;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef;
import org.nrg.xdat.security.user.XnatUserProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * XnatDefaultDicomObjectIdentifier is a CompositeDicomObjectIdentifier.
 *
 * It pulls dynamic extractors from expressions in the configService and falls back to the specified fixed extractors.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class XnatDefaultDicomObjectIdentifier extends CompositeDicomObjectIdentifier {

	private final ExtractorProvider _extractorProvider;

	private static final ImmutableList<Extractor> attributeExtractors = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "AA", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.StudyComments, "AA", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, "AA", Pattern.CASE_INSENSITIVE))
			.build();
	private static final ImmutableList<Extractor> sessionExtractors   = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "Session", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.StudyComments, "Session", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, "Session", Pattern.CASE_INSENSITIVE))
			.add(new TextExtractor(Tag.PatientID))
			.build();
	private static final ImmutableList<Extractor> subjectExtractors   = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "Subject", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.StudyComments, "Subject", Pattern.CASE_INSENSITIVE))
			.add(new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, "Subject", Pattern.CASE_INSENSITIVE))
			.add(new TextExtractor(Tag.PatientName))
			.build();

    public XnatDefaultDicomObjectIdentifier(final String name, final XnatUserProvider userProvider, final DicomProjectIdentifier identifier) {
        super(name, identifier, subjectExtractors, sessionExtractors, attributeExtractors);
        setUserProvider(userProvider);
		_extractorProvider = new ExtractorFromConfigProvider();

	}

	@Override
	protected List<Extractor> getDynamicExtractors(ExtractorType type) {
		return _extractorProvider.provide( type);
	}

    public static List<Extractor> getAAExtractors() { return attributeExtractors; }
    @SuppressWarnings("unused")
    public static List<Extractor> getSessionExtractors() { return sessionExtractors; }
    @SuppressWarnings("unused")
    public static List<Extractor> getSubjectExtractors() { return subjectExtractors; }

	@Nullable
	public static List<AbstractConditionalAttrDef.Rule> getRulesFromConfig(ExtractorType type) {
		ExtractorProvider extractorProvider = new ExtractorFromConfigProvider();
		return extractorProvider.provideRules( type);
	}

}
