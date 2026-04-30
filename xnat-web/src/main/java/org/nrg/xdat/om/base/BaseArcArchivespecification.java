/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseArcArchivespecification
 * XNAT http://www.xnat.org
 * Copyright (c) 2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.om.ArcPathinfo;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.base.auto.AutoArcArchivespecification;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nullable;

import java.io.Serial;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author XDAT
 */
@Slf4j
@SuppressWarnings("unused")
public abstract class BaseArcArchivespecification extends AutoArcArchivespecification {
    @Serial
    private static final long serialVersionUID = 1;
    public BaseArcArchivespecification(final ItemI item) {
        super(item);
        log.debug("Initializing arc spec with item of type {}", item.getXSIType());
    }

    public BaseArcArchivespecification(final UserI user) {
        super(user);
        log.debug("Initializing arc spec for user {}", user.getUsername());
    }

    /*
     * @deprecated Use BaseArcArchivespecification(UserI user)
     **/
    public BaseArcArchivespecification() {
        log.debug("Initializing arc spec with default constructor");
    }

    public BaseArcArchivespecification(final Hashtable properties, final UserI user) {
        super(properties, user);
        log.debug("Initializing arc spec for user {} and properties: {}", user.getUsername(), properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A extends ArcProjectI> List<A> getProjects_project() {
        //noinspection unchecked
        return (List<A>) new ArrayList<>(getArcProjects().values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProjects_project(final ItemI item) throws Exception {
        log.debug("Setting the arc projects from an item of type {}", item.getXSIType());
        clearArcProjectCache();
        super.setProjects_project(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeProjects_project(final int index) throws java.lang.IndexOutOfBoundsException {
        log.debug("Removing the arc project at index {}", index);
        super.removeProjects_project(index);
        removeOrphanedArcProjects();
    }

    public String getGlobalArchivePath() {
        return getGlobalPath("archivePath");
    }

    public String getGlobalPrearchivePath() {
        return getGlobalPath("prearchivePath");
    }

    public String getGlobalCachePath() {
        return getGlobalPath("cachePath");
    }

    public String getGlobalBuildPath() {
        return getGlobalPath("buildPath");
    }

    @Nullable
    public String getArchivePathForProject(final String id) {
        return getProjectPath(getProjectArc(id), "archivePath");
    }

    @Nullable
    public String getCachePathForProject(final String id) {
        return getProjectPath(getProjectArc(id), "cachePath");
    }

    @Nullable
    public Integer getPrearchiveCodeForProject(final String id) {
        final ArcProject project = getProjectArc(id);
        if (project == null) {
            log.warn("Got request for prearchive code for project {}, but couldn't find the arc project for that ID", id);
            return null;
        }

        final Integer prearchiveCode = project.getPrearchiveCode();
        log.debug("Got request for prearchive code for arc project {}, returning value \"{}\"", project.getId(), prearchiveCode);
        return prearchiveCode;
    }

    @Nullable
    public Integer getAutoQuarantineCodeForProject(final String id) {
        final ArcProject project = getProjectArc(id);
        if (project == null) {
            log.warn("Got request for prearchive code for project {}, but couldn't find the arc project for that ID", id);
            return null;
        }

        final Integer quarantineCode = project.getQuarantineCode();
        log.debug("Got request for quarantine code for arc project {}, returning value \"{}\"", project.getId(), quarantineCode);
        return quarantineCode;
    }

    @Nullable
    public String getPrearchivePathForProject(final String id) {
        return getProjectPath(getProjectArc(id), "prearchivePath");
    }

    @Nullable
    public String getBuildPathForProject(final String id) {
        return getProjectPath(getProjectArc(id), "buildPath");
    }

    @Nullable
    public ArcProject getProjectArc(final String projectId) {
        final ArcProject project = getArcProject(projectId);
        if (project == null) {
            log.error("Unable to find arc project with ID {}", projectId);
        } else {
            log.debug("Found requested arc project with ID {}", projectId);
        }
        return project;
    }

    public boolean isComplete() {
        return getGlobalpaths() != null && !StringUtils.isAnyBlank(getSiteId(), getSiteAdminEmail(), getGlobalpaths().getArchivepath(), getGlobalpaths().getPrearchivepath(), getGlobalpaths().getCachepath(), getGlobalpaths().getBuildpath(), getGlobalpaths().getFtppath());
    }

    private NamedParameterJdbcTemplate getTemplate() {
        if (_template == null) {
            _template = XDAT.getNamedParameterJdbcTemplate();
        }
        return _template;
    }

    private synchronized void clearArcProjectCache() {
        _projects.clear();
    }

    private ArcProject getArcProject(final String projectId) {
        return (ArcProject) Optional.ofNullable(getArcProjects().get(projectId)).orElseGet(() -> {
            final ArcProject arcProject = getArcProjectFromXft(projectId);
            if (arcProject != null) {
                _projects.put(projectId, arcProject);
            }
            return arcProject;
        });
    }

    private ArcProject getArcProjectFromXft(final String projectId) {
        final ArcProject arcProject = ArcProject.getArcProjectsById(projectId, getUser(), false);
        if (arcProject == null) {
            log.warn("Tried to retrieve arc project for project {}, but got null", projectId);
        }
        return arcProject;
    }

    private synchronized Map<String, ArcProjectI> getArcProjects() {
        log.trace("Got request for list of arc projects");
        if (!_projects.isEmpty()) {
            // Get all current arc projects with last modified date as timestamp.
            final Map<String, Long> projectDates = getArcProjectIdsAndDates();
            log.debug("Comparing {} cached arc projects against {} current arc projects", _projects.size(), projectDates.size());

            // Find all current arc projects where cache doesn't contain project ID or timestamp is out of sync. Insert or replace those entries.
            _projects.putAll(projectDates.entrySet().stream()
                                         .filter(this::isCachedArcProjectStale)
                                         .map(Map.Entry::getKey)
                                         .map(this::getArcProjectFromXft)
                                         .filter(Objects::nonNull)
                                         .collect(COLLECTOR_MAP_ARC_PROJECT_BY_ID));
        }

        if (_projects.isEmpty()) {
            log.info("No cached arc projects found, initializing");
            _projects.putAll(getArcProjectIdsAndDates().keySet().stream()
                                                       .map(this::getArcProjectFromXft)
                                                       .filter(Objects::nonNull)
                                                       .collect(COLLECTOR_MAP_ARC_PROJECT_BY_ID));
            if (log.isDebugEnabled()) {
                log.debug("Added {} arc projects to cache: {}", _projects.size(), String.join(", ", _projects.keySet()));
            }
        }

        log.debug("Returning {} cached arc projects", _projects.size());
        return _projects;
    }

    private Map<String, Long> getArcProjectIdsAndDates() {
        return getTemplate().query(QUERY_ARC_PROJECT_LAST_MODIFIED, ARC_PROJECT_LAST_MODIFIED);
    }

    private void removeOrphanedArcProjects() {
        removeOrphanedArcProjects(getArcProjectIdsAndDates().keySet());
    }

    private void removeOrphanedArcProjects(final Set<String> projectIds) {
        // Get all IDs from the cache and filter out those that aren't in projectDates: because projectDates was *just* generated, it has all
        // existing project IDs, so any project IDs in the project cache that aren't in projectDates are for projects that have been deleted.
        final Set<String> removed = _projects.keySet().stream().filter(projectId -> !projectIds.contains(projectId)).collect(Collectors.toSet());

        // If we found any IDs in the cache that aren't in the current arc projects...
        if (!removed.isEmpty()) {
            log.debug("Found {} project IDs in the arc project cache that have been removed, clearing from cache: {}", removed.size(), removed);
            // Remove those: the projects have been deleted.
            removed.forEach(_projects::remove);
        }
    }

    private boolean isCachedArcProjectStale(final Map.Entry<String, Long> projectDate) {
        final String projectId = projectDate.getKey();
        if (!_projects.containsKey(projectId)) {
            log.debug("No arc-project cache entry found for project {}, marking stale", projectId);
            return true;
        }
        final Long projectTime = projectDate.getValue();
        if (projectTime == null) {
            log.debug("The arc-project cache entry for project {} has no associated timestamp, marking stale", projectId);
            return true;
        }
        final ArcProject arcProject = (ArcProject) _projects.get(projectId);
        if (arcProject == null) {
            log.warn("The arc-project cache entry for project ID {} is null. Regenerating but this is weird and I thought you should know.", projectId);
            return true;
        }
        final XFTItem item = arcProject.getItem();
        if (item == null) {
            log.warn("The XFT item associated with the arc-project cache entry for project ID {} is null. Regenerating but this is weird and I thought you should know.", projectId);
            return true;
        }
        final long    cachedTime = item.getLastModified().getTime();
        final boolean isStale    = projectTime != cachedTime;
        log.debug("Testing cached arc project entry for ID {} with time {} against current time {}, entry {} stale", projectId, cachedTime, projectTime, isStale ? "is" : "is not");
        return isStale;
    }

    @Nullable
    private String getProjectPath(final ArcProject project, final String path) {
        if (project == null) {
            return null;
        }
        final String rawPath  = getRawPathForProject(project, path);
        final String resolved = StringUtils.isNotBlank(rawPath) ? StringUtils.appendIfMissing(StringUtils.replaceChars(rawPath, '\\', '/'), "/") : null;
        log.debug("Got request for path \"{}\" from arc project {}, returning value \"{}\"", path, project.getId(), resolved);
        return resolved;
    }

    private String getGlobalPath(final String path) {
        final String rawPath  = getRawGlobalPath(path);
        final String resolved = StringUtils.isNotBlank(rawPath) ? StringUtils.appendIfMissing(StringUtils.replaceChars(rawPath, '\\', '/'), "/") : null;
        log.debug("Got request for global path \"{}\", returning value \"{}\"", path, resolved);
        return resolved;
    }

    private String getRawPathForProject(final ArcProject project, final String path) {
        if (project == null) {
            log.warn("Got request for path \"{}\" from an arc project but the submitted project is null", path);
            return null;
        }
        final String projectPath = getPathInfoProperty(project.getPaths(), path);
        if (StringUtils.isNotBlank(projectPath)) {
            log.debug("Got request for path \"{}\" from arc project {}, returning value \"{}\"", path, project.getId(), projectPath);
            return projectPath;
        }
        final String globalPath = getRawGlobalPath(project, path);
        log.debug("Got request for path \"{}\" from arc project {}, but that wasn't set for the project. Returning global value for this path: \"{}\"", path, project.getId(), globalPath);
        return globalPath;
    }

    private String getRawGlobalPath(final String path) {
        return getRawGlobalPath(null, path);
    }

    private String getRawGlobalPath(final @Nullable ArcProject project, final String path) {
        final String resolved = StringUtils.defaultIfBlank(getPathInfoProperty(getGlobalpaths(), path), ".");
        return project != null ? Path.of(resolved, project.getId()).toString() : resolved;
    }

    private boolean arcProjectExists(final String arcProjectId) {
        return _template.queryForObject(QUERY_ARC_PROJECT_EXISTS, new MapSqlParameterSource("id", arcProjectId), Boolean.class);
    }

    private static String getPathInfoProperty(final ArcPathinfo pathInfo, final String path) {
        if (pathInfo == null || StringUtils.isBlank(path)) {
            return null;
        }
        try {
            return pathInfo.getStringProperty(path);
        } catch (ElementNotFoundException | FieldNotFoundException e) {
            log.error("An error occurred trying to get path info for {}", path, e);
            return null;
        }
    }

    private static final String QUERY_ARC_PROJECT_EXISTS        = "SELECT EXISTS(SELECT TRUE FROM arc_project WHERE id = :id) AS exists";
    private static final String QUERY_ARC_PROJECT_LAST_MODIFIED = "SELECT "
                                                                  + "    p.id, "
                                                                  + "    m.last_modified "
                                                                  + "FROM "
                                                                  + "    arc_project p "
                                                                  + "    LEFT JOIN arc_project_meta_data m ON p.project_info = m.meta_data_id";

    private static final ResultSetExtractor<Map<String, Long>> ARC_PROJECT_LAST_MODIFIED = results -> {
        final Map<String, Long> dates = new HashMap<>();
        while (results.next()) {
            dates.put(results.getString("id"), results.getTimestamp("last_modified").getTime());
        }
        return dates;
    };

    private static final Collector<ArcProject, ?, Map<String, ArcProject>> COLLECTOR_MAP_ARC_PROJECT_BY_ID = Collectors.toMap(ArcProject::getId, Function.identity());

    private final Map<String, ArcProjectI>   _projects = new HashMap<>();
    private       NamedParameterJdbcTemplate _template;
}
