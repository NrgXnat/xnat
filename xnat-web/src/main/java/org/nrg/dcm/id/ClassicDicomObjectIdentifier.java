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
import org.dcm4che2.data.Tag;
import org.nrg.dcm.ContainedAssignmentExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.TextExtractor;
import org.nrg.xnat.utils.XnatUserProvider;

import java.util.List;
import java.util.regex.Pattern;

public class ClassicDicomObjectIdentifier extends CompositeDicomObjectIdentifier {
    private static final ImmutableList<Extractor> attributeExtractors = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "AA", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "AA", Pattern.CASE_INSENSITIVE))
                                                                                                              .build();
    private static final ImmutableList<Extractor> sessionExtractors   = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "Session", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "Session", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new TextExtractor(Tag.PatientID))
                                                                                                              .build();
    private static final ImmutableList<Extractor> subjectExtractors   = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "Subject", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "Subject", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new TextExtractor(Tag.PatientName))
                                                                                                              .build();

    public ClassicDicomObjectIdentifier(final XnatUserProvider userProvider) {
        this(null, userProvider);
    }

    public ClassicDicomObjectIdentifier(final String name, final XnatUserProvider userProvider) {
        super(name, new Xnat15DicomProjectIdentifier(), subjectExtractors, sessionExtractors, attributeExtractors);
        setUserProvider(userProvider);
    }

    public static List<Extractor> getAAExtractors() { return attributeExtractors; }
    public static List<Extractor> getSessionExtractors() { return sessionExtractors; }
    public static List<Extractor> getSubjectExtractors() { return subjectExtractors; }
}
