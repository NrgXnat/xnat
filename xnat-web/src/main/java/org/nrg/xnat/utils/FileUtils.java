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
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatQcmanualassessordata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        log.debug("Found cache sub-directory: {}", subDir.getAbsolutePath());
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

    private static Map<String, List<String>> convertXFTTableForProjectSharedPathsElements(XFTTable elementsTable) {
        Map<String, String> elementsMap = elementsTable.convertToMap("label", "origProject", String.class, String.class);
        return elementsMap.keySet().stream().collect(Collectors.groupingBy(elementsMap::get));
    }

    private static Map<String, String> getAllChangedElementLabels(XFTTable elementsTable) {
        Map<String, String> elementsMap = elementsTable.convertToMap("label", "originalLabel", String.class, String.class);
        return elementsMap.entrySet().stream().filter(f -> !f.getKey().equals(f.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<Path, Path> getAllSharedPaths(final String projectId, final UserI user, final boolean includeProjectResources,
                                                      final boolean includeSubjectResources, final boolean removeArcs, final boolean addExperimentLabel)
            throws DBPoolException, SQLException, IOException, BaseXnatExperimentdata.UnknownPrimaryProjectException, InvalidArchiveStructure {
        XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        Map<Path, Path> allPathsMap = new HashMap<>();
        if (!projectData.getProjectHasSharedExperiments()) {
            if (!includeSubjectResources || !projectData.getProjectHasSharedSubjects()) {
                return allPathsMap;
            }
        }
        XFTTable subjectsTable = projectData.getSubjectsByProject();
        XFTTable experimentsTable = projectData.getExperimentsByProject();
        Map<String, List<String>> allSubjectsForProject = convertXFTTableForProjectSharedPathsElements(subjectsTable);
        Map<String, List<String>> allExperimentsForProject = convertXFTTableForProjectSharedPathsElements(experimentsTable);
        Map<String, String> subjectLabelChanges = getAllChangedElementLabels(subjectsTable);
        BiMap<String, String> experimentNewLabelToOriginal = HashBiMap.create(getAllChangedElementLabels(experimentsTable));
        Path archivePath = Paths.get(XDAT.getSiteConfigPreferences().getArchivePath());
        int totalSessions = 0;
        for (Map.Entry<String, List<String>> entry : allExperimentsForProject.entrySet()) {
            String origProject = entry.getKey();
            Path pathTranslationPath = archivePath.resolve(origProject);
            List<String> experimentsForProject = entry.getValue();
            for (String experiment: experimentsForProject) {
                XnatExperimentdata currentExperiment = XnatExperimentdata.GetExptByProjectIdentifier(origProject, experimentNewLabelToOriginal.getOrDefault(experiment, experiment), user, false);
                //Check to see if the current experiment is accessible to the user. In the case that it's not - for instance
                //the user group that the user is a part of does not have read access to that type of element - the current experiment
                //field will be null and we can move onto the next experiment.
                if (currentExperiment == null) {
                    continue;
                }
                Path fullPath;
                final String assessorFolderString = "ASSESSORS";
                boolean isAssessor = false;
                if (currentExperiment instanceof XnatImageassessordata) {
                    fullPath = currentExperiment.getExpectedSessionDir().toPath().resolve(assessorFolderString).resolve(currentExperiment.getLabel());
                    isAssessor = true;
                } else {
                    fullPath = getExperimentFullPath(origProject, currentExperiment.getLabel());
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
                for (String subject : subjectsForProject) {
                    XnatSubjectdata currentSubject = XnatSubjectdata.GetSubjectByIdOrProjectlabelCaseInsensitive(origProject, subjectLabelChanges.getOrDefault(subject, subject), user, false);
                    if (currentSubject == null) {
                        continue;
                    }
                    Path fullPath = getSubjectFullPath(origProject, currentSubject.getLabel());
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
        return allPathsMap;
    }

    public static Path createDirectoryForSharedData(Map<Path, Path> pathsMap, final Path inputLinksDirectory) throws IOException {
        Path destinationBaseDirectory = Paths.get(XDAT.getSiteConfigPreferences().getArchivePath()).resolve(SHARED_PROJECT_DIRECTORY_STRING).resolve(inputLinksDirectory);
        for (Map.Entry<Path, Path> pathConversion : pathsMap.entrySet()) {
            Path destinationPathForCurrentFile = destinationBaseDirectory.resolve(pathConversion.getValue());
            if (Files.exists(destinationPathForCurrentFile)) {
                continue;
            }
            Files.createDirectories(destinationPathForCurrentFile.getParent());
            if (XDAT.getSiteConfigPreferences().getFileOperationUsedForJobsWithSharedData().equals("hard_link")) {
                Files.createLink(destinationPathForCurrentFile, pathConversion.getKey());
            } else {
                Files.copy(pathConversion.getKey(), destinationPathForCurrentFile);
            }

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
