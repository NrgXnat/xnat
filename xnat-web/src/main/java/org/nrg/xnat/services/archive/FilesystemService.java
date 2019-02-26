package org.nrg.xnat.services.archive;

import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.List;

@Service
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
     * @param preferences
     */
    void activate(AbstractPreferenceBean preferences);

    /**
     * Retrieves the file indicated by inputPath (URL or local path)
     *
     * @param inputPath may be a URL or may be an absolute local path
     * @return File
     * @throws Exception if issue
     */
    InputStream getInputStream(String inputPath) throws Exception;

    /**
     * Retrieves the file indicated by inputPath (URL or local path)
     *
     * @param inputPath may be a URL or may be an absolute local path
     * @return File
     */
    File get(String inputPath);

    /**
     * Retrieves the file indicated by inputPath (URL or local path), and saves it to destinationPath
     *
     * @param inputPath may be a URL or may be an absolute local path
     * @param destinationPath absolute local path to which file ought to be saved
     * @return File
     */
    File get(String inputPath, String destinationPath);

    /**
     * Archives the file to remote filesystem
     *
     * @param file the file to archive
     * @return boolean for success
     */
    boolean put(File file);

    /**
     * Archives the file to remote filesystem based on remote URI, not local path
     *
     * @param file the file to archive
     * @param remoteUri the remote URI
     * @return boolean for successfully pushed
     */
    boolean put(File file, String remoteUri);

    /**
     * Deletes the file (indicated by a local absolutePath) on remote filesystem
     *
     * @param absolutePath absolute local path
     * @return true if file successfully deleted OR file doesn't exist
     */
    boolean delete(String absolutePath);

    /**
     * Deletes the remoteURI (corresponding to local absolutePath) on remote filesystem
     *
     * @param absolutePath absolute local path
     * @param remoteUri the remote URI
     * @return true if file successfully deleted OR file doesn't exist
     */
    boolean delete(String absolutePath, String remoteUri);

    /**
     * Moves the file on remote filesystem. Not supported for full URL catalog entries.
     *
     * @param originalPath old absolute local path
     * @param newFile moved file (already moved locally)
     * @return success if file successfully moved OR file not on that filesystem
     */
    boolean move(String originalPath, File newFile);

    /**
     * Retrieves the directory indicated by directoryPath. Should NOT overwrite local files.
     *
     * @param directoryPath absolute local path
     * @return boolean for success OR doesn't exist
     */
    boolean getDirectory(String directoryPath);

    /**
     * Copies the directory to new location on remote filesystem, doesn't change local filesystem
     *
     * @param currentPath current absolute local path
     * @param newPath new absolute local path
     * @return boolean for success OR doesn't exist
     */
    boolean copyDirectory(String currentPath, String newPath);

    /**
     * Deletes the directory (indicated by a local absolutePath) on remote filesystem
     *
     * @param directoryPath absolute local path
     */
    void deleteDirectory(String directoryPath);

    /**
     * Returns a list of supported URL protocols or null if none
     *
     * @return list of URL protocols that can be passed into the get method for this filesystem service
     */
    List<String> supportedUrlProtocols();

    /**
     * Retrieves the metadata for the file indicated inputPath (URL or local path)
     *
     * @param inputPath may be a URL or may be an absolute local path
     * @return CatalogUtils.CatalogEntryAttributes
     */
    CatalogUtils.CatalogEntryAttributes getMetadata(String inputPath);

    /**
     * Retrieves the metadata for the file indicated inputPath (URL or local path)
     *
     * @param inputPath may be a URL or may be an absolute local path
     * @param catalogPath is parent path of catalog file (used to set ID relative to catalogPath)
     * @return CatalogUtils.CatalogEntryAttributes
     */
    CatalogUtils.CatalogEntryAttributes getMetadata(String inputPath, String catalogPath);

    /**
     * Returns a list of all files AND DIRECTORIES within root, <strong>directories must end with "/"</strong>
     * @param root  the topmost directory
     * @return      list of subdirs & files
     */
    List<String> listAllFiles(@Nullable String root);
}
