/*
 * web: org.nrg.xnat.utils.CatalogUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.*;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.presentation.ChangeSummaryBuilderA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.archive.FilesystemService;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;
import java.security.MessageDigest;

/**
 * @author timo
 */
@Slf4j
@SuppressWarnings("deprecation")
public class CatalogUtils {

    public final static String[] FILE_HEADERS = {"Name", "Size", "URI", "collection", "file_tags", "file_format", "file_content", "cat_ID", "digest"};
    public final static String[] FILE_HEADERS_W_FILE = {"Name", "Size", "URI", "collection", "file_tags", "file_format", "file_content", "cat_ID", "file", "digest"};

    public final static String SIZE_METAFIELD = "size_in_bytes";

    public static class CatalogEntryAttributes {
        public long size;
        public Date lastModified;
        public String md5;
        public String relativePath;
        public String name;

        public CatalogEntryAttributes(long size, Date lastModified, String md5, String relativePath, String name) {
            this.size = size;
            this.md5 = md5;
            this.lastModified = lastModified;
            this.relativePath = relativePath;
            this.name = name;
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
                _checksumConfig = Boolean.parseBoolean(checksumProperty);
            }
        }
        return _checksumConfig;
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
        Boolean hold = _checksumConfig;
        _checksumConfig = checksumConfig;
        return hold;
    }

    public static void calculateResourceChecksums(final CatCatalogI cat, final File f) {
        for (CatEntryI entry : cat.getEntries_entry()) {
            CatalogUtils.setChecksum(entry, f.getParent());
        }
    }

    /**
     * Set digest field on entry with corresponding MD5
     *
     * @param entry CatEntryI for operation
     * @param path  Path to catalog (used for relative paths)
     * @return true if entry was modified, false if not.
     */
    private static boolean setChecksum(final CatEntryI entry, final String path) {
        if (StringUtils.isBlank(entry.getDigest())) {//this should only occur if the MD5 isn't already there.
            // Catalogs are sometimes generated by client tools / contain remote URLs.
            // Thus, URI may not stay relative to the catalog, as XNAT would make them.
            final File file = CatalogUtils.getFile(entry, path);
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
     * currently XNAT only supports MD5 hashes. If an error that occurs while calculating the checksum, the error is
     * logged and this method returns an empty string.
     *
     * @param file The file for which the checksum should be calculated.
     *
     * @return The checksum for the file if successful, an empty string otherwise.
     */
    @Nonnull
    public static String getHash(final File file) {
        //try {
        //    return MD5.asHex(MD5.getHash(file));
        //} catch (IOException e) {
        //    log.error("An error occurred calculating the checksum for a file at the path: " + file.getPath(), e);
        //    return "";
        //}
        //faster to use java's MD5 than FastMD5 (com.twmacinta.util.MD5) library
        return getFileHash(file.getAbsolutePath(), file.length());
    }

    /**
     * Speed up getHash using java7, allow for use of other hashing algorithms
     */
    @Nonnull
    private static String getFileHash(String filepath, long size) {
        return getFileHash(filepath, size, "MD5");
    }

    @Nonnull
    private static String getFileHash(String filepath, long size, String hash_type) {
        String digest = "";
        int buf_size;
        if (size < 512) {
            buf_size = 512;
        } else if (size > 65536) {
            buf_size = 65536;
        } else {
            buf_size = (int) size;
        }
        try {
            //read into buffer and update md5
            MessageDigest md5 = MessageDigest.getInstance(hash_type);
            RandomAccessFile store = new RandomAccessFile(filepath, "r");
            FileChannel channel = store.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(buf_size);
            channel.read(buffer);
            buffer.flip();
            md5.update(buffer);
            channel.close();
            store.close();

            //compute hex
            digest = Hex.encodeHexString(md5.digest());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unsupported hashing algorithm " + hash_type, e);
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
    public static String getRelativePathForCatalogEntry(CatEntryI entry) {
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

    public static List<Object[]> getEntryDetails(@Nonnull CatCatalogI cat, String parentPath, String uriPath,
                                                 XnatResource _resource, boolean includeFile,
                                                 final CatEntryFilterI filter, XnatProjectdata proj, String locator) {
        final ArrayList<Object[]> al = new ArrayList<>();
        for (final CatCatalogI subset : cat.getSets_entryset()) {
            al.addAll(getEntryDetails(subset, parentPath, uriPath, _resource, includeFile, filter, proj, locator));
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
                String size = "";
                CatEntryMetafieldI mf = getMetaFieldByName((CatEntryBean) entry, SIZE_METAFIELD);
                if (mf != null) {
                    size = mf.getMetafield();
                }
                File file = null;
                if (includeFile || StringUtils.isEmpty(size) || StringUtils.isEmpty(name)) {
                    file = getFile(entry, parentPath);
                    if (file != null) {
                        name = file.getName();
                        size = String.valueOf(file.length());
                    } else {
                        log.error("Unable to locate file for catalog entry {}. Using metadata from catalog for " +
                                "now, but catalog {}/{} should probably be refreshed", entry, parentPath, cat.getId());
                    }
                }
                row.add(name);
                row.add(includeFile ? 0 : size);
                if (locator.equalsIgnoreCase("URI")) {
                    row.add(FileUtils.AppendSlash(uriPath, "") + getRelativePathForCatalogEntry(entry, parentPath));
                } else if (locator.equalsIgnoreCase("absolutePath")) {
                    row.add(entryPath);
                } else if (locator.equalsIgnoreCase("projectPath")) {
                    String projectPath;
                    try {
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
                    if (!meta.getName().equals(SIZE_METAFIELD)) {
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
                al.add(row.toArray());
            }
        }

        return al;
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

    public static Map<File, CatEntryI> getCatalogEntriesForFiles(final String rootPath, final XnatResourcecatalog catalog, final List<File> files) {
        final File catFile = catalog.getCatalogFile(rootPath);
        final String parentPath = catFile.getParent();
        final CatCatalogBean cat = getCatalog(rootPath, catalog);

        final Map<File, CatEntryI> entries = Maps.newHashMap();
        if (cat != null) {
            for (final CatEntryI entry : cat.getEntries_entry()) {
                final File file = getFile(entry, parentPath);
                if (file != null && files.contains(file)) {
                    entries.put(file, entry);
                }
            }
        }
        return entries;
    }

    public static CatCatalogMetafieldI getMetaFieldByName(CatCatalogBean cat, String name) {
        for (CatCatalogMetafieldI mf : cat.getMetafields_metafield()) {
            if (mf.getName().equals(name)) {
                return mf;
            }
        }
        return null;
    }
    public static CatEntryMetafieldI getMetaFieldByName(CatEntryBean entry, String name) {
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
            if (!mf.getMetafield().equals(value)) {
                mf.setMetafield(value);
                return true;
            }
        }
        return false;
    }
    public static boolean setMetaFieldByName(CatEntryBean entry, String name, String value) {
        CatEntryMetafieldI mf = getMetaFieldByName(entry, name);
        if (mf == null) {
            mf = new CatEntryMetafieldBean();
            mf.setName(name);
            mf.setMetafield(value);
            entry.addMetafields_metafield((CatEntryMetafieldBean) mf);
            return true;
        } else {
            if (!mf.getMetafield().equals(value)) {
                mf.setMetafield(value);
                return true;
            }
        }
        return false;
    }

    public static boolean updateExistingCatEntry(CatEntryBean entry, File f, String absolutePath, String relativePath, long fsize, UserI user,
                                                 Number eventId, int eventIdInt, Date now,
                                                 AtomicInteger modded, AtomicInteger count, AtomicLong size,
                                                 final boolean populateStats, final boolean checksums){

        boolean mod = false;

        //logic mimics CatalogUtils.formalizeCatalog(cat, catFile.getParent(), user, now, checksums, removeMissingFiles);
        //older catalog files might have missing entries?
        if (entry.getCreatedby() == null && user != null) {
            entry.setCreatedby(user.getUsername());
            mod = true;
        }
        if (entry.getCreatedtime() == null) {
            entry.setCreatedtime(now);
            mod = true;
        }
        if (entry.getCreatedeventid() == null && eventId != null) {
            entry.setCreatedeventid(eventId.toString());
            mod = true;
        }
        if (StringUtils.isEmpty(entry.getId()) ||
                FileUtils.IsAbsolutePath(entry.getUri()) && !entry.getId().equals(relativePath)) {
            entry.setId(relativePath);
            mod = true;
        }
        if (FileUtils.IsAbsolutePath(entry.getUri()) && !entry.getCachepath().equals(relativePath)) {
            entry.setCachepath(relativePath);
            mod = true;
        }
        if (StringUtils.isEmpty(entry.getName())) {
            entry.setName(f.getName());
            mod = true;
        }
        // CatDcmentryBeans fail to set format correctly because it's not in their xml
        if (entry.getClass().equals(CatDcmentryBean.class)) {
            entry.setFormat("DICOM");
            mod = true;
        }
        //Set size
        if (setMetaFieldByName(entry, SIZE_METAFIELD, Long.toString(fsize))) {
            mod = true;
        }

        //this used to be run as part of writeCatalogFile
        //however, that code didn't update checksums if they'd changed
        if (checksums) {
            String digest = getFileHash(absolutePath, fsize);
            if (!StringUtils.isEmpty(digest) && !digest.equals(entry.getDigest())) {
                entry.setDigest(digest);
                if (user != null) entry.setModifiedby(user.getUsername());
                entry.setModifiedtime(now);
                entry.setModifiedeventid(eventIdInt);
                mod = true;
                modded.getAndIncrement();
            }
        }
        //this used to be run as populateStats
        if (populateStats) {
            size.addAndGet(fsize);
            count.getAndIncrement();
        }
        return mod;
    }

    /**
     * Reviews the catalog directory and adds any files that aren't already referenced in the catalog,
     *  removes any that have been deleted, computes checksums, and updates catalog stats.
     *
     * @param catRes                catalog resource
     * @param catFile               path to catalog xml file
     * @param cat                   content of catalog xml file
     * @param user                  user for transaction
     * @param eventId               event id for transaction
     * @param addUnreferencedFiles  adds files not referenced in catalog
     * @param removeMissingFiles    removes files referenced in catalog but not on filesystem
     * @param populateStats         updates file count & size for catRes in XNAT db
     * @param checksums             computes/updates checksums
     * @return new Object[] { modified, audit_summary }     modified: true if cat modified and needs save
     *                                                      audit_summary: audit hashmap
     */
    public static Object[] refreshCatalog(XnatResourcecatalog catRes, final File catFile, final CatCatalogBean cat,
                                         final UserI user, final Number eventId,
                                         final boolean addUnreferencedFiles, final boolean removeMissingFiles,
                                         final boolean populateStats, final boolean checksums) {

        final AtomicLong size = new AtomicLong(0);
        final AtomicInteger count = new AtomicInteger(0);
        final Path catalogPath = catFile.getParentFile().toPath();
        final String catalogDirectory = catFile.getParent();
        final Date now = Calendar.getInstance().getTime();
        boolean modified;
        final int eventIdInt = Integer.parseInt(eventId.toString());

        final AtomicInteger rtn = new AtomicInteger(0);
        final XnatResourceInfo info = XnatResourceInfo.buildResourceInfo(null, null,
                null, null, user, now, now, eventId);

        //Needed for audit summary
        final AtomicInteger added = new AtomicInteger(0);
        final AtomicInteger modded = new AtomicInteger(0);

        //Build a hashmap so that instead of repeatedly looping through all the catalog entries,
        //comparing URI to our relative path, we can just do an O(1) lookup in our hashmap
        //The below pulls all files into local FS, we may want to change this if it's too slow
        final HashMap<String, Object[]> catalog_map = buildCatalogMap(cat, catalogDirectory);

        try {
            Files.walkFileTree(catFile.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    boolean mod = false;
                    File f = file.toFile();

                    if (f.equals(catFile) || f.isHidden()) {
                        //don't add the catalog xml, ignore hidden files
                        return FileVisitResult.CONTINUE;
                    }

                    //verify that there is only one catalog xml in this directory
                    //fail if more then one is present -- otherwise they will be merged.
                    if (f.getName().endsWith(".xml") && isCatalogFile(f)) {
                        log.error("Multiple catalog files - not refreshing");
                        rtn.set(-1);
                        return FileVisitResult.TERMINATE;
                    }

                    //check if file exists in catalog already
                    final String absolutePath = f.getAbsolutePath();
                    final String relative = catalogPath.relativize(f.toPath()).toString();

                    if (catalog_map.containsKey(relative)) {
                        //map_entry[0] is the catalog entry
                        //map_entry[1] is the catalog or entryset containing the above catalog entry
                        //map_entry[2] is a bool for existing on filesystem
                        Object[] map_entry = catalog_map.get(relative);
                        CatEntryBean entry = (CatEntryBean) map_entry[0];
                        map_entry[2] = true; //mark that file exists
                        mod = updateExistingCatEntry(entry, f, absolutePath, relative, attrs.size(), user,
                                eventId, eventIdInt, now, modded, count, size, populateStats, checksums);
                    } else {
                        if (addUnreferencedFiles) {
                            CatEntryBean newEntry = populateAndAddCatEntry(cat,relative,relative,f.getName(),info,attrs.size());

                            //this used to be run as part of writeCatalogFile
                            if (checksums) {
                                newEntry.setDigest(getFileHash(absolutePath, attrs.size()));
                            }

                            mod = true;

                            //this used to be run as populateStats
                            //this conditional has to be inside the "addUnreferencedFiles" conditional so that the stats
                            //don't go out of sync with the catalog (if files aren't added, they shouldn't be counted)
                            if (populateStats) {
                                size.addAndGet(attrs.size());
                                count.getAndIncrement();
                            }

                            added.getAndIncrement();
                        }
                    }

                    //if we traverse any file and modify its entry, set rtn=1
                    //if no file entries are modified during the whole walk, rtn will remain 0
                    if (mod) rtn.set(1);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    // Skip dirs that can't be traversed
                    log.error("Skipped: " + file + " (" + e.getMessage() + ")", e);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    // Ignore and log errors traversing a dir
                    if (e != null) {
                        log.error("Error traversing: " + dir + " (" + e.getMessage() + ")",e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("Files.walkFileTree shouldn't throw IOException since we modified " +
                    "SimpleFileVisitor not to do so");
        }

        int rtn_val = rtn.get();
        if (rtn_val == -1) {
            //multiple catalog files
            return new Object[] {false, null};
        }
        modified = rtn_val == 1;

        int nremoved = 0;
        if (removeMissingFiles) {
            for (Object[] map_entry : catalog_map.values()) {
                //map_entry[0] is the catalog entry
                //map_entry[1] is the catalog or entryset containing the above catalog entry
                //map_entry[2] is a bool for existing on filesystem
                if (!(boolean) map_entry[2]) {
                    //File wasn't visited, doesn't exist, remove from catalog
                    ((CatCatalogBean) map_entry[1]).getEntries_entry().remove(map_entry[0]);
                    modified = true;
                    nremoved++;
                }
            }
        }

        //Add to audit_summary
        Map<String, Map<String, Integer>> audit_summary = new HashMap<>();
        if (nremoved > 0)
            addAuditEntry(audit_summary, eventIdInt, now, ChangeSummaryBuilderA.REMOVED, nremoved);
        int nmod = modded.get();
        if (nmod > 0)
            addAuditEntry(audit_summary, eventIdInt, now, ChangeSummaryBuilderA.MODIFIED, nmod);
        int nadded = added.get();
        if (nadded > 0)
            addAuditEntry(audit_summary, eventIdInt, now, ChangeSummaryBuilderA.ADDED, nadded);

        //Update resource with new file count & size
        // This is going to implicitly removeMissingFiles, since file count & size attributes are computed
        // while walking the file tree. If removeMissingFiles isn't specified, file count & size
        // won't match the catalog xml. This has always been the case, since you can't compute file size for
        // a non-existent file. It used to implicitly addUnreferencedFiles, too (see populateStats method).
        if (populateStats) {
            Integer c = count.get();
            Long s = size.get();
            if (!c.equals(catRes.getFileCount())) {
                catRes.setFileCount(c);
                modified = true;
            }

            if (!s.equals(catRes.getFileSize())) {
                catRes.setFileSize(s);
                modified = true;
            }
        }
        return new Object[]{ modified, audit_summary};
    }

    public static HashMap<String, Object[]> buildCatalogMap(CatCatalogI cat, String catPath) {
        return buildCatalogMap(cat, catPath, true);
    }

    public static HashMap<String, Object[]> buildCatalogMap(CatCatalogI cat, String catPath, boolean pull) {
        return buildCatalogMap(cat, catPath, pull, "", false);
    }

    public static HashMap<String, Object[]> buildCatalogMap(CatCatalogI cat, String catPath, boolean pull, String prefix) {
        return buildCatalogMap(cat, catPath, pull, prefix, false);
    }

    /**
     * HashMap with key = path relative to catalog (or URI if useUri=T) and value=Object[]:
     *     map_entry[0] is the catalog entry
     *     map_entry[1] is the catalog or entryset containing the above catalog entry
     *     map_entry[2] is a bool for existing on filesystem <strong>meant for use by other methods, always false upon return of this method</strong>
     *
     * @param cat the catalog bean
     * @param catPath the catalog parent path (path to dir containing catalog)
     * @param pull true if files should be pulled from remote
     * @param prefix prefix path that will be prepended to map keys
     * @param addUriAndRelative true if a given catalog entry should have 2 map entries if URI field is not a relative path
     *                          (one for URI, one for relative path)
     * @return map
     */
    public static HashMap<String, Object[]> buildCatalogMap(CatCatalogI cat, String catPath, boolean pull, String prefix,
                                                            boolean addUriAndRelative) {
        HashMap<String, Object[]> catalog_map = new HashMap<>();
        for (CatCatalogI subset : cat.getSets_entryset()) {
            catalog_map.putAll(buildCatalogMap(subset, catPath));
        }

        prefix = StringUtils.defaultString(prefix, "");
        List<CatEntryI> entries = cat.getEntries_entry();
        for (CatEntryI entry : entries) {
            //map_entry[0] is the catalog entry
            //map_entry[1] is the catalog or entryset containing the above catalog entry
            //map_entry[2] is a bool for existing on filesystem
            Object[] map_entry = new Object[]{entry, cat, false};

            File f = null;
            if (pull) f = getFile(entry, catPath); //pulls from remote FS

            // Want the HashMap key to be the relative path on the filesystem (or the URI if requested).
            // Originally/by default, this is the URI, but now we support URL paths as URIs, it should be the ID (and
            // the cachePath if it's a URL). Still, we default to URI (see getRelativePathForCatalogEntry)
            // for backward compatibility (old IDs not set correctly)
            String uri = entry.getUri();
            String relativePath;
            // relativePath should be set to the same thing regardless of which of these we use
            if (f != null) {
                relativePath = Paths.get(catPath).relativize(f.toPath()).toString();
            } else {
                relativePath = getRelativePathForCatalogEntry(entry, catPath);
            }
            if (addUriAndRelative && !uri.equals(relativePath)) {
                catalog_map.put(uri, map_entry);
            }
            catalog_map.put(FileUtils.AppendRootPath(prefix, relativePath), map_entry);
        }
        return catalog_map;
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
                log.error("Error occurred filtering catalog entry: " + entry, exception);
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
                log.error("Error occurred filtering catalog entry: " + entry, exception);
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

    public static List<File> getFiles(CatCatalogI cat, String parentPath) {
        List<File> al = new ArrayList<>();
        for (CatCatalogI subset : cat.getSets_entryset()) {
            al.addAll(getFiles(subset, parentPath));
        }

        for (CatEntryI entry : cat.getEntries_entry()) {
            File f = getFile(entry, parentPath);

            if (f != null)
                al.add(f);
        }

        return al;
    }

    /**
     * Gets file from file system.  This method supports relative or absolute or URL paths in the CatEntryI. It also supports files that are gzipped on the file system, but don't include .gz in the catalog URI (this used to be very common).
     *
     * @param entry      Catalog Entry for file to be retrieved
     * @param parentPath Path to catalog file directory
     * @return File object represented by CatEntryI
     */
    public static File getFile(CatEntryI entry, String parentPath) {
        // If the URI is an absolute path, entryPath = URI
        String entryPath = StringUtils.replace(FileUtils.AppendRootPath(parentPath, entry.getUri()), "\\", "/");

        // For a local file, entryPath = entryPathLocal
        String entryPathLocal = entryPath;
        if (FileUtils.IsUrl(entryPath, true)) {
            String relPath = getRelativePathForCatalogEntry(entry, parentPath);
            entryPathLocal = StringUtils.replace(FileUtils.AppendRootPath(parentPath, relPath), "\\", "/");
        }

        return getFileOnLocalFileSystem(entryPath, entryPathLocal);
    }

    public static Stats getFileStats(CatCatalogI cat, String parentPath) {
        return new Stats(cat, parentPath);
    }

    public static class Stats {
        public int count;
        public long size;

        public Stats(CatCatalogI cat, String parentPath) {
            count = 0;
            size = 0;
            for (final File f : getFiles(cat, parentPath)) {
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
                if (entry.getUri().matches(regex) || entry.getId().matches(regex)) {
                    entries.add(entry);
                }
            } catch (Exception exception) {
                log.error("Error occurred testing catalog entry: " + entry, exception);
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
     * Get configured filesystem services
     * @return List of active FilesystemService instances (empty list if none)
     */
    public static List<FilesystemService> getFilesystemServices() {
        //If alternate filesystem configured via plugin and active, return it
        Map<String, FilesystemService> fsMap = XDAT.getContextService().getBeansOfType(FilesystemService.class);
        List<FilesystemService> fsList = new ArrayList<>();
        if (fsMap == null) return fsList;
        for (FilesystemService fs : fsMap.values()) {
            if (fs.isActive()) {
                fsList.add(fs);
            }
        }
        return fsList;
    }

    /**
     * Does this XNAT instance have configured, active filesystem services?
     * @return T/F
     */
    public static boolean hasActiveExternalFilesystem() {
        return !getFilesystemServices().isEmpty();
    }

    /**
     * getFileOnLocalFileSystem will return the local file if it exists. If not, it will check
     * if any alternative filesystems have been configured via service and if so, try pulling the file from there.
     * If the input is a URL, we'll try to pull that, too.
     *
     * @param uri the uri
     * @param localPath the local path to put file, can be empty
     * @return File
     */
    @Nullable
    public static File getFileOnLocalFileSystem(String uri, String localPath) {
        File f = getFileOnLocalFileSystemOrig(uri);
        if (f == null) {
            //Try to pull from other filesystem if uri is a local path or a supported URL
            List<FilesystemService> fsList = getFilesystemServices();
            String protocol = UrlUtils.GetUrlProtocol(uri);
            for (FilesystemService fs : fsList) {
                if (protocol == null || fs.supportedUrlProtocols().contains(protocol)) {
                    f = fs.get(uri, localPath);
                    if (f != null) break;
                }
            }
            if (f == null && FileUtils.IsUrl(uri)) {
                f = UrlUtils.downloadUrl(uri, localPath);
            }
        }
        return f;
    }

    /**
     * See {@link #getFileOnLocalFileSystem(String, String)}, localPath set to empty string
     */
    public static File getFileOnLocalFileSystem(String fullPath) {
        return getFileOnLocalFileSystem(fullPath,"");
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
     * Download directory from remote filesystem
     * @param directoryPath absolute local path
     * @return success
     */
    public static boolean getRemoteDirectory(String directoryPath) {
        boolean success = true;
        List<FilesystemService> fsList = getFilesystemServices();
        for (FilesystemService fs : fsList) {
            //get from any filesystem that has it, will overwrite
            // note that "true" is returned if not on external system
            success &= fs.getDirectory(directoryPath);
        }
        return success;
    }

    /**
     * Copy directory on remote filesystem, nothing changes locally
     * @param currentPath current path (on local FS)
     * @param newPath new path (on local FS)
     * @return success
     */
    public static boolean copyRemoteDirectory(String currentPath, String newPath) {
        boolean success = true;
        List<FilesystemService> fsList = getFilesystemServices();
        for (FilesystemService fs : fsList) {
            //copy on all filesystems that (might) have it
            //note that "true" is returned if not on external system
            success &= fs.copyDirectory(currentPath, newPath);
        }
        return success;
    }

    /**
     * Delete directory from remote filesystem
     * @param directoryPath absolute local path
     */
    public static void deleteRemoteDirectory(String directoryPath) {
        List<FilesystemService> fsList = getFilesystemServices();
        for (FilesystemService fs : fsList) {
            //delete from all filesystems that (might) have it
            fs.deleteDirectory(directoryPath);
        }
    }

    /**
     * Delete files from remote filesystem
     * @param files list of local files
     */
    public static void deleteRemoteFile(List<File> files) {
        for (File file : files) {
            deleteRemoteFile(file, null);
        }
    }

    /**
     * Delete file from remote filesystem
     * @param file local file
     */
    public static void deleteRemoteFile(File file, String remoteUri) {
        List<FilesystemService> fsList = getFilesystemServices();
        for (FilesystemService fs : fsList) {
            //delete from all filesystems that (might) have it
            if (!fs.delete(file.getAbsolutePath(), remoteUri)) {
                log.warn("Error attempting to delete file " + file.getAbsolutePath() + " on " + fs);
            }
        }
    }

    /**
     * Move file on remote filesystem
     * @param oldPath original local path
     * @param newFile new file object
     */
    public static void moveRemoteFile(String oldPath, File newFile) {
        List<FilesystemService> fsList = CatalogUtils.getFilesystemServices();
        for (FilesystemService fs : fsList) {
            //move on any filesystems that have it
            if (!fs.move(oldPath, newFile)) {
                log.warn("Unable to move " + oldPath + " to " +
                        newFile.getAbsolutePath() + " on " + fs);
            }
        }
    }

    /**
     * Push file to remote filesystem based on URI
     * @param file the file object
     * @param remoteUri the remote URI
     * @return true if file pushed, false otherwise
     */
    public static boolean putRemoteFile(File file, String remoteUri) {
        if (!FileUtils.IsUrl(remoteUri, true)) return false;
        boolean pushed = false;
        List<FilesystemService> fsList = CatalogUtils.getFilesystemServices();
        for (FilesystemService fs : fsList) {
            if (fs.supportedUrlProtocols().contains(UrlUtils.GetUrlProtocol(remoteUri))) {
                if (pushed = fs.put(file, remoteUri)) break;
            }
        }
        return pushed;
    }

    public static void configureEntry(final CatEntryBean newEntry, final XnatResourceInfo info, boolean modified) {
        if (info.getDescription() != null) {
            newEntry.setDescription(info.getDescription());
        }
        if (info.getFormat() != null) {
            newEntry.setFormat(info.getFormat());
        }
        if (info.getContent() != null) {
            newEntry.setContent(info.getContent());
        }
        if (info.getTags().size() > 0) {
            for (final String entry : info.getTags()) {
                final CatEntryTagBean t = new CatEntryTagBean();
                t.setTag(entry);
                newEntry.addTags_tag(t);
            }
        }
        if (info.getMeta().size() > 0) {
            for (final Map.Entry<String, String> entry : info.getMeta().entrySet()) {
                final CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                meta.setName(entry.getKey());
                meta.setMetafield(entry.getValue());
                newEntry.addMetafields_metafield(meta);
            }
        }

        if (modified) {
            if (info.getUser() != null && newEntry.getModifiedby() == null) {
                newEntry.setModifiedby(info.getUser().getUsername());
            }
            if (info.getLastModified() != null) {
                newEntry.setModifiedtime(info.getLastModified());
            }
            if (info.getEvent_id() != null && newEntry.getModifiedeventid() == null) {
                newEntry.setModifiedeventid(info.getEvent_id().toString());
            }
        } else {
            if (info.getUser() != null && newEntry.getCreatedby() == null) {
                newEntry.setCreatedby(info.getUser().getUsername());
            }
            if (info.getCreated() != null && newEntry.getCreatedtime() == null) {
                newEntry.setCreatedtime(info.getCreated());
            }
            if (info.getEvent_id() != null && newEntry.getCreatedeventid() == null) {
                newEntry.setCreatedeventid(info.getEvent_id().toString());
            }
        }

    }

    public static void configureEntry(final XnatResource newEntry, final XnatResourceInfo info, final UserI user) throws Exception {
        if (info.getDescription() != null) {
            newEntry.setDescription(info.getDescription());
        }
        if (info.getFormat() != null) {
            newEntry.setFormat(info.getFormat());
        }
        if (info.getContent() != null) {
            newEntry.setContent(info.getContent());
        }
        if (info.getTags().size() > 0) {
            for (final String entry : info.getTags()) {
                final XnatAbstractresourceTag t = new XnatAbstractresourceTag(user);
                t.setTag(entry);
                newEntry.setTags_tag(t);
            }
        }
        if (info.getMeta().size() > 0) {
            for (final Map.Entry<String, String> entry : info.getMeta().entrySet()) {
                final XnatAbstractresourceTag t = new XnatAbstractresourceTag(user);
                t.setTag(entry.getValue());
                t.setName(entry.getKey());
                newEntry.setTags_tag(t);
            }
        }
    }

    public static List<String> storeCatalogEntry(final List<? extends FileWriterWrapperI> fileWriters, final String destination, final XnatResourcecatalog catResource, final XnatProjectdata proj, final boolean extract, final XnatResourceInfo info, final boolean overwrite, final EventMetaI ci) throws Exception {
        final File catFile = catResource.getCatalogFile(proj.getRootArchivePath());
        final String parentPath = catFile.getParent();
        final CatCatalogBean cat = catResource.getCleanCatalog(proj.getRootArchivePath(), false, null, null);

        List<String> duplicates = new ArrayList<>();

        for (FileWriterWrapperI fileWriter : fileWriters) {
            String filename = fileWriter.getName();

            int index = filename.lastIndexOf('\\');
            if (index < filename.lastIndexOf('/')) {
                index = filename.lastIndexOf('/');
            }

            if (index > 0) {
                filename = filename.substring(index + 1);
            }

            String compression_method = (filename.contains(".")) ? filename.substring(filename.lastIndexOf(".")) : "";

            if (extract && (compression_method.equalsIgnoreCase(".tar") || compression_method.equalsIgnoreCase(".gz") || compression_method.equalsIgnoreCase(".zip") || compression_method.equalsIgnoreCase(".zar"))) {
                if (log.isDebugEnabled()) {
                    log.debug("Found archive file " + filename);
                }

                File destinationDir = catFile.getParentFile();
                final InputStream is = fileWriter.getInputStream();

                ZipI zipper;
                if (compression_method.equalsIgnoreCase(".tar")) {
                    zipper = new TarUtils();
                } else if (compression_method.equalsIgnoreCase(".gz")) {
                    zipper = new TarUtils();
                    zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                } else {
                    zipper = new ZipUtils();
                }

                @SuppressWarnings("unchecked")
                final List<File> files = zipper.extract(is, destinationDir.getAbsolutePath(), overwrite, ci);
                try {
                    String content= org.apache.commons.io.FileUtils.readFileToString(catFile);

                    for (final File f : files) {
                        if (!f.isDirectory()) {
                            //relative path is used to compare to existing catalog entries, and add it if its missing.  entry paths are relative to the location of the catalog file.
                            final String relative = destinationDir.toURI().relativize(f.toURI()).getPath();

                            if (!checkEntryByURI(content,relative)) {
                                populateAndAddCatEntry(cat,relative,relative,f.getName(),info,f.length());
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(),e);
                }

                if (!overwrite) {
                    duplicates.addAll(zipper.getDuplicates());
                }
            } else {
                File parentFolder = new File(parentPath);
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

                final File saveTo = new File(parentFolder, instance);

                if (saveTo.exists() && !overwrite) {
                    duplicates.add(instance);
                } else {
                    if (saveTo.exists()) {
                        final CatEntryBean e = (CatEntryBean) getEntryByURI(cat, instance);
                        CatalogUtils.moveToHistory(catFile, saveTo, e, ci);
                    }

                    if (!saveTo.getParentFile().mkdirs() && !saveTo.getParentFile().exists()) {
                        throw new Exception("Failed to create required directory: " + saveTo.getParentFile().getAbsolutePath());
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Saving filename " + filename + " to file " + saveTo.getAbsolutePath());
                    }

                    fileWriter.write(saveTo);

                    if (saveTo.isDirectory()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found a directory: " + saveTo.getAbsolutePath());
                        }

                        @SuppressWarnings("unchecked")
                        final Iterator<File> iterator = org.apache.commons.io.FileUtils.iterateFiles(saveTo, null, true);
                        while (iterator.hasNext()) {
                            final File movedF = iterator.next();

                            String relativePath = instance + "/" + FileUtils.RelativizePath(saveTo, movedF).replace('\\', '/');
                            updateEntry(cat, relativePath, movedF, info, ci);
                        }

                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Updating catalog entry for file " + saveTo.getAbsolutePath());
                        }
                        updateEntry(cat, instance, saveTo, info, ci);
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Writing catalog file " + catFile.getAbsolutePath() + " with " + cat.getEntries_entry().size() + " total entries");
        }

        writeCatalogToFile(cat, catFile);

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

    public static void refreshAuditSummary(CatCatalogI cat, Map<String, Map<String, Integer>> audit_summary) {
        CatCatalogMetafieldI field = getAuditField(cat);
        if (audit_summary == null) {
            //TODO right now, this removes any "deleted" audit entries, perhaps it should build off of any existing audit tag
            //rebuild from each catalog entry
            audit_summary = buildAuditSummary(cat);
            field.setMetafield(convertAuditToString(audit_summary));
        } else {
            String prev_audit = StringUtils.defaultIfBlank(field.getMetafield(),"");
            String cur_audit = convertAuditToString(audit_summary);
            if (!prev_audit.isEmpty() && !cur_audit.isEmpty()) {
                prev_audit+="|";
            }
            field.setMetafield(prev_audit + cur_audit);
        }
    }

    public static Map<String, Map<String, Integer>> retrieveAuditySummary(CatCatalogI cat) {
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
            summary.put(key, new HashMap<String, Integer>());
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

    public static void writeCatalogToFile(CatCatalogI xml, File dest) throws Exception {
        try {
            writeCatalogToFile(xml, dest, getChecksumConfiguration());
        } catch (ConfigServiceException exception) {
            throw new Exception("Error attempting to retrieve checksum configuration", exception);
        }
    }

    public static void writeCatalogToFile(CatCatalogI xml, File dest, boolean calculateChecksums) throws Exception {
        writeCatalogToFile(xml, dest, calculateChecksums, null);
    }

    public static void writeCatalogToFile(CatCatalogI xml, File dest, boolean calculateChecksums,
                                          Map<String, Map<String, Integer>> audit_summary) throws Exception {

        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs() && !dest.getParentFile().exists()) {
                throw new IOException("Failed to create required directory: " + dest.getParentFile().getAbsolutePath());
            }
        }

        if (calculateChecksums) {
            CatalogUtils.calculateResourceChecksums(xml, dest);
        }

        refreshAuditSummary(xml, audit_summary);

        try {
            final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(dest, false);
            fl.tryLock(2L, TimeUnit.MINUTES);
            //log.trace(System.currentTimeMillis() + " writer start: " + fl.toString());
            try (final FileOutputStream fos = new FileOutputStream(dest)) {
                final OutputStreamWriter fw = new OutputStreamWriter(fos);
                xml.toXML(fw);
                fw.flush();
            } finally {
                fl.unlock();
                //log.trace(System.currentTimeMillis() + " writer finish: " + fl.toString());
            }
        } catch (Exception e) {
            log.error("Error writing catalog file", e);
            throw e;
        } finally {
            ThreadAndProcessFileLock.removeThreadAndProcessFileLock(dest);
        }
    }

    public static File getCatalogFile(final String rootPath, final XnatResourcecatalogI resource) {
        String fullPath = getFullPath(rootPath, resource);
        if (fullPath.endsWith("\\")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }


        File f = new File(fullPath);
        if (!f.exists()) {
            f = new File(fullPath + ".gz");
        }

        if (!f.exists()) {
            f = new File(fullPath);

            CatCatalogBean cat = new CatCatalogBean();
            if (resource.getLabel() != null) {
                cat.setId(resource.getLabel());
            } else {
                cat.setId("" + Calendar.getInstance().getTimeInMillis());
            }

            try {
                writeCatalogToFile(cat, f);
            } catch (IOException exception) {
                log.error("Error writing to the folder: " + f.getParentFile().getAbsolutePath(), exception);
            } catch (Exception exception) {
                log.error("Error creating the folder: " + f.getParentFile().getAbsolutePath(), exception);
            }
        }

        return f;
    }

    @Nullable
    public static CatCatalogBean getCatalog(File catalogFile) {
        if (!catalogFile.exists()) return null;
        InputStream inputStream = null;
        try {
            final ThreadAndProcessFileLock fl = ThreadAndProcessFileLock.getThreadAndProcessFileLock(catalogFile, true);
            fl.tryLock(2L, TimeUnit.MINUTES);
            //log.trace(System.currentTimeMillis() + " reader start: " + fl.toString());
            try (FileInputStream fis = new FileInputStream(catalogFile)) {
                if (catalogFile.getName().endsWith(".gz")) {
                    inputStream = new GZIPInputStream(fis);
                } else {
                    inputStream = fis;
                }

                XDATXMLReader reader = new XDATXMLReader();
                BaseElement base = reader.parse(inputStream);
                if (base instanceof CatCatalogBean) {
                    return (CatCatalogBean) base;
                }
            } catch (FileNotFoundException exception) {
                log.error("Couldn't find file: {}", catalogFile, exception);
            } catch (IOException exception) {
                log.error("Error occurred reading file: {}", catalogFile, exception);
            } catch (SAXException exception) {
                log.error("Error processing XML in file: {}", catalogFile, exception);
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
                fl.unlock();
                //log.trace(System.currentTimeMillis() + " reader finish: " + fl.toString());
            }
        } catch (IOException e) {
            log.error("Unable to obtain read lock for file: {}", catalogFile, e);
        } finally {
            ThreadAndProcessFileLock.removeThreadAndProcessFileLock(catalogFile);
        }

        return null;
    }

    /**
     * Parses catalog xml for resource and returns the Bean object.  Returns null if not found.
     *
     * @param rootPath The root path for the catalog.
     * @param resource The resource catalog.
     * @return The initialized catalog bean.
     */
    public static CatCatalogBean getCatalog(String rootPath, XnatResourcecatalogI resource) {
        File catalogFile = null;
        try {
            catalogFile = CatalogUtils.getCatalogFile(rootPath, resource);
            if (catalogFile.getName().endsWith(".gz")) {
                FileUtils.GUnzipFiles(catalogFile);
                catalogFile = CatalogUtils.getCatalogFile(rootPath, resource);
            }
        } catch (FileNotFoundException exception) {
            log.error("Couldn't find file: " + catalogFile, exception);
        } catch (IOException exception) {
            log.error("Error occurred reading file: " + catalogFile, exception);
        } catch (Exception exception) {
            log.error("Unknown exception reading file at: " + rootPath, exception);
        }

        return catalogFile != null ? getCatalog(catalogFile) : null;
    }

    public static CatCatalogBean getCleanCatalog(String rootPath, XnatResourcecatalogI resource, boolean includeFullPaths) {
        return getCleanCatalog(rootPath, resource, includeFullPaths, null, null);
    }

    public static CatCatalogBean getCleanCatalog(String rootPath, XnatResourcecatalogI resource,
                                                 boolean includeFullPaths, UserI user, EventMetaI c) {
        File catalogFile = handleCatalogFile(rootPath, resource);
        CatCatalogBean cat = getCatalog(catalogFile);

        if (cat != null) {
            String parentPath = catalogFile.getParent();
            formalizeCatalog(cat, parentPath, user, c);
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
     * @param user    User in operation
     * @param now     Corresponding event
     * @return true if catalog was modified, otherwise false
     */
    public static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, UserI user, EventMetaI now) {
        return formalizeCatalog(cat, catPath, user, now, false, false);
        //default to false for checksums for now.  Maybe it should use the default setting for the server.  But, this runs everytime a catalog xml is loaded.  So, it will get re-run over and over.  Not sure we want to add that amount of processing.
    }

    /**
     * Reviews existing catalog and adds any missing fields
     *
     * @param cat                Catalog entry to be cleaned
     * @param catPath            Path to catalog file (used to access files with relative paths).
     * @param user               User in operation
     * @param now                Corresponding event
     * @param createChecksums    Boolean whether or not to generate checksums (if missing)
     * @param removeMissingFiles Boolean whether or not to delete references to missing files
     * @return true if catalog was modified, otherwise false
     */
    public static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, UserI user, EventMetaI now, boolean createChecksums, boolean removeMissingFiles) {
        return formalizeCatalog(cat, catPath, "", user, now, createChecksums, removeMissingFiles);
    }

    public static String getFullPath(String rootPath, XnatResourcecatalogI resource) {

        String fullPath = StringUtils.replace(FileUtils.AppendRootPath(rootPath, resource.getUri()), "\\", "/");
        while (fullPath.contains("//")) {
            fullPath = StringUtils.replace(fullPath, "//", "/");
        }

        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }

        return fullPath;
    }

    @SuppressWarnings("unused")
    public boolean modifyEntry(CatCatalogI cat, CatEntryI oldEntry, CatEntryI newEntry) {
        for (int i = 0; i < cat.getEntries_entry().size(); i++) {
            CatEntryI e = cat.getEntries_entry().get(i);
            if (e.getUri().equals(oldEntry.getUri())) {
                cat.getEntries_entry().remove(i);
                cat.getEntries_entry().add(newEntry);
                return true;
            }
        }

        for (CatCatalogI subset : cat.getSets_entryset()) {
            if (modifyEntry(subset, oldEntry, newEntry)) {
                return true;
            }
        }

        return false;
    }

    public static List<File> findHistoricalCatFiles(File catFile) {
        final List<File> files = new ArrayList<>();

        final File historyDir = FileUtils.BuildHistoryParentFile(catFile);

        final String name = catFile.getName();

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                return (arg1.equals(name));
            }
        };

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
            _maintainFileHistory = XDAT.getBoolSiteConfigurationProperty("audit.maintain-file-history", false);
        }
        return _maintainFileHistory;
    }

    public static void moveToHistory(File catFile, File f, CatEntryBean entry, EventMetaI ci) throws Exception {
        //move existing file to audit trail
        if (CatalogUtils.maintainFileHistory()) {
            final File newFile = FileUtils.MoveToHistory(f, EventUtils.getTimestamp(ci));
            addCatHistoryEntry(catFile, newFile.getAbsolutePath(), entry, ci);
        }
    }

    public static void addCatHistoryEntry(File catFile, String f, CatEntryBean entry, EventMetaI ci) throws Exception {
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

        File newCatFile = FileUtils.BuildHistoryFile(catFile, EventUtils.getTimestamp(ci));
        CatCatalogBean newCat;
        if (newCatFile.exists()) {
            newCat = CatalogUtils.getCatalog(newCatFile);
        } else {
            newCat = new CatCatalogBean();
        }

        if (newCat == null) {
            throw new Exception("Catalog bean corresponding to " + newCatFile.getAbsolutePath() + " is null, " +
                    "have your admin check utils.log for the cause");
        }
        newCat.addEntries_entry(newEntryBean);

        CatalogUtils.writeCatalogToFile(newCat, newCatFile);
    }

    public static XFTTable populateTable(XFTTable table, UserI user, XnatProjectdata proj, boolean cacheFileStats) {
        XFTTable newTable = new XFTTable();
        String[] fields = {"xnat_abstractresource_id", "label", "element_name", "category", "cat_id", "cat_desc", "file_count", "file_size", "tags", "content", "format"};
        newTable.initTable(fields);
        table.resetRowCursor();
        while (table.hasMoreRows()) {
            Object[] old = table.nextRow();
            Object[] _new = new Object[11];
            if (log.isDebugEnabled()) {
                log.debug("Found resource with ID: " + old[0] + "(" + old[1] + ")");
            }
            _new[0] = old[0];
            _new[1] = old[1];
            _new[2] = old[2];
            _new[3] = old[3];
            _new[4] = old[4];
            _new[5] = old[5];

            XnatAbstractresource res = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(old[0], user, false);
            if (res == null) {
                log.error("XnatAbstractresource {} is null", old[0]);
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
                        log.error("Failed to save updates to resource catalog: " + res.getLabel(), exception);
                    } else {
                        log.error("Failed to save updates to abstract resource: " + res.getXnatAbstractresourceId(), exception);
                    }
                }
            }else{
                if(!XDAT.getBoolSiteConfigurationProperty("uiShowCachedFileCounts", false)){
                    res.setFileCount(res.getCount(proj.getRootArchivePath()));
                    res.setFileSize(res.getSize(proj.getRootArchivePath()));
                }else{
                    if (res.getFileCount() == null) {
                        res.setFileCount(res.getCount(proj.getRootArchivePath()));
                    }
                    if (res.getFileSize() == null) {
                        res.setFileSize(res.getSize(proj.getRootArchivePath()));
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

    public static CatEntryBean populateAndAddCatEntry(CatCatalogBean cat, String uri, String id, String fname,
                                                      XnatResourceInfo info, long size) {
        return populateAndAddCatEntry(cat, uri, id, fname, info, size, null);
    }

    public static CatEntryBean populateAndAddCatEntry(CatCatalogBean cat, String uri, String id, String fname,
                                                      XnatResourceInfo info, long size, String relativePath) {
        CatEntryBean newEntry = new CatEntryBean();
        newEntry.setUri(uri);
        newEntry.setName(fname);
        newEntry.setId(id);
        if (StringUtils.isNotBlank(relativePath)) {
            newEntry.setCachepath(relativePath);
        }
        setMetaFieldByName(newEntry, SIZE_METAFIELD, Long.toString(size));
        configureEntry(newEntry, info, false);
        cat.addEntries_entry(newEntry);
        return newEntry;
    }

    private static void updateEntry(CatCatalogBean cat, String dest, File f, XnatResourceInfo info, EventMetaI ci) {
        final CatEntryBean e = (CatEntryBean) getEntryByURI(cat, dest);

        if (e == null) {
            populateAndAddCatEntry(cat,dest,dest,f.getName(),info, f.length());
        } else {
            if (ci != null) {
                if (ci.getUser() != null)
                    e.setModifiedby(ci.getUser().getUsername());
                e.setModifiedtime(ci.getEventDate());
                if (ci.getEventId() != null) {
                    e.setModifiedeventid(ci.getEventId().toString());
                }
            }
        }
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

    private static Map<String, Map<String, Integer>> convertAuditToMap(String audit) {
        Map<String, Map<String, Integer>> summary = new HashMap<>();
        for (String changeSet : audit.split("|")) {
            String[] split1 = changeSet.split("=");
            if (split1.length > 1) {
                String key = split1[0];
                Map<String, Integer> counts = new HashMap<>();
                for (String operation : split1[1].split(";")) {
                    String[] entry = operation.split(":");
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
            addAuditEntry(summary, entry.getCreatedeventid(), entry.getCreatedtime(), ChangeSummaryBuilderA.ADDED, 1);

            if (entry.getModifiedtime() != null) {
                addAuditEntry(summary, entry.getModifiedeventid(), entry.getModifiedtime(), ChangeSummaryBuilderA.MODIFIED, 1);
            }
        }
    }

    private static File handleCatalogFile(final String rootPath, final XnatResourcecatalogI resource) {
        File catalog = CatalogUtils.getCatalogFile(rootPath, resource);
        if (catalog.getName().endsWith(".gz")) {
            try {
                FileUtils.GUnzipFiles(catalog);
                catalog = CatalogUtils.getCatalogFile(rootPath, resource);
            } catch (FileNotFoundException exception) {
                log.error("Couldn't find file: " + catalog, exception);
            } catch (IOException exception) {
                log.error("Error occurred reading file: " + catalog, exception);
            }
        }
        return catalog;
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
    @SuppressWarnings("unchecked")
    public static boolean addUnreferencedFiles(final File catFile, final CatCatalogI cat, final UserI user, final Number event_id) {
        //list of all files in the catalog folder
        final Collection<File> files = org.apache.commons.io.FileUtils.listFiles(catFile.getParentFile(), null, true);

        //verify that there is only one catalog xml in this directory
        //fail if more then one is present -- otherwise they will be merged.
        for (final File f : files) {
            if (!f.equals(catFile)) {
                if (f.getName().endsWith(".xml") && isCatalogFile(f)) {
                    return false;
                }
            }
        }

        //URI object for the catalog folder (used to generate relative file paths)
        final URI catFolderURI = catFile.getParentFile().toURI();

        final Date now = Calendar.getInstance().getTime();

        boolean modified = false;

        try {
            String content= org.apache.commons.io.FileUtils.readFileToString(catFile);
            final XnatResourceInfo info = XnatResourceInfo.buildResourceInfo(null, null, null, null, user, now, now, event_id);
            for (final File f : files) {
                if (!f.equals(catFile)) {//don't add the catalog xml to its own list
                    //relative path is used to compare to existing catalog entries, and add it if its missing.  entry paths are relative to the location of the catalog file.
                    final String relative = catFolderURI.relativize(f.toURI()).getPath();

                    if (!checkEntryByURI(content,relative)) {
                        populateAndAddCatEntry((CatCatalogBean) cat,relative,relative,f.getName(),info,f.length());
                        modified = true;
                    }

                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }

        return modified;
    }

    public static boolean isCatalogFile(File f) {
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
     * @return true if the cat was modified (and needs to be saved).
     */
    public static List<String> getUnreferencedFiles(final File catFolder) {
        final List<String> unreferenced = Lists.newArrayList();

        //list of all files in the catalog folder
        @SuppressWarnings("unchecked")
        final String[] files = catFolder.list();

        //identify the catalog XMLs in this folder
        final List<CatCatalogI> catalogs = Lists.newArrayList();
        if (files != null) {
            for (final String filename : files) {
                if (filename.endsWith(".xml")) {
                    File f = new File(catFolder, filename);
                    if (isCatalogFile(f)) {
                        CatCatalogI cat = CatalogUtils.getCatalog(f);
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
                if (!(f.endsWith(".xml"))) {//ignore catalog files
                    if (!cataloged.remove(f)) {
                        unreferenced.add(f);
                    }
                }
            }
        }

        return unreferenced;
    }

    private static boolean formalizeCatalog(final CatCatalogI cat, final String catPath, String header, UserI user, EventMetaI now, final boolean createChecksum, final boolean removeMissingFiles) {
        boolean modified = false;

        for (CatCatalogI subSet : cat.getSets_entryset()) {
            if (formalizeCatalog(subSet, catPath, FileUtils.AppendSlash(header,"") + subSet.getId(), user, now, createChecksum, removeMissingFiles)) {
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
                if (CatalogUtils.setChecksum(entry, catPath)) {
                    modified = true;
                }
            }

            if (StringUtils.isEmpty(entry.getId()) || removeMissingFiles) {
                File f = getFile(entry, catPath);
                if (f != null && StringUtils.isEmpty(entry.getId())) {
                    entry.setId(Paths.get(catPath).relativize(f.toPath()).toString());
                    modified = true;
                } else if (f == null) {
                    if (removeMissingFiles) {
                        toRemove.add(entry);
                        modified = true;
                    } else {
                        log.error("Missing Resource:" + entry.getUri());
                    }
                }
            }

            if (entry.getClass().equals(CatDcmentryBean.class)) { // CatDcmentryBeans fail to set format correctly because it's not in their xml
                entry.setFormat("DICOM");
            }
        }

        if (toRemove.size() > 0) {
            for (CatEntryI entry : toRemove) {
                CatalogUtils.removeEntry(cat, entry);
            }
        }

        return modified;
    }

    private static Boolean _maintainFileHistory = null;
    private static Boolean _checksumConfig = null;
}
