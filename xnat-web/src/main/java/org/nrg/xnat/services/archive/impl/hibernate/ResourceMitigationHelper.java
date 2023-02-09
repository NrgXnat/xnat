package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidReference;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceMitigationReport;
import org.nrg.xnat.services.archive.ResourceMitigationReport.ResourceMitigationReportBuilder;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is not the class you're looking for.
 */
@Slf4j
public class ResourceMitigationHelper implements Callable<ResourceMitigationReport> {
    public static final String FILE_MITIGATION = "file-mitigation";
    public static final String REQUEST         = "request";
    public static final String DELETED         = "deleted";
    public static final String UPDATED         = "updated";

    private static final FileTime         DEFAULT_FILE_TIME = FileTime.fromMillis(0);
    private static final Comparator<File> FILES_BY_DATE     = Comparator.comparing(file -> {
        try {
            return Files.readAttributes(file.toPath().toAbsolutePath(), BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            return DEFAULT_FILE_TIME;
        }
    });

    private final ResourceSurveyRequest _request;
    private final Path                  _cachePath;
    private final WrkWorkflowdata       _workflow;
    private final UserI                 _requester;

    public ResourceMitigationHelper(final ResourceSurveyRequest request,
                                    final WrkWorkflowdata workflow,
                                    final UserI requester,
                                    final SiteConfigPreferences preferences) throws InitializationException {
        log.debug("Creating a new resource mitigation helper for resource survey request {} for resource {} for user {}", request.getId(), request.getResourceId(), requester.getUsername());
        _request   = request;
        _workflow  = workflow;
        _requester = requester;
        _cachePath = getCachePath(preferences.getCachePath());
    }

    @Override
    public ResourceMitigationReport call() {
        final long requestId  = _request.getId();
        final int  resourceId = _request.getResourceId();

        final ResourceMitigationReportBuilder builder = ResourceMitigationReport.builder();
        builder.resourceSurveyRequestId(requestId);
        builder.cachePath(_cachePath);

        final ResourceSurveyReport report = _request.getSurveyReport();

        final XnatResourcecatalog                       catalogResource = getCatalogResource();
        final CatalogUtils.CatalogData                  catalogData     = getCatalogData(catalogResource);
        final Map<String, CatalogUtils.CatalogMapEntry> catalogMap      = CatalogUtils.buildCatalogMap(catalogData);

        final Path                                    sourcePath      = Paths.get(catalogData.catPath);
        final Function<File, Path>                    backupMapper    = file -> _cachePath.resolve(sourcePath.relativize(file.toPath()));
        final Function<Map.Entry<File, String>, Path> renameMapper    = entry -> sourcePath.resolve(entry.getValue());
        final boolean                                 isFileHistoryOn = CatalogUtils.maintainFileHistory();

        try (final PrintWriter writer = getMitigationLogWriter()) {
            writer.println("Beginning mitigation from resource survey request " + requestId + " for resource " + resourceId + "\n");
            writer.println("Have the following items:");

            // ALL files get backed up: renames get backed and then renamed, all remaining are deleted after renaming finished.
            final Map<Path, Path> backups = new HashMap<>();
            final Map<Path, Path> moves   = new HashMap<>();

            final List<File> badFiles = report.getBadFiles();
            writer.format(" * %d bad files (unparsable, etc.)\n", badFiles.size());
            if (!badFiles.isEmpty()) {
                badFiles.forEach(badFile -> writer.println("    - " + badFile));
                writer.println("\nNote: bad files are not removed from the resource folder and are recorded for later review and possible mitigation.");
            }

            final Map<File, String> mismatchedFiles = report.getMismatchedFiles();
            writer.format(" * %d mismatched files\n", mismatchedFiles.size());
            if (!mismatchedFiles.isEmpty()) {
                backups.putAll(mismatchedFiles.keySet().stream().collect(Collectors.toMap(File::toPath, backupMapper)));
                moves.putAll(mismatchedFiles.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toPath(), renameMapper)));
            }

            final Map<Pair<String, String>, Map<File, String>> duplicates = ResourceSurveyReport.flattenDuplicateMaps(report.getDuplicates());
            writer.format(" * %d unique class and instance UIDs with duplicate files, %d duplicate files total\n\nIssues:\n\n", duplicates.size(), duplicates.values().stream().mapToInt(Map::size).sum());
            if (!duplicates.isEmpty()) {
                duplicates.forEach((uid, map) -> {
                    final Set<String> calculatedNames = new HashSet<>(map.values());
                    if (calculatedNames.size() > 1) {
                        writer.format(" * There are %d calculated names for SOP class UID %s and instance UID %s, I can't fix this myself.", calculatedNames.size(), uid.getKey(), uid.getValue());
                    } else {
                        backups.putAll(map.keySet().stream().collect(Collectors.toMap(File::toPath, backupMapper)));
                        map.keySet().stream().max(FILES_BY_DATE).ifPresent(file -> moves.put(file.toPath(), sourcePath.resolve(map.get(file))));
                    }
                });
            }

            writer.format("Actions:\n\nI have %d files to backup, %d files to rename\n\n", backups.size(), moves.size());
            writer.format(isFileHistoryOn ? "Files to be moved/deleted (not backing up, maintain file history is turned on\n" : "Backing up files\n");
            final Map<File, Map<String, String>> backupErrors = new HashMap<>();
            final Map<File, Map<String, String>> moveErrors   = new HashMap<>();
            final Map<File, String>              deleteErrors = new HashMap<>();

            log.info("Running mitigation for resource survey request {} for resource {}: resource contains:\n * {} bad files\n * {} mismatched files\n * {} SOP class/instance UIDs with duplicated files\n * {} backups", requestId, resourceId, badFiles.size(), mismatchedFiles.size(), duplicates.size(), backups.size());
            final List<Path> deletes = backups.entrySet().stream().map(entry -> {
                final Path source = entry.getKey();
                final Path target = entry.getValue();
                try {
                    final boolean isMove = moves.containsKey(source);
                    if (isFileHistoryOn) {
                        log.debug("Processing resource survey request {} for resource {}: file {} will be {}", requestId, resourceId, source, isMove ? "moved" : "deleted");
                        writer.format(" * %s (to be " + (isMove ? "moved" : "deleted") + ")", source.toAbsolutePath());
                    } else {
                        log.debug("Processing resource survey request {} for resource {}: copying file {} to {}", requestId, resourceId, source, target);
                        Files.copy(source, target);
                        writer.format(" * %s backed up to %s\n", source.toAbsolutePath(), target.toAbsolutePath());
                    }
                    // Don't add files we're going to move to the deletes list.
                    if (!isMove) {
                        return source;
                    }
                } catch (IOException e) {
                    backupErrors.put(source.toFile(), ResourceMitigationReport.mapFileError(target.toFile(), makeErrorMessage(e)));
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            // Run deletes so files are cleared for potential moves with the same names.
            if (!deletes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing resource survey request {} for resource {}: resource contains {} deletes:\n * {}", requestId, resourceId, deletes.size(), deletes.stream().map(Objects::toString).collect(Collectors.joining("\n * ")));
                } else {
                    log.info("Processing resource survey request {} for resource {}: resource contains {} deletes", requestId, resourceId, deletes.size());
                }
                writer.println("\nDeleting files");
                deletes.forEach(path -> {
                    try {
                        log.debug("Processing resource survey request {} for resource {}: deleting file {}", requestId, resourceId, path);
                        final Optional<File> optional = removeFromCatalog(path, sourcePath, catalogData, catalogMap, _workflow.buildEvent());
                        if (optional.isPresent()) {
                            final Path destination = optional.get().toPath();
                            backups.put(path, destination);
                            writer.println(" * " + path + " (backed up to " + destination + ")");
                        } else {
                            writer.println(" * " + path);
                        }
                    } catch (InitializationException | IOException e) {
                        deleteErrors.put(path.toFile(), makeErrorMessage(e));
                    }
                });
            }

            if (!moves.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing resource survey request {} for resource {}: resource contains {} renames:\n * {}", requestId, resourceId, moves.size(), moves.entrySet().stream().map(entry -> entry.getKey() + " -> " + entry.getValue()).collect(Collectors.joining("\n * ")));
                } else {
                    log.info("Processing resource survey request {} for resource {}: resource contains {} renames", requestId, resourceId, moves.size());
                }
                writer.println("\nRenaming files");
                moves.forEach((source, target) -> {
                    try {
                        // We may end up with situations where a file should be
                        // "moved" onto itself. Don't do that.
                        if (!source.equals(target)) {
                            log.debug("Processing resource survey request {} for resource {}: moving file {} to {}", requestId, resourceId, source, target);
                            final Optional<File> optional = updateCatalogEntry(source, target, sourcePath, catalogData, catalogMap, _workflow.buildEvent());
                            if (optional.isPresent()) {
                                final Path destination = optional.get().toPath();
                                backups.put(source, destination);
                                writer.println(" * " + source.toAbsolutePath() + " -> " + target.toAbsolutePath() + " (backed up to " + destination.toAbsolutePath() + ")");
                            } else {
                                writer.println(" * " + source.toAbsolutePath() + " -> " + target.toAbsolutePath());
                            }
                        }
                        deletes.remove(source);
                    } catch (IOException | InvalidReference | InitializationException e) {
                        moveErrors.put(source.toFile(), ResourceMitigationReport.mapFileError(target.toFile(), makeErrorMessage(e)));
                    }
                });
            }

            log.debug("Completed file operations for mitigating resource survey request {} for resource {}", requestId, resourceId);
            builder.removedFiles(backups.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toAbsolutePath().toFile(), entry -> entry.getValue().toAbsolutePath().toFile())));
            builder.movedFiles(moves.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toAbsolutePath().toFile(), entry -> entry.getValue().toAbsolutePath().toFile())));

            if (!backupErrors.isEmpty()) {
                log.warn("Found {} backup errors for mitigating resource survey request {} for resource {}", backupErrors.size(), requestId, resourceId);
                writer.format("\n\nBackup errors:");
                backupErrors.forEach((source, info) -> writer.println(String.join("\n      ",
                                                                                  " *  From: " + source.getAbsolutePath(),
                                                                                  "To: " + info.get(ResourceMitigationReport.KEY_FILE),
                                                                                  info.get(ResourceMitigationReport.KEY_MESSAGE))));
                writer.println();
                builder.backupErrors(backupErrors);
            }
            if (!moveErrors.isEmpty()) {
                log.warn("Found {} move errors for mitigating resource survey request {} for resource {}", moveErrors.size(), requestId, resourceId);
                writer.format("\n\nMove errors:\n");
                moveErrors.forEach((source, info) -> {
                    writer.println(String.join("\n      ",
                                               " *  From: " + source.getAbsolutePath(),
                                               "To: " + info.get(ResourceMitigationReport.KEY_FILE),
                                               info.get(ResourceMitigationReport.KEY_MESSAGE)));
                    moves.remove(source.toPath());
                });
                writer.println();
                builder.moveErrors(moveErrors);
            }
            if (!deleteErrors.isEmpty()) {
                log.warn("Found {} delete errors for mitigating resource survey request {} for resource {}", deleteErrors.size(), requestId, resourceId);
                writer.format("\n\nDelete errors:\n");
                deleteErrors.forEach((source, info) -> {
                    writer.println(" * " + source.getAbsolutePath() + " " + info);
                    deletes.remove(source.toPath());
                });
                writer.println();
                builder.deleteErrors(deleteErrors);
            }

            // Write the catalog
            try {
                log.info("Finishing mitigation for resource survey request {} for resource {}, preparing to write catalog {}", requestId, resourceId, catalogData.catFile);
                CatalogUtils.writeCatalogToFile(catalogData);
            } catch (Exception e) {
                log.error("Error processing resource survey request {} for resource {}: unable to save catalog after resource mitigation on {}", requestId, resourceId, catalogResource.getUri(), e);
                builder.catalogWriteError(String.format("Unable to write catalog %s: %s %s. To access this data, " +
                                                        "you will need to perform a catalog refresh. It would be ideal to then download and " +
                                                        "re-import the data or pull scan data from headers so it is properly understood as DICOM.",
                                                        catalogResource.getUri(), e.getClass().getSimpleName(), e.getMessage()));
            }

            // Populate resource statistics
            if (CatalogUtils.populateStats(catalogResource, catalogData.catPath)) {
                log.debug("Populated stats for catalog resource while finishing mitigation for resource survey request {} on resource {}: {}", requestId, resourceId, catalogResource.getUri());
            } else {
                log.info("Populated stats for catalog resource while finishing mitigation for resource survey request {} on resource {} but apparently there were no relevant updates: {}", requestId, resourceId, catalogResource.getUri());
            }

            try {
                SaveItemHelper.authorizedSave(catalogResource, _requester, false, false, _workflow.buildEvent());
                log.info("Saved catalog resource after mitigation for resource survey request {} on resource {}: {}", requestId, resourceId, catalogResource.getUri());

                _workflow.setStepDescription("Mitigated");
                _workflow.setPercentagecomplete("100%");
                _workflow.setDetails("Completed resource survey request " + requestId + " mitigation on resource " + resourceId + " with " + deletes.size() + " files deleted and " + moves.size() + " files moved.");
                PersistentWorkflowUtils.complete(_workflow, _workflow.buildEvent());
                log.info("Updated and completed workflow {} after mitigation for resource survey request {} on resource {}", _workflow.getWorkflowId(), requestId, resourceId);
            } catch (Exception e) {
                log.error("Error processing resource survey request {} for resource {}: unable to update resource statistics for {}", requestId, resourceId, catalogResource.getXnatAbstractresourceId(), e);
                builder.resourceSaveError(String.format("Unable to update resource statistics on %s: %s %s. Running " +
                                                        "a catalog refresh requesting the populateStats operation will hopefully fix the issue",
                                                        catalogResource.getXnatAbstractresourceId(), e.getClass().getSimpleName(), e.getMessage()));
                try {
                    _workflow.setStepDescription("Failed");
                    _workflow.setPercentagecomplete("100%");
                    _workflow.setDetails("Mitigation for resource survey request " + requestId + " on resource " + resourceId + " failed with the error: " + e.getMessage());
                    PersistentWorkflowUtils.fail(_workflow, _workflow.buildEvent());
                    log.info("Updated and failed workflow {} after mitigation for resource survey request {} on resource {}", _workflow.getWorkflowId(), requestId, resourceId);
                } catch (Exception ex) {
                    log.error("An error occurred processing resource survey request {} for resource {}: tried to fail the workflow with ID {}", requestId, resourceId, _workflow.getWorkflowId(), ex);
                }
            }

            // Let everyone know what happened here today.
            log.debug("All done mitigating resource survey request {} for resource {}, firing an update event on catalog resource {}", requestId, resourceId, catalogResource.getUri());
            XDAT.triggerXftItemEvent(catalogResource, XftItemEvent.UPDATE, getEventParameters(_request, deletes, moves));
        }

        return builder.build();
    }

    /**
     * Creates or retrieves a path to a cache folder under the system cache folder, with the subfolder path
     * <i>projectId</i>/<i>experimentId</i>/<i>requestTime</i>/<i>scanId</i>.
     *
     * @param rootCachePath The system root cache path, usually taken from the site config preferences
     *
     * @return The requested cache path.
     */
    private Path getCachePath(final String rootCachePath) throws InitializationException {
        final Path cachePath = Paths.get(rootCachePath)
                                    .resolve(_request.getProjectId())
                                    .resolve(_request.getExperimentId())
                                    .resolve(_request.getRequestTime())
                                    .resolve(Integer.toString(_request.getScanId()));
        try {
            Files.createDirectories(cachePath);
        } catch (IOException e) {
            throw new InitializationException("Got an error trying to create the folders for the cache path: " + cachePath, e);
        }
        log.debug("Created cache path {} for resource survey request {} for resource {} on experiment {} in project {}", cachePath, _request.getId(), _request.getResourceId(), _request.getExperimentId(), _request.getProjectId());
        return cachePath;
    }


    /**
     * An error message from exception
     *
     * @param e the exception
     *
     * @return the error message
     */
    private String makeErrorMessage(Exception e) {
        return "Error: (" + e.getClass().getSimpleName() + ") " + e.getMessage();
    }

    /**
     * Get catalog resource XFT object
     *
     * @return the XnatResourcecatalog
     */
    private XnatResourcecatalog getCatalogResource() {
        final XnatResource resource = XnatResource.getXnatResourcesByXnatAbstractresourceId(_request.getResourceId(),
                                                                                            null, false);
        if (!(resource instanceof XnatResourcecatalog)) {
            throw new RuntimeException(_request.getResourceId() + " is not a catalog resource");
        }
        return (XnatResourcecatalog) resource;
    }

    /**
     * Get catalog data for resource
     *
     * @param catalogResource the catalog resource
     *
     * @return the catalog data object
     */
    private CatalogUtils.CatalogData getCatalogData(final XnatResourcecatalog catalogResource) {
        final CatalogUtils.CatalogData catalogData;
        try {
            catalogData = CatalogUtils.CatalogData.get(catalogResource, _request.getProjectId())
                                                  .orElseThrow(() -> new RuntimeException("Catalog file does not exist for resource " +
                                                                                          catalogResource.getXnatAbstractresourceId()));
        } catch (ServerException e) {
            throw new RuntimeException(String.format("%s (%s)", e.getMessage(),
                                                     catalogResource.getXnatAbstractresourceId()));
        }
        return catalogData;
    }

    /**
     * Remove entry from catalog
     *
     * @param path        The path to the file whose catalog entry should be removed
     * @param sourcePath  the catalog path
     * @param catalogData the catalog data object
     * @param catalogMap  map of relative paths to catalog entries
     * @param event       The event metadata to record the operation.
     */
    private Optional<File> removeFromCatalog(final Path path,
                                             final Path sourcePath,
                                             final CatalogUtils.CatalogData catalogData,
                                             final Map<String, CatalogUtils.CatalogMapEntry> catalogMap,
                                             final EventMetaI event) throws IOException, InitializationException {
        final File file = path.toFile();

        final CatalogUtils.CatalogMapEntry mapEntry = catalogMap.get(sourcePath.relativize(path).toString());
        try {
            if (file.exists() && mapEntry != null && mapEntry.entry instanceof CatEntryBean) {
                try {
                    return CatalogUtils.moveToHistory(catalogData.catFile, catalogData.project, path.toFile(), (CatEntryBean) mapEntry.entry, event);
                } catch (Exception e) {
                    throw new InitializationException("An error occurred trying to delete or move the file " + path, e);
                }
            }
            Files.delete(path);
            return Optional.empty();
        } finally {
            // If mapEntry were null, we wouldn't really care bc we're removing it anyway
            if (mapEntry != null) {
                catalogData.catBean.getEntries_entry().remove(mapEntry.entry);
            }
        }
    }

    /**
     * Update catalog entry with new URI and ID corresponding to a new filename
     *
     * @param source     the original file
     * @param target     the renamed file
     * @param sourcePath the catalog path
     * @param catalogMap map of relative paths to catalog entries
     * @param event      the event for the file move
     *
     * @throws InvalidReference if an entry for the original file cannot be located in the catalog
     */
    private Optional<File> updateCatalogEntry(final Path source,
                                              final Path target,
                                              final Path sourcePath,
                                              final CatalogUtils.CatalogData catalogData,
                                              final Map<String, CatalogUtils.CatalogMapEntry> catalogMap,
                                              final EventMetaI event) throws InvalidReference, IOException, InitializationException {
        final CatalogUtils.CatalogMapEntry mapEntry = Optional.ofNullable(catalogMap.get(sourcePath.relativize(source).toString())).orElseThrow(() -> new InvalidReference("Unable to locate catalog entry for original DICOM file " + source));
        CatEntryBean                       entry    = (CatEntryBean) mapEntry.entry;

        // Presuming we found a catalog entry, copy the source file to the target file (it should already be backed up)...
        Files.copy(source, target);
        final Optional<File> file;
        try {
            file = CatalogUtils.copyToHistory(catalogData.catFile, catalogData.project, source.toFile(), entry, event);
            Files.delete(source);
        } catch (Exception e) {
            throw new InitializationException("An error occurred trying to copy a file to history: " + source, e);
        }

        final String relativePath = sourcePath.relativize(target).toString();
        entry.setUri(relativePath);
        entry.setId(relativePath);
        CatalogUtils.updateModificationEvent(entry, event);

        return file;
    }

    private Map<String, ?> getEventParameters(final ResourceSurveyRequest request, final List<Path> deletes, final Map<Path, Path> moves) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(XftItemEventI.OPERATION, FILE_MITIGATION);
        parameters.put(REQUEST, request);
        parameters.put(DELETED, deletes);
        parameters.put(UPDATED, moves);
        return parameters;
    }

    private PrintWriter getMitigationLogWriter() {
        final File mitigationLog = _cachePath.resolve(_request.getMitigationId() + ".log").toFile();
        try {
            final PrintWriter writer = new PrintWriter(new FileWriter(mitigationLog));
            log.info("Starting mitigation operation for resource survey request {} for resource {}. Mitigation log should be available at {}.", _request.getId(), _request.getResourceId(), mitigationLog);
            return writer;
        } catch (IOException e) {
            log.warn("Starting mitigation operation for resource survey request {} for resource {}. An error occurred trying to open the mitigation logfile {}, providing a writer to stdout instead.", _request.getId(), _request.getResourceId(), mitigationLog, e);
            return new PrintWriter(System.out);
        }
    }
}
