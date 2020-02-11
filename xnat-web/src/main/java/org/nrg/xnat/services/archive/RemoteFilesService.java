package org.nrg.xnat.services.archive;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.UnsupportedRemoteFilesOperationException;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.springframework.core.io.InputStreamSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public interface RemoteFilesService {

    /**
     * Retrieves the file indicated by url, and saves it to destinationPath
     *
     * @param url may be a URL or may be an absolute local path
     * @param destinationPath absolute local path to which file ought to be saved
     * @param project the project or null if unknown
     * @return File
     * @throws FileNotFoundException if url isn't accessible from any configured remote filesystem (use FNF because it doesn't require loading exceptions from the plugin)
     */
    @Nonnull
    File pullFile(String url, String destinationPath, @Nullable String project) throws FileNotFoundException;

    /**
     * Is the url accessible?
     *
     * @param url the url
     * @param project the project or null if unknown
     * @return T/F if we can HEAD the url
     */
    boolean canPullFile(String url, @Nullable String project);

    /**
     * Delete file at url on remote filesystem
     *
     * @param url the url
     * @param project the project or null if unknown
     * @return true if file successfully deleted
     */
    boolean deleteFile(String url, @Nullable String project);

    /**
     * Does the catalog resource have remote files?
     *
     * @param resource the resource
     * @return T/F
     */
    boolean catalogHasRemoteFiles(final XnatResourcecatalog resource);

    /**
     * Blocking pull item's files to destination.
     * @param item                  the security item
     * @param resources             list of item's resources to pull (to obtain all resources for item, use resourceURI.getResources(true))
     * @param archiveRelativeDir    the XNAT archive-relative directory or null to use expected current directory for item
     * @param destinationDir        the destination path equivalent of archiveRelativeDir, null to use archiveRelativeDir
     * @throws ServerException      if remote resources aren't able to be pulled
     */
    void pullItem(final ArchivableItem item,
                  final List<XnatAbstractresourceI> resources,
                  @Nullable String archiveRelativeDir,
                  @Nullable String destinationDir) throws ServerException;

    /**
     * Push files to remote filesystem and add URLs to catalog
     * @param user          The user
     * @param item          The item whose security settings we'll use to allow/deny the operation
     * @param catalog       The catalog resource
     * @param files         Collection of files/dirs to push
     * @param preserveDirectories   If true, files are added to catalog with relative directory paths; if false,
     *                              we attmept to use only names
     * @param parentEventId Nullable eventId for parent workflow
     * @throws ClientException for invalid request
     * @throws ServerException if resources cannot be pushed to remote
     * @throws UnsupportedRemoteFilesOperationException if project doesn't support remote resources
     */
    void pushProcessingOutputsAndAddUrlsToCatalog(final UserI user, final ArchivableItem item,
                                                  final XnatResourcecatalog catalog, Map<String, ? extends InputStreamSource> files,
                                                  final boolean preserveDirectories, @Nullable Integer parentEventId)
            throws ClientException, ServerException, UnsupportedRemoteFilesOperationException;
}
