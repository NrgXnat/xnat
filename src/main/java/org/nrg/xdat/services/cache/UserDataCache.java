package org.nrg.xdat.services.cache;

import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Represents a cache space where users can store and access working files.
 */
public interface UserDataCache extends XnatCache {
    /**
     * Options for file operations.
     */
    enum Options {
        Folder,
        Overwrite,
        Append,
        DeleteOnExit
    }

    /**
     * Returns the location of the specified user's cache folder.
     *
     * @param user The user object.
     *
     * @return The path to the user's cache folder.
     *
     * @see #getUserDataCache(String)
     * @see #getUserDataCache(int)
     */
    @Nonnull
    Path getUserDataCache(final @Nonnull UserI user);

    /**
     * Returns the location of the specified user's cache folder.
     *
     * @param username The username.
     *
     * @return The path to the user's cache folder.
     *
     * @throws UserNotFoundException When the submitted username doesn't match a user on the XNAT system.
     * @see #getUserDataCache(UserI)
     * @see #getUserDataCache(int)
     */
    @Nonnull
    Path getUserDataCache(final @Nonnull String username) throws UserNotFoundException;

    /**
     * Returns the location of the specified user's cache folder.
     *
     * @param userId The ID of the user.
     *
     * @return The path to the user's cache folder.
     *
     * @see #getUserDataCache(UserI)
     * @see #getUserDataCache(String)
     */
    @Nonnull
    Path getUserDataCache(final int userId);

    /**
     * Returns a file object for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * If the specified path is hierarchical, e.g. foo/bar.txt, all folders are created under the user's cache folder if they
     * don't already exist. If the {@link Options#Folder} option is specified, all elements in the path are created,
     * but if not the <b>path</b> is considered to be a file, in which case the file itself is not created if it does not
     * already exist.
     *
     * @param user    The user object.
     * @param path    The path to the requested file.
     * @param options Any {@link Options options} required for the operation.
     *
     * @return A file object representing the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @see #getUserDataCacheFile(String, Path, Options...)
     * @see #getUserDataCacheFile(int, Path, Options...)
     */
    @Nonnull
    File getUserDataCacheFile(final @Nonnull UserI user, final @Nonnull Path path, final Options... options) throws IllegalArgumentException;

    /**
     * Returns a file object for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * If the specified path is hierarchical, e.g. foo/bar.txt, all folders are created under the user's cache folder if they
     * don't already exist. If the {@link Options#Folder} option is specified, all elements in the path are created,
     * but if not the <b>path</b> is considered to be a file, in which case the file itself is not created if it does not
     * already exist.
     *
     * @param username The username.
     * @param path     The path to the requested file.
     * @param options  Any {@link Options options} required for the operation.
     *
     * @return A file object representing the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws UserNotFoundException    When the submitted username doesn't match a user on the XNAT system.
     * @see #getUserDataCacheFile(UserI, Path, Options...)
     * @see #getUserDataCacheFile(int, Path, Options...)
     */
    @Nonnull
    File getUserDataCacheFile(final @Nonnull String username, final @Nonnull Path path, final Options... options) throws IllegalArgumentException, UserNotFoundException;

    /**
     * Returns a file object for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * If the specified path is hierarchical, e.g. foo/bar.txt, all folders are created under the user's cache folder if they
     * don't already exist. If the {@link Options#Folder} option is specified, all elements in the path are created,
     * but if not the <b>path</b> is considered to be a file, in which case the file itself is not created if it does not
     * already exist.
     *
     * @param userId  The ID of the user.
     * @param path    The path to the requested file.
     * @param options Any {@link Options options} required for the operation.
     *
     * @return A file object representing the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @see #getUserDataCacheFile(UserI, Path, Options...)
     * @see #getUserDataCacheFile(String, Path, Options...)
     */
    @Nonnull
    File getUserDataCacheFile(final int userId, final @Nonnull Path path, final Options... options) throws IllegalArgumentException;

    /**
     * Returns an output stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file: if the value is hierarchical, e.g. foo/bar.txt, the specified folders are
     * created under the user's cache folder if they don't already exist. However, the file itself is not created if it
     * does not already exist. The {@link Options#Folder} option is ignored for this method.
     *
     * @param user    The user object.
     * @param path    The path to the requested file.
     * @param options Any {@link Options options} required for the operation.
     *
     * @return An output stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @see #openUserDataCacheFileForWrite(String, Path, Options...)
     * @see #openUserDataCacheFileForWrite(int, Path, Options...)
     */
    @Nonnull
    OutputStream openUserDataCacheFileForWrite(final @Nonnull UserI user, final @Nonnull Path path, final Options... options) throws IllegalArgumentException;

    /**
     * Returns an output stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file: if the value is hierarchical, e.g. foo/bar.txt, the specified folders are
     * created under the user's cache folder if they don't already exist. However, the file itself is not created if it
     * does not already exist. The {@link Options#Folder} option is ignored for this method.
     *
     * @param username The username.
     * @param path     The path to the requested file.
     * @param options  Any {@link Options options} required for the operation.
     *
     * @return An output stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws UserNotFoundException    When the submitted username doesn't match a user on the XNAT system.
     * @see #openUserDataCacheFileForWrite(UserI, Path, Options...)
     * @see #openUserDataCacheFileForWrite(int, Path, Options...)
     */
    @Nonnull
    OutputStream openUserDataCacheFileForWrite(final @Nonnull String username, final @Nonnull Path path, final Options... options) throws IllegalArgumentException, UserNotFoundException;

    /**
     * Returns an output stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file: if the value is hierarchical, e.g. foo/bar.txt, the specified folders are
     * created under the user's cache folder if they don't already exist. However, the file itself is not created if it
     * does not already exist. The {@link Options#Folder} option is ignored for this method.
     *
     * @param userId  The ID of the user.
     * @param path    The path to the requested file.
     * @param options Any {@link Options options} required for the operation.
     *
     * @return An output stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @see #openUserDataCacheFileForWrite(UserI, Path, Options...)
     * @see #openUserDataCacheFileForWrite(String, Path, Options...)
     */
    @Nonnull
    OutputStream openUserDataCacheFileForWrite(final int userId, final @Nonnull Path path, final Options... options) throws IllegalArgumentException;

    /**
     * Returns an input stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file and all elements in the path must already exist.
     *
     * @param user The user object.
     * @param path The path to the requested file.
     *
     * @return An input stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @see #openUserDataCacheFileForRead(String, Path)
     * @see #openUserDataCacheFileForRead(int, Path)
     */
    @Nonnull
    InputStream openUserDataCacheFileForRead(final @Nonnull UserI user, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException;

    /**
     * Returns an input stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file and all elements in the path must already exist.
     *
     * @param username The username.
     * @param path     The path to the requested file.
     *
     * @return An input stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @throws UserNotFoundException    When the submitted username doesn't match a user on the XNAT system.
     * @see #openUserDataCacheFileForRead(UserI, Path)
     * @see #openUserDataCacheFileForRead(int, Path)
     */
    @Nonnull
    InputStream openUserDataCacheFileForRead(final @Nonnull String username, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException, UserNotFoundException;

    /**
     * Returns an input stream for the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}.
     * Note that the submitted <b>path</b> should be relative: if the path is absolute, this method throws an exception.
     * The <b>path</b> is considered to be a file and all elements in the path must already exist.
     *
     * @param userId The ID of the user.
     * @param path   The path to the requested file.
     *
     * @return An input stream to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @see #openUserDataCacheFileForRead(UserI, Path)
     * @see #openUserDataCacheFileForRead(String, Path)
     */
    @Nonnull
    InputStream openUserDataCacheFileForRead(final int userId, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException;

    /**
     * Deletes the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}. Note that the submitted
     * <b>path</b> should be relative: if the path is absolute, this method throws an exception. If the path represents a
     * file, just that file is deleted. If the path represents a folder, the folder and any folders and files underneath it
     * are deleted.
     *
     * @param user The user object.
     * @param path The path to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @see #deleteUserDataCacheFile(String, Path)
     * @see #deleteUserDataCacheFile(int, Path)
     */
    void deleteUserDataCacheFile(final @Nonnull UserI user, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException;

    /**
     * Deletes the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}. Note that the submitted
     * <b>path</b> should be relative: if the path is absolute, this method throws an exception. If the path represents a
     * file, just that file is deleted. If the path represents a folder, the folder and any folders and files underneath it
     * are deleted.
     *
     * @param username The username.
     * @param path     The path to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @throws UserNotFoundException    When the submitted ID doesn't match a user on the XNAT system.
     * @see #deleteUserDataCacheFile(UserI, Path)
     * @see #deleteUserDataCacheFile(int, Path)
     */
    void deleteUserDataCacheFile(final @Nonnull String username, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException, UserNotFoundException;

    /**
     * Deletes the submitted path under the {@link #getUserDataCache(UserI) user's cache folder}. Note that the submitted
     * <b>path</b> should be relative: if the path is absolute, this method throws an exception. If the path represents a
     * file, just that file is deleted. If the path represents a folder, the folder and any folders and files underneath it
     * are deleted.
     *
     * @param userId The ID of the user.
     * @param path   The path to the requested file.
     *
     * @throws IllegalArgumentException When the specified path is not relative.
     * @throws FileNotFoundException    When the requested file doesn't exist.
     * @see #deleteUserDataCacheFile(UserI, Path)
     * @see #deleteUserDataCacheFile(String, Path)
     */
    void deleteUserDataCacheFile(final int userId, final @Nonnull Path path) throws IllegalArgumentException, FileNotFoundException;
}
