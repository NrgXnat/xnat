/*
 * core: org.nrg.xft.utils.zip.ZipI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils.zip;

import org.nrg.xft.event.EventMetaI;

import java.io.*;
import java.util.List;

/**
 * @author timo
 */
@SuppressWarnings("unused")
public interface ZipI extends Closeable {
    /**
     * Sets the output stream for writing the archive.
     *
     * @param outStream The output stream to which the archive should be written.
     *
     * @throws IOException When an error occurs setting the output stream.
     */
    void setOutputStream(OutputStream outStream) throws IOException;

    /**
     *  Sets the output stream for writing the archive along with the compression method that should be used with the
     * output stream.
     *
     * @param outStream         The output stream to which the archive should be written.
     * @param compressionMethod The compression method to use when writing to the output stream.
     *
     * @throws IOException When an error occurs setting the output stream.
     */
    void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException;

     /**
      * Extracts the contents of the archive file
      *
      * @param archive         The archive to extract from.
      * @param destination     The destination folder into which the archive contents should be extracted.
      * @param deleteOnExtract Whether the original archive file should be deleted once the archive has been
      *                        successfully extracted.
      *
      * @throws IOException When an error occurs reading the archive or writing to the destination folder.
      */
    void extract(File archive, String destination, boolean deleteOnExtract) throws IOException;

    List<File> extract(InputStream is, String dir, boolean overwrite, EventMetaI ci) throws IOException;

    List<File> extract(InputStream is, String dir) throws IOException;

    List<String> getDuplicates();

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     *
     * @throws FileNotFoundException When the requested file or folder can't be located.
     * @throws IOException           When an error occurs with reading or writing data.
     */
    void write(String relativePath, String absolutePath) throws IOException;
   /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void write(String relativePath, File f) throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void write(String relativePath, InputStream f) throws IOException;

    /**
     * @param dir Path for directory.
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void writeDirectory(File dir) throws IOException;

    /**
     * @throws IOException When an error occurs with reading or writing data.
     */
    @Override
    void close() throws IOException;

    /**
     * Indicates whether compressed files should be decompressed before being written to the archive.
     *
     * @return Whether compressed files should be decompressed before being written.
     */
    boolean getDecompressFilesBeforeZipping();

    /**
     * Set whether compressed files should be decompressed before being written to the archive.
     *
     * @param method Whether compressed files should be decompressed before being written.
     */
    void setDecompressFilesBeforeZipping(boolean method);

    /**
     * Returns the compression method to be used writing to the archive. Possible values are dependent on the
     * implementation.
     *
     * @return Returns the compression method.
     */
    int getCompressionMethod();

    /**
     * Sets the compression method to be used writing to the archive. Possible values are dependent on the
     * implementation.
     *
     * @param method Returns the compression method.
     */
    void setCompressionMethod(int method);
}
