/*
 * web: org.nrg.dcm.id.CompositeDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.ChainExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Labels;

import javax.inject.Provider;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class CompositeDicomObjectIdentifier implements DicomObjectIdentifier<XnatProjectdata> {
    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {
        this(null, identifier, new ChainExtractor(subjectExtractors), new ChainExtractor(sessionExtractors), new ChainExtractor(aaExtractors));
    }

    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        this(null, identifier, subjectExtractor, sessionExtractor, aaExtractor);
    }

    public CompositeDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {
        this(name, identifier, new ChainExtractor(subjectExtractors), new ChainExtractor(sessionExtractors), new ChainExtractor(aaExtractors));
    }

    public CompositeDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        _name = StringUtils.defaultIfBlank(name, StringUtils.uncapitalize(getClass().getSimpleName()));
        _identifier = identifier;
        _subjectExtractor = subjectExtractor;
        _sessionExtractor = sessionExtractor;
        _aaExtractor = aaExtractor;
    }

    public String getName() {
        return _name;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getProject(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final XnatProjectdata getProject(final DicomObject o) {
        if (null == user && null != userProvider) {
            user = userProvider.get();
        }
        return _identifier.apply(user, o);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getSessionLabel(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final String getSessionLabel(final DicomObject o) {
        return Labels.toLabelChars(_sessionExtractor.extract(o));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getSubjectLabel(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final String getSubjectLabel(final DicomObject o) {
        return Labels.toLabelChars(_subjectExtractor.extract(o));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getTags()
     */
    @Override
    public final ImmutableSortedSet<Integer> getTags() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableSortedSet.copyOf(_tags);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#requestsAutoarchive(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final Boolean requestsAutoarchive(DicomObject o) {
        final String aa = _aaExtractor.extract(o);
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

    private void initialize() {
        ImmutableSortedSet.Builder<Integer> builder = ImmutableSortedSet.naturalOrder();
        builder.addAll(_identifier.getTags());
        builder.addAll(_aaExtractor.getTags());
        builder.addAll(_sessionExtractor.getTags());
        builder.addAll(_subjectExtractor.getTags());
        _tags.addAll(builder.build());
        _initialized = true;
    }

    private static final Pattern TRUE  = Pattern.compile("t(?:rue)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE = Pattern.compile("f(?:alse)?", Pattern.CASE_INSENSITIVE);

    private UserI           user         = null;
    private Provider<UserI> userProvider = null;

    private final String                 _name;
    private final DicomProjectIdentifier _identifier;
    private final Extractor              _subjectExtractor, _sessionExtractor, _aaExtractor;
    private final SortedSet<Integer> _tags        = new TreeSet<>();
    private       boolean            _initialized = false;
}
