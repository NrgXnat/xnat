/*
 * org.nrg.dcm.id.DbBackedProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 */
package org.nrg.dcm.id;


import java.util.SortedSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.dcm4che2.data.DicomObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

public class DbBackedProjectIdentifier implements DicomProjectIdentifier {
    private final Logger logger = LoggerFactory.getLogger(DbBackedProjectIdentifier.class);
    private final Iterable<DicomDerivedString> extractors;
    private final ImmutableSortedSet<Integer> tags;

    public DbBackedProjectIdentifier(final Iterable<DicomDerivedString> identifiers) {
        this.extractors = Lists.newArrayList(identifiers);
        final ImmutableSortedSet.Builder<Integer> b = ImmutableSortedSet.naturalOrder();
        for (final DicomObjectFunction f : identifiers) {
            b.addAll(f.getTags());
        }
        tags = b.build();
    }

    public final XnatProjectdata apply(final UserI user, final DicomObject o) {
        final Cache cache = GradualDicomImporter.getUserProjectCache(user);
        for (final DicomDerivedString extractor : extractors) {
            final String alias = extractor.apply(o);
            if (!Strings.isNullOrEmpty(alias)) {
                // added caching here to prevent duplicate project queries in every file transaction
                // the cache is shared with the one in gradual dicom importer, which does a similar query.
                final Element pe = cache.get(alias);
                if (null == pe) {
                    // no cached value, look in the db
                    final XnatProjectdata p = XnatProjectdata.getProjectByIDorAlias(alias.toString(), user, false);
                    if (null != p && canCreateIn(user,p)) {
                        cache.put(new Element(alias, p));
                        return p;
                    } else {
                        // this alias is either not a project or not one we can write to
                        GradualDicomImporter.cacheNonWriteableProject(cache, alias);
                    }                        
                } else if (!GradualDicomImporter.isCachedNotWriteableProject(pe)) {
                    return (XnatProjectdata)pe.getObjectValue();
                } // else cache returned no-such-writeable-project, so continue
            }
        }
        return null;
    }


    private boolean canCreateIn(final UserI user, final XnatProjectdata p) {
        try {
            return PrearcUtils.canModify(user, p.getId());
        } catch (Exception e) {
            logger.error("Unable to check permissions for " + user + " in " + p, e);
            return false;
        }
    }

    public final SortedSet<Integer> getTags() { return tags; }
}
