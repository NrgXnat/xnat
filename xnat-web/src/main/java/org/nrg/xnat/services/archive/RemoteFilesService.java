package org.nrg.xnat.services.archive;

import org.nrg.action.ServerException;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public interface RemoteFilesService {

    /**
     * Retrieves the file indicated by url, and saves it to destinationPath
     *
     * @param url may be a URL or may be an absolute local path
     * @param localPath absolute local path within archive where file would be located
     * @param destinationPath absolute local path to which file ought to be saved
     * @return File or null if url isn't accessible from any configured remote filesystem
     */
    @Nullable
    File pullFile(String url, String localPath, String destinationPath);

    /**
     * Does the catalog resource have remote files?
     *
     * @param resource the resource
     * @return T/F
     */
    boolean catalogHasRemoteFiles(final XnatResourcecatalogI resource);

    /**
     * Blocking pull item's files to destination.
     * @param item              the security item
     * @param resources         list of item's resources to pull (to obtain all resources for item, use resourceURI.getResources(true))
     * @param user              the user
     * @param itemDestPath      the destination path equivalent of item's archive path, null to use item's archive path
     * @throws ServerException  if remote resources aren't able to be pulled
     */
    void pullItem(final ArchivableItem item,
                  final List<XnatAbstractresourceI> resources,
                  final UserI user,
                  @Nullable String itemDestPath) throws ServerException;
}
