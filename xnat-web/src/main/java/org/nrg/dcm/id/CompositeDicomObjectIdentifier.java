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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.ChainExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Labels;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class CompositeDicomObjectIdentifier implements DicomObjectIdentifier<XnatProjectdata> {
    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {
        this(null, identifier, subjectExtractors, sessionExtractors, aaExtractors);
    }

    public CompositeDicomObjectIdentifier(final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        this(null, identifier, Collections.singletonList(subjectExtractor),
                Collections.singletonList(sessionExtractor), Collections.singletonList(aaExtractor));
    }

    public CompositeDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Extractor subjectExtractor,
                                          final Extractor sessionExtractor,
                                          final Extractor aaExtractor) {
        this(name, identifier, Collections.singletonList(subjectExtractor),
                Collections.singletonList(sessionExtractor), Collections.singletonList(aaExtractor));
    }

    public CompositeDicomObjectIdentifier(final String name,
                                          final DicomProjectIdentifier identifier,
                                          final Iterable<Extractor> subjectExtractors,
                                          final Iterable<Extractor> sessionExtractors,
                                          final Iterable<Extractor> aaExtractors) {

        _name = StringUtils.defaultIfBlank(name, StringUtils.uncapitalize(getClass().getSimpleName()));
        _identifier = identifier;
        _subjectExtractors = subjectExtractors;
        _sessionExtractors = sessionExtractors;
        _aaExtractors = aaExtractors;
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
        return Labels.toLabelChars(addAndChainExtractors(_sessionExtractors, ExtractorType.SESSION).extract(o));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getSubjectLabel(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final String getSubjectLabel(final DicomObject o) {
        return Labels.toLabelChars(addAndChainExtractors(_subjectExtractors, ExtractorType.SUBJECT).extract(o));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#getTags()
     */
    @Override
    public ImmutableSortedSet<Integer> getTags() {
        if (!_initialized) {
            initialize();
        }
        // The below can change at any time, so must be added "live"
        List<Extractor> dynamicExtractors = new ArrayList<>();
        for (ExtractorType type : ExtractorType.values()) {
            List<Extractor> e = getDynamicExtractors(type);
            if (e != null) {
                dynamicExtractors.addAll(e);
            }
        }
        if (!dynamicExtractors.isEmpty()) {
            ImmutableSortedSet.Builder<Integer> builder = ImmutableSortedSet.naturalOrder();
            builder.addAll(_tags);
            for (Extractor e : dynamicExtractors) {
                builder.addAll(e.getTags());
            }
            return builder.build();
        } else {
            return ImmutableSortedSet.copyOf(_tags);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.DicomObjectIdentifier#requestsAutoarchive(org.dcm4che2.data.DicomObject)
     */
    @Override
    public final Boolean requestsAutoarchive(DicomObject o) {
        final String aa = addAndChainExtractors(_aaExtractors, ExtractorType.AA).extract(o);
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

    public enum ExtractorType {
        PROJECT,
        SUBJECT,
        SESSION,
        AA
    }

    @Nullable
    protected List<Extractor> getDynamicExtractors(ExtractorType type) {
        return null;
    }

    private Extractor addAndChainExtractors(final Iterable<Extractor> extractors, ExtractorType type) {
        List<Extractor> dynamicExtractors = getDynamicExtractors(type);
        if (dynamicExtractors != null) {
            List<Extractor> allExtractors = new ArrayList<>(dynamicExtractors);
            for (Extractor e : extractors) {
                allExtractors.add(e);
            }
            return new ChainExtractor(allExtractors);
        } else {
            return new ChainExtractor(extractors);
        }
    }

    public final void setUser(final UserI user) {
        this.user = user;
    }

    public final void setUserProvider(final Provider<UserI> userProvider) {
        this.userProvider = userProvider;
    }

    public DicomProjectIdentifier getProjectIdentifier() {
        return _identifier;
    }

    protected synchronized void initialize() {
        if (!_initialized) {
            ImmutableSortedSet.Builder<Integer> builder = ImmutableSortedSet.naturalOrder();
            builder.addAll(_identifier.getTags());
            for (Iterable<Extractor> ei : Arrays.asList(_aaExtractors, _sessionExtractors, _subjectExtractors)) {
                for (Extractor e : ei) {
                    builder.addAll(e.getTags());
                }
            }
            _tags.addAll(builder.build());
            _initialized = true;
        }
    }

    private static final Pattern TRUE  = Pattern.compile("t(?:rue)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE = Pattern.compile("f(?:alse)?", Pattern.CASE_INSENSITIVE);

    private UserI           user         = null;
    private Provider<UserI> userProvider = null;

    private final String                 _name;
    private final DicomProjectIdentifier _identifier;
    private final Iterable<Extractor>    _subjectExtractors, _sessionExtractors, _aaExtractors;
    private final SortedSet<Integer>     _tags        = new TreeSet<>();
    private     boolean                  _initialized = false;
}
