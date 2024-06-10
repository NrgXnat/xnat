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

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
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
    final File sourceCatFile;
    final File destCatFile;
    final String sourceProject;
    final String destProject;

    /**
     * Merge source catalog into destination catalog. This will only merge the catalog beans, files need to be
     * moved separately and the catalog file will need to be saved separately (usually as part of the overall file move)
     *
     * @param src the catalog bean containing new/incoming files
     * @param dest the existing catalog bean
     * @param overwrite should existing files be overwritten?
     * @param ci the event data
     * @param destCatFile the existing catalog file
     * @param destProject the project containing the existing catalog
     *
     * @deprecated This constructor doesn't allow updates to the catalog based on the file data (checksum, size, etc.).
     * Use {@link MergeCatCatalog#MergeCatCatalog(CatalogUtils.CatalogData, CatalogUtils.CatalogData, boolean, EventMetaI)}
     * or be sure to run a catalog refresh after the files are in place.
     */
    @Deprecated
    public MergeCatCatalog(final CatCatalogI src, final CatCatalogI dest, final boolean overwrite, final EventMetaI ci,
                           final File destCatFile, String destProject) {
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
        this.ci = ci;
        this.destCatFile = destCatFile;
        this.destProject = destProject;
        this.sourceCatFile = null;
        this.sourceProject = null;
    }

    /**
     * Merge source catalog into destination catalog. This will only merge the catalog beans, files need to be
     * moved separately and the catalog file will need to be saved separately (usually as part of the overall file move)
     *
     * @param srcCatalogData catalog data object for new/incoming files
     * @param destCatalogData catalog data object for existing files
     * @param overwrite should existing files be overwritten?
     * @param ci the event data
     */
    public MergeCatCatalog(final CatalogUtils.CatalogData srcCatalogData,
                           final CatalogUtils.CatalogData destCatalogData,
                           final boolean overwrite,
                           final EventMetaI ci) {
        this.src = srcCatalogData.catBean;
        this.dest = destCatalogData.catBean;
        this.overwrite = overwrite;
        this.ci = ci;
        this.destCatFile = destCatalogData.catFile;
        this.destProject = destCatalogData.project;
        this.sourceCatFile = srcCatalogData.catFile;
        this.sourceProject = srcCatalogData.project;
    }

    public MergeSessionsA.Results<Boolean> call() throws Exception {
        return merge(src, dest, overwrite, ci, sourceCatFile, destCatFile, sourceProject, destProject);
    }

    private static MergeSessionsA.Results<Boolean> merge(final CatCatalogI incomingCatalog,
                                                         final CatCatalogI existingCatalog,
                                                         final boolean overwrite,
                                                         final EventMetaI ci,
                                                         @Nullable final File incomingCatalogFile,
                                                         final File existingCatalogFile,
                                                         @Nullable final String incomingCatalogProject,
                                                         final String existingCatalogProject)
            throws Exception {

        boolean merge = false;
        final MergeSessionsA.Results<Boolean> result = new MergeSessionsA.Results<>();
        for (final CatCatalogI subCat : incomingCatalog.getSets_entryset()) {
            final MergeSessionsA.Results<Boolean> r = merge(subCat, existingCatalog, overwrite, ci,
                    incomingCatalogFile, existingCatalogFile, incomingCatalogProject, existingCatalogProject);
            if (r.result) {
                merge = true;
            }
            result.addAll(r);
        }

        for (final CatEntryI incomingEntry : incomingCatalog.getEntries_entry()) {
            merge = true;   // if there are any entries in the catalog, we're merging
                            // (or throwing an exception if overwrite not permitted)

            final Optional<CatEntryI> existingEntryOptional = locateExistingEntry(incomingEntry, existingCatalog);
            if (!existingEntryOptional.isPresent()) {
                existingCatalog.addEntries_entry(incomingEntry);
                continue; // Additive change, moving along
            }

            final CatEntryI existingEntry = existingEntryOptional.get();
            if (!overwrite) {
                // There's already an entry for this file, throw an exception
                if (existingEntry instanceof CatDcmentryI) {
                    throw new DCMEntryConflict("Duplicate DICOM file uploaded.", new Exception());
                } else {
                    throw new EntryConflict("Duplicate file uploaded.", new Exception());
                }
            }

            throwForDicomUidConflict(incomingEntry, existingCatalog);
            copyEntryToCatalogHistoryAndHandleFilenameChange(incomingEntry, existingEntry, existingCatalogFile,
                    existingCatalogProject, result, ci);
            updateExistingEntry(incomingEntry, existingEntry, ci, incomingCatalogFile, incomingCatalogProject);
        }

        return result.setResult(merge);
    }

    private static Optional<CatEntryI> locateExistingEntry(final CatEntryI incomingEntry,
                                                           final CatCatalogI existingCatalog) {
        // Locate entry in the existing catalog that corresponds to the incoming entry
        CatEntryI existingEntry = null;
        if (incomingEntry instanceof CatDcmentryI) {
            final String uid = ((CatDcmentryI) incomingEntry).getUid();
            // If we should identify by UID, try to do that
            if (XDAT.getSiteConfigPreferences().getUseSopInstanceUidToUniquelyIdentifyDicom()
                    && StringUtils.isNotBlank(uid)) {
                existingEntry = CatalogUtils.getDCMEntryByUID(existingCatalog, uid);
            }
        }
        // If we are identifying by filename, or if we couldn't find a match based on UID, use URI
        if (existingEntry == null) {
            existingEntry = CatalogUtils.getEntryByURI(existingCatalog, incomingEntry.getUri());
        }
        return Optional.ofNullable(existingEntry);
    }

    private static void throwForDicomUidConflict(final CatEntryI incomingEntry,
                                                 final CatCatalogI existingCatalog)
            throws DCMEntryConflict {
        if (!(incomingEntry instanceof CatDcmentryI)) {
            // Not DICOM, no potential for UID conflict
            return;
        }

        if (!XDAT.getSiteConfigPreferences().getUseSopInstanceUidToUniquelyIdentifyDicom()) {
            // We identified the existing entry by filename, so there isn't a risk of overwriting some other file
            // in a different entry that happens to have a matching filename
            return;
        }

        // If we identified the existing entry by UID, we want to be sure we don't overwrite a file corresponding to
        // a different entry whose filename happens to match that of the incoming file.
        final CatEntryI existingEntryByURI = CatalogUtils.getEntryByURI(existingCatalog, incomingEntry.getUri());
        if (existingEntryByURI == null) {
            // No matches, all good
            return;
        }

        if (!(existingEntryByURI instanceof CatDcmentryI)) {
            // Name conflict with non-DICOM file
            throw new DCMEntryConflict("A non-DICOM file exists with the same name as an incoming DICOM file. " +
                    "There is no way to override this conflict, you will need to fix your data.", new Exception());
        }

        if (!StringUtils.equals(((CatDcmentryI) incomingEntry).getUid(), ((CatDcmentryI) existingEntryByURI).getUid())) {
            // This is basically saying that the existingEntryByURI is NOT the same as the existingEntry we already
            // identified by UID (with UID of incomingEntry). We could alternatively compare the URIs of the existing entries
            throw new DCMEntryConflict("A DICOM file with the same name but different UID already exists. " +
                    "There is no way to override this conflict, you will need to fix your data.", new Exception());
        }
    }

    private static void copyEntryToCatalogHistoryAndHandleFilenameChange(final CatEntryI incomingEntry,
                                                                         final CatEntryI existingEntry,
                                                                         final File existingCatalogFile,
                                                                         final String existingProject,
                                                                         final MergeSessionsA.Results<Boolean> result,
                                                                         final EventMetaI ci) throws Exception {
        // Both these operations need access to the file that will be overwritten, but often we don't need to do either.
        // We only want to stat that file if we actually need it.
        final boolean filenameChanged = !StringUtils.equals(incomingEntry.getUri(), existingEntry.getUri());
        if (!CatalogUtils.maintainFileHistory() && !filenameChanged) {
            return;
        }
        final File fileToBeOverwritten = CatalogUtils.getFile(existingEntry,
                existingCatalogFile.getParentFile().getAbsolutePath(), existingProject);
        if (fileToBeOverwritten == null) {
            // We can't locate the file we're trying to overwrite, so... nothing to do!
            return;
        }
        if (filenameChanged) {
            handleFilenameChange(fileToBeOverwritten, result, ci);
        }
        if (CatalogUtils.maintainFileHistory()) {
            // This needs to run before we update the metadata on existingEntry object
            copyCatalogEntryToHistory(fileToBeOverwritten, existingEntry, existingCatalogFile, existingProject, ci);
        }
    }

    private static void handleFilenameChange(final File fileToBeOverwritten,
                                             final MergeSessionsA.Results<Boolean> result,
                                             final EventMetaI ci) {
        result.getBeforeDirMerge().add(() -> {
            if (CatalogUtils.maintainFileHistory()) {
                FileUtils.MoveToHistory(fileToBeOverwritten, EventUtils.getTimestamp(ci));
            } else {
                Files.delete(fileToBeOverwritten.toPath());
            }
            return true;
        });
    }

    private static void copyCatalogEntryToHistory(final File fileToBeOverwritten,
                                                  final CatEntryI existingEntry,
                                                  final File existingCatalogFile,
                                                  final String existingProject,
                                                  final EventMetaI ci) throws Exception {
        // This copies the existing catalog entry (corresponding to the file we will overwrite in archive)
        // to the .history catalog file. The file it references (fileToBeOverwritten) will be moved (to f) during the
        // directory merge step. We need to be sure to add the existingEntry to the history catalog prior to
        // altering the object in updateCatalogEntry.
        final File f = FileUtils.BuildHistoryFile(fileToBeOverwritten, EventUtils.getTimestamp(ci));
        CatalogUtils.addCatHistoryEntry(existingCatalogFile, existingProject, f.getAbsolutePath(),
                (CatEntryBean) existingEntry, ci);
    }

    private static void updateExistingEntry(final CatEntryI incomingEntry,
                                            final CatEntryI existingEntry,
                                            final EventMetaI ci,
                                            @Nullable final File incomingCatalogFile,
                                            final String incomingProject) {
        final File catalogParentFile = incomingCatalogFile != null ? incomingCatalogFile.getParentFile() : null;
        final File incomingFile = catalogParentFile != null ?
                CatalogUtils.getFile(incomingEntry, catalogParentFile.getAbsolutePath(), incomingProject) : null;
        if (incomingFile != null) {
            final String relativePath = catalogParentFile.toPath().relativize(incomingFile.toPath()).toString();
            CatalogUtils.updateExistingCatEntry(existingEntry, incomingFile, relativePath, ci);
        } else {
            // We cannot locate the incoming file to update size, checksum, etc. on the entry. We update what we can
            // here, but a catalog refresh will be necessary for accurate metadata.
            if (!StringUtils.equals(incomingEntry.getUri(), existingEntry.getUri())) {
                // Update name, this should only ever happen with DICOM with same SOP Instance UID but different name
                existingEntry.setUri(incomingEntry.getUri());
                existingEntry.setId(incomingEntry.getId());
                existingEntry.setCachepath(incomingEntry.getCachepath());
            }

            if (ci != null) {
                // Update existing catalog entry with this event
                CatalogUtils.updateModificationEvent(existingEntry, ci);
            }
        }
    }
}
