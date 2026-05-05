package org.nrg.dicom.dicomedit.pixels.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyLogger implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(MyLogger.class);
    private FileOutputStream fos = null;

    public MyLogger( String name) {
        try {
            Path tmpDir = Paths.get("/tmp/pixelEdit");
            Files.createDirectories(tmpDir);
            File logFile = Files.createTempFile(tmpDir, name, ".log").toFile();
            fos = new FileOutputStream(logFile);
        }
        catch (IOException e) {
            logger.error("Failed to create pixel-edit log file for '{}': {}", name, e.getMessage(), e);
        }
    }

    public void log( String s) throws IOException {
        fos.write( s.getBytes());
        fos.flush();
    }

    @Override
    public void close() throws IOException {
        if( fos != null) fos.close();
    }
}
