package org.nrg.xnat.snapshot.services;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xapi.exceptions.NotFoundException;

import java.io.File;

/**
 * @author pradeep.d
 */
public interface SnapshotGenerationService {
    /**
     * Gets the URI to the specified snapshot. If the snapshot doesn't already exist, it is generated and added to the
     * scan resources. Note that the session ID here must be the actual experiment ID, not the session label.
     *
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which snapshot(s) should be generated.
     *
     * @return The URI to the specified snapshot.
     */
    Pair<File, File> getSnapshot(final String sessionId, final String scanId) throws NotFoundException;

    /**
     * Gets the URI to the specified snapshot. If the snapshot doesn't already exist, it is generated and added to the
     * scan resources. Note that the session ID here must be the actual experiment ID, not the session label.
     *
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which snapshot(s) should be generated.
     * @param gridView  The grid-view specifier.
     *
     * @return The URI to the specified snapshot.
     */
    Pair<File, File> getSnapshot(final String sessionId, final String scanId, final String gridView) throws NotFoundException;
}
