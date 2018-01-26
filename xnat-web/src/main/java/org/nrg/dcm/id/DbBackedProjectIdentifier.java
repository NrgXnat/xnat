/*
 * web: org.nrg.dcm.id.DbBackedProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.services.cache.UserProjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class DbBackedProjectIdentifier implements DicomProjectIdentifier {
    /**
     * Creates a new project identifier instance that uses the submitted cache to store and retrieve project objects.
     *
     * @param cache The cache used to store retrieved project instances for
     */
    public DbBackedProjectIdentifier(final UserProjectCache<XnatProjectdata> cache) {
        _log.debug("Creating DbBackedProjectIdentifier object with default constructor.");
        _cache = cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final XnatProjectdata apply(final UserI user, final DicomObject dicomObject) {
        if (!_initialized) {
            initialize();
        }
        final String userId = user.getUsername();
        for (final DicomDerivedString extractor : _extractors) {
            final String alias = extractor.apply(dicomObject);
            if (_log.isDebugEnabled()) {
                dumpExtractor(extractor, dicomObject, alias);
            }

            if (StringUtils.isNotBlank(alias)) {
                _log.debug("Looking for alias {} for user {}", alias, userId);
                final XnatProjectdata project = _cache.get(user, alias);
                if (project != null) {
                    _log.debug("Found project {} by ID or alias {} for user {}", project.getId(), alias, userId);
                    return project;
                }
                _log.debug("No project found for ID or alias {} for user {}", alias, userId);
            } else {
                _log.debug("The extractor didn't find any useful value for alias.");
            }
        }
        _log.debug("None of the extractors found an identifiable project, returning null, probably unassigned.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableSortedSet.copyOf(_tags);
    }

    /**
     * Resets the identifier, causing it to reload its configuration on the next access.
     */
    @Override
    public void reset() {
        _initialized = false;
    }

    abstract protected List<DicomDerivedString> getIdentifiers();

    private synchronized void initialize() {
        _extractors.clear();
        _tags.clear();
        for (final DicomDerivedString function : getIdentifiers()) {
            _extractors.add(function);
            _tags.addAll(function.getTags());
        }
        _initialized = true;
    }

    private void dumpExtractor(final DicomDerivedString extractor, final DicomObject dicomObject, final String alias) {
        final Class<? extends DicomDerivedString> extractorClass = extractor.getClass();
        _log.debug("Extractor:   {}", extractorClass.getSimpleName());
        _log.debug(" toString(): {}", extractor.toString());
        _log.debug(" found():    {}", StringUtils.defaultIfBlank(alias, "(blank)"));

        for (final int tag : extractor.getTags()) {
            final DicomElement tagValue = dicomObject.get(tag);
            final String       display  = tagValue == null ? "(null)" : tagValue.getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
            _log.debug(" tag {}:     {}", DicomUtils.getDicomAttribute(tag), display);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DbBackedProjectIdentifier.class);

    private final UserProjectCache<XnatProjectdata> _cache;

    private final List<DicomDerivedString> _extractors  = new ArrayList<>();
    private final SortedSet<Integer>       _tags        = new TreeSet<>();
    private       boolean                  _initialized = false;
}
