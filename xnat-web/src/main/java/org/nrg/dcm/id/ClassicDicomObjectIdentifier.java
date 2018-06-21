/*
 * web: org.nrg.dcm.id.ClassicDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import java.util.List;
import java.util.SortedSet;
import java.util.regex.Pattern;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.StringUtils;
import org.nrg.dcm.ContainedAssignmentExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.TextExtractor;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier;
import org.nrg.framework.utilities.SortedSets;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class ClassicDicomObjectIdentifier extends CompositeDicomObjectIdentifier {
    private static final ImmutableList<Extractor> attributeExtractors = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "AA", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "AA", Pattern.CASE_INSENSITIVE))
                                                                                                              .build();
    private static final ImmutableList<Extractor> sessionExtractors   = new ImmutableList.Builder<Extractor>().add(new StraightAssignmentExtractor(Tag.AdditionalPatientHistory, 2))
            																								  .add(new ContainedAssignmentExtractor(Tag.PatientComments, "Session", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "Session", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new TextExtractor(Tag.PatientID))
                                                                                                              .build();
    private static final ImmutableList<Extractor> subjectExtractors   = new ImmutableList.Builder<Extractor>().add(new StraightAssignmentExtractor(Tag.AdditionalPatientHistory, 1))
    																										  .add(new ContainedAssignmentExtractor(Tag.PatientComments, "Subject", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new ContainedAssignmentExtractor(Tag.StudyComments, "Subject", Pattern.CASE_INSENSITIVE))
                                                                                                              .add(new TextExtractor(Tag.PatientName))
                                                                                                              .build();

    public ClassicDicomObjectIdentifier(final String name, final XnatUserProvider userProvider, final UserProjectCache userProjectCache) {
        super(name, new Xnat15DicomProjectIdentifier(userProjectCache), subjectExtractors, sessionExtractors, attributeExtractors);
        setUserProvider(userProvider);
    }

    public static List<Extractor> getAAExtractors() { return attributeExtractors; }
    public static List<Extractor> getSessionExtractors() { return sessionExtractors; }
    public static List<Extractor> getSubjectExtractors() { return subjectExtractors; }

    public static class StraightAssignmentExtractor   implements Extractor {
        private final Logger logger = LoggerFactory.getLogger(StraightAssignmentExtractor.class);
    	private int tag;
    	private int index;
        public StraightAssignmentExtractor(final int tag, final int index) {
            super();
            this.tag=tag;
            this.index=index;
        }

		@Override
		public String extract(DicomObject o) {
			final String v = o.getString(tag);
	        if (Strings.isNullOrEmpty(v)) {
	            logger.trace("no match to {}: null or empty tag", this);
	            return null;
	        } else {
	        	if(StringUtils.count(v, ',')==2){
	        		String[] chunks=StringUtils.split(v, ',');
	        		if(chunks.length==3){
	        			return chunks[index].trim();
	        		}else{
		                logger.trace("input {} did not match rule {}", v, this);
		                return null;
	        		}
	        	}else{
	                logger.trace("input {} did not match rule {}", v, this);
	                return null;
	        	}
	        }
		}

		@Override
	    public SortedSet<Integer> getTags() {
	        return SortedSets.singleton(Tag.AdditionalPatientHistory);
	    }
    }
}
