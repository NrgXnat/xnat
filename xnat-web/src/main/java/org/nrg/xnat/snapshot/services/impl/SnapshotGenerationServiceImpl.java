package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of SnapshotGenerationService.
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
    public SnapshotGenerationServiceImpl(final CatalogService catalogService, final SnapshotResourceGenerator snapshotResourceGenerator, final XnatUserProvider primaryAdminUserProvider) {
        _catalogService = catalogService;
        _snapshotGenerator = snapshotResourceGenerator;
        _provider = primaryAdminUserProvider;
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
    public Optional<FileResource> getThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        log.debug("Provide thumbnail for scan {} of session {} with {} rows by {} cols, scaling rows by {} and columns by {}", sessionId, scanId, rows, cols, scaleRows, scaleCols);
        return provideSnapshotOrThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
    }

    /*
     * Reuse existing SnapshotProvider if one already exists handling a previous request.
     */
    private Optional<FileResource> provideSnapshotOrThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        final String lockCode = getLockCode(sessionId, scanId, rows, cols, scaleRows, scaleCols);
        synchronized (_locks) {
            if (!_locks.containsKey(lockCode)) {
                _locks.put(lockCode, new SnapshotProvider(_catalogService, _snapshotGenerator, _provider));
            }
        }
        final SnapshotProvider provider = _locks.get(lockCode);
        try {
            return provider.provideSnapshotOrThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
        } finally {
            if (!provider.isReferenced()) {
                _locks.remove(lockCode);
            }
        }
    }

    private static String getLockCode(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) {
        return String.join(":", sessionId, scanId, Integer.toString(rows), Integer.toString(cols), Float.toString(scaleRows), Float.toString(scaleCols));
    }

    private final Map<String, SnapshotProvider> _locks = new HashMap<>();

    private final CatalogService            _catalogService;
    private final SnapshotResourceGenerator _snapshotGenerator;
    private final XnatUserProvider          _provider;
}
