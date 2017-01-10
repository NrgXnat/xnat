/*
 * web: org.nrg.dcm.id.DbBackedProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.dcm4che2.data.DicomObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class DbBackedProjectIdentifier implements DicomProjectIdentifier {
    public DbBackedProjectIdentifier() {
        _log.debug("Creating DbBackedProjectIdentifier object with default constructor.");
    }

    public final XnatProjectdata apply(final UserI user, final DicomObject o) {
        if (!_initialized) {
            initialize();
        }
        final Cache cache = GradualDicomImporter.getUserProjectCache(user);
        for (final DicomDerivedString extractor : _extractors) {
            final String alias = extractor.apply(o);
            if (!Strings.isNullOrEmpty(alias)) {
                // added caching here to prevent duplicate project queries in every file transaction
                // the cache is shared with the one in gradual dicom importer, which does a similar query.
                final Element pe = cache.get(alias);
                if (null == pe) {
                    // no cached value, look in the db
                    final XnatProjectdata p = XnatProjectdata.getProjectByIDorAlias(alias, user, false);
                    if (null != p && canCreateIn(user, p)) {
                        cache.put(new Element(alias, p));
                        return p;
                    } else {
                        // this alias is either not a project or not one we can write to
                        GradualDicomImporter.cacheNonWriteableProject(cache, alias);
                    }
                } else if (!GradualDicomImporter.isCachedNotWriteableProject(pe)) {
                    return (XnatProjectdata) pe.getObjectValue();
                } // else cache returned no-such-writable-project, so continue
            }
        }
        return null;
    }

    public SortedSet<Integer> getTags() {
        if (!_initialized) {
            initialize();
        }
        return ImmutableSortedSet.copyOf(_tags);
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

    private boolean canCreateIn(final UserI user, final XnatProjectdata p) {
        try {
            return PrearcUtils.canModify(user, p.getId());
        } catch (Exception e) {
            _log.error("Unable to check permissions for " + user + " in " + p, e);
            return false;
        }
    }

    private final Logger                   _log         = LoggerFactory.getLogger(DbBackedProjectIdentifier.class);
    private final List<DicomDerivedString> _extractors  = new ArrayList<>();
    private final SortedSet<Integer>       _tags        = new TreeSet<>();
    private       boolean                  _initialized = false;
}
