package org.nrg.dicom.dicomedit.pixels.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyLogger implements Closeable {
    private FileOutputStream fos = null;

    public MyLogger( String name) {
        try {
            Path tmpDir = Paths.get("/tmp/pixelEdit");
            Files.createDirectories(tmpDir);
            File logFile = Files.createTempFile(tmpDir, name, ".log").toFile();
            fos = new FileOutputStream(logFile);
        }
        catch (IOException e) {
            e.printStackTrace();
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
