/*
 * web: org.nrg.xnat.utils.FileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import static org.nrg.xft.utils.FileUtils.MoveDir;
import static org.nrg.xft.utils.FileUtils.renameWTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class FileUtils {
    public static List<String> nodeToList(final JsonNode node) {
        return node.isArray()
               ? StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).collect(Collectors.toList())
               : (Collections.singletonList(node.isTextual()
                                            ? node.asText()
                                            : node.toString()));
    }

    public static void moveToCache(final String project, final String subDir, final File src) throws IOException {
        // should include a timestamp in folder name
        if (src.exists()) {
            final File cache = (StringUtils.isBlank(subDir)) ? new File(XDAT.getSiteConfigPreferences().getCachePath(), project) : new File(new File(XDAT.getSiteConfigPreferences().getCachePath(), project), subDir);

            final File dest = new File(cache, renameWTimestamp(src.getName()));

            MoveDir(src, dest, false);
        }
    }

    public static File buildCachepath(final String project, final String subDir, final String destName) {
        final Path root = Paths.get(XDAT.getSiteConfigPreferences().getCachePath(), StringUtils.defaultIfBlank(project, "Unknown"));
        return (StringUtils.isEmpty(subDir) ? root : root.resolve(subDir)).resolve(renameWTimestamp(destName)).toFile();
    }

    /**
     * This attempts to retrieve the XNAT version from a combination of the tags
     * and tip.txt files in the {@link XFT#GetConfDir() default configuration folder}.
     * Failing that, it will use the VERSION file.
     *
     * @return The current version of XNAT as a String.
     */
    public static String getXNATVersion() throws IOException {
        if (VERSION == null) {
            // The CHANGESET_PATTERN is just a convenient static object to synchronize on.
            synchronized (MUTEX) {
                log.debug("Version information not found, extracted and caching");

                final String location = XFT.GetConfDir();
                if (StringUtils.isEmpty(location)) {
                    throw new IOException("Can't look for version in empty location.");
                }

                // First try to get the tags file at the indicated location.
                final File tags = Paths.get(location, "tags").toFile();
                // If that doesn't exist...
                if (!tags.exists()) {
                    // Get the value from the VERSION file
                    return VERSION = getSimpleVersion();
                }

                try (final ReversedLinesFileReader tagsReader = new ReversedLinesFileReader(tags, Charset.defaultCharset())){
                    final String last = tagsReader.readLine();

                    // If the last non-null line was empty, then we don't know
                    // what's going on.
                    if (StringUtils.isBlank(last)) {
                        return VERSION = getSimpleVersion();
                    }

                    // Split on the space, the last line should be something like
                    // "123456789abcdef0 1.5.0"
                    final String[] components = last.split(" ");

                    // If it didn't meet that criteria, we don't know what's going on.
                    if (components.length != 2) {
                        return VERSION = getSimpleVersion();
                    }

                    // If we got back a two-element array, the second element should be the version as indicated by the
                    // HG tag. Use that as the default VERSION value. We'll see if there's any reason to override it.
                    final String tag = VERSION = components[1];

                    // Interpret version containing the '-' character as non-release, e.g. snapshot or RC
                    if (tag.contains("-")) {
                        // Then try to get more info from the tip.txt file.
                        final File tip = new File(location + File.separator + "tip.txt");
                        if (tip.exists()) {
                            final Pair<String, String> changesetAndDate = getChangesetAndDate(tip);
                            VERSION = String.format("%s-%s %s", tag, changesetAndDate.getKey(), new SimpleDateFormat("yyyyMMddHHmmss").format(new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(changesetAndDate.getValue())));
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Error reading file at the indicated location: " + location, e);
                }
            }
        }

        log.debug("Found version information: {}", VERSION);
        return VERSION;
    }

    @SafeVarargs
    public static <T extends String> File buildCacheSubDir(T... directories) {
        final File subDir = Paths.get(XDAT.getSiteConfigPreferences().getCachePath(), directories).toFile();
        if (log.isDebugEnabled()) {
            log.debug("Found cache sub-directory: {}", subDir.getAbsolutePath());
        }
        return subDir;
    }

    private static String getSimpleVersion() throws IOException {
        final File version = Paths.get(XFT.GetConfDir(), "VERSION").toFile();
        if (!version.exists()) {
            log.error("Can't find the VERSION file at the indicated location: {}", XFT.GetConfDir());
            return "Unknown";
        }
        // It's pretty simple, just read it and spit it back out.
        final List<String> lines = IOUtils.readLines(new BufferedReader(new FileReader(version)));
        return lines.isEmpty() ? "" : lines.get(0);
    }

    private static Pair<String, String> getChangesetAndDate(final File file) {
        try (final ReversedLinesFileReader reader = new ReversedLinesFileReader(file, Charset.defaultCharset())) {
            String current, changeset = "", date = "";
            while ((current = reader.readLine()) != null && StringUtils.isAnyBlank(changeset, date)) {
                if (current.contains("changeset:")) {
                    changeset = current.split(":")[2];
                } else if (current.contains("date:")) {
                    date = current.split(":\\s+")[1];
                }
            }
            return Pair.of(changeset, date);
        } catch (IOException e) {
            log.error("An error occurred trying to read changeset and date from file {}", file, e);
            return ImmutablePair.nullPair();
        }
    }

    /**
     * Get the current arc directory of a project.
     * This is more or less identical to {@link BaseXnatProjectdata#getCurrentArc()}, except static and not requiring
     * read permission on the base project (as is necessary to obtain the origin location of shared data - for instance
     * in the case of creating a job directory).
     * @param projectId The project id
     * @return The project's current arcXXX directory
     */
    private static String getCurrentArc(final String projectId) {
        final ArcProject arcProject = ArcSpecManager.GetInstance().getProjectArc(projectId);
        return StringUtils.defaultIfBlank(arcProject != null ? arcProject.getCurrentArc() : null, "arc001");
    }

    private static Path getExperimentFullPath(final String projectId, final String experimentLabel) {
        return Paths.get(XDAT.getSiteConfigPreferences().getArchivePath(), projectId, getCurrentArc(projectId), experimentLabel);
    }

    private static Path getSubjectFullPath(final String projectId, final String subjectLabel) {
        return Paths.get(XDAT.getSiteConfigPreferences().getArchivePath(), projectId, "subjects", subjectLabel);
    }

    /**
     * Cached permission check for shared data path resolution.
     * For owned data (origProject == projectId), checks elementName/project against projectId.
     * For shared data (origProject != projectId), checks elementName/sharing/share/project against projectId,
     * since the user's access flows through their membership in the destination project.
     */
    private static boolean canReadElement(final UserI user, final String elementName, final String origProject,
                                          final String projectId, final Set<String> granted, final Set<String> denied) {
        final boolean isShared = !origProject.equals(projectId);
        final String field = elementName + (isShared ? "/sharing/share/project" : "/project");
        final String key = field + "|" + projectId;
        if (denied.contains(key)) {
            return false;
        }
        if (granted.contains(key)) {
            return true;
        }
        try {
            if (Permissions.canRead(user, field, projectId)) {
                granted.add(key);
                return true;
            } else {
                denied.add(key);
                return false;
            }
        } catch (Exception e) {
            log.warn("Permission check failed for {} in project {}", field, projectId, e);
            denied.add(key);
            return false;
        }
    }

    private static Map<String, List<String>> convertXFTTableForProjectSharedPathsElements(XFTTable elementsTable) {
        Map<String, String> elementsMap = elementsTable.convertToMap("label", "origProject", String.class, String.class);
        long nullCount = elementsMap.entrySet().stream().filter(e -> e.getKey() == null || e.getValue() == null).count();
        if (nullCount > 0) {
            log.warn("Shared data contains {} entries with null label or project, these will be excluded", nullCount);
        }
        return elementsMap.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    private static Map<String, String> getAllChangedElementLabels(XFTTable elementsTable) {
        Map<String, String> elementsMap = elementsTable.convertToMap("label", "originalLabel", String.class, String.class);
        return elementsMap.entrySet().stream()
                .filter(f -> f.getKey() != null && f.getValue() != null && !f.getKey().equals(f.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<Path, Path> getAllSharedPaths(final String projectId, final UserI user, final boolean includeProjectResources,
                                                      final boolean includeSubjectResources, final boolean removeArcs, final boolean addExperimentLabel)
            throws DBPoolException, SQLException, IOException, BaseXnatExperimentdata.UnknownPrimaryProjectException, InvalidArchiveStructure {
        final long startTime = log.isDebugEnabled() ? System.nanoTime() : 0;
        XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        Map<Path, Path> allPathsMap = new HashMap<>();
        if (!projectData.getProjectHasSharedExperiments()) {
            if (!includeSubjectResources || !projectData.getProjectHasSharedSubjects()) {
                return allPathsMap;
            }
        }
        XFTTable subjectsTable = projectData.getSubjectsByProject();
        XFTTable experimentsTable = projectData.getExperimentsByProject();
        final long queryTime = log.isDebugEnabled() ? System.nanoTime() : 0;
        Map<String, List<String>> allSubjectsForProject = convertXFTTableForProjectSharedPathsElements(subjectsTable);
        Map<String, List<String>> allExperimentsForProject = convertXFTTableForProjectSharedPathsElements(experimentsTable);
        Map<String, String> subjectLabelChanges = getAllChangedElementLabels(subjectsTable);
        BiMap<String, String> experimentNewLabelToOriginal = HashBiMap.create(getAllChangedElementLabels(experimentsTable));

        // Build lookup maps from enriched experiment data (element_name, imagesession_id, parent session info)
        final Map<String, String> experimentElementNames = new HashMap<>();
        final Map<String, String> experimentParentSessionProjects = new HashMap<>();
        final Map<String, String> experimentParentSessionLabels = new HashMap<>();
        final Set<String> assessorExperiments = new HashSet<>();

        final Integer labelIdx = experimentsTable.getColumnIndex("label");
        final Integer elementNameIdx = experimentsTable.getColumnIndex("element_name");
        final Integer sessionIdIdx = experimentsTable.getColumnIndex("imagesession_id");
        final Integer parentSessionProjectIdx = experimentsTable.getColumnIndex("parent_session_project");
        final Integer parentSessionLabelIdx = experimentsTable.getColumnIndex("parent_session_label");

        experimentsTable.resetRowCursor();
        while (experimentsTable.hasMoreRows()) {
            final Object[] row = experimentsTable.nextRow();
            final String label = (String) row[labelIdx];
            if (row[elementNameIdx] != null) {
                experimentElementNames.put(label, (String) row[elementNameIdx]);
            }
            if (row[sessionIdIdx] != null) {
                assessorExperiments.add(label);
                if (row[parentSessionProjectIdx] != null) {
                    experimentParentSessionProjects.put(label, (String) row[parentSessionProjectIdx]);
                }
                if (row[parentSessionLabelIdx] != null) {
                    experimentParentSessionLabels.put(label, (String) row[parentSessionLabelIdx]);
                }
            }
        }

        final Set<String> permissionGranted = new HashSet<>();
        final Set<String> permissionDenied = new HashSet<>();

        Path archivePath = Paths.get(XDAT.getSiteConfigPreferences().getArchivePath());
        int totalSessions = 0;
        for (Map.Entry<String, List<String>> entry : allExperimentsForProject.entrySet()) {
            String origProject = entry.getKey();
            Path pathTranslationPath = archivePath.resolve(origProject);
            List<String> experimentsForProject = entry.getValue();
            for (String experiment: experimentsForProject) {
                final String originalLabel = experimentNewLabelToOriginal.getOrDefault(experiment, experiment);

                final String elementName = experimentElementNames.get(experiment);
                if (elementName == null) {
                    log.warn("Missing element_name for experiment {}, skipping (cannot verify permissions)", experiment);
                    continue;
                }
                if (!canReadElement(user, elementName, origProject, projectId, permissionGranted, permissionDenied)) {
                    continue;
                }

                Path fullPath;
                final String assessorFolderString = "ASSESSORS";
                boolean isAssessor = assessorExperiments.contains(experiment);
                if (isAssessor) {
                    // Derive assessor path from parent session info without loading XFT objects
                    final String parentProject = experimentParentSessionProjects.get(experiment);
                    final String parentLabel = experimentParentSessionLabels.get(experiment);
                    if (parentProject == null || parentLabel == null) {
                        log.warn("Missing parent session info for assessor {}, skipping", experiment);
                        continue;
                    }
                    Path parentSessionPath = getExperimentFullPath(parentProject, parentLabel);
                    fullPath = parentSessionPath.resolve(assessorFolderString).resolve(originalLabel);
                } else {
                    fullPath = getExperimentFullPath(origProject, originalLabel);
                    totalSessions+=1;
                }
                try (Stream<Path> walk = Files.walk(fullPath)) {
                    List<Path> collectedExperimentFiles;
                    if (!isAssessor) {
                        collectedExperimentFiles = walk.filter(Files::isRegularFile).filter(f -> !fullPath.relativize(f).startsWith(assessorFolderString)).collect(Collectors.toList());
                    } else {
                        collectedExperimentFiles = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                    }
                    for (Path path : collectedExperimentFiles) {
                        Path newPath;
                        if (isAssessor) {
                            //this is a rather large hack, but it's the only way I could think of to effectively do path translation given that
                            //assessors need not be made by the home projects that their sessions were created in and the fact
                            //that those assessors still live *inside* the sessions in the archive.
                            newPath = path.toAbsolutePath().subpath(pathTranslationPath.getNameCount(), path.getNameCount());
                        } else {
                            newPath = pathTranslationPath.relativize(path);
                        }
                        //this is another hack where we're getting the session portion of the path from the assessors path within the archive
                        //by recognizing that after you've removed the archive base path it will always be in the second position.
                        //This is kind of the only way I can think of to get it for cases of label change of sessions which have assessors associated
                        //which is found within the else if statement below.
                        String sessionNameWithinAssessorPath = newPath.getName(1).toString();
                        if (experimentNewLabelToOriginal.containsKey(experiment)) {
                            newPath = Paths.get(newPath.toString().replaceFirst(Objects.requireNonNull(experimentNewLabelToOriginal.get(experiment)), experiment));
                        }
                        if (isAssessor && experimentNewLabelToOriginal.inverse().containsKey(sessionNameWithinAssessorPath)) {
                            String replacementAssessorPath = experimentNewLabelToOriginal.inverse().get(sessionNameWithinAssessorPath);
                            newPath = Paths.get(newPath.toString().replaceFirst(sessionNameWithinAssessorPath, replacementAssessorPath));
                        }
                        if (removeArcs) {
                            newPath = newPath.getName(0).relativize(newPath);
                            if(addExperimentLabel) {
                                newPath = Paths.get("experiments").resolve(newPath);
                            }
                        }
                        allPathsMap.put(path, newPath);
                    }
                } catch (NoSuchFileException ignored) {

                }
            }
        }
        final long experimentWalkTime = log.isDebugEnabled() ? System.nanoTime() : 0;
        int maxNumberOfSessions = XDAT.getSiteConfigPreferences().getMaxNumberOfSessionsForJobsWithSharedData();
        if (totalSessions > maxNumberOfSessions) {
            throw new RuntimeException("With the inclusion of shared data, more than " + maxNumberOfSessions + " sessions are present in the current project. " +
                    "Your site administrator has set this as the maximum amount of sessions allowed for a job. Please contact them to change this value if you need to continue running jobs on this data.");
        }

        if (includeSubjectResources) {
            for (Map.Entry<String, List<String>> entry: allSubjectsForProject.entrySet()) {
                final String origProject = entry.getKey();
                final List<String> subjectsForProject = entry.getValue();
                Path pathTranslationPath = archivePath.resolve(origProject);

                if (!canReadElement(user, "xnat:subjectData", origProject, projectId, permissionGranted, permissionDenied)) {
                    continue;
                }

                for (String subject : subjectsForProject) {
                    final String originalSubjectLabel = subjectLabelChanges.getOrDefault(subject, subject);
                    Path fullPath = getSubjectFullPath(origProject, originalSubjectLabel);
                    try (Stream<Path> walk = Files.walk(fullPath)) {
                        List<Path> collectedSubjectFiles = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                        for (Path path : collectedSubjectFiles) {
                            Path newPath = pathTranslationPath.relativize(path);
                            if (subjectLabelChanges.containsKey(subject)) {
                                newPath = Paths.get(newPath.toString().replaceFirst(subjectLabelChanges.get(subject), subject));
                            }
                            allPathsMap.put(path, newPath);
                        }
                    } catch (NoSuchFileException ignored) {

                    }
                }
            }
        }

        if (includeProjectResources) {
            Path resourcesPath = Paths.get(projectData.getArchiveRootPath(), "resources");
            try (Stream<Path> walk = Files.walk(resourcesPath)) {
                List<Path> collectedResourceFiles = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                for (Path resourcePath : collectedResourceFiles) {
                    Path pathTranslationPath = archivePath.resolve(projectData.getId());
                    Path newPath = pathTranslationPath.relativize(resourcePath);
                    allPathsMap.put(resourcePath, newPath);
                }
            } catch (NoSuchFileException ignored) {

            }
        }
        if (log.isDebugEnabled()) {
            final long endTime = System.nanoTime();
            log.debug("getAllSharedPaths for project {} completed in {}ms (queries={}ms, experimentWalks={}ms, subjectsAndResources={}ms) — {} experiments, {} subjects, {} total files",
                    projectId,
                    (endTime - startTime) / 1_000_000,
                    (queryTime - startTime) / 1_000_000,
                    (experimentWalkTime - queryTime) / 1_000_000,
                    (endTime - experimentWalkTime) / 1_000_000,
                    allExperimentsForProject.values().stream().mapToInt(List::size).sum(),
                    allSubjectsForProject.values().stream().mapToInt(List::size).sum(),
                    allPathsMap.size());
        }
        return allPathsMap;
    }

    public static Path createDirectoryForSharedData(Map<Path, Path> pathsMap, final Path inputLinksDirectory) throws IOException {
        return createDirectoryForSharedData(pathsMap, inputLinksDirectory, null);
    }

    public static Path createDirectoryForSharedData(Map<Path, Path> pathsMap, final Path inputLinksDirectory, final IntConsumer progressCallback) throws IOException {
        final long startTime = log.isDebugEnabled() ? System.nanoTime() : 0;
        Path destinationBaseDirectory = Paths.get(XDAT.getSiteConfigPreferences().getArchivePath()).resolve(SHARED_PROJECT_DIRECTORY_STRING).resolve(inputLinksDirectory);
        final boolean useHardLink = "hard_link".equals(XDAT.getSiteConfigPreferences().getFileOperationUsedForJobsWithSharedData());
        final AtomicInteger processedCount = new AtomicInteger(0);
        final int reportInterval = Math.max(pathsMap.size() / 20, 100);
        try {
            pathsMap.entrySet().parallelStream().forEach(pathConversion -> {
                try {
                    Path destinationPathForCurrentFile = destinationBaseDirectory.resolve(pathConversion.getValue());
                    if (!Files.exists(destinationPathForCurrentFile)) {
                        Files.createDirectories(destinationPathForCurrentFile.getParent());
                        if (useHardLink) {
                            Files.createLink(destinationPathForCurrentFile, pathConversion.getKey());
                        } else {
                            Files.copy(pathConversion.getKey(), destinationPathForCurrentFile);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (progressCallback != null) {
                    int processed = processedCount.incrementAndGet();
                    if (processed % reportInterval == 0) {
                        progressCallback.accept(processed);
                    }
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        if (log.isDebugEnabled()) {
            log.debug("createDirectoryForSharedData completed in {}ms — {} files, mode={}",
                    (System.nanoTime() - startTime) / 1_000_000,
                    pathsMap.size(),
                    useHardLink ? "hard_link" : "copy");
        }
        return destinationBaseDirectory;
    }

    public static void removeCombinedFolder(final Path baseLinksDirectory) throws IOException {
        Path baseArchiveDirectory = Paths.get(XDAT.getSiteConfigPreferences().getArchivePath());
        Path directoryToRemove = baseArchiveDirectory.resolve(SHARED_PROJECT_DIRECTORY_STRING).resolve(baseLinksDirectory);
        org.apache.commons.io.FileUtils.deleteDirectory(new File(directoryToRemove.toUri()));
    }


    private static final Object MUTEX = new Object();

    private static String VERSION;

    public static final String SHARED_PROJECT_DIRECTORY_STRING = "SHARED.PROJECT.DATA.STORAGE.DIRECTORY";
}
