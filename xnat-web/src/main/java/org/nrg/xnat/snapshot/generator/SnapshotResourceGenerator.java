package org.nrg.xnat.snapshot.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xnat.snapshot.FileResource;

import java.util.Optional;

public interface SnapshotResourceGenerator {

    boolean hasSnapshot(final String sessionId, final String scanId) throws Exception;

    Optional<FileResource> createSnaphot(String SessionId, String scanId) throws Exception;
    Optional<FileResource> createMontage( String SessionId, String scanId, int nRows, int nCols) throws Exception;
    Optional<Pair<FileResource, FileResource>> createSnapshotAndThumbnail( String SessionId, String scanId, float scaleRows, float scaleCols) throws Exception;
    Optional<Pair<FileResource, FileResource>> createMontageAndThumbnail( String SessionId, String scanId, int nRows, int nCols, float scaleRows, float scaleCols) throws Exception;

    String getFormat();

    String getSnapshotContentName(String sessionId, String scanId, int rows, int cols);

    String getThumbnailContentName(String sessionId, String scanId, int rows, int cols);

    String getSnapshotResourceName(final String sessionId, final String scanId, final int rows, int cols, String format);

    String getThumbnailResourceName(final String sessionId, final String scanId, final int rows, int cols, String format);
}
