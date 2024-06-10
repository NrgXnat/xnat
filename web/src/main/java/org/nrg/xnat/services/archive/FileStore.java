package org.nrg.xnat.services.archive;

import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnat.entities.FileStoreInfo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides functions to manage files outside of the standard XNAT project structure.
 */
public interface FileStore {
    /**
     * Creates a new file-store entry with the specified coordinates using the contents of the submitted input stream.
     * If the specified coordinates already exist, this method throws {@link ResourceAlreadyExistsException}. Otherwise
     * it returns the {@link FileStoreInfo} of the newly created file-store record.
     *
     * @param inputStream A stream of data for the file contents.
     * @param coordinates The coordinates for storing the file.
     *
     * @return {@link FileStoreInfo} for the newly created file-store record.
     *
     * @throws ResourceAlreadyExistsException Thrown when a resource with the specified coordinates already exists.
     */
    FileStoreInfo create(final InputStream inputStream, final String... coordinates) throws ResourceAlreadyExistsException;

    /**
     * Gets a file based on the ID of the file in the service.
     *
     * @param storeId The ID of the file in the service.
     *
     * @return An input stream with which to access the file contents.
     *
     * @throws NotFoundException Thrown when a resource with the specified ID doesn't exist.
     */
    InputStream open(final long storeId) throws NotFoundException, IOException;

    /**
     * Gets a file based on the coordinates used to store the file.
     *
     * @param coordinates The coordinates used to store the desired file.
     *
     * @return An input stream with which to access the file contents.
     *
     * @throws NotFoundException Thrown when a resource with the specified coordinates doesn't exist.
     */
    InputStream open(final String... coordinates) throws NotFoundException, IOException;

    /**
     * Updates the contents of the file-store entry with the specified store ID from the submitted input stream. If a
     * file-store entry with the specified store ID doesn't exist, this method throws {@link NotFoundException}.
     * Otherwise it returns the {@link FileStoreInfo} of the updated file-store record.
     *
     * @param inputStream A stream of data for the file contents.
     * @param storeId     The coordinates for storing the file.
     *
     * @return {@link FileStoreInfo} for the updated file-store record.
     *
     * @throws NotFoundException Thrown when a resource with the specified ID doesn't exist.
     */
    FileStoreInfo update(final InputStream inputStream, final long storeId) throws NotFoundException;

    /**
     * Updates the contents of the file-store entry with the specified coordinates from the submitted input stream. If a
     * file-store entry with the specified coordinates doesn't exist, this method throws {@link NotFoundException}.
     * Otherwise it returns the {@link FileStoreInfo} of the updated file-store record.
     *
     * @param inputStream A stream of data for the file contents.
     * @param coordinates The coordinates for storing the file.
     *
     * @return {@link FileStoreInfo} for the updated file-store record.
     *
     * @throws NotFoundException Thrown when a resource with the specified coordinates doesn't exist.
     */
    FileStoreInfo update(final InputStream inputStream, final String... coordinates) throws NotFoundException;

    /**
     * Deletes a file based on the ID of the file in the service.
     *
     * @param storeId The ID of the file in the service.
     */
    void delete(final long storeId);

    /**
     * Deletes a file based on the coordinates used to store the file.
     *
     * @param coordinates The coordinates used to store the desired file.
     *
     * @throws NotFoundException Thrown when a resource with the specified coordinates doesn't exist.
     */
    void delete(final String... coordinates) throws NotFoundException;

    /**
     * Deletes a file based on the full {@link FileStoreInfo} used to store the file.
     *
     * @param info The {@link FileStoreInfo} used to store the desired file.
     */
    void delete(final FileStoreInfo info) throws NotFoundException;

    FileStoreInfo getFileStoreInfo(final long storeId) throws NotFoundException;

    FileStoreInfo getFileStoreInfo(final String... coordinates) throws NotFoundException;
}
