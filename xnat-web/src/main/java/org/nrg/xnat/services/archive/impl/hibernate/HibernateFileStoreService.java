package org.nrg.xnat.services.archive.impl.hibernate;

import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnat.entities.FileStoreInfo;
import org.nrg.xnat.preferences.FileStorePreferences;
import org.nrg.xnat.services.archive.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@Slf4j
public class HibernateFileStoreService extends AbstractHibernateEntityService<FileStoreInfo, FileStoreInfoDAO> implements FileStore {
    @Autowired
    public HibernateFileStoreService(final FileStorePreferences preferences) {
        _preferences = preferences;
    }

    public static List<String> splitCoordinates(final String coordinates) {
        return Arrays.asList(StringUtils.split(coordinates, "/"));
    }

    public static String joinCoordinates(final List<String> coordinates) {
        return StringUtils.join(coordinates, "/");
    }

    public static String joinCoordinates(final String... coordinates) {
        return StringUtils.join(coordinates, "/");
    }

    @Override
    public FileStoreInfo create(final InputStream inputStream, final String... coordinates) throws ResourceAlreadyExistsException {
        final String joined = joinCoordinates(coordinates);
        if (getDao().existsByCoordinates(joined)) {
            throw new ResourceAlreadyExistsException("fileStore", joined);
        }
        final Path fileStorePath = getFileStorePath(joined);
        final Path fullPath      = Paths.get(_preferences.getFileStorePath()).resolve(fileStorePath);
        try {
            final Pair<String, Long> write = writeFile(inputStream, fullPath);
            return create(FileStoreInfo.builder()
                                       .checksum(write.getKey())
                                       .size(write.getValue())
                                       .coordinates(coordinates)
                                       .storeUri(fullPath.toUri())
                                       .build());
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to open a file for processing an incoming file-store entry.", e);
        }
    }

    @Override
    public InputStream open(final long storeId) throws NotFoundException, IOException {
        return Files.newInputStream(Paths.get(getFileStoreInfo(storeId).getStoreUri()));
    }

    @Override
    public InputStream open(final String... coordinates) throws NotFoundException, IOException {
        return Files.newInputStream(Paths.get(getFileStoreInfo(coordinates).getStoreUri()));
    }

    @Override
    public FileStoreInfo update(final InputStream inputStream, final long storeId) throws NotFoundException {
        final FileStoreInfo info          = getFileStoreInfo(storeId);
        final Path          fileStorePath = getFileStorePath(info.getCoordinates());
        final Path          fullPath      = Paths.get(_preferences.getFileStorePath()).resolve(fileStorePath);
        try {
            final Pair<String, Long> write = writeFile(inputStream, fullPath);
            info.setChecksum(write.getKey());
            info.setSize(write.getValue());
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to open a file for processing an incoming file-store entry.", e);
        }
        update(info);
        return getDao().retrieve(info.getId());
    }

    @Override
    public FileStoreInfo update(final InputStream inputStream, final String... coordinates) throws NotFoundException {
        final String joined = joinCoordinates(coordinates);
        if (!getDao().existsByCoordinates(joined)) {
            throw new NotFoundException("fileStore", joined);
        }
        return update(inputStream, getDao().findByCoordinates(joined).getId());
    }

    @Override
    public void delete(final long storeId) {
        try {
            delete(getFileStoreInfo(storeId));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(final String... coordinates) throws NotFoundException {
        delete(getFileStoreInfo(coordinates));
    }

    @Override
    public void delete(final FileStoreInfo info) {
        try {
            Files.delete(Paths.get(info.getStoreUri()));
            super.delete(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nonnull
    public FileStoreInfo getFileStoreInfo(final long storeId) throws NotFoundException {
        if (!getDao().exists("id", storeId)) {
            throw new NotFoundException("fileStore", storeId);
        }
        return getDao().findById(storeId);
    }

    @Override
    @Nonnull
    public FileStoreInfo getFileStoreInfo(final String... coordinates) throws NotFoundException {
        final String joined = joinCoordinates(coordinates);
        if (!getDao().existsByCoordinates(joined)) {
            throw new NotFoundException("fileStore", joined);
        }
        return getDao().findByCoordinates(joined);
    }

    /**
     * Writes the contents of the input stream to the destination path and returns the SHA-256 checksum value for the output.
     *
     * @param input       The input stream of data to be written to the file.
     * @param destination The path to the file into which the data should be written.
     *
     * @return The SHA-256 checksum for the output data.
     *
     * @throws IOException Thrown if an error occurs while creating or writing to the output file.
     */
    static Pair<String, Long> writeFile(final InputStream input, final Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (final OutputStream output = Files.newOutputStream(destination)) {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[]              buffer = new byte[1024];
            long                count;
            int                 bytesRead;
            for (count = 0L; -1 != (bytesRead = input.read(buffer)); count += bytesRead) {
                digest.update(buffer, 0, bytesRead);
                output.write(buffer, 0, bytesRead);
            }
            return Pair.of(BaseEncoding.base16().encode(digest.digest()), count);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    static Path getFileStorePath(final String joined) {
        return getFileStorePath(splitHashedPath(getHashedPath(joined)));
    }

    static Path getFileStorePath(final String[] split) {
        return Arrays.asList(split).subList(1, split.length).stream().map(Paths::get).reduce(Paths.get(split[0]), Path::resolve);
    }

    static String getHashedPath(final String joined) {
        try {
            return BaseEncoding.base16().encode(MessageDigest.getInstance("SHA-256").digest(joined.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    static String[] splitHashedPath(final String hashed) {
        return hashed.split("(?<=\\G.{8})");
    }

    private final FileStorePreferences _preferences;
}
