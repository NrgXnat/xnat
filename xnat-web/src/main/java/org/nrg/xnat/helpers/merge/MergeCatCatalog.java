/*
 * web: org.nrg.xnat.helpers.merge.MergeCatCatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatDcmentryI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MergeCatCatalog implements Callable<MergeSessionsA.Results<Boolean>> {
    public static class DCMEntryConflict extends Exception {
        public DCMEntryConflict(String string, Exception exception) {
            super(string, exception);
        }

    }

    public static class EntryConflict extends Exception {
        public EntryConflict(String string, Exception exception) {
            super(string, exception);
        }

    }

    final CatCatalogI src, dest;
    final boolean overwrite;
    final EventMetaI ci;
    final File destCatFile;
    final String destProject;

    public MergeCatCatalog(final CatCatalogI src, final CatCatalogI dest, final boolean overwrite, final EventMetaI ci,
                           final File destCatFile, String destProject) {
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
        this.ci = ci;
        this.destCatFile = destCatFile;
        this.destProject = destProject;
    }

    public MergeSessionsA.Results<Boolean> call() throws Exception {
        return merge(src, dest, overwrite, ci, destCatFile, destProject);
    }

    private static MergeSessionsA.Results<Boolean> merge(final CatCatalogI src, final CatCatalogI dest,
                                                         final boolean overwrite, final EventMetaI ci,
                                                         final File destCatFile, final String destProject)
            throws Exception {

        boolean merge = false;
        final MergeSessionsA.Results<Boolean> result = new MergeSessionsA.Results<>();
        for (final CatCatalogI subCat : src.getSets_entryset()) {
            final MergeSessionsA.Results<Boolean> r = merge(subCat, dest, overwrite, ci, destCatFile, destProject);
            if (r.result) {
                merge = true;
            }
            result.addAll(r);
        }

        for (final CatEntryI entry : src.getEntries_entry()) {
            merge = true;   // if there are any entries in the catalog, we're merging
                            // (or throwing an exception if overwrite not permitted)

            final String uid = entry instanceof CatDcmentryI ? ((CatDcmentryI) entry).getUid() : null;
            final Optional<CatEntryI> existingEntry = locateExistingEntry(entry, uid, dest);
            if (!existingEntry.isPresent()) {
                dest.addEntries_entry(entry);
                continue; // Additive change, moving along
            }

            final CatEntryI destEntry = existingEntry.get();
            if (!overwrite) {
                // There's already an entry for this file, throw an exception
                if (destEntry instanceof CatDcmentryI) {
                    throw new DCMEntryConflict("Duplicate DICOM file uploaded.", new Exception());
                } else {
                    throw new EntryConflict("Duplicate file uploaded.", new Exception());
                }
            }

            if (destEntry instanceof CatDcmentryI) {
                if (!StringUtils.equals(uid, ((CatDcmentryI) destEntry).getUid())) {
                    // Do not permit overwriting an existing file with a different UID, regardless of overwrite = true
                    throw new DCMEntryConflict("A DICOM file with the same name but different UID already exists.",
                            new Exception());
                }

                if (!StringUtils.equals(entry.getUri(), destEntry.getUri())) {
                    // Update name
                    destEntry.setUri(entry.getUri());
                    destEntry.setId(entry.getId());
                    destEntry.setCachepath(entry.getCachepath());
                }
            }

            if (ci != null) {
                // Update existing catalog entry with this event
                CatalogUtils.updateModificationEvent(destEntry, ci);
            }

            if (CatalogUtils.maintainFileHistory()) {
                //backup existing file
                //the entry should be copied to the .history catalog, the file will be moved separately
                result.getAfter().add(() -> {
                    File destFile = CatalogUtils.getFile(destEntry, destCatFile.getParentFile().getAbsolutePath(), destProject);
                    if (destFile != null) {
                        File f = FileUtils.BuildHistoryFile(destFile, EventUtils.getTimestamp(ci));
                        CatalogUtils.addCatHistoryEntry(destCatFile, destProject, f.getAbsolutePath(), (CatEntryBean) entry, ci);
                    }
                    return true;
                });
            }
        }

        return result.setResult(merge);
    }

    private static Optional<CatEntryI> locateExistingEntry(final CatEntryI entry, final String uid, final CatCatalogI dest) {
        CatEntryI destEntry = null;
        if (XDAT.getSiteConfigPreferences().getUseSopInstanceUidToUniquelyIdentifyDicom() &&
                entry instanceof CatDcmentryI && !StringUtils.isBlank(uid)) {
            destEntry = CatalogUtils.getDCMEntryByUID(dest, uid);
        }
        if (destEntry == null) {
            destEntry = CatalogUtils.getEntryByURI(dest, entry.getUri());
        }
        return Optional.ofNullable(destEntry);
    }
}
