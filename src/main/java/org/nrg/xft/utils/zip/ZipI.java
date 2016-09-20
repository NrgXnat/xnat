/*
 * core: org.nrg.xft.utils.zip.ZipI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils.zip;

import org.nrg.xft.event.EventMetaI;
import org.nrg.xnat.srb.XNATDirectory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author timo
 */
public interface ZipI extends Closeable {
    void setOutputStream(OutputStream outStream) throws IOException;

    void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException;

    void extract(File f, String dir, boolean deleteZip) throws IOException;

    ArrayList extract(InputStream is, String dir, boolean overwrite, EventMetaI ci) throws IOException;

    ArrayList extract(InputStream is, String dir) throws IOException;

    List<String> getDuplicates();

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    void write(String relativePath, String absolutePath) throws IOException;

    /**
     * @param dir Directory to write into.
     * @throws IOException
     */
    void write(XNATDirectory dir)
            throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     * @throws IOException
     */
    void write(String relativePath, File f)
            throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     * @throws IOException
     */
    void write(String relativePath, InputStream f)
            throws IOException;

    /**
     * @param dir Path for directory.
     * @throws IOException
     */
    void writeDirectory(File dir)
            throws IOException;

    /**
     * @throws IOException
     */
    @Override
    void close() throws IOException;

    /**
     * @return Returns the _compressionMethod.
     */
    boolean getDecompressFilesBeforeZipping();

    /**
     * @param method The _compressionMethod to set.
     */
    void setDecompressFilesBeforeZipping(boolean method);

    /**
     * @return Returns the _compressionMethod.
     */
    int getCompressionMethod();

    /**
     * @param method The _compressionMethod to set.
     */
    void setCompressionMethod(int method);
}
