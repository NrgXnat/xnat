package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementation of SnapshotGenerationService.
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
    public SnapshotGenerationServiceImpl(final SnapshotProviderPool snapshotProviderPool) {
        _snapshotProviderPool = snapshotProviderPool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> getSnapshot(final String sessionId, final String scanId, final int rows, final int cols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        log.debug("Provide snapshot for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
        return provideSnapshotOrThumbnail(sessionId, scanId, rows, cols, -1, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> getThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) {
        log.debug("Provide thumbnail for scan {} of session {} with {} rows by {} cols, scaling rows by {} and columns by {}", sessionId, scanId, rows, cols, scaleRows, scaleCols);
        return provideSnapshotOrThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
    }

    /**
     * Provide the requested Resource.
     *
     * Pool deals one SnapshotProvider per scan, blocks if there are two requests for the same scan. No two requests will
     * be working simultaneously on the same scan.
     *
     * @param sessionId    The ID of the session
     * @param scanId       The ID of the scan
     * @param rows         The number of rows to be generated
     * @param columns      The number of columns to be generated
     * @param scaleRows    The scaling factor to use for generating rows
     * @param scaleColumns The scaling factor to use for generating columns
     *
     * @return The generated snapshot or thumbnail file.
     */
    private Optional<FileResource> provideSnapshotOrThumbnail(final String sessionId, final String scanId, final int rows, final int columns, float scaleRows, float scaleColumns) {
        final String lockCode = getLockCode(sessionId, scanId);
        try (final SnapshotProvider provider = _snapshotProviderPool.borrowObject(lockCode)) {
            return provider.provideSnapshotOrThumbnail(sessionId, scanId, rows, columns, scaleRows, scaleColumns);
        } catch (Exception e) {
            log.warn("Exception from snapshot-provider pool", e);
            return Optional.empty();
        }
    }

    private static String getLockCode(final String sessionId, final String scanId) {
        return String.join(":", sessionId, scanId);
    }

    private final SnapshotProviderPool _snapshotProviderPool;
}
