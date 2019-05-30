package org.nrg.xnat.services.archive;

import org.nrg.action.ServerException;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

public interface FilesystemService {
    String separator = File.separator;

    /**
     * Is the remote filesystem service configured and active? (needed to differentiate from just having plugin installed)
     *
     * @return T/F
     */
    boolean isActive();

    /**
     * If filesystem needs to be activated via preferences (and reactivated if these change), use this method.
     *
     * @param preferences the fs service preference bean
     */
    void activate(AbstractPreferenceBean preferences);

    /**
     * Does the filesystem service support the provided url
     * @param url the url
     * @return T/F
     */
    boolean supportsUrl(String url);

    /**
     * Retrieves an input stream for the file indicated by url
     *
     * @param url may be a URL or may be an absolute local path
     * @return File
     * @throws Exception if issue
     */
    InputStream getInputStream(String url) throws Exception;

    /**
     * Retrieves the file indicated by url, and saves it to destinationPath
     *
     * @param url may be a URL or may be an absolute local path
     * @param localPath absolute local path within archive where file would be located
     * @param destinationPath absolute local path to which file ought to be saved
     * @return File
     */
    File pullFile(String url, String localPath, String destinationPath);

    /**
     * Archives the file to remote filesystem
     *
     * @param file the file to archive
     * @param url the remote url
     * @return boolean for successfully pushed
     */
    boolean pushFile(File file, String url);

    /**
     * Blocking pull resource files to destination
     * @param resource          the resource
     * @param item              the security item for the resource
     * @param itemArchivePath   the archive path for the security item of the resource
     * @param parentDestPath    the destination path
     * @param user              the user
     * @return true if pulled, false otherwise (aka if resource isn't archived with this filesystem)
     * @throws ServerException exceptions from remote FS API
     */
    boolean pullResource(final XnatResourcecatalogI resource,
                         final ArchivableItem item,
                         final String itemArchivePath,
                         final String parentDestPath,
                         final UserI user) throws ServerException;

    /**
     * Non-blocking pull resource files to destination
     * @param resource          the resource
     * @param item              the security item for the resource
     * @param itemArchivePath   the archive path for the security item of the resource
     * @param parentDestPath    the destination path
     * @param user              the user
     * @return pull tracking object
     * @throws ServerException exceptions from remote FS API
     */
    Object initiatePullResource(final XnatResourcecatalogI resource,
                                          final ArchivableItem item,
                                          final String itemArchivePath,
                                          final String parentDestPath,
                                          final UserI user) throws ServerException;

    /**
     * Poll progress of non-blocking download of directory from remote filesystem
     *
     * @param progressTracker object needed for tracking pull progress or null
     * @return bytes downloaded out of total as percentage, -1 for failure, 101 for completed
     * @throws ServerException exceptions from remote FS API
     */
    double pollPullResource(Object progressTracker) throws ServerException;

    /**
     * Does the filesystem have remote files for this resource?
     * @param resource  the resource
     * @return T/F
     */
    boolean containsFilesForResource(final XnatResourcecatalogI resource);

    /**
     * Retrieves the metadata for the file indicated url
     *
     * @param url may be a URL or may be an absolute local path
     * @param catalogPath is parent path of catalog file (used to determine local path)
     * @return CatalogUtils.CatalogEntryAttributes
     */
    @Nullable
    CatalogUtils.CatalogEntryAttributes getMetadata(String url, String catalogPath);

    /**
     * Make a URI on the remote filesystem based on local absolute path
     * @param absoluteLocalPath the local path
     * @return the remote URI
     */
    String makeUriFromLocal(String absoluteLocalPath);
}
