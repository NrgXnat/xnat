package org.nrg.xnat.snapshot.services;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.snapshot.FileResource;

import java.io.IOException;
import java.util.Optional;

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
     * @param rows      The number of rows in the view.
     * @param cols      The number of cols in the view.
     *
     * @return The URI to the specified snapshot.
     */
    Optional<FileResource> getSnapshot(final String sessionId, final String scanId, final int rows, int cols) throws DataFormatException, NotFoundException, InitializationException, IOException;

    /**
     * Gets the URI to the specified thumbnail. If the thumbnail doesn't already exist, it is generated and added to the
     * scan resources. Note that the session ID here must be the actual experiment ID, not the session label.
     *
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which thumbnail(s) should be generated.
     * @param rows      The number of rows in the view.
     * @param cols      The number of cols in the view.
     * @param scaleRows The percentage by which the image's Y axis should be scaled down
     * @param scaleCols The percentage by which the image's X axis should be scaled down
     *
     * @return The URI to the specified thumbnail.
     */
    Optional<FileResource> getThumbnail(final String sessionId, final String scanId, final int rows, int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException;
}
