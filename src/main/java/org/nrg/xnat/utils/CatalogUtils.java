/*
 * web: org.nrg.xnat.utils.CatalogUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import static org.apache.commons.io.FileUtils.listFiles;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.*;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.fileExtraction.FileExtractor;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.presentation.ChangeSummaryBuilderA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.archive.RemoteFilesService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author timo
 */
@Slf4j
@SuppressWarnings({"deprecation", "UnusedReturnValue"})
public class CatalogUtils {

    public final static String[] FILE_HEADERS = {"Name", "Size", "URI", "collection", "file_tags", "file_format", "file_content", "cat_ID", "digest"};
    public final static String[] FILE_HEADERS_W_FILE = {"Name", "Size", "URI", "collection", "file_tags", "file_format", "file_content", "cat_ID", "file", "digest"};

    public static final String PROJECT_PATH  = "projectPath";
    public static final String ABSOLUTE_PATH = "absolutePath";
    public static final String LOCATOR       = "locator";
    public static final String URI           = "URI";
    public static final IOFileFilter XNAT_CATALOGABLE_FILE_FILTER = new AndFileFilter(new NotFileFilter(new SuffixFileFilter("_catalog.xml")),
            new NotFileFilter(new PrefixFileFilter(ThreadAndProcessFileLock.LOCKFILE_PREFIX)));

    public static class CatalogEntryAttributes {
        public String relativePath;
        public String name;
        public long size;
        public Date lastModified;
        public String md5;
        public String format;
        public String content;

        public CatalogEntryAttributes(String relativePath, String name, long size, Date lastModified, String md5, String format, String content) {
            this.relativePath = relativePath;
            this.name = name;
            this.size = size;
            this.md5 = md5;
            this.lastModified = lastModified;
            this.format = format;
            this.content = content;
        }
    }

    public static class CatalogData {
        @Nullable
        public XnatResourcecatalog catRes = null;

        @Nonnull
        public File catFile;

        @Nullable
        public String catFileChecksum = null;

        @Nonnull
        public String catPath;

        @Nonnull
        public CatCatalogBean catBean;

        @Nullable
        public String project;

        /**
         * This constructor is provided for backwards compatibility, it shouldn't be used if you can avoid it,
         * esp if parameters are null
         * @param catBean the catalog bean
         * @param catFile the catalog file
         * @param project the project
         * @param catFileChecksum the checksum of the cat file when it was read
         */
        @Deprecated
        public CatalogData(@Nonnull CatCatalogBean catBean, @Nonnull File catFile, @Nullable String project,
                           @Nullable String catFileChecksum) {
            this.catBean = catBean;
            this.catFile = catFile;
            this.project = project;
            this.catFileChecksum = catFileChecksum;
            this.catPath = catFile.getParent();
        }

        public CatalogData(@Nonnull File catFile, @Nullable String project) throws ServerException {
            this(catFile, project, true);
        }

        public CatalogData(@Nonnull File catFile, @Nullable String project, boolean create) throws ServerException {
            this(catFile, null, project, null, create);
        }

        public CatalogData(@Nonnull File catFile, @Nullable XnatResourcecatalog catRes, @Nullable String project)
                throws ServerException {
            this(catFile, catRes, project, null);
        }

        public CatalogData(@Nonnull File catFile, @Nullable XnatResourcecatalog catRes, @Nullable String project,
                           @Nullable String catId) throws ServerException {
            this(catFile, catRes, project, catId, true);
        }

        public CatalogData(@Nonnull File catFile, @Nullable XnatResourcecatalog catRes, @Nullable String project,
                           @Nullable String catId, boolean create) throws ServerException {
            this.catFile = catFile;
            this.catPath = this.catFile.getParent();
            this.catRes  = catRes;
            if (this.catFile.exists()) {
                this.catBean = readCatalogBeanFromCatalogFile(catId);
            } else if (create) {
                CatCatalogBean cat = new CatCatalogBean();
                if (StringUtils.isNotBlank(catId)) cat.setId(catId);
                this.catBean = cat;
            } else {
                throw new ServerException(this.catFile.getAbsolutePath() + " doesn't exist");
            }

            // Determine project: it may be provided to this constructor, it may be in the catalog as a metafield,
            // or we may need to get it from the resource by running a SQL query
            if (StringUtils.isBlank(project)) {
                project = getCatalogProject(catBean);
                if (StringUtils.isBlank(project) && catRes != null) {
                    project = queryResourceProject();
                }
            }
            this.project = project;
            setCatalogProject(catBean, this.project);
        }

        private CatCatalogBean readCatalogBeanFromCatalogFile(final String catId) throws ServerException {
            CatCatalogBean cat = null;
            InputStream inputStream = null;
            try {
                final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(catFile, true);
                fl.tryLock(2L, TimeUnit.MINUTES);
                //log.trace("{} reader start: {}", System.currentTimeMillis(), fl.toString());
                try (FileInputStream fis = new FileInputStream(catFile)) {
                    if (catFile.getName().endsWith(".gz")) {
                        inputStream = new GZIPInputStream(fis);
                    } else {
                        inputStream = fis;
                    }

                    final XDATXMLReader reader = new XDATXMLReader();
                    BaseElement base;
                    try {
                        base = reader.parse(inputStream);
                    } catch (SAXParseException exception) {
                        if (exception.getColumnNumber() == 1 && exception.getLineNumber() == 1 && StringUtils.startsWith(exception.getMessage(), PREMATURE_EOF)) {
                            log.warn("Tried to read the catalog file at {}, but it was empty. I'm going to regenerate the catalog but you should know something happened to the original file.", catFile.getAbsolutePath());
                            final CatCatalogBean catalog = new CatCatalogBean();
                            catalog.setId(catId);
                            base = catalog;
                        } else {
                            throw exception;
                        }
                    }
                    if (base instanceof CatCatalogBean) {
                        cat = (CatCatalogBean) base;
                        catFileChecksum = getHash(catFile, false);
                        if (StringUtils.isBlank(catFileChecksum)) {
                            throw new ServerException("Unable to compute checksum for " + catFile + ". This will be needed to safely write the catalog");
                        }
                    }
                } catch (FileNotFoundException exception) {
                    log.error("Couldn't find file: {}", catFile, exception);
                } catch (IOException exception) {
                    log.error("Error occurred reading file: {}", catFile, exception);
                } catch (SAXException exception) {
                    log.error("Error processing XML in file: {}", catFile, exception);
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    fl.unlock();
                    //log.trace("{} reader finish: {}", System.currentTimeMillis(), fl.toString());
                }
            } catch (IOException e) {
                log.error("Unable to obtain read lock for file: {}", catFile, e);
            } finally {
                ThreadAndProcessFileLock.removeThreadAndProcessFileLock(catFile);
            }

            if (cat == null) {
                throw new ServerException("No catalog bean stored in " + catFile);
            }
            return cat;
        }

        @Nullable
        private String queryResourceProject() {
            Integer id;
            if (catRes == null || (id = catRes.getXnatAbstractresourceId()) == null) {
                return null;
            }
            NamedParameterJdbcTemplate template = getNamedParameterJdbcTemplateInstance();
            if (template == null) {
                log.error("Unable to query for resource project bc couldn't acquire NamedParameterJdbcTemplate");
                return null;
            }
            final List<String> projects = template.query(QUERY_PROJECT_FROM_RESOURCE, new MapSqlParameterSource("abstractResourceId", id), (resultSet, index) -> resultSet.getString("project"));
            if (projects.isEmpty()) {
                log.warn("No projects associated with resource {}", catRes);
                return null;
            } else if (projects.size() > 1) {
                log.warn("Multiple projects associated with resource id={}: {}. Using the first...", id, projects);
            }
            return projects.get(0);
        }

        @Nonnull
        public static CatalogData getOrCreate(ArchivableItem item, final XnatResourcecatalogI resource)
                throws ServerException {
            String archivePath;
            try {
                archivePath = item.getArchiveRootPath();
            } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
                throw new ServerException("Unable to determine item archive root path for " + item.getId());
            }
            return getOrCreate(archivePath, resource, item.getProject());
        }

        @Nonnull
        public static CatalogData getOrCreate(final String rootPath,
                                              final XnatResourcecatalogI resource,
                                              @Nullable final String project)
                throws ServerException {
            File f = getOrCreateCatalogFile(rootPath, resource, project);
            XnatResourcecatalog catRes = (resource instanceof XnatResourcecatalog) ?
                    (XnatResourcecatalog) resource : null;
            return new CatalogData(f, catRes, project);
        }

        @Nonnull
        public static CatalogData getOrCreateAndClean(final String rootPath,
                                                      final XnatResourcecatalogI resource,
                                                      final boolean includeFullPaths,
                                                      @Nullable final String project) throws ServerException {
            return getOrCreateAndClean(rootPath, resource, includeFullPaths, project, null, null);
        }

        @Nonnull
        public static CatalogData getOrCreateAndClean(final String rootPath,
                                                      final XnatResourcecatalogI resource,
                                                      final boolean includeFullPaths,
                                                      @Nullable final String project,
                                                      final UserI user,
                                                      final EventMetaI c) throws ServerException {
            CatalogData catalogData = getOrCreate(rootPath, resource, project);

            formalizeCatalog(catalogData.catBean, catalogData.catPath, catalogData.project, user, c);

            if (includeFullPaths) {
                CatCatalogMetafieldBean mf = new CatCatalogMetafieldBean();
                mf.setName("CATALOG_LOCATION");
                mf.setMetafield(catalogData.catPath);
                catalogData.catBean.addMetafields_metafield(mf);
            }
            return catalogData;
        }
    }

    public static boolean getChecksumConfiguration(final XnatProjectdata project) throws ConfigServiceException {
        final String projectId = project.getId();
        final Configuration configuration = XDAT.getConfigService().getConfig("checksums", "checksums",
                StringUtils.isBlank(projectId) ? Scope.Site : Scope.Project, projectId);

        if (configuration != null) {
            final String checksumProperty = XDAT.getSiteConfigurationProperty("checksums");
            if (!StringUtils.isBlank(checksumProperty)) {
                return Boolean.parseBoolean(checksumProperty);
            }
        }

        return getChecksumConfiguration();
    }

    public static Boolean getChecksumConfiguration() throws ConfigServiceException {
        if (_checksumConfig == null) {
            String checksumProperty = XDAT.getSiteConfigurationProperty("checksums");
            if (!StringUtils.isBlank(checksumProperty)) {
                _checksumConfig = new AtomicBoolean(Boolean.parseBoolean(checksumProperty));
            }
        }
        return _checksumConfig.get();
    }

    /**
     * This sets the cached value for the checksum configuration. Note that this does <i>not</i> set the persisted
     * configuration value for the checksum configuration. This is used by the {@link org.nrg.xnat.utils.ChecksumsSiteConfigurationListener}
     * to update the cached value whenever the database value is changed elsewhere.
     *
     * @param checksumConfig The value to set for the cached checksum configuration setting.
     * @return The previous value for the cached checksum configuration setting.
     */
    public static Boolean setChecksumConfiguration(boolean checksumConfig) {
        return _checksumConfig.getAndSet(checksumConfig);
    }

    public static void calculateResourceChecksums(final CatalogData catalogData) {
        for (CatEntryI entry : catalogData.catBean.getEntries_entry()) {
            CatalogUtils.setChecksum(entry, catalogData.catPath, catalogData.project);
        }
    }

    /**
     * Set digest field on entry with corresponding MD5
     *
     * @param entry CatEntryI for operation
     * @param path  Path to catalog (used for relative paths)
     * @param project The project
     * @return true if entry was modified, false if not.
     */
    private static boolean setChecksum(final CatEntryI entry, final String path, final String project) {
        if (StringUtils.isBlank(entry.getDigest())) {//this should only occur if the MD5 isn't already there.
            // Catalogs are sometimes generated by client tools / contain remote URLs.
            // Thus, URI may not stay relative to the catalog, as XNAT would make them.
            final File file = CatalogUtils.getFile(entry, path, project);
            if (file != null && file.exists()) {//fail safe to missing files, maybe the files haven't been put in place yet...
                final String checksum = getHash(file);
                if (StringUtils.isNotBlank(checksum)) {
                    entry.setDigest(checksum);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Calculates a checksum hash for the submitted file based on the currently configured hash algorithm. Note that
     * currently XNAT only supports MD5 hashes. If an error occurs while calculating the checksum, the error is
     * logged and this method returns an empty string.
     *
     * Note that this method will attempt to obtain a read lock for this file. If you already have the lock, use
     * {@link #getHash(File, boolean)}
     *
     * @param file The file for which the checksum should be calculated.
     *
     * @return The checksum for the file if successful, an empty string otherwise.
     */
    @Nonnull
    public static String getHash(final File file) {
        return getHash(file, true);
    }

    /**
     * Calculates a checksum hash for the submitted file based on the currently configured hash algorithm. Note that
     * currently XNAT only supports MD5 hashes. If an error occurs while calculating the checksum, the error is
     * logged and this method returns an empty string.
     *
     * @param file The file for which the checksum should be calculated.
     * @param needLock set to false if you already have a lock for the file (attempting to acquire another would
     *                 result in deadlock)
     * @return The checksum for the file if successful, an empty string otherwise.
     */
    @Nonnull
    public static String getHash(File file, boolean needLock) {
        return getHash(file, needLock, "MD5");
    }

    @SuppressWarnings("SameParameterValue")
    @Nonnull
    private static String getHash(File file, boolean needLock, String hashType) {
        String digest = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance(hashType);

            //read into buffer and update md5
            try {
                ThreadAndProcessFileLock fl = null;
                if (needLock) {
                    fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(file, true);
                    fl.tryLock(10L, TimeUnit.SECONDS);
                }
                try (RandomAccessFile store = new RandomAccessFile(file, "r");
                     FileChannel channel = store.getChannel()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (channel.read(buffer) > 0) {
                        buffer.flip();
                        md5.update(buffer);
                        buffer.clear();
                    }

                    //compute hex
                    digest = Hex.encodeHexString(md5.digest());

                } catch (IOException e) {
                    log.error("Error computing file hash for {}", file.getAbsolutePath(), e);
                } finally {
                    if (needLock) fl.unlock();
                }
            } catch (IOException e) {
                log.error("Unable to obtain read lock for file {}", file.getAbsolutePath(), e);
            } finally {
                if (needLock) ThreadAndProcessFileLock.removeThreadAndProcessFileLock(file);
            }
        } catch(NoSuchAlgorithmException e){
            log.error("Unsupported hashing algorithm {}", hashType, e);
        }
        return digest;
    }

    /**
     * Get the relative path for the file indicated by the catalog entry.
     * Originally/by default, the relative path is in the URI attribute, but now that we support URL paths as URIs,
     * we store the relative path in the ID attribute (and also the cachePath if it's a URL).
     *
     * So: default to relative path = URI for backward compatibility (old IDs not set correctly), but if URI is an
     * absolute path, we try cachePath and then ID for the relative path.
     *
     * @param entry the catalog entry
     * @return the relative path
     */
    @SuppressWarnings("unused")
    public static String getRelativePathForCatalogEntry(final CatEntryI entry) {
        return getRelativePathForCatalogEntry(entry, null);
    }

    /**
     * The same as {@link #getRelativePathForCatalogEntry(CatEntryI)}, but if cachePath and ID are both blank and
     * a catalog path is provided, try to get a relative path by relativizing the URI against the catalog path
     *
     * @param entry the catalog entry
     * @param catalogPath the catalog path
     * @return the relative path
     */
    public static String getRelativePathForCatalogEntry(CatEntryI entry, String catalogPath) {
        // Originally/by default, the relative path is the URI, but now we support URL paths as URIs, it should be the ID (and
        // the cachePath if it's a URL). Still, we default to URI for backward compatibility (old IDs not set correctly)
        String uri = entry.getUri();
        String relPath = null;
        if (FileUtils.IsAbsolutePath(uri)) {
            relPath = StringUtils.defaultIfBlank(entry.getCachepath(), entry.getId());
            if (StringUtils.isBlank(relPath) && catalogPath != null) {
                // Try to relativize against parent
                relPath = Paths.get(catalogPath).relativize(Paths.get(uri)).toString();
            }
        }
        return StringUtils.defaultIfBlank(relPath, uri);
    }

    /**
     * Get catalog entry details
     * @param cat           the catalog bean
     * @param parentPath    the parent directory path
     * @param uriPath       the parent uri
     * @param _resource     the catalog resource
     * @param includeFile   T/F include java file object (includes pulling file if not local)
     * @param filter        catalog entry filter
     * @param proj          the project
     * @param locator       desired locator string, one of URI (uriPath + /RELPATH), absolutePath (path to entry,
     *                      may be remote URL), projectPath (path to entry relative to proj archive path, may be remote
     *                      URL)
     *
     * @return list of object arrays containing:
     *  0: name, 1: size, 2: URI/absolutePath/projectPath (based on locator input string), 3: label,
     *  4: cav of fields and tags, 5: format, 6: content, 7: abstract resource id, 8: file object if includeFile,
     *  8 or 9: MD5 digest
     *  (see also CatalogUtils.FILE_HEADERS_W_FILE / CatalogUtils.FILE_HEADERS)
     */
    public static List<Object[]> getEntryDetails(final @Nonnull CatCatalogI cat, final String parentPath,
                                                 final String uriPath, final XnatResource _resource,
                                                 final boolean includeFile, final CatEntryFilterI filter,
                                                 @Nullable final XnatProjectdata proj, final String locator) {
        final List<Object[]> catalogEntries = new ArrayList<>();
        for (final CatCatalogI subset : cat.getSets_entryset()) {
            catalogEntries.addAll(getEntryDetails(subset, parentPath, uriPath, _resource, includeFile, filter, proj, locator));
        }

        final String projectId;
        if (proj == null) {
            projectId = cat instanceof CatCatalogBean ? getCatalogProject((CatCatalogBean) cat) : null;
        } else {
            projectId = proj.getId();
        }
        for (final CatEntryI entry : cat.getEntries_entry()) {
            if (filter == null || filter.accept(entry)) {
                final List<Object> row = Lists.newArrayList();
                final String entryPath = StringUtils.replace(FileUtils.AppendRootPath(parentPath, entry.getUri()),
                        "\\", "/");
                String name = entry.getName();
                if (StringUtils.isEmpty(name)) {
                    name = entryPath.replaceAll(".*/", "");
                }
                String size = getCatalogEntrySizeString(entry);
                File file = null;
                if (includeFile || StringUtils.isEmpty(size) || StringUtils.isEmpty(name)) {
                    file = getFile(entry, parentPath, projectId);
                    if (file != null) {
                        name = file.getName();
                        size = String.valueOf(file.length());
                    } else {
                        log.warn("Unable to locate file for catalog entry {}. Using metadata from catalog for " +
                                "now, but catalog {}/{} should probably be refreshed", entry, parentPath, cat.getId());
                    }
                }
                row.add(name);
                //TODO Why are we always setting size = 0 when includeFile = true? This looks broken, but I'm not going
                // to fix it in case things are coded against it as-is
                row.add(includeFile ? 0 : size);
                if (locator.equalsIgnoreCase(URI)) {
                    row.add(FileUtils.AppendSlash(uriPath, "") + getRelativePathForCatalogEntry(entry, parentPath));
                } else if (locator.equalsIgnoreCase(ABSOLUTE_PATH)) {
                    row.add(entryPath);
                } else if (locator.equalsIgnoreCase(PROJECT_PATH)) {
                    String projectPath;
                    try {
                        assert proj != null;
                        projectPath = Paths.get(proj.getRootArchivePath()).relativize(Paths.get(entryPath)).toString();
                    } catch (IllegalArgumentException e) {
                        // Not relative to project, likely a full path
                        projectPath = entryPath;
                    }
                    row.add(projectPath);
                } else {
                    row.add("");
                }
                row.add(_resource.getLabel());
                final List<String> fieldsAndTags = Lists.newArrayList();
                for (CatEntryMetafieldI meta : entry.getMetafields_metafield()) {
                    if (!meta.getName().equals(SIZE)) {
                        fieldsAndTags.add(meta.getName() + "=" + meta.getMetafield());
                    }
                }
                for (CatEntryTagI tag : entry.getTags_tag()) {
                    fieldsAndTags.add(tag.getTag());
                }
                row.add(Joiner.on(",").join(fieldsAndTags));
                row.add(entry.getFormat());
                row.add(entry.getContent());
                row.add(_resource.getXnatAbstractresourceId());
                if (includeFile) {
                    row.add(file);
                }
                row.add(entry.getDigest());
                catalogEntries.add(row.toArray());
            }
        }

        return catalogEntries;
    }

    @Nullable
    public static String getCatalogProject(CatCatalogBean cat) {
        String project = null;
        CatCatalogMetafieldI mf = getMetaFieldByName(cat, PROJECT);
        if (mf != null) {
            project = mf.getMetafield();
        }
        return project;
    }
    public static boolean setCatalogProject(CatCatalogBean cat, String project) {
        return setMetaFieldByName(cat, PROJECT, project);
    }

    public static String getCatalogEntrySizeString(CatEntryI entry) {
        String size = "";
        CatEntryMetafieldI mf = getMetaFieldByName(entry, SIZE);
        if (mf != null) {
            size = mf.getMetafield();
        }
        return size;
    }

    public static long getCatalogEntrySize(CatEntryI entry) {
        String sizeStr = getCatalogEntrySizeString(entry);
        return Long.parseLong(StringUtils.defaultIfBlank(sizeStr,"0"));
    }

    /**
     * Takes a size of a file or heap of memory in the form of a long and returns a formatted readable version in the
     * form of byte units. For example, 46 would become 46B, 1,024 would become 1KB, 1,048,576 would become 1MB, etc.
     *
     * @param size The size in bytes to be formatted.
     * @return A formatted string representing the byte size.
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %sB", size / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    /**
     * Formats an object's file statistics for display.
     *
     * @param label     The label of the object (session, scan, resource, etc.)
     * @param fileCount The number of files that compose the object.
     * @param rawSize   The size of the files that compose the object.
     * @return A formatted display of the file statistics.
     */
    @SuppressWarnings("unused")
    public static String formatFileStats(final String label, final long fileCount, final Object rawSize) {
        long size = 0;
        if (rawSize != null) {
            if (rawSize instanceof Integer) {
                size = (Integer) rawSize;
            } else if (rawSize instanceof Long) {
                size = (Long) rawSize;
            }
        }
        if (label == null || label.equals("") || label.equalsIgnoreCase("total")) {
            return String.format("%s in %s files", formatSize(size), fileCount);
        }
        return String.format("%s: %s in %s files", label, formatSize(size), fileCount);
    }

    @SuppressWarnings("unused")
    public static Map<File, CatEntryI> getCatalogEntriesForFiles(final String rootPath,
                                                                 final XnatResourcecatalog catalog,
                                                                 final List<File> files,
                                                                 final String project) {
        final Map<File, CatEntryI> entries = new HashMap<>();

        CatalogData catalogData;
        try {
            catalogData = CatalogData.getOrCreate(rootPath, catalog, project);
        } catch (ServerException e) {
            log.error("Unable to locate or create catalog for resource {}", catalog.getXnatAbstractresourceId(), e);
            return entries;
        }

        final String parentPath = catalogData.catPath;
        for (final CatEntryI entry : catalogData.catBean.getEntries_entry()) {
            final File file = getFile(entry, parentPath, project);
            if (file != null && files.contains(file)) {
                entries.put(file, entry);
            }
        }
        return entries;
    }

    @Nullable
    public static CatCatalogMetafieldI getMetaFieldByName(CatCatalogBean cat, String name) {
        for (CatCatalogMetafieldI mf : cat.getMetafields_metafield()) {
            if (mf.getName().equals(name)) {
                return mf;
            }
        }
        return null;
    }

    @Nullable
    public static CatEntryMetafieldI getMetaFieldByName(CatEntryI entry, String name) {
        for (CatEntryMetafieldI mf : entry.getMetafields_metafield()) {
            if (mf.getName().equals(name)) {
                return mf;
            }
        }
        return null;
    }

    public static boolean setMetaFieldByName(CatCatalogBean cat, String name, String value) {
        CatCatalogMetafieldI mf = getMetaFieldByName(cat, name);
        if (mf == null) {
            mf = new CatCatalogMetafieldBean();
            mf.setName(name);
            mf.setMetafield(value);
            cat.addMetafields_metafield((CatCatalogMetafieldBean) mf);
            return true;
        } else {
            String currentValue;
            if ((currentValue = mf.getMetafield()) == null || !currentValue.equals(value)) {
                mf.setMetafield(value);
                return true;
            }
        }
        return false;
    }

    public static boolean setMetaFieldByName(CatEntryI entry, String name, String value) {
        CatEntryMetafieldI mf = getMetaFieldByName(entry, name);
        if (mf == null) {
            mf = new CatEntryMetafieldBean();
            mf.setName(name);
            mf.setMetafield(value);
            try {
                entry.addMetafields_metafield(mf);
                return true;
            } catch (Exception e) {
                log.error("Unable to set size metafield", e);
            }
        } else {
            if (!mf.getMetafield().equals(value)) {
                mf.setMetafield(value);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the {@link #RELATIVE_PATH relative path} and {@link #SIZE size} metafields to the submitted {@link CatEntryBean catalog entry bean}. Sets entry's cachePath to the relative path.
     *
     * @param entry    The catalog entry bean.
     * @param relative The relative path to the bean's associated resources.
     * @param size     The total size of the bean's associated resources.
     */
    public static void setCatEntryBeanMetafields(final CatEntryBean entry, final String relative, final String size) {
        entry.setCachepath(relative);
        setMetaFieldByName(entry, RELATIVE_PATH, relative);
        setMetaFieldByName(entry, SIZE, size);
    }

    /**
     * Refresh catalog bean and write catalog to file

     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param resourceMap           map of resource metadata
     * @param now                   event for transaction
     * @param addUnreferencedFiles  add files present in directory but not in catalog
     * @param removeMissingFiles    remove catalog entries for files not present in directory
     * @param populateStats         update size and count on catalog resource object
     * @throws Exception            for issues writing the catalog file or saving the resource
     */
    public static void refreshAndWriteCatalog(final CatalogData catalogData,
                                              final UserI user,
                                              final XnatResourceInfoMap resourceMap,
                                              final EventMetaI now,
                                              final boolean addUnreferencedFiles,
                                              final boolean removeMissingFiles,
                                              final boolean populateStats) throws Exception {
        refreshAndWriteCatalog(catalogData, user, resourceMap, now, addUnreferencedFiles, removeMissingFiles,
                populateStats, getChecksumConfiguration());
    }

    /**
     * Refresh catalog bean and write catalog to file

     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param resourceMap           map of resource metadata
     * @param now                   event for transaction
     * @param addUnreferencedFiles  add files present in directory but not in catalog
     * @param removeMissingFiles    remove catalog entries for files not present in directory
     * @param populateStats         update size and count on catalog resource object
     * @param checksums             compute checksums of files in catalog
     * @throws Exception            for issues writing the catalog file or saving the resource
     */
    public static void refreshAndWriteCatalog(final CatalogData catalogData,
                                              final UserI user,
                                              final XnatResourceInfoMap resourceMap,
                                              final EventMetaI now,
                                              final boolean addUnreferencedFiles,
                                              final boolean removeMissingFiles,
                                              final boolean populateStats,
                                              final boolean checksums) throws Exception {
        final Pair<Boolean, Map<String, Map<String, Integer>>> refreshInfo = CatalogUtils.refreshCatalog(user, catalogData, resourceMap, now, addUnreferencedFiles, removeMissingFiles, populateStats, checksums);
        if (refreshInfo.getLeft()) {
            final Map<String, Map<String, Integer>> auditSummary = refreshInfo.getRight();
            //checksums and auditSummary computed in CatalogUtils.refreshCatalog
            CatalogUtils.writeCatalogToFile(catalogData, false, auditSummary);
            if (populateStats && catalogData.catRes != null) {
                catalogData.catRes.save(user, false, false, now);
            }
        }
    }


    /**
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog,
     *  removes any that have been deleted, computes checksums, and updates catalog stats.
     *
     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param eventMeta             event for transaction
     * @param addUnreferencedFiles  adds files not referenced in catalog
     * @param removeMissingFiles    removes files referenced in catalog but not on filesystem
     * @param populateStats         updates file count & size for catRes in XNAT db
     * @param checksums             computes/updates checksums
     * @return new Object[] { modified, auditSummary }     modified: true if cat modified and needs save
     *                                                     auditSummary: audit hashmap
     * @deprecated Use {@link #refreshCatalog(UserI, CatalogData, EventMetaI, boolean, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object[] refreshCatalog(final CatalogData catalogData, final UserI user, final EventMetaI eventMeta,
                                          final boolean addUnreferencedFiles, final boolean removeMissingFiles,
                                          final boolean populateStats, final boolean checksums) {
        final Pair<Boolean, Map<String, Map<String, Integer>>> pair = refreshCatalog(user, catalogData, eventMeta, addUnreferencedFiles, removeMissingFiles, populateStats, checksums);
        return new Object[]{pair.getKey(), pair.getValue()};
    }

    /**
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog,
     *  removes any that have been deleted, computes checksums, and updates catalog stats.
     *
     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param eventMeta             event for transaction
     * @param addUnreferencedFiles  adds files not referenced in catalog
     * @param removeMissingFiles    removes files referenced in catalog but not on filesystem
     * @param populateStats         updates file count & size for catRes in XNAT db
     * @return Pair with boolean set as true if the catalog was modified and needs to be saved. The map value contains info on the changes made.
     */
    public static Pair<Boolean, Map<String, Map<String, Integer>>> refreshCatalog(final UserI user,
                                                                                  final CatalogData catalogData,
                                                                                  final EventMetaI eventMeta,
                                                                                  final boolean addUnreferencedFiles,
                                                                                  final boolean removeMissingFiles,
                                                                                  final boolean populateStats) {
        boolean checksums = false;
        try {
            checksums = getChecksumConfiguration();
        } catch (ConfigServiceException e) {
            log.error("Unable to determine checksum configuration", e);
        }
        return refreshCatalog(user, catalogData, null, eventMeta, addUnreferencedFiles, removeMissingFiles,
                populateStats, checksums);
    }

    /**
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog,
     *  removes any that have been deleted, computes checksums, and updates catalog stats.
     *
     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param eventMeta             event for transaction
     * @param addUnreferencedFiles  adds files not referenced in catalog
     * @param removeMissingFiles    removes files referenced in catalog but not on filesystem
     * @param populateStats         updates file count & size for catRes in XNAT db
     * @param checksums             computes/updates checksums
     * @return Pair with boolean set as true if the catalog was modified and needs to be saved. The map value contains info on the changes made.
     */
    public static Pair<Boolean, Map<String, Map<String, Integer>>> refreshCatalog(final UserI user, final CatalogData catalogData, final EventMetaI eventMeta,
                                                                                  final boolean addUnreferencedFiles, final boolean removeMissingFiles,
                                                                                  final boolean populateStats, final boolean checksums) {
        return refreshCatalog(user, catalogData, null, eventMeta, addUnreferencedFiles, removeMissingFiles, populateStats, checksums);
    }

    /**
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog,
     *  removes any that have been deleted, computes checksums, and updates catalog stats.
     *
     * @param catalogData           catalog data object
     * @param user                  user for transaction
     * @param eventMeta             event for transaction
     * @param addUnreferencedFiles  adds files not referenced in catalog
     * @param removeMissingFiles    removes files referenced in catalog but not on filesystem
     * @param populateStats         updates file count & size for catRes in XNAT db
     * @param checksums             computes/updates checksums
     * @return Pair with boolean set as true if the catalog was modified and needs saved. The map value contains info on the changes made.
     */
    public static Pair<Boolean, Map<String, Map<String, Integer>>> refreshCatalog(@SuppressWarnings("unused") final UserI user, final CatalogData catalogData, final XnatResourceInfoMap resources,
                                                                                  final EventMetaI eventMeta, final boolean addUnreferencedFiles, final boolean removeMissingFiles,
                                                                                  final boolean populateStats, final boolean checksums) {
        final Date now = eventMeta.getEventDate();
        final int eventId = eventMeta.getEventId().intValue();

        final AtomicInteger rtn = new AtomicInteger(0);
        final XnatResourceInfo info = XnatResourceInfo.builder().created(now).lastModified(now).eventId(eventId).build();

        //Needed for audit summary
        final AtomicInteger added = new AtomicInteger(0);
        final AtomicInteger modded = new AtomicInteger(0);

        //Build a hashmap so that instead of repeatedly looping through all the catalog entries,
        //comparing URI to our relative path, we can just do an O(1) lookup in our hashmap
        final Map<String, CatalogMapEntry> catalogMap = buildCatalogMap(catalogData);

        final Path catalogPath = Paths.get(catalogData.catPath);
        try {
            Files.walkFileTree(catalogPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    boolean mod = false;
                    File f = file.toFile();
                    final String relative = catalogPath.relativize(file).toString();
                    final String format, content;
                    if (resources != null && resources.containsKey(relative)) {
                        format = resources.get(relative).getFormat();
                        content = resources.get(relative).getContent();
                    } else {
                        format = null;
                        content = null;
                    }

                    if (f.equals(catalogData.catFile)) {
                        //don't add the catalog xml
                        return FileVisitResult.CONTINUE;
                    }

                    //verify that there is only one catalog xml in this directory
                    //fail if more then one is present -- otherwise they will be merged.
                    if (f.getName().endsWith(".xml") && isCatalogFile(f)) {
                        log.error("Multiple catalog files - not refreshing");
                        rtn.set(-1);
                        return FileVisitResult.TERMINATE;
                    }

                    if (!XNAT_CATALOGABLE_FILE_FILTER.accept(f)) {
                        //ignore files XNAT doesn't track (e.g., lock files)
                        return FileVisitResult.CONTINUE;
                    }

                    //check if file exists in catalog already
                    if (catalogMap.containsKey(relative)) {
                        CatalogMapEntry mapEntry = catalogMap.get(relative);
                        mapEntry.entryExists = true; //mark that file exists
                        mod = updateExistingCatEntry(mapEntry.entry, null, relative, f.getName(), attrs.size(),
                                checksums ? getHash(f) : null, format, content, info, eventMeta);
                        if (mod) modded.getAndIncrement();
                    } else {
                        if (addUnreferencedFiles) {
                            //this used to be run as part of writeCatalogFile
                            String digest = checksums ? getHash(f) : null;

                            CatEntryBean entry = populateAndAddCatEntry(catalogData.catBean, relative, relative,
                                    f.getName(), attrs.size(), info, digest, format, content, null);
                            catalogMap.put(relative, new CatalogMapEntry(entry, catalogData.catBean, true));

                            mod = true;
                            added.getAndIncrement();
                        }
                    }

                    //if we traverse any file and add or modify its entry, set rtn=1
                    //if no file entries are modified during the whole walk, rtn will remain 0
                    if (mod) rtn.set(1);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    // Skip dirs that can't be traversed
                    log.error("Skipped: {} ({})", file, e.getMessage(), e);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    // Ignore and log errors traversing a dir
                    if (e != null) {
                        log.error("Error traversing: {} ({})", dir, e.getMessage(), e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("Files.walkFileTree shouldn't throw IOException since we modified SimpleFileVisitor not to do so");
        }

        int rtn_val = rtn.get();
        if (rtn_val == -1) {
            //multiple catalog files
            return Pair.of(false, null);
        }

        //For resource stats
        final AtomicLong size = new AtomicLong(0);
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean modified = new AtomicBoolean(rtn_val == 1);
        int nRemoved = 0;
        if (removeMissingFiles || populateStats) {
            for (CatalogMapEntry mapEntry : catalogMap.values()) {
                if (mapEntry.entryExists) {
                    if (populateStats) {
                        size.getAndAdd(getCatalogEntrySize(mapEntry.entry));
                        count.getAndIncrement();
                    }
                } else if (removeMissingFiles) {
                    //File wasn't visited, doesn't exist, remove from catalog
                    mapEntry.catalog.getEntries_entry().remove(mapEntry.entry);
                    modified.set(true);
                    nRemoved++;
                }
            }
        }

        //Add to auditSummary
        final Map<String, Map<String, Integer>> auditSummary = new HashMap<>();
        if (nRemoved > 0)
            addAuditEntry(auditSummary, eventId, now, ChangeSummaryBuilderA.REMOVED, nRemoved);
        final int nModified = modded.get();
        if (nModified > 0)
            addAuditEntry(auditSummary, eventId, now, ChangeSummaryBuilderA.MODIFIED, nModified);
        final int nAdded = added.get();
        if (nAdded > 0)
            addAuditEntry(auditSummary, eventId, now, ChangeSummaryBuilderA.ADDED, nAdded);

        //Update resource with new file count & size
        // This is going to implicitly removeMissingFiles, since file count & size attributes are computed
        // while walking the file tree. If removeMissingFiles isn't specified, file count & size
        // won't match the catalog xml. This has always been the case, since you can't compute file size for
        // a non-existent file. It used to implicitly addUnreferencedFiles, too (see populateStats method).
        if (populateStats && catalogData.catRes != null) {
            Integer c = count.get();
            Long s = size.get();
            if (!c.equals(catalogData.catRes.getFileCount())) {
                catalogData.catRes.setFileCount(c);
                modified.set(true);
            }

            if (!s.equals(catalogData.catRes.getFileSize())) {
                catalogData.catRes.setFileSize(s);
                modified.set(true);
            }
        }
        return Pair.of(modified.get(), auditSummary);
    }

    public static class CatalogMapEntry {
        public CatEntryI entry;
        public CatCatalogI catalog;
        public boolean entryExists;

        /**
         * Make CatalogMapEntry object
         * @param entry         the catalog entry
         * @param catalog       the catalog bean
         * @param entryExists   bool for existing on filesystem
         */
        public CatalogMapEntry(CatEntryI entry, CatCatalogI catalog, boolean entryExists) {
            this.entry = entry;
            this.catalog = catalog;
            this.entryExists = entryExists;
        }
    }

    /**
     * Map with key = path relative to catalog (or absolute path if absoluteLocalPathAsKey=T or URI if uriOnlyAsKey=T)
     * and value=CatalogMapEntry
     *
     * @param catalogData the catalog data object
     * @return map
     */
    public static Map<String, CatalogMapEntry> buildCatalogMap(CatalogData catalogData) {
        return buildCatalogMap(catalogData, false);
    }

    /**
     * Map with key = path relative to catalog (or absolute path if absoluteLocalPathAsKey=T or URI if uriOnlyAsKey=T)
     * and value=CatalogMapEntry
     *
     * @param catalogData the catalog data object
     * @param bothUriAndLocalPathAsKeys true if a given catalog entry should have 2 map entries if URI field is not a
     *                                  relative path (one for URI, one for relative path - or absolute path if
     *                                  absoluteLocalPathAsKey=true. This is useful when adding a new entry and wanting
     *                                  to confirm it doesn't already exist (by URI) and, provided not, that the relative
     *                                  path isn't already in use
     * @return map
     */
    public static Map<String, CatalogMapEntry> buildCatalogMap(CatalogData catalogData,
                                                               boolean bothUriAndLocalPathAsKeys) {
        return buildCatalogMap(catalogData.catBean, catalogData.catPath, catalogData.project, bothUriAndLocalPathAsKeys);
    }

    /**
     * Map with key = path relative to catalog (or absolute path if absoluteLocalPathAsKey=T or URI if uriOnlyAsKey=T)
     * and value=CatalogMapEntry
     *
     * @param cat the catalog bean
     * @param catPath the catalog parent path (path to dir containing catalog)
     * @param project the project
     * @param bothUriAndLocalPathAsKeys true if a given catalog entry should have 2 map entries if URI field is not a
     *                                  relative path (one for URI, one for relative path - or absolute path if
     *                                  absoluteLocalPathAsKey=true. This is useful when adding a new entry and wanting
     *                                  to confirm it doesn't already exist (by URI) and, provided not, that the relative
     *                                  path isn't already in use
     * @return map
     */
    private static Map<String, CatalogMapEntry> buildCatalogMap(CatCatalogI cat,
                                                                String catPath,
                                                                String project,
                                                                boolean bothUriAndLocalPathAsKeys) {

        Map<String, CatalogMapEntry> catalogMap = new HashMap<>();
        RemoteFilesService remoteFilesService = XDAT.getContextService().getBeanSafely(RemoteFilesService.class);
        for (CatCatalogI subset : cat.getSets_entryset()) {
            catalogMap.putAll(buildCatalogMap(subset, catPath, project, bothUriAndLocalPathAsKeys));
        }

        List<CatEntryI> entries = cat.getEntries_entry();
        for (CatEntryI entry : entries) {
            CatalogMapEntry mapEntry = new CatalogMapEntry(entry, cat, false);

            String uri = entry.getUri();
            if (FileUtils.IsUrl(uri, true)) {
                mapEntry.entryExists = remoteFilesService != null && remoteFilesService.canPullFile(uri, project);
            }

            // Determine the catalog-relative path on the filesystem.
            String relPath = getRelativePathForCatalogEntry(entry, catPath);
            if (bothUriAndLocalPathAsKeys && !uri.equals(relPath)) {
                catalogMap.put(uri, mapEntry);
            }
            catalogMap.put(relPath, mapEntry);
        }
        return catalogMap;
    }

    @SuppressWarnings("unused")
    public static void saveUpdatedCatalog(final CatalogData catalogData, Map<String, Map<String, Integer>> auditSummary,
                                          long catSize, int fileCount, final EventMetaI eventMeta, final UserI user)
            throws Exception {
        saveUpdatedCatalog(catalogData, auditSummary, catSize, fileCount, eventMeta, user, null, false);
    }

    public static void saveUpdatedCatalog(final CatalogData catalogData, Map<String, Map<String, Integer>> auditSummary,
                                          long catSize, int fileCount, final EventMetaI eventMeta, final UserI user,
                                          @Nullable final Map<CatEntryI, File> historyMap, final boolean removeFiles)
            throws Exception {

        writeCatalogToFile(catalogData, false, auditSummary);

        // Update resource stats
        assert catalogData.catRes != null;
        catalogData.catRes.setFileSize(catSize);
        catalogData.catRes.setFileCount(fileCount);
        catalogData.catRes.save(user, false, false, eventMeta);

        if (historyMap == null || historyMap.isEmpty()) {
            return;
        }

        CatalogData historyCatalogData = null;
        if (maintainFileHistory()) {
            historyCatalogData = new CatalogData(FileUtils.BuildHistoryFile(catalogData.catFile,
                    EventUtils.getTimestamp(eventMeta)), catalogData.project);
        }

        for (CatEntryI entry : historyMap.keySet()) {
            File file = historyMap.get(entry);

            if (historyCatalogData != null) {
                //move existing file to audit trail
                CatEntryBean newEntryBean = (CatEntryBean) ((CatEntryBean) entry).copy();
                if (file.exists()) {
                    final File newFile = FileUtils.MoveToHistory(file, EventUtils.getTimestamp(eventMeta));
                    newEntryBean.setUri(newFile.getAbsolutePath());
                }

                if (eventMeta != null) {
                    newEntryBean.setModifiedtime(eventMeta.getEventDate());
                    if (eventMeta.getEventId() != null) {
                        newEntryBean.setModifiedeventid(eventMeta.getEventId().toString());
                    }
                    if (eventMeta.getUser() != null) {
                        newEntryBean.setModifiedby(eventMeta.getUser().getUsername());
                    }
                }
                historyCatalogData.catBean.addEntries_entry(newEntryBean);
            }

            if (removeFiles) {
                if (!CatalogUtils.deleteFile(file, entry.getUri(), catalogData.project)) {
                    log.error("Error attempting to delete physical (and/or possibly remote) file {} " +
                            "for deleted catalog entry: {}", file.getAbsolutePath(), entry);
                }

                //if parent folder is empty, then delete folder
                if (FileUtils.CountFiles(file.getParentFile(), true) == 0) {
                    FileUtils.DeleteFile(file.getParentFile());
                }
            }
        }

        if (historyCatalogData != null) {
            writeCatalogToFile(historyCatalogData);
        }
    }


    public interface CatEntryFilterI {
        boolean accept(final CatEntryI entry);
    }

    public static CatEntryI getEntryByFilter(final CatCatalogI cat, final CatEntryFilterI filter) {
        CatEntryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getEntryByFilter(subset, filter);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            try {
                if (filter.accept(entry)) {
                    return entry;
                }
            } catch (Exception exception) {
                log.error("Error occurred filtering catalog entry: {}", entry, exception);
            }
        }

        return null;
    }

    public static Collection<CatEntryI> getEntriesByFilter(final CatCatalogI cat, final CatEntryFilterI filter) {
        List<CatEntryI> entries = new ArrayList<>();

        for (CatCatalogI subset : cat.getSets_entryset()) {
            entries.addAll(getEntriesByFilter(subset, filter));
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            try {
                if (filter == null || filter.accept(entry)) {
                    entries.add(entry);
                }
            } catch (Exception exception) {
                log.error("Error occurred filtering catalog entry: {}", entry, exception);
            }
        }

        return entries;
    }

    @SuppressWarnings("unused")
    public static CatCatalogI getCatalogByFilter(final CatCatalogI cat) {
        CatCatalogI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getCatalogByFilter(subset);
            if (e != null) return e;
        }

        return null;
    }

    public static List<File> getFiles(CatCatalogI cat, String parentPath, @Nullable String project) {
        List<File> al = new ArrayList<>();
        for (CatCatalogI subset : cat.getSets_entryset()) {
            al.addAll(getFiles(subset, parentPath, project));
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            File f = getFile(entry, parentPath, project);

            if (f != null)
                al.add(f);
        }

        return al;
    }

    /**
     * Gets file from file system.  This method supports relative or absolute or URL paths in the CatEntryI. It also
     * supports files that are gzipped on the file system, but don't include .gz in the catalog URI (this used to be
     * very common).
     *
     * @param entry      Catalog Entry for file to be retrieved
     * @param parentPath Path to catalog file directory
     * @param project    The project
     * @return File object represented by CatEntryI
     */
    @Nullable
    public static File getFile(CatEntryI entry, String parentPath, @Nullable String project) {
        return getFile(entry, parentPath, project, null);
    }

    /**
     * Gets file from file system and put it in destParentPath.  This method supports relative or absolute or URL paths
     * in the CatEntryI. It also supports files that are gzipped on the file system, but don't include .gz in the
     * catalog URI (this used to be very common).
     *
     * @param entry             Catalog Entry for file to be retrieved
     * @param parentPath        Path to catalog file directory
     * @param project           The project
     * @param destParentPath    Path to catalog file directory desired output location
     * @return File object represented by CatEntryI
     */
    @Nullable
    public static File getFile(CatEntryI entry,
                               String parentPath,
                               @Nullable String project,
                               @Nullable String destParentPath) {
        if (destParentPath == null) {
            destParentPath = parentPath;
        }

        CatalogEntryPathInfo info = new CatalogEntryPathInfo(entry, parentPath, destParentPath);
        return getFileOnLocalFileSystem(info.entryPath, info.entryPathDest, project);
    }

    public static class CatalogEntryPathInfo {
        public String entryPath;            // may be full archive-local path or uri
        public String entryPathDest;        // full path to destination location
        public String catalogRelativePath;  // path relative to catalog

        public CatalogEntryPathInfo(CatEntryI entry, String parentPath) {
            this(entry, parentPath, null);
        }

        public CatalogEntryPathInfo(CatEntryI entry, String parentPath, @Nullable String destParentPath) {
            destParentPath = StringUtils.defaultIfBlank(destParentPath, parentPath);

            String uri = entry.getUri();
            catalogRelativePath = getRelativePathForCatalogEntry(entry, parentPath);
            if (FileUtils.IsUrl(uri, true)) {
                entryPath = uri;
            } else {
                entryPath = StringUtils.replace(FileUtils.AppendRootPath(parentPath, catalogRelativePath),
                        "\\", "/");
            }
            entryPathDest = StringUtils.replace(FileUtils.AppendRootPath(destParentPath, catalogRelativePath), "\\", "/");
        }
    }

    public static Stats getFileStats(CatCatalogI cat, String parentPath, String project) {
        return new Stats(cat, parentPath, project);
    }

    public static class Stats {
        public int count;
        public long size;

        public Stats(CatCatalogI cat, String parentPath, String project) {
            count = 0;
            size = 0;
            for (final File f : getFiles(cat, parentPath, project)) {
                if (f != null && f.exists() && !f.getName().endsWith("catalog.xml")) {
                    count++;
                    size += f.length();
                }
            }
        }
    }

    public static Collection<CatEntryI> getEntriesByRegex(final CatCatalogI cat, String regex) {
        List<CatEntryI> entries = new ArrayList<>();
        for (CatCatalogI subset : cat.getSets_entryset()) {
            entries.addAll(getEntriesByRegex(subset, regex));
        }
        for (CatEntryI entry : cat.getEntries_entry()) {
            try {
                if (entry.getUri().matches(regex) || entry.getId().matches(regex) ||
                        entry.getCachepath().matches(regex)) {
                    entries.add(entry);
                }
            } catch (Exception exception) {
                log.error("Error occurred testing catalog entry: {}", entry, exception);
            }
        }
        return entries;
    }

    public static Collection<String> getURIs(CatCatalogI cat) {
        Collection<String> all = Lists.newArrayList();
        for (CatCatalogI subset : cat.getSets_entryset()) {
            all.addAll(getURIs(subset));
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            all.add(entry.getUri());
        }

        return all;
    }

    public static boolean checkEntryByURI(String content, String uri) {
        return StringUtils.contains(content,"URI=\"" + uri + "\"");
    }

    public static CatEntryI getEntryByURI(CatCatalogI cat, String name) {
        CatEntryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getEntryByURI(subset, name);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry.getUri().equals(name)) {
                return entry;
            }
        }

        //do the decoded check after the basic match.  URLDecoder is horribly non-performant.
        final String decoded = URLDecoder.decode(name);
        if (decoded != null) {
            for (CatEntryI entry : cat.getEntries_entry()) {
                if ((entry.getUri().equals(decoded))) {
                    return entry;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static CatEntryI getEntryByName(CatCatalogI cat, String name) {
        CatEntryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getEntryByName(subset, name);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            String decoded = URLDecoder.decode(name);
            if (entry.getName().equals(name) || entry.getName().equals(decoded)) {
                return entry;
            }
        }

        return null;
    }

    public static CatEntryI getEntryById(CatCatalogI cat, String name) {
        CatEntryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getEntryById(subset, name);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry.getId().equals(name)) {
                return entry;
            }
        }

        return null;
    }

    public static CatEntryI getEntryByURIOrId(final CatCatalogBean catalogBean, final String filePath) {
        final CatEntryI entry = CatalogUtils.getEntryByURI(catalogBean, filePath);
        if (entry != null) {
            return entry;
        }
        return CatalogUtils.getEntryById(catalogBean, filePath);
    }

    public static CatDcmentryI getDCMEntryByUID(CatCatalogI cat, String uid) {
        CatDcmentryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getDCMEntryByUID(subset, uid);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry instanceof CatDcmentryI && ((CatDcmentryI) entry).getUid().equals(uid)) {
                return (CatDcmentryI) entry;
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static CatDcmentryI getDCMEntryByInstanceNumber(CatCatalogI cat, Integer num) {
        CatDcmentryI e;
        for (CatCatalogI subset : cat.getSets_entryset()) {
            e = getDCMEntryByInstanceNumber(subset, num);
            if (e != null) return e;
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry instanceof CatDcmentryI && ((CatDcmentryI) entry).getInstancenumber().equals(num)) {
                return (CatDcmentryI) entry;
            }
        }

        return null;
    }

    /**
     * getFileOnLocalFileSystem will return the local file if it exists. If the uri is a URL, it will check
     * if any external filesystems have been configured via service and if so, try pulling the file from there.
     *
     * @param uri the uri
     * @param destPath any arbitrary local path for file, can be null
     * @param project the project or null if not known
     * @return File
     */
    @Nullable
    public static File getFileOnLocalFileSystem(String uri,
                                                @Nullable String destPath,
                                                @Nullable String project) {
        if (StringUtils.isBlank(destPath)) {
            if (FileUtils.IsUrl(uri, true)) {
                log.error("Cannot pull remote URI {} without a destination path", uri);
                return null;
            }
            destPath = uri;
        }

        File f = getFileOnLocalFileSystemOrig(destPath);
        if (f == null) {
            RemoteFilesService remoteFilesService = XDAT.getContextService().getBeanSafely(RemoteFilesService.class);
            if (remoteFilesService != null) {
                try {
                    f = remoteFilesService.pullFile(uri, destPath, project);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                    f = null;
                }
            }
        }
        return f;
    }

    /**
     * See {@link #getFileOnLocalFileSystem(String, String, String)}, destPath set to null
     */
    @SuppressWarnings("unused")
    public static File getFileOnLocalFileSystem(String fullPath) {
        return getFileOnLocalFileSystem(fullPath, null, null);
    }

    /**
     * Original implementation of getFileOnLocalFileSystem, returns java File object for fullPath string, appending gz if needed
     *
     * @param fullPath the path
     * @return java File object
     */
    public static File getFileOnLocalFileSystemOrig(String fullPath) {
        File f = new File(fullPath);
        if (!f.exists()) {
            if (!fullPath.endsWith(".gz")) {
                f = new File(fullPath + ".gz");
                if (!f.exists()) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return f;
    }

    /**
     * Delete file locally and also at URL if provided.
     *
     * @param f       the file
     * @param url     the url or null
     * @param project the project
     * @return true if file deleted, false otherwise
     */
    public static boolean deleteFile(File f, @Nullable String url, @Nullable String project) {
        boolean success = !f.exists() || f.delete();
        return success & deleteRemoteFile(url, project);
    }

    /**
     * Delete remote file
     *
     * @param url     the url or null
     * @param project the project
     * @return true if remote file deleted or no remote file, false otherwise
     */
    public static boolean deleteRemoteFile(@Nullable String url, @Nullable String project) {
        RemoteFilesService remoteFilesService;
        if (StringUtils.isBlank(url) || !FileUtils.IsUrl(url, true) ||
                (remoteFilesService = XDAT.getContextService().getBeanSafely(RemoteFilesService.class)) == null) {
            return true;
        }
        return remoteFilesService.deleteFile(url, project);
    }

    public static boolean configureEntry(final CatEntryBean entry, @Nonnull final XnatResourceInfo info, boolean isNew) {
        boolean modified = false;
        if (info.getDescription() != null) {
            entry.setDescription(info.getDescription());
            modified = true;
        }
        if (info.getFormat() != null) {
            entry.setFormat(info.getFormat());
            modified = true;
        }
        if (info.getContent() != null) {
            entry.setContent(info.getContent());
            modified = true;
        }
        if (info.getTags().size() > 0) {
            for (final String tag : info.getTags()) {
                final CatEntryTagBean t = new CatEntryTagBean();
                t.setTag(tag);
                entry.addTags_tag(t);
            }
            modified = true;
        }
        if (!info.getMetadata().isEmpty()) {
            for (final Map.Entry<String, String> e : info.getMetadata().entrySet()) {
                final CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                meta.setName(e.getKey());
                meta.setMetafield(e.getValue());
                entry.addMetafields_metafield(meta);
            }
            modified = true;
        }

        if (isNew) {
            if (StringUtils.isNotBlank(info.getUsername()) && entry.getCreatedby() == null) {
                entry.setCreatedby(info.getUsername());
            }
            if (info.getCreated() != null && entry.getCreatedtime() == null) {
                entry.setCreatedtime(info.getCreated());
            }
            if (info.getEventId() != null && entry.getCreatedeventid() == null) {
                entry.setCreatedeventid(info.getEventId().toString());
            }
        }

        return modified;
    }

    public static boolean configureResource(final XnatResource resource, final XnatResourceInfo info, final UserI user) throws Exception {
        boolean modified = false;
        if (info.getDescription() != null) {
            resource.setDescription(info.getDescription());
            modified = true;
        }
        if (info.getFormat() != null) {
            resource.setFormat(info.getFormat());
            modified = true;
        }
        if (info.getContent() != null) {
            resource.setContent(info.getContent());
            modified = true;
        }
        if (info.getTags().size() > 0) {
            for (final String entry : info.getTags()) {
                final XnatAbstractresourceTag t = new XnatAbstractresourceTag(user);
                t.setTag(entry);
                resource.setTags_tag(t);
            }
            modified = true;
        }
        if (!info.getMetadata().isEmpty()) {
            for (final Map.Entry<String, String> entry : info.getMetadata().entrySet()) {
                final XnatAbstractresourceTag t = new XnatAbstractresourceTag(user);
                t.setTag(entry.getValue());
                t.setName(entry.getKey());
                resource.setTags_tag(t);
            }
            modified = true;
        }
        return modified;
    }

    public static List<String> storeCatalogEntry(final List<? extends FileWriterWrapperI> fileWriters,
                                                 final String destination,
                                                 final XnatResourcecatalog catResource,
                                                 final XnatProjectdata proj,
                                                 final boolean extract,
                                                 final XnatResourceInfo info,
                                                 final boolean overwrite,
                                                 final EventMetaI ci) throws Exception {

        final CatalogData catalogData = CatalogData.getOrCreate(proj.getRootArchivePath(), catResource, proj.getId());
        final Map<String, CatalogMapEntry> catalogMap = buildCatalogMap(catalogData);
        final Path destinationDir = catalogData.catFile.getParentFile().toPath();

        List<String> duplicates = new ArrayList<>();

        for (final FileWriterWrapperI fileWriter : fileWriters) {
            final String filename = Paths.get(StringUtils.replace(fileWriter.getName(), "\\", "/")).getFileName().toString();
            if (extract && ZipUtils.isCompressedFile(filename)) {
                log.debug("Found archive file {}", filename);
                final FileExtractor extractor = new FileExtractor();
                final List<File>    files     =  extractor.extract(filename, fileWriter.getInputStream(), destinationDir, overwrite, ci);
                for (final File file : files) {
                    if (!file.isDirectory() && XNAT_CATALOGABLE_FILE_FILTER.accept(file)) {
                        // relative path is used to compare to existing catalog entries, and add if missing.
                        // entry paths are relative to the location of the catalog file.
                        final String relativePath = destinationDir.toUri().relativize(file.toURI()).getPath();
                        if (overwrite || !catalogMap.containsKey(relativePath)) {
                            addOrUpdateEntry(catalogData, catalogMap.get(relativePath) == null ? null : catalogMap.get(relativePath).entry, relativePath, relativePath, file, info, ci);
                        }
                    }
                }

                if (!overwrite) {
                    duplicates.addAll(extractor.getDuplicates());
                }
            } else {
                final String instance;
                if (!StringUtils.isBlank(fileWriter.getNestedPath())) {
                    instance = makePath(fileWriter.getNestedPath(), filename);
                } else if (StringUtils.isBlank(destination)) {
                    instance = filename;
                } else if (destination.startsWith("/")) {
                    instance = destination.substring(1);
                } else {
                    instance = destination;
                }

                final File saveTo = destinationDir.resolve(instance).toFile();
                if (saveTo.isFile() && !XNAT_CATALOGABLE_FILE_FILTER.accept(saveTo)) {
                    throw new Exception("XNAT cannot catalog " + saveTo.getName());
                }

                if (saveTo.exists() && !overwrite) {
                    duplicates.add(instance);
                } else {
                    final CatalogMapEntry mapEntry;
                    if (saveTo.exists() && (mapEntry = catalogMap.get(instance)) != null) {
                        if (mapEntry.entry instanceof CatEntryBean) {
                            CatalogUtils.moveToHistory(catalogData.catFile, catalogData.project, saveTo,
                                    (CatEntryBean) mapEntry.entry, ci);
                        }
                    }

                    if (!saveTo.getParentFile().exists() && !saveTo.getParentFile().mkdirs()) {
                        throw new Exception("Failed to create required directory: " + saveTo.getParentFile().getAbsolutePath());
                    }

                    log.debug("Saving filename {} to file {}", filename, saveTo.getAbsolutePath());

                    fileWriter.write(saveTo);

                    if (saveTo.isDirectory()) {
                        log.debug("Found a directory: {}", saveTo.getAbsolutePath());
                        for (final File file : listFiles(saveTo, XNAT_CATALOGABLE_FILE_FILTER, DirectoryFileFilter.DIRECTORY)) {
                            final String relativePath = instance + "/" +
                                    FileUtils.RelativizePath(saveTo, file).replace('\\', '/');
                            log.debug("Adding or updating catalog entry for file {}", file);
                            CatEntryI entry = catalogMap.get(relativePath) == null ? null : catalogMap.get(relativePath).entry;
                            addOrUpdateEntry(catalogData, entry, relativePath, relativePath, file, info, ci);
                        }
                    } else {
                        log.debug("Adding or updating catalog entry for file {}", saveTo.getAbsolutePath());
                        CatEntryI entry = catalogMap.get(instance) == null ? null : catalogMap.get(instance).entry;
                        addOrUpdateEntry(catalogData, entry, instance, instance, saveTo, info, ci);
                    }
                }
            }
        }

        log.debug("Writing catalog file {} with {} total entries", catalogData.catFile.getAbsolutePath(), catalogData.catBean.getEntries_entry().size());
        writeCatalogToFile(catalogData);

        return duplicates;
    }

    private static String makePath(String nestedPath, String name) {
        String separator = nestedPath.contains("\\") ? "\\" : "/";
        StringBuilder path = new StringBuilder(nestedPath);
        if (!nestedPath.endsWith(separator)) {
            path.append(separator);
        }
        return path.append(name).toString();
    }

    public static CatCatalogMetafieldI getAuditField(CatCatalogI cat) {
        CatCatalogMetafieldI field = null;
        for (CatCatalogMetafieldI mf : cat.getMetafields_metafield()) {
            if ("AUDIT".equals(mf.getName())) {
                field = mf;
                break;
            }
        }

        if (field == null) {
            field = new CatCatalogMetafieldBean();
            field.setName("AUDIT");
            try {
                cat.addMetafields_metafield(field);
            } catch (Exception ignored) {
            }
        }
        return field;
    }

    public static void refreshAuditSummary(CatCatalogI cat, Map<String, Map<String, Integer>> auditSummary) {
        CatCatalogMetafieldI field = getAuditField(cat);
        if (auditSummary == null) {
            //TODO right now, this removes any "deleted" audit entries, perhaps it should build off of any existing audit tag
            //rebuild from each catalog entry
            auditSummary = buildAuditSummary(cat);
            field.setMetafield(convertAuditToString(auditSummary));
        } else {
            String prev_audit = StringUtils.defaultIfBlank(field.getMetafield(),"");
            String cur_audit = convertAuditToString(auditSummary);
            if (!prev_audit.isEmpty() && !cur_audit.isEmpty()) {
                prev_audit+="|";
            }
            field.setMetafield(prev_audit + cur_audit);
        }
    }

    public static Map<String, Map<String, Integer>> retrieveAuditSummary(CatCatalogI cat) {
        if (cat == null) return new HashMap<>();
        CatCatalogMetafieldI field = null;
        for (CatCatalogMetafieldI mf : cat.getMetafields_metafield()) {
            if ("AUDIT".equals(mf.getName())) {
                field = mf;
                break;
            }
        }

        if (field != null) {
            return convertAuditToMap(field.getMetafield());
        } else {
            return buildAuditSummary(cat);
        }

    }

    public static void addAuditEntry(Map<String, Map<String, Integer>> summary, String key, String action, Integer i) {
        if (!summary.containsKey(key)) {
            summary.put(key, new HashMap<>());
        }

        if (!summary.get(key).containsKey(action)) {
            summary.get(key).put(action, 0);
        }

        summary.get(key).put(action, summary.get(key).get(action) + i);
    }

    public static void addAuditEntry(Map<String, Map<String, Integer>> summary, Integer eventId, Object d, String action, Integer i) {
        String key = eventId + ":" + d;
        addAuditEntry(summary, key, action, i);
    }

    /**
     * Deprecated: use {@link #writeCatalogToFile(CatalogData)}
     * @param xml the catalog bean
     * @param dest the file destination
     * @param project the project
     * @throws Exception for errors
     */
    @Deprecated
    public static void writeCatalogToFile(CatCatalogI xml, File dest, String project) throws Exception {
        try {
            writeCatalogToFile(xml, dest, project, getChecksumConfiguration());
        } catch (ConfigServiceException exception) {
            throw new Exception("Error attempting to retrieve checksum configuration", exception);
        }
    }

    /**
     * Deprecated: use {@link #writeCatalogToFile(CatalogData, boolean)}
     * @param xml the catalog bean
     * @param dest the file destination
     * @param project the project
     * @param calculateChecksums compute checksums or skip?
     * @throws Exception for errors
     * @deprecated Use {@link #writeCatalogToFile(CatalogData, boolean)} instead.
     */
    @Deprecated
    public static void writeCatalogToFile(CatCatalogI xml, File dest, String project, boolean calculateChecksums) throws Exception {
        writeCatalogToFile(xml, dest, project, calculateChecksums, null);
    }

    /**
     * This method is deprecated.
     * @param xml the catalog bean
     * @param dest the file destination
     * @param project the project
     * @param calculateChecksums compute checksums or skip?
     * @param auditSummary the audit summary map
     * @throws Exception for errors
     * @deprecated Use {@link #writeCatalogToFile(CatalogData, boolean, Map)} instead.
     */
    @Deprecated
    public static void writeCatalogToFile(CatCatalogI xml, File dest, String project, boolean calculateChecksums,
                                          Map<String, Map<String, Integer>> auditSummary) throws Exception {
        writeCatalogToFile(xml, dest, project, calculateChecksums, auditSummary, null);
    }

    /**
     * This method is deprecated.
     * @param xml the catalog bean
     * @param dest the file destination
     * @param project the project
     * @param calculateChecksums compute checksums or skip?
     * @param auditSummary the audit summary map
     * @param previousCatalogChecksum the checksum of the catalog when it was read, so that it isn't incorrectly overwritten
     * @throws Exception for errors
     * @deprecated Use {@link #writeCatalogToFile(CatalogData, boolean, Map)} instead.
     */
    @Deprecated
    public static void writeCatalogToFile(CatCatalogI xml, File dest, String project, boolean calculateChecksums,
                                          Map<String, Map<String, Integer>> auditSummary,
                                          @Nullable String previousCatalogChecksum) throws Exception {
        if (!(xml instanceof CatCatalogBean)) {
            throw new Exception("Expected catalog bean to be CatCatalogBean: " + xml);
        }
        CatalogData catalogData = new CatalogData((CatCatalogBean) xml, dest, project, previousCatalogChecksum);
        writeCatalogToFile(catalogData, calculateChecksums, auditSummary);
    }

    /**
     * @param catalogData the catalog data
     * @throws Exception for errors
     */
    public static void writeCatalogToFile(CatalogData catalogData) throws Exception {
        try {
            writeCatalogToFile(catalogData, getChecksumConfiguration());
        } catch (ConfigServiceException exception) {
            throw new Exception("Error attempting to retrieve checksum configuration", exception);
        }
    }

    /**
     * @param catalogData the catalog data
     * @param calculateChecksums compute checksums or skip?
     * @throws Exception for errors
     */
    public static void writeCatalogToFile(CatalogData catalogData, boolean calculateChecksums) throws Exception {
        writeCatalogToFile(catalogData, calculateChecksums, null);
    }

    /**
     * @param catalogData the catalog data
     * @param calculateChecksums compute checksums or skip?
     * @param auditSummary the audit summary map
     * @throws Exception for errors
     */
    public static void writeCatalogToFile(CatalogData catalogData, boolean calculateChecksums,
                                          Map<String, Map<String, Integer>> auditSummary) throws Exception {

        File catPathFile;
        if (!(catPathFile = new File(catalogData.catPath)).exists() && !catPathFile.mkdirs()) {
            throw new IOException("Failed to create required directory: " + catalogData.catPath);
        }

        if (calculateChecksums) {
            CatalogUtils.calculateResourceChecksums(catalogData);
        }

        refreshAuditSummary(catalogData.catBean, auditSummary);

        try {
            final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(catalogData.catFile,
                    false);
            fl.tryLock(10L, TimeUnit.SECONDS);
            //log.trace("{} writer start: {}", System.currentTimeMillis(), fl.toString());
            try {
                // Now that we have the lock, let's be sure no one changed the file contents since we last read it
                if (catalogData.catFile.exists() &&
                        !getHash(catalogData.catFile, false).equals(catalogData.catFileChecksum)) {
                    throw new ConcurrentModificationException("Another thread or process modified " +
                            catalogData.catFile + " since I last read it or I don't have a previous checksum to compare. " +
                            "To avoid overwriting changes, I'm throwing an exception.");
                }
                try (final FileOutputStream fos = new FileOutputStream(catalogData.catFile)) {
                    final OutputStreamWriter fw = new OutputStreamWriter(fos);
                    catalogData.catBean.toXML(fw);
                    fw.flush();
                }
                // update checksum after we write so this catalogData object will allow a future write
                catalogData.catFileChecksum = getHash(catalogData.catFile, false);
            } finally {
                fl.unlock();
                //log.trace("{} writer finish: {}", System.currentTimeMillis(), fl.toString());
            }
        } catch (Exception e) {
            log.error("Error writing catalog file {}", catalogData.catFile, e);
            throw e;
        } finally {
            ThreadAndProcessFileLock.removeThreadAndProcessFileLock(catalogData.catFile);
        }
    }

    @Nonnull
    public static File getOrCreateCatalogFile(String rootPath, XnatResourcecatalogI resource, @Nullable String project)
            throws ServerException {
        String fullPath = getFullPath(rootPath, resource);
        if (fullPath.endsWith("\\")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }

        File f = new File(fullPath);
        if (f.exists()) {
            return f;
        }

        f = new File(fullPath + ".gz");
        if (f.exists()) {
            try {
                FileUtils.GUnzipFiles(f);
            } catch (IOException e) {
                throw new ServerException(e);
            }
            return getOrCreateCatalogFile(rootPath, resource, project);
        }

        f = new File(fullPath);
        try {
            XnatResourcecatalog catRes = (resource instanceof XnatResourcecatalog) ?
                    (XnatResourcecatalog) resource : null;
            String catId = resource.getLabel() != null ? resource.getLabel() :
                    Long.toString(Calendar.getInstance().getTimeInMillis());
            CatalogData catalogData = new CatalogData(f, catRes, project, catId);
            writeCatalogToFile(catalogData);
            return catalogData.catFile;
        } catch (IOException e) {
            log.error("Error writing to the folder: {}", f.getParentFile().getAbsolutePath(), e);
            throw new ServerException(e);
        } catch (Exception e) {
            log.error("Error creating the folder: {}", f.getParentFile().getAbsolutePath(), e);
            throw new ServerException(e);
        }
    }

    /**
     * Deprecated use {@link #getOrCreateCatalogFile} with appropriate exception handling
     * @param project  the project
     * @param rootPath the root path
     * @param resource the resource
     * @return the catalog file
     */
    @Nullable
    @Deprecated
    public static File getCatalogFile(final String project, final String rootPath, final XnatResourcecatalogI resource) {
        try {
            return getOrCreateCatalogFile(rootPath, resource, project);
        } catch (ServerException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deprecated use {@link #getOrCreateCatalogFile} with appropriate exception handling
     * @param rootPath the root path
     * @param resource the resource
     * @return the catalog file
     */
    @Nullable
    @Deprecated
    public static File getCatalogFile(final String rootPath, final XnatResourcecatalogI resource) {
        try {
            return getOrCreateCatalogFile(rootPath, resource, null);
        } catch (ServerException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deprecated use {@link CatalogUtils.CatalogData} object with appropriate exception handling
     * @param catalogFile   the catalog file
     * @param project       the project
     * @return the catalog bean
     */
    @Nullable
    @Deprecated
    public static CatCatalogBean getCatalog(File catalogFile, @Nullable String project) {
        try {
            CatalogData catalogData = new CatalogData(catalogFile, project,false);
            return catalogData.catBean;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deprecated should use {@link CatalogUtils.CatalogData#getOrCreate(String, XnatResourcecatalogI, String)} with
     * appropriate exception handling
     *
     * Parses catalog xml for resource and returns the Bean object.  Returns null if not found.
     *
     * @param rootPath The root path for the catalog.
     * @param resource The resource catalog.
     * @param project   The project
     * @return The initialized catalog bean.
     */
    @Deprecated
    public static CatCatalogBean getCatalog(String rootPath, XnatResourcecatalogI resource, String project) {
        File catalogFile = null;
        try {
            catalogFile = CatalogUtils.getCatalogFile(project, rootPath, resource);
            if (catalogFile == null) {
                throw new FileNotFoundException();
            }
            if (catalogFile.getName().endsWith(".gz")) {
                FileUtils.GUnzipFiles(catalogFile);
                catalogFile = CatalogUtils.getCatalogFile(project, rootPath, resource);
            }
        } catch (FileNotFoundException exception) {
            log.error("Couldn't find file: {}", catalogFile, exception);
        } catch (IOException exception) {
            log.error("Error occurred reading file: {}", catalogFile, exception);
        } catch (Exception exception) {
            log.error("Unknown exception reading file at: {}", rootPath, exception);
        }

        return catalogFile != null ? getCatalog(catalogFile, project) : null;
    }

    /**
     * Deprecated should use {@link CatalogUtils.CatalogData#getOrCreateAndClean(String, XnatResourcecatalogI, boolean, String)}
     * with appropriate exception handling
     *
     * @param project  The project
     * @param rootPath The root path for the catalog.
     * @param resource The resource catalog.
     * @param includeFullPaths T to set CATALOG_LOCATION metafield
     * @return the catalog bean
     */
    @Deprecated
    public static CatCatalogBean getCleanCatalog(String project, String rootPath, XnatResourcecatalogI resource, boolean includeFullPaths) {
        return getCleanCatalog(project, rootPath, resource, includeFullPaths, null, null);
    }

    /**
     * Deprecated should use {@link CatalogUtils.CatalogData#getOrCreateAndClean(String, XnatResourcecatalogI, boolean, String, UserI, EventMetaI)} with appropriate exception handling
     *
     *
     * @param project  The project
     * @param rootPath The root path for the catalog.
     * @param resource The resource catalog.
     * @param includeFullPaths T to set CATALOG_LOCATION metafield
     * @param user the user
     * @param c the event
     * @return the catalog bean
     */
    @Deprecated
    public static CatCatalogBean getCleanCatalog(String project, String rootPath, XnatResourcecatalogI resource,
                                                 boolean includeFullPaths, UserI user, EventMetaI c) {
        File catalogFile = handleCatalogFile(project, rootPath, resource);
        CatCatalogBean cat = getCatalog(catalogFile, project);

        if (cat != null && catalogFile != null) {
            String parentPath = catalogFile.getParent();
            formalizeCatalog(cat, parentPath, project, user, c);
            if (includeFullPaths) {
                CatCatalogMetafieldBean mf = new CatCatalogMetafieldBean();
                mf.setName("CATALOG_LOCATION");
                mf.setMetafield(parentPath);
                cat.addMetafields_metafield(mf);
            }
            return cat;
        }

        return null;
    }

    /**
     * Reviews existing catalog and adds any missing fields
     *
     * @param cat     Catalog entry to be cleaned
     * @param catPath Path to catalog file (used to access files with relative paths).
     * @param project The project
     * @param user    User in operation
     * @param now     Corresponding event
     * @return true if catalog was modified, otherwise false
     */
    public static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, final String project, UserI user, EventMetaI now) {
        return formalizeCatalog(cat, catPath, project, user, now, false, false);
        // Default to false for checksums for now.  Maybe it should use the default setting for the server.
        // But, this runs every time a catalog xml is loaded.  So, it will get re-run over and over.
        // Not sure we want to add that amount of processing.
    }

    /**
     * Reviews existing catalog and adds any missing fields
     *
     * @param cat                Catalog entry to be cleaned
     * @param catPath            Path to catalog file (used to access files with relative paths).
     * @param project            The project
     * @param user               User in operation
     * @param now                Corresponding event
     * @param createChecksums    Boolean whether or not to generate checksums (if missing)
     * @param removeMissingFiles Boolean whether or not to delete references to missing files
     * @return true if catalog was modified, otherwise false
     */
    public static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, final String project, UserI user, EventMetaI now, boolean createChecksums, boolean removeMissingFiles) {
        return formalizeCatalog(cat, catPath, project,"", user, now, createChecksums, removeMissingFiles);
    }

    public static String getFullPath(String rootPath, XnatResourcecatalogI resource) {
        return StringUtils.appendIfMissing(RegExUtils.replaceAll(StringUtils.replace(FileUtils.AppendRootPath(rootPath, resource.getUri()), "\\", "/"), "/{2,}", "/"), "/");
    }

    @SuppressWarnings("unused")
    public boolean modifyEntry(final CatCatalogI cat, final CatEntryI oldEntry, final CatEntryI newEntry) {
        final List<CatEntryI>     entries = cat.getEntries_entry();
        final Optional<CatEntryI> current       = entries.stream().filter(entry -> StringUtils.equals(entry.getUri(), oldEntry.getUri())).findAny();
        if (current.isPresent()) {
            entries.remove(current.get());
            entries.add(newEntry);
            return true;
        }
        return cat.getSets_entryset().stream().anyMatch(entrySet -> modifyEntry(entrySet, oldEntry, newEntry));
    }

    public static List<File> findHistoricalCatFiles(File catFile) {
        final List<File> files = new ArrayList<>();

        final File historyDir = FileUtils.BuildHistoryParentFile(catFile);

        final String name = catFile.getName();

        final FilenameFilter filter = (arg0, arg1) -> (arg1.equals(name));

        if (historyDir.exists()) {
            final File[] historyFiles = historyDir.listFiles();
            if (historyFiles != null) {
                for (File d : historyFiles) {
                    if (d.isDirectory()) {
                        final File[] matched = d.listFiles(filter);
                        if (matched != null && matched.length > 0) {
                            files.addAll(Arrays.asList(matched));
                        }
                    }
                }
            }
        }

        return files;
    }

    public static boolean removeEntry(CatCatalogI cat, CatEntryI entry) {
        for (int i = 0; i < cat.getEntries_entry().size(); i++) {
            CatEntryI e = cat.getEntries_entry().get(i);
            if (e.getUri().equals(entry.getUri())) {
                cat.getEntries_entry().remove(i);
                return true;
            }
        }

        for (CatCatalogI subset : cat.getSets_entryset()) {
            if (removeEntry(subset, entry)) {
                return true;
            }
        }

        return false;
    }

    public static Boolean maintainFileHistory() {
        if (_maintainFileHistory == null) {
            _maintainFileHistory = new AtomicBoolean(XDAT.getBoolSiteConfigurationProperty("maintainFileHistory", false));
        }
        return _maintainFileHistory.get();
    }

    public static void moveToHistory(File catFile, String project, File f, CatEntryBean entry, EventMetaI ci) throws Exception {
        //move existing file to audit trail
        if (CatalogUtils.maintainFileHistory()) {
            final File newFile = FileUtils.MoveToHistory(f, EventUtils.getTimestamp(ci));
            addCatHistoryEntry(catFile, project, newFile.getAbsolutePath(), entry, ci);
        } else {
            Files.delete(f.toPath());
        }
    }

    public static void addCatHistoryEntry(File catFile, String project, String f, CatEntryBean entry, EventMetaI ci)
            throws Exception {
        //move existing file to audit trail
        CatEntryBean newEntryBean = (CatEntryBean) entry.copy();
        newEntryBean.setUri(f);
        if (ci != null) {
            newEntryBean.setModifiedtime(ci.getEventDate());
            if (ci.getEventId() != null) {
                newEntryBean.setModifiedeventid(ci.getEventId().toString());
            }
            if (ci.getUser() != null) {
                newEntryBean.setModifiedby(ci.getUser().getUsername());
            }
        }

        CatalogData catalogData = new CatalogData(FileUtils.BuildHistoryFile(catFile, EventUtils.getTimestamp(ci)), project);
        catalogData.catBean.addEntries_entry(newEntryBean);

        CatalogUtils.writeCatalogToFile(catalogData);
    }

    public static XFTTable populateTable(XFTTable table, UserI user, XnatProjectdata proj, boolean cacheFileStats) {
        XFTTable newTable = new XFTTable();
        String[] fields = {"xnat_abstractresource_id", "label", "element_name", "category", "cat_id", "cat_desc", "file_count", "file_size", "tags", "content", "format"};
        newTable.initTable(fields);
        table.resetRowCursor();
        while (table.hasMoreRows()) {
            Object[] old = table.nextRow();
            Object[] _new = new Object[11];
            log.debug("Found resource with ID: {}({})", old[0], old[1]);
            _new[0] = old[0];
            _new[1] = old[1];
            _new[2] = old[2];
            _new[3] = old[3];
            _new[4] = old[4];
            _new[5] = old[5];

            XnatAbstractresource res = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(old[0], user, false);
            if (res == null) {
                log.warn("User {} tried to get an abstract resource for the ID {}, but that was null.",
                        user.getUsername(), old[0]);
                continue;
            }

            if (cacheFileStats) {
                if (res.getFileCount() == null) {
                    res.setFileCount(res.getCount(proj.getRootArchivePath()));
                }
                if (res.getFileSize() == null) {
                    res.setFileSize(res.getSize(proj.getRootArchivePath()));
                }
                try {
                    res.save(user, true, false, null);
                } catch (Exception exception) {
                    if (res instanceof XnatResourcecatalog) {
                        log.error("Failed to save updates to resource catalog: {}", res.getLabel(), exception);
                    } else {
                        log.error("Failed to save updates to abstract resource: {}", res.getXnatAbstractresourceId(), exception);
                    }
                }
            }

            _new[6] = res.getFileCount();
            _new[7] = res.getFileSize();
            _new[8] = res.getTagString();
            _new[9] = res.getContent();
            _new[10] = res.getFormat();

            newTable.rows().add(_new);
        }

        return newTable;
    }

    public static boolean populateStats(XnatAbstractresource abstractResource, String rootPath) {
        Integer c = abstractResource.getCount(rootPath);
        Long s = abstractResource.getSize(rootPath);

        boolean modified = false;

        if (!c.equals(abstractResource.getFileCount())) {
            abstractResource.setFileCount(c);
            modified = true;
        }

        if (!s.equals(abstractResource.getFileSize())) {
            abstractResource.setFileSize(s);
            modified = true;
        }

        return modified;
    }

    public static CatEntryBean populateAndAddCatEntry(CatCatalogBean cat, String uri, String id, String filename,
                                                      long size, XnatResourceInfo info) {
        return populateAndAddCatEntry(cat, uri, id, filename, size, info, null);
    }

    public static CatEntryBean populateAndAddCatEntry(CatCatalogBean cat, String uri, String id, String filename,
                                                      long size, XnatResourceInfo info, @Nullable String digest) {
        return populateAndAddCatEntry(cat, uri, id, filename, size, info, digest, null, null, null);
    }

    public static CatEntryBean populateAndAddCatEntry(CatCatalogBean cat, String uri, String id, String filename,
                                                      long size, XnatResourceInfo info, @Nullable String digest,
                                                      @Nullable String format, @Nullable String content,
                                                      @Nullable String cachePath) {
        CatEntryBean newEntry = new CatEntryBean();
        newEntry.setUri(uri);
        newEntry.setName(filename);
        newEntry.setId(id);
        newEntry.setCachepath(StringUtils.defaultIfBlank(cachePath, id));
        if (StringUtils.isNotBlank(digest)) {
            newEntry.setDigest(digest);
        }
        if (StringUtils.isNotBlank(format)) {
            newEntry.setFormat(format);
        }
        if (StringUtils.isNotBlank(content)) {
            newEntry.setContent(content);
        }
        setMetaFieldByName(newEntry, SIZE, Long.toString(size));
        configureEntry(newEntry, info, true);
        cat.addEntries_entry(newEntry);
        return newEntry;
    }

    @SuppressWarnings("unused")
    public static CatEntryBean populateAndAddCatEntry(final CatCatalogBean cat,
                                                      @Nullable final String uri,
                                                      final CatalogEntryAttributes attr,
                                                      final XnatResourceInfo info) {
        return populateAndAddCatEntry(cat, StringUtils.defaultIfBlank(uri, attr.relativePath), attr.relativePath, attr.name, attr.size, info, attr.md5, attr.format, attr.content, attr.relativePath);
    }

    public static void addOrUpdateEntry(CatalogData catalogData, @Nullable CatEntryI entry, String uri,
                                        String relativePath, File f, XnatResourceInfo info, EventMetaI ci) {
        if (entry == null) {
            populateAndAddCatEntry(catalogData.catBean, uri, relativePath, f.getName(), f.length(), info);
        } else {
            String digest = null;
            try {
                digest = getChecksumConfiguration() ? getHash(f) : null;
            } catch (ConfigServiceException e) {
                // Ignore
            }
            updateExistingCatEntry(entry, uri, relativePath, f.getName(), f.length(), digest, null, null, info, ci);
        }
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static boolean updateExistingCatEntry(final CatEntryI entry,
                                                 @Nullable String uri,
                                                 final CatalogEntryAttributes attr,
                                                 final EventMetaI eventMeta) {
        return updateExistingCatEntry(entry, StringUtils.defaultIfBlank(uri, attr.relativePath),
                attr.relativePath, attr.name, attr.size, attr.md5, attr.format, attr.content, null, eventMeta);
    }

    public static boolean updateExistingCatEntry(CatEntryI entry, File f, String relativePath,
                                                 final EventMetaI eventMeta) {
        String digest = null;
        try {
            if (getChecksumConfiguration()) {
                digest = getHash(f);
            }
        } catch (ConfigServiceException ignored) {
            //Ignore
        }
        return updateExistingCatEntry(entry, relativePath, relativePath, f.getName(), f.length(), digest, null, null,
                null, eventMeta);
    }

    public static boolean updateExistingCatEntry(CatEntryI entry, @Nullable String uri, String relativePath, String name,
                                                 @Nullable Long fileSize, @Nullable String digest, @Nullable String format,
                                                 @Nullable String content, @Nullable XnatResourceInfo info, final EventMetaI eventMeta) {

        boolean mod = false;

        String origUri = entry.getUri();
        if (StringUtils.isNotBlank(uri) && !uri.equals(origUri)) {
            entry.setUri(uri);
            if (FileUtils.IsUrl(origUri) && getMetaFieldByName(entry, ORIG_URI) == null) {
                setMetaFieldByName(entry, ORIG_URI, origUri);
            }
            mod = true;
        }

        //logic mimics CatalogUtils.formalizeCatalog(cat, catFile.getParent(), user, now, checksums, removeMissingFiles);
        //older catalog files might have missing entries?
        UserI user = eventMeta.getUser();
        Date now = eventMeta.getEventDate();
        Integer eventId = eventMeta.getEventId() != null ? eventMeta.getEventId().intValue() : null;

        if (entry.getCreatedby() == null && user != null) {
            entry.setCreatedby(user.getUsername());
            mod = true;
        }
        if (entry.getCreatedtime() == null) {
            if (entry instanceof CatEntryBean) {
                // This method throws illegal arg exception on CatEntryBean objects
                ((CatEntryBean) entry).setCreatedtime(now);
            } else {
                entry.setCreatedtime(now);
            }
            mod = true;
        }
        if (entry.getCreatedeventid() == null && eventId != null) {
            entry.setCreatedeventid(eventId);
            mod = true;
        }
        if (!relativePath.equals(entry.getId())) {
            entry.setId(relativePath);
            mod = true;
        }
        if (!relativePath.equals(entry.getCachepath())) {
            entry.setCachepath(relativePath);
            mod = true;
        }
        if (!name.equals(entry.getName())) {
            entry.setName(name);
            mod = true;
        }

        //Set size
        if (fileSize != null && setMetaFieldByName(entry, SIZE, Long.toString(fileSize))) {
            mod = true;
        }

        //this used to be run as part of writeCatalogFile
        //however, that code didn't update checksums if they'd changed
        if (StringUtils.isNotBlank(digest) && !digest.equals(entry.getDigest())) {
            entry.setDigest(digest);
            mod = true;
        }

        // CatDcmentryBeans fail to set format correctly because it's not in their xml
        if (entry.getClass().equals(CatDcmentryBean.class)) {
            entry.setFormat("DICOM");
            mod = true;
        } else {
            if (UNSET_STRING.equals(format)) {
                mod = StringUtils.isNotEmpty(entry.getFormat());
                entry.setFormat(null);
            } else if (StringUtils.isNotBlank(format) && !StringUtils.equals(format, entry.getFormat())) {
                entry.setFormat(format);
                mod = true;
            }
        }

        if (UNSET_STRING.equals(content)) {
            mod = StringUtils.isNotEmpty(entry.getContent());
            entry.setContent(null);
        } else if (StringUtils.isNotBlank(content) && !StringUtils.equals(content, entry.getContent())) {
            entry.setContent(content);
            mod = true;
        }

        if (info != null && entry instanceof CatEntryBean) {
            mod |= configureEntry((CatEntryBean) entry, info, false);
        }

        if (mod) {
            updateModificationEvent(entry, eventMeta);
        }

        return mod;
    }

    /**
     * Update catalog entry with modification event details
     * @param entry catalog entry to update
     * @param eventMeta the modification event object
     */
    public static void updateModificationEvent(final CatEntryI entry, final EventMetaI eventMeta) {
        final UserI user = eventMeta.getUser();
        final Date now = eventMeta.getEventDate();
        final Integer eventId = eventMeta.getEventId() != null ? eventMeta.getEventId().intValue() : null;

        if (eventId != null) {
            entry.setModifiedeventid(eventId);
        }
        if (user != null) {
            entry.setModifiedby(user.getUsername());
        }
        if (entry instanceof CatEntryBean) {
            // Generic method throws illegal arg exception on CatEntryBean objects
            ((CatEntryBean) entry).setModifiedtime(now);
        } else {
            entry.setModifiedtime(now);
        }
    }

    public static Collection<CatEntryI> findCatEntriesWithinPath(String path,
                                                                 CatalogData catalogData) {

        final Map<String, CatalogMapEntry> catalogMapByRelPath = buildCatalogMap(catalogData);

        Collection<CatEntryI> entries = new ArrayList<>();

        String regex = null;
        if (path.endsWith("*")) {
            regex = path.replaceAll("\\*$", ".*");
        }

        for (String key : catalogMapByRelPath.keySet()) {
            if (key.equals(path) || key.startsWith(path) || (regex != null && key.matches(regex))) {
                entries.add(catalogMapByRelPath.get(key).entry);
            }
        }

        return entries;
    }

    private static String convertAuditToString(Map<String, Map<String, Integer>> summary) {
        StringBuilder sb = new StringBuilder();
        int counter1 = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : summary.entrySet()) {
            if (counter1++ > 0) sb.append("|");
            sb.append(entry.getKey()).append("=");
            int counter2 = 0;
            for (Map.Entry<String, Integer> sub : entry.getValue().entrySet()) {
                if (counter2++ > 0) sb.append(";");
                sb.append(sub.getKey()).append(":").append(sub.getValue());
            }

        }
        return sb.toString();
    }

    private static Map<String, Map<String, Integer>> convertAuditToMap(final String audit) {
        Map<String, Map<String, Integer>> summary = new HashMap<>();
        for (final String changeSet : audit.split("\\|")) {
            final String[] split1 = changeSet.split("=");
            if (split1.length > 1) {
                final String key = split1[0];
                final Map<String, Integer> counts = new HashMap<>();
                for (final String operation : split1[1].split(";")) {
                    final String[] entry = operation.split(":");
                    counts.put(entry[0], Integer.valueOf(entry[1]));
                }
                summary.put(key, counts);
            }
        }
        return summary;
    }

    private static Map<String, Map<String, Integer>> buildAuditSummary(CatCatalogI cat) {
        Map<String, Map<String, Integer>> summary = new HashMap<>();
        buildAuditSummary(cat, summary);
        return summary;
    }

    private static void buildAuditSummary(CatCatalogI cat, Map<String, Map<String, Integer>> summary) {
        for (CatCatalogI subSet : cat.getSets_entryset()) {
            buildAuditSummary(subSet, summary);
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry.getCreatedeventid() != null) {
                addAuditEntry(summary, entry.getCreatedeventid(), entry.getCreatedtime(), ChangeSummaryBuilderA.ADDED, 1);
            }

            if (entry.getModifiedtime() != null) {
                addAuditEntry(summary, entry.getModifiedeventid(), entry.getModifiedtime(), ChangeSummaryBuilderA.MODIFIED, 1);
            }
        }
    }

    @Nullable
    private static File handleCatalogFile(String project, final String rootPath, final XnatResourcecatalogI resource) {
        CatalogData catalogData;
        try {
            catalogData = CatalogData.getOrCreate(rootPath, resource, project);
        } catch (ServerException e) {
            log.error("Error reading catalog for resource {}", resource.getXnatAbstractresourceId(), e);
            return null;
        }
        return catalogData.catFile;
    }

    /**
     *
     * THIS HAS BEEN DEPRECATED BY {@link #refreshCatalog}
     *
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog.
     *
     * @param catFile  path to catalog xml file
     * @param cat      content of catalog xml file
     * @param user     user for transaction
     * @param event_id event id for transaction
     * @return true if the cat was modified (and needs to be saved).
     */
    @Deprecated
    public static boolean addUnreferencedFiles(final File catFile, final CatCatalogI cat, @SuppressWarnings("unused") final UserI user, final Number event_id) {
        //list of all files in the catalog folder
        final Collection<File> files = listFiles(catFile.getParentFile(), null, true);

        //verify that there is only one catalog xml in this directory
        //fail if more then one is present -- otherwise they will be merged.
        for (final File f : files) {
            if (!f.equals(catFile)) {
                if (f.getName().endsWith(".xml") && isCatalogFile(f)) {
                    return false;
                }
            }
        }

        final String content;
        try {
            content = org.apache.commons.io.FileUtils.readFileToString(catFile);
        } catch (IOException e) {
            log.error("Failed reading the catalog file {} while trying to add entries for the following files: {}", catFile.getAbsolutePath(), files.stream().map(File::getPath).collect(Collectors.joining(", ")), e);
            return false;
        }

        //URI object for the catalog folder (used to generate relative file paths)
        final URI              catFolderURI = catFile.getParentFile().toURI();
        final Date             now          = Calendar.getInstance().getTime();
        final AtomicBoolean    modified     = new AtomicBoolean();
        final XnatResourceInfo info         = XnatResourceInfo.builder().created(now).lastModified(now).eventId(event_id).build();
        for (final File file : files) {
            if (!file.equals(catFile) && XNAT_CATALOGABLE_FILE_FILTER.accept(file)) {//don't add the catalog xml or other files XNAT wont catalog
                //relative path is used to compare to existing catalog entries, and add it if its missing.  entry paths are relative to the location of the catalog file.
                final String relative = catFolderURI.relativize(file.toURI()).getPath();
                if (!checkEntryByURI(content, relative)) {
                    populateAndAddCatEntry((CatCatalogBean) cat, relative, relative, file.getName(), file.length(), info);
                    modified.set(true);
                }
            }
        }

        return modified.get();
    }

    public static boolean isCatalogFile(final File f) {
        if (f.getName().endsWith("_catalog.xml")) {
            return true;
        }
        try {
            if (org.apache.commons.io.FileUtils.readFileToString(f, Charset.defaultCharset()).contains("<cat:Catalog")) {
                return true;
            }
        } catch (IOException e) {
            // Do nothing for now
        }
        return false;
    }

    /**
     * Reviews the catalog directory and returns any files that aren't already referenced in the catalogs in that folder.
     *
     * @param catFolder path to catalog xml folder
     * @param project   the project
     * @return true if the cat was modified (and needs to be saved).
     */
    public static List<String> getUnreferencedFiles(final File catFolder, final String project) {
        final List<String> unreferenced = Lists.newArrayList();

        //list of all files in the catalog folder
        final String[] files = catFolder.list();

        //identify the catalog XMLs in this folder
        final List<CatCatalogI> catalogs = Lists.newArrayList();
        if (files != null) {
            for (final String filename : files) {
                if (filename.endsWith(".xml")) {
                    File f = new File(catFolder, filename);
                    if (isCatalogFile(f)) {
                        CatCatalogI cat = CatalogUtils.getCatalog(f, project);
                        if (cat != null) {
                            catalogs.add(cat);
                        }
                    }
                }
            }
        }

        Collection<String> cataloged = new TreeSet<>();
        for (CatCatalogI cat : catalogs) {
            cataloged.addAll(getURIs(cat));
        }

        if (files != null) {
            for (final String f : files) {
                if (XNAT_CATALOGABLE_FILE_FILTER.accept(new File(f))) {
                    if (!cataloged.remove(f)) {
                        unreferenced.add(f);
                    }
                }
            }
        }

        return unreferenced;
    }

    private static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, final String project, String header, UserI user, EventMetaI now, final boolean createChecksum, final boolean removeMissingFiles) {
        boolean modified = false;

        for (CatCatalogI subSet : cat.getSets_entryset()) {
            if (formalizeCatalog(subSet, catPath, project, FileUtils.AppendSlash(header,"") + subSet.getId(), user, now, createChecksum, removeMissingFiles)) {
                modified = true;
            }
        }

        List<CatEntryI> toRemove = Lists.newArrayList();

        for (CatEntryI entry : cat.getEntries_entry()) {
            if (entry.getCreatedby() == null && user != null) {
                entry.setCreatedby(user.getUsername());
                modified = true;
            }
            if (entry.getCreatedtime() == null && now != null) {
                ((CatEntryBean) entry).setCreatedtime(now.getEventDate());
                modified = true;
            }
            if (entry.getCreatedeventid() == null && now != null && now.getEventId() != null) {
                ((CatEntryBean) entry).setCreatedeventid(now.getEventId().toString());
                modified = true;
            }

            if (createChecksum) {
                if (setChecksum(entry, catPath, project)) {
                    modified = true;
                }
            }

            if (removeMissingFiles) {
                File f = getFile(entry, catPath, project);
                if (f == null) {
                    toRemove.add(entry);
                    modified = true;
                }
            }

            if (StringUtils.isEmpty(entry.getId())) {
                entry.setId(getRelativePathForCatalogEntry(entry, catPath));
                modified = true;
            }

            if (entry.getClass().equals(CatDcmentryBean.class)) {
                // CatDcmentryBeans fail to set format correctly because it's not in their xml
                entry.setFormat("DICOM");
            }
        }

        if (!toRemove.isEmpty()) {
            cat.getEntries_entry().removeAll(toRemove);
        }

        return modified;
    }

    private static NamedParameterJdbcTemplate getNamedParameterJdbcTemplateInstance() {
        _jdbcTemplate.compareAndSet(null, XDAT.getNamedParameterJdbcTemplate());
        return _jdbcTemplate.get();
    }

    public static final String RELATIVE_PATH = "RELATIVE_PATH";
    public static final String SIZE          = "SIZE";
    public static final String PROJECT       = "PROJECT";
    public static final String ORIG_URI      = "ORIG_URI";

    private static final String PREMATURE_EOF = "Premature end of file";
    private static final String UNSET_STRING  = "NULL";

    private static       AtomicBoolean                               _maintainFileHistory = null;
    private static       AtomicBoolean                               _checksumConfig      = null;
    private static final AtomicReference<NamedParameterJdbcTemplate> _jdbcTemplate        = new AtomicReference<>(null);

    // Previous query was less efficient for targeted query, i.e. with the WHERE clause, which is what this query uses.
    // For an aggregate query on resources, use the query from earlier revisions of this code.
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    private static final String QUERY_PROJECT_FROM_RESOURCE = "SELECT " +
                                                              "    a.xnat_abstractresource_id, " +
                                                              "    CASE " +
                                                              "        WHEN xpr.xnat_projectdata_id IS NOT NULL        THEN xpr.xnat_projectdata_id " +
                                                              "        WHEN xsr.xnat_subjectdata_id IS NOT NULL        THEN (SELECT project FROM xnat_subjectdata WHERE id = xsr.xnat_subjectdata_id) " +
                                                              "        WHEN xer.xnat_experimentdata_id IS NOT NULL     THEN (SELECT project FROM xnat_experimentdata WHERE id = xer.xnat_experimentdata_id) " +
                                                              "        WHEN xis.xnat_imagescandata_id IS NOT NULL      THEN (SELECT project FROM xnat_experimentdata WHERE id = xis.image_session_id) " +
                                                              "        WHEN iair.xnat_imageassessordata_id IS NOT NULL THEN (SELECT project FROM xnat_experimentdata WHERE id = iair.xnat_imageassessordata_id) " +
                                                              "        WHEN iaor.xnat_imageassessordata_id IS NOT NULL THEN (SELECT project FROM xnat_experimentdata WHERE id = iaor.xnat_imageassessordata_id) " +
                                                              "    END AS project, " +
                                                              "    r.uri " +
                                                              "FROM " +
                                                              "    xnat_abstractresource a " +
                                                              "        LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                              "        LEFT JOIN xnat_projectdata_resource xpr ON a.xnat_abstractresource_id = xpr.xnat_abstractresource_xnat_abstractresource_id " +
                                                              "        LEFT JOIN xnat_subjectdata_resource xsr ON a.xnat_abstractresource_id = xsr.xnat_abstractresource_xnat_abstractresource_id " +
                                                              "        LEFT JOIN xnat_experimentdata_resource xer ON a.xnat_abstractresource_id = xer.xnat_abstractresource_xnat_abstractresource_id " +
                                                              "        LEFT JOIN xnat_imagescandata xis ON a.xnat_imagescandata_xnat_imagescandata_id = xis.xnat_imagescandata_id " +
                                                              "        LEFT JOIN img_assessor_in_resource iair ON a.xnat_abstractresource_id = iair.xnat_abstractresource_xnat_abstractresource_id " +
                                                              "        LEFT JOIN img_assessor_out_resource iaor ON a.xnat_abstractresource_id = iaor.xnat_abstractresource_xnat_abstractresource_id " +
                                                              "WHERE " +
                                                              "    a.xnat_abstractresource_id = :abstractResourceId";
}
