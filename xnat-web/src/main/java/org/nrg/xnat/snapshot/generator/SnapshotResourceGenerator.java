package org.nrg.xnat.snapshot.generator;

import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xnat.snapshot.FileResource;

import java.io.IOException;
import java.util.Optional;

public interface SnapshotResourceGenerator {
    String ORIGINAL                   = "ORIGINAL";
    String THUMBNAIL                  = "THUMBNAIL";
    String SNAPSHOT_NAME_TEMPLATE     = "%s_%s%s_qc.%s";
    String THUMBNAIL_NAME_TEMPLATE    = "%s_%s%s_qc_t.%s";
    String SNAPSHOT_CONTENT_TEMPLATE  = "%dX%d";
    String THUMBNAIL_CONTENT_TEMPLATE = "%dX%d_THUMBNAIL";
    String DIMENSION_TEMPLATE         = "_%sx%s";

    Optional<FileResource> createSnapshot(String SessionId, String scanId, int nRows, int nCols) throws InitializationException, IOException;

    Optional<FileResource> createThumbnail(String SessionId, String scanId, int nRows, int nCols, float scaleRows, float scaleCols) throws InitializationException, IOException;

    static String getContentName(final int rows, final int cols, final float scaleRows, final float scaleCols) {
        if( scaleCols < 0.0 || scaleRows < 0.0) {
            return getThumbnailContentName( rows, cols);
        }
        else {
            return getSnapshotContentName( rows, cols);
        }
    }

    static String getSnapshotContentName(final int rows, final int cols) {
        return (rows == 1 && cols == 1) ? ORIGINAL : String.format(SNAPSHOT_CONTENT_TEMPLATE, cols, rows);
    }

    static String getThumbnailContentName(final int rows, final int cols) {
        return (rows == 1 && cols == 1) ? THUMBNAIL : String.format(THUMBNAIL_CONTENT_TEMPLATE, cols, rows);
    }

    static String getSnapshotResourceName(final String sessionId, final String scanId, final int rows, int cols, String format) {
        return String.format(SNAPSHOT_NAME_TEMPLATE, sessionId, scanId, rows == 1 && cols == 1 ? "" : String.format(DIMENSION_TEMPLATE, cols, rows), format.toLowerCase());
    }

    static String getThumbnailResourceName(final String sessionId, final String scanId, final int rows, int cols, String format) {
        return String.format(THUMBNAIL_NAME_TEMPLATE, sessionId, scanId, rows == 1 && cols == 1 ? "" : String.format(DIMENSION_TEMPLATE, cols, rows), format.toLowerCase());
    }
}
