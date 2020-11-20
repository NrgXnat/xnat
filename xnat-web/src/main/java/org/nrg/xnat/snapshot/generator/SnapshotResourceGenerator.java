package org.nrg.xnat.snapshot.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xnat.snapshot.generator.impl.SnapshotResourceGeneratorImpl;

import java.io.File;

public interface SnapshotResourceGenerator {

    SnapshotResourceGeneratorImpl setScan(final String sessionId, final String scanId) throws Exception;

    File createSnaphot() throws Exception;
    File createMontage( int nRows, int nCols) throws Exception;
    Pair<File, File> createSnapshotAndThumbnail( float scaleRows, float scaleCols) throws Exception;
    Pair<File, File> createMontageAndThumbnail( int nRows, int nCols, float scaleRows, float scaleCols) throws Exception;

    String getResourceName( String sessionId, String scanId, int rows, int cols);
    Pair<String, String> getResourceNames( String sessionId, String scanId, int rows, int cols);

}
