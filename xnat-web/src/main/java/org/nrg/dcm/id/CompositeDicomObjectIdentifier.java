/*
 * org.nrg.dcm.id.CompositeDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import java.util.regex.Pattern;

import javax.inject.Provider;

import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.ChainExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Labels;

import com.google.common.collect.ImmutableSortedSet;

public class CompositeDicomObjectIdentifier implements
DicomObjectIdentifier<XnatProjectdata> {
    private final DicomProjectIdentifier projectID;
    private final Extractor subjectExtractor, sessionExtractor, aaExtractor;
    private final ImmutableSortedSet<Integer> tags;

    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier projectID,
            final Extractor subjectExtractor,
            final Extractor sessionExtractor,
            final Extractor aaExtractor) {
        this.projectID = projectID;
        this.subjectExtractor = subjectExtractor;
        this.sessionExtractor = sessionExtractor;
        this.aaExtractor = aaExtractor;
        
        ImmutableSortedSet.Builder<Integer> builder = ImmutableSortedSet.naturalOrder();
        builder.addAll(projectID.getTags());
        builder.addAll(aaExtractor.getTags());
        builder.addAll(sessionExtractor.getTags());
        builder.addAll(subjectExtractor.getTags());
        tags = builder.build();  
    }
    
    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier projectID,
            final Iterable<Extractor> subjectExtractors,
            final Iterable<Extractor> sessionExtractors,
            final Iterable<Extractor> aaExtractors) {
        this(projectID, new ChainExtractor(subjectExtractors),
                new ChainExtractor(sessionExtractors), new ChainExtractor(aaExtractors));
    }

    private UserI user = null;
    private Provider<UserI> userProvider = null;
    
    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getProject(org.dcm4che2.data.DicomObject)
     */
    public final XnatProjectdata getProject(final DicomObject o) {
	if (null == user && null != userProvider) {
	    user = userProvider.get();
	}
	return projectID.apply(user, o);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getSessionLabel(org.dcm4che2.data.DicomObject)
     */
    public final String getSessionLabel(final DicomObject o) {
        return Labels.toLabelChars(sessionExtractor.extract(o));
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getSubjectLabel(org.dcm4che2.data.DicomObject)
     */
    public final String getSubjectLabel(final DicomObject o) {
        return Labels.toLabelChars(subjectExtractor.extract(o));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getTags()
     */
    public final ImmutableSortedSet<Integer> getTags() {
        return tags;
    }
    
    private static final Pattern TRUE = Pattern.compile("t(?:rue)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE = Pattern.compile("f(?:alse)?", Pattern.CASE_INSENSITIVE);

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#requestsAutoarchive(org.dcm4che2.data.DicomObject)
     */
    public final Boolean requestsAutoarchive(DicomObject o) {
        final String aa = aaExtractor.extract(o);
        if (null == aa) {
            return null;
        } else if (TRUE.matcher(aa).matches()) {
            return true;
        } else if (FALSE.matcher(aa).matches()) {
            return false;
        } else {
            return null;
        }
    }
    
    public final void setUser(final UserI user) {
        this.user = user;
    }
    
    public final void setUserProvider(final Provider<UserI> userProvider) {
        this.userProvider = userProvider;
    }
}
