package org.nrg.xnat.snapshot.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xnat.snapshot.generator.impl.SnapshotResourceGeneratorImpl;

import java.io.File;
import java.util.Optional;

public interface SnapshotResourceGenerator {

    boolean hasSnapshot(final String sessionId, final String scanId) throws Exception;

    Optional<File> createSnaphot( String SessionId, String scanId) throws Exception;
    Optional<File> createMontage( String SessionId, String scanId, int nRows, int nCols) throws Exception;
    Optional<Pair<File, File>> createSnapshotAndThumbnail( String SessionId, String scanId, float scaleRows, float scaleCols) throws Exception;
    Optional<Pair<File, File>> createMontageAndThumbnail( String SessionId, String scanId, int nRows, int nCols, float scaleRows, float scaleCols) throws Exception;

    String getResourceName( String sessionId, String scanId, int rows, int cols);
    Pair<String, String> getResourceNames( String sessionId, String scanId, int rows, int cols);

}
