/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAbstractresource
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatAbstractresource;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * @author XDAT
 */
@SuppressWarnings({"rawtypes", "unused"})
public abstract class BaseXnatAbstractresource extends AutoXnatAbstractresource {

    @Serial
    private static final long serialVersionUID = 1;

    public BaseXnatAbstractresource(ItemI item) {
        super(item);
    }

    public BaseXnatAbstractresource(UserI user) {
        super(user);
    }

    /*
     * @deprecated Use BaseXnatAbstractresource(UserI user)
     **/
    public BaseXnatAbstractresource() {
    }

    public BaseXnatAbstractresource(Hashtable properties, UserI user) {
        super(properties, user);
    }

    /**
     * Returns ArrayList of java.io.File objects
     *
     * @return The corresponding files.
     */
    public abstract ArrayList<File> getCorrespondingFiles(String rootPath);

    /**
     * Returns Map of java.io.File objects to Catalog Entries within rootPath
     *
     * @return The map
     */
    public abstract Map<File, CatEntryI> getCorrespondingFilesWithCatEntries(String rootPath);

    /**
     * Returns ArrayList of java.lang.String objects
     *
     * @return The corresponding file names.
     */
    public abstract ArrayList getCorrespondingFileNames(String rootPath);

    @Override
    public ItemI getParent() {
        return getParentFromItem().orElseGet(this::findParent);
    }

    Long size = null;

    public long getSize(String rootPath) {
        if (size == null) {
            calculate(rootPath);
        }
        return size;
    }

    public String getReadableFileStats() {
        return CatalogUtils.formatFileStats(getLabel(), getFileCount(), getFileSize());
    }

    public String getReadableFileSize() {
        Object fileSize = getFileSize();
        if (fileSize == null) {
            return "Empty";
        }
        return CatalogUtils.formatSize((Long) fileSize);
    }

    public void calculate(String rootPath) {
        long sizeI  = 0;
        int  countI = 0;
        for (File f : this.getCorrespondingFiles(rootPath)) {
            if (f.exists()) {
                countI++;
                sizeI += f.length();
            }
        }

        size = sizeI;
        count = countI;
    }

    Integer count = null;

    public Integer getCount(String rootPath) {
        if (count == null) {
            calculate(rootPath);
        }
        return count;
    }

    /**
     * Prepends this path to the enclosed URI or path variables.
     *
     * @param root The root path to prepend.
     */
    public abstract void prependPathsWith(String root);

    /**
     * Relatives this path from the first occurrence of the indexOf string.
     *
     * @param indexOf       The string from which to relativize.
     * @param caseSensitive Whether the string should be matched exactly or not.
     */
    public abstract void relativizePaths(String indexOf, boolean caseSensitive);

    /**
     * Appends this path to the enclosed URI or path variables.
     */
    public abstract ArrayList<String> getUnresolvedPaths();

    public boolean isInRAWDirectory() {
        boolean hasRAW = false;
        for (String path : getUnresolvedPaths()) {
            if (path.contains("RAW/")) {
                hasRAW = true;
                break;
            }
            if (path.contains("SCANS/")) {
                hasRAW = true;
                break;
            }
        }
        return hasRAW;
    }

    /**
     * Path to Files
     *
     * @return The full path.
     */
    public String getFullPath(String rootPath) {
        return "";
    }

    public String getContent() {
        return "";
    }

    public String getFormat() {
        return "";
    }

    /**
     * Deletes the folder for this resource catalog, saving a backup copy in the cache.
     *
     * @param rootPath The root path of the archive file system.
     * @param user     The user requesting the delete operation.
     * @param c        The event data
     *
     * @throws Exception When an error occurs.
     * @deprecated Call {@link #deleteWithBackup(String, String, UserI, EventMetaI)} instead. This will be removed in future releases.
     */
    @Deprecated
    public void deleteWithBackup(final String rootPath, final UserI user, final EventMetaI c) throws Exception {
        deleteWithBackup(rootPath, null, user, c);
    }

    /**
     * Deletes the folder for this resource catalog, saving a backup copy in the cache.
     *
     * @param rootPath The root path of the archive file system.
     * @param project  The project that contains the abstract resource.
     * @param user     The user requesting the delete operation.
     * @param c        The event data
     *
     * @throws Exception When an error occurs.
     */
    public void deleteWithBackup(final String rootPath, final String project, final UserI user, final EventMetaI c) throws Exception {
        deleteFromFileSystem(rootPath, project);
    }

    /**
     * Deletes the folder for this resource catalog, saving a backup copy in the cache.
     *
     * @param rootPath  The root path of the archive file system.
     * @param project   The project that contains the abstract resource.
     * @param user      The user requesting the delete operation.
     * @param c         The event data
     * @param timestamp The timestamp for the delete operation. This allows multiple delete operations to be grouped in the same DELETED folder when backups are on.
     *
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("RedundantThrows")
    public void deleteWithBackup(final String rootPath, final String project, final UserI user, final EventMetaI c, final String timestamp) throws Exception {
        deleteFromFileSystem(rootPath, project, timestamp);
    }

    public void deleteFromFileSystem(final String rootPath, final String project) {
        deleteFromFileSystem(rootPath, project, FileUtils.getMsTimestamp());
    }

    public void deleteFromFileSystem(final String rootPath, final String project, final String timestamp) {
        final Map<File, CatEntryI> files = getCorrespondingFilesWithCatEntries(rootPath);
        for (final File file : files.keySet()) {
            try {
                FileUtils.MoveToCache(file, timestamp);
                final CatEntryI entry = files.get(file);
                if (entry != null) {
                    CatalogUtils.deleteRemoteFile(entry.getUri(), project);
                }
                final File parent = file.getParentFile();
                if (!FileUtils.HasFiles(parent)) {
                    FileUtils.DeleteFile(parent);
                }
            } catch (IOException e) {
                logger.error("An error occurred try to delete the file and corresponding catalog entry " + file, e);
            }
        }
    }

    public String getTagString() {
        return getTags_tag().stream().map(tag -> (StringUtils.isNotBlank(tag.getName()) ? tag.getName() + "=" : "") + tag.getTag())
                            .collect(Collectors.joining(","));
    }

    private String base_URI = null;

    public String getBaseURI() {
        return base_URI;
    }

    public void setBaseURI(final String b) {
        base_URI = StringUtils.startsWithAny(b, "/REST", "/data") ? b : "/data" + b;
    }

    public abstract void moveTo(File newSessionDir, String existingSessionDir, String rootPath,
                                @Nullable String currentProject, @Nullable String destinationProject,
                                UserI user, EventMetaI ci) throws Exception;

    private Optional<ItemI> getParentFromItem() {
        return getItem() == null ? Optional.empty() : Optional.ofNullable(getItem().getParent());
    }

    private ItemI findParent() {
        final NamedParameterJdbcTemplate template = XDAT.getNamedParameterJdbcTemplate();
        if (template == null) {
            return null;
        }

        final MapSqlParameterSource parameter = new MapSqlParameterSource("resourceId", getXnatAbstractresourceId());

        final Integer scanId;
        try {
            scanId = getIntegerProperty(SCHEMA_ELEMENT_NAME + "/xnat_imagescandata_xnat_imagescandata_id");
        } catch (FieldNotFoundException | ElementNotFoundException e) {
            throw new RuntimeException("An error occurred trying to retrieve the property \"xnat_imagescandata_xnat_imagescandata_id\"", e);
        }
        if (scanId != null) {
            return XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scanId, null, false);
        }
        final String experimentId = EXPERIMENT_QUERIES.stream().map(query -> getResourceParentId(template, query, parameter)).filter(StringUtils::isNotBlank).findFirst().orElse(null);
        if (StringUtils.isNotBlank(experimentId)) {
            return XnatExperimentdata.getXnatExperimentdatasById(experimentId, null, false);
        }
        final String projectId = getResourceParentId(template, QUERY_PROJECT_ID, parameter);
        if (StringUtils.isNotBlank(projectId)) {
            return XnatProjectdata.getXnatProjectdatasById(projectId, null, false);
        }
        final String subjectId = getResourceParentId(template, QUERY_SUBJECT_ID, parameter);
        return StringUtils.isNotBlank(subjectId) ? XnatSubjectdata.getXnatSubjectdatasById(subjectId, null, false) : null;
    }

    private String getResourceParentId(final NamedParameterJdbcTemplate template, final String query, final SqlParameterSource parameter) {
        try {
            return template.queryForObject(query, parameter, String.class);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private static final String       QUERY_SCAN_ID                    = "SELECT coalesce(xnat_imagescandata_xnat_imagescandata_id, 0) FROM xnat_abstractresource WHERE xnat_abstractresource_id = :resourceId";
    private static final String       QUERY_EXPERIMENT_ID              = "SELECT coalesce(xnat_experimentdata_id, '') FROM xnat_experimentdata_resource WHERE xnat_abstractresource_xnat_abstractresource_id = :resourceId";
    private static final String       QUERY_IMAGE_ASSESSOR_FROM_IN_ID  = "SELECT coalesce(xnat_imageassessordata_id, '') FROM img_assessor_in_resource WHERE xnat_abstractresource_xnat_abstractresource_id = :resourceId";
    private static final String       QUERY_IMAGE_ASSESSOR_FROM_OUT_ID = "SELECT coalesce(xnat_imageassessordata_id, '') FROM img_assessor_out_resource WHERE xnat_abstractresource_xnat_abstractresource_id = :resourceId";
    private static final String       QUERY_PROJECT_ID                 = "SELECT xnat_projectdata_id FROM xnat_projectdata_resource WHERE xnat_abstractresource_xnat_abstractresource_id = :resourceId";
    private static final String       QUERY_SUBJECT_ID                 = "SELECT xnat_subjectdata_id FROM xnat_subjectdata_resource WHERE xnat_abstractresource_xnat_abstractresource_id = :resourceId";
    private static final List<String> EXPERIMENT_QUERIES               = Arrays.asList(QUERY_EXPERIMENT_ID, QUERY_IMAGE_ASSESSOR_FROM_OUT_ID, QUERY_IMAGE_ASSESSOR_FROM_IN_ID);
}
