package org.nrg.xdat.services.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class DefaultUserDataCacheService implements UserDataCache {
    @Autowired
    public DefaultUserDataCacheService(final SiteConfigPreferences preferences) {
        _userDataCachePath = Paths.get(preferences.getCachePath(), "USERS");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getCacheName() {
        return USER_DATA_CACHE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Path getUserDataCache(@NotNull final UserI user) {
        return getUserDataCache(user.getID());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Path getUserDataCache(@NotNull final String username) throws UserNotFoundException {
        final Integer userId = Users.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException(username);
        }
        return getUserDataCache(userId);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Path getUserDataCache(final int userId) {
        final File folder = _userDataCachePath.resolve(Integer.toString(userId)).toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            log.warn("The user {} requested their user data cache folder \"{}\", which doesn't exist so I tried to make it but the method I called is telling me that didn't work, so things might get weird.", Users.getUsername(userId), folder.getAbsolutePath());
        }
        return folder.toPath();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public File getUserDataCacheFile(@NotNull final UserI user, @NotNull final Path path, final Options... options) throws IllegalArgumentException {
        return getUserDataCacheFile(user.getID(), path, options);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public File getUserDataCacheFile(@NotNull final String username, @NotNull final Path path, final Options... options) throws IllegalArgumentException, UserNotFoundException {
        final Integer userId = Users.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException(username);
        }
        return getUserDataCacheFile(userId, path, options);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public File getUserDataCacheFile(final int userId, @NotNull final Path path, final Options... options) throws IllegalArgumentException {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("The specified path is absolute, but must be relative: " + path.toString());
        }
        final File    requested = getUserDataCache(userId).resolve(path).toFile();
        final boolean asFolder  = ArrayUtils.contains(options, Options.Folder);
        if (requested.exists()) {
            if (requested.isDirectory()) {
                if (!asFolder) {
                    throw new IllegalArgumentException("User " + Users.getUsername(userId) + " requested the path \"" + path.toString() + "\" as a file but that already exists as a folder.");
                }
                log.trace("User with ID {} requested the path \"{}\" as a folder, returning existing folder.", userId, path);
            } else {
                if (asFolder) {
                    throw new IllegalArgumentException("User " + Users.getUsername(userId) + " requested the path \"" + path.toString() + "\" as a file but that already exists as a folder.");
                }
                log.trace("User with ID {} requested the path \"{}\" as a file, returning existing file.", userId, path);
            }
            return requested;
        }
        // Create all the folders required to write a file. If this was requested as a folder, we'll create everything, but if not
        // we'll create the parent folder(s).
        if (asFolder || path.getNameCount() > 1) {
            final File target = asFolder ? requested : requested.getParentFile();
            if (target.exists()) {
                if (target.isFile()) {
                    throw new IllegalArgumentException("The parent of the specified path already exists as a file, can't get it as a folder in which to create the file: " + path.toString());
                }
                log.trace("User with ID {} requested the path \"{}\" as a file, that file doesn't exist but its parent does.", userId, path);
            } else {
                if (!target.mkdirs()) {
                    log.warn("User with ID {} requested a data cache file at {}, but it looks like I couldn't create the parent folder. Things might get weird. ", userId, path);
                } else {
                    log.trace("User with ID {} requested the path \"{}\" as a file, that file  and at least its immediate parent doesn't exist, so created the parent folder.", userId, path);
                }
            }
        }
        return requested;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public OutputStream openUserDataCacheFileForWrite(@NotNull final UserI user, @NotNull final Path path, final Options... options) throws IllegalArgumentException {
        return openUserDataCacheFileForWrite(user.getID(), path, options);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public OutputStream openUserDataCacheFileForWrite(@Nonnull final String username, @NotNull final Path path, final Options... options) throws IllegalArgumentException, UserNotFoundException {
        final Integer userId = Users.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException(username);
        }
        return openUserDataCacheFileForWrite(userId, path, options);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public OutputStream openUserDataCacheFileForWrite(final int userId, @NotNull final Path path, final Options... options) throws IllegalArgumentException {
        // Suppress warning about returning null in non-null-annotated method, because we know this is not going to be null.
        //noinspection ConstantConditions
        return getOutputStreamNoException(getUserDataCacheFile(userId, path, ArrayUtils.contains(options, Options.Folder) ? ArrayUtils.removeElement(options, Options.Folder) : options));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public InputStream openUserDataCacheFileForRead(@NotNull final UserI user, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException {
        return openUserDataCacheFileForRead(user.getID(), path);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public InputStream openUserDataCacheFileForRead(@NotNull final String username, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException, UserNotFoundException {
        final Integer userId = Users.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException(username);
        }
        return openUserDataCacheFileForRead(userId, path);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public InputStream openUserDataCacheFileForRead(final int userId, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException {
        return new FileInputStream(getUserDataCacheFile(userId, path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserDataCacheFile(@NotNull final UserI user, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException {
        deleteUserDataCacheFile(user.getID(), path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserDataCacheFile(@NotNull final String username, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException, UserNotFoundException {
        final Integer userId = Users.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException(username);
        }
        deleteUserDataCacheFile(userId, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserDataCacheFile(final int userId, @NotNull final Path path) throws IllegalArgumentException, FileNotFoundException {
        final File file = getUserDataCacheFile(userId, path);
        if (!file.exists()) {
            throw new FileNotFoundException("The file \"" + file + "\" does not exist, so I can't delete it.");
        }
        if (!FileUtils.deleteQuietly(file)) {
            log.warn("The user {} requested to delete the file \"{}\". I tried but it may not have happened.", Users.getUsername(userId), path);
        }
    }

    private static OutputStream getOutputStreamNoException(final File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException ignored) {
            return null;
        }
    }

    private static final String USER_DATA_CACHE_NAME = "userDataCache";

    private final Path _userDataCachePath;
}
