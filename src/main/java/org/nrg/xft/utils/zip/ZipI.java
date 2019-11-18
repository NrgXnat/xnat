/*
 * core: org.nrg.xft.utils.zip.ZipI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils.zip;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xnat.srb.XNATDirectory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * @author timo
 */
@SuppressWarnings("unused")
public interface ZipI extends Closeable {
    static boolean isCompressedFile(final String filename, final String... extras) {
        return StringUtils.endsWithAny(filename.toLowerCase(), ".tar", ".tgz", ".tar.gz", ".zip", ".gz") || StringUtils.endsWithAny(filename.toLowerCase(), extras);
    }

    static String getCompression(final String filename, final String... extras) {
        if (!isCompressedFile(filename, extras)) {
            return "";
        }
        final String normalized = filename.toLowerCase();
        if (StringUtils.endsWithAny(normalized, ".tgz", ".tar.gz")) {
            return "tgz";
        }
        final String extension = FilenameUtils.getExtension(normalized);
        if (StringUtils.equalsAny(extension, ArrayUtils.addAll(extras, "zip", "tar", "gz"))) {
            return extension;
        }
        throw new RuntimeException("File " + filename + " doesn't match any of the specified extensions for compression, even though isCompressedFile() said it did.");
    }

    static void extractFile(final File file, final Path destination) throws IOException {
        extractFile(file, destination, getCompression(file.getName()));
    }

    static void extractFile(final File file, final Path destination, final String compression) throws IOException {
        try (final InputStream input = new FileInputStream(file)) {
            extractFile(input, destination, compression);
        }
    }

    static void extractFile(final InputStream input, final Path destination, final String compression) throws IOException {
        final ZipI zipper;
        switch (compression) {
            case "tar":
                zipper = new TarUtils();
                break;
            case "tgz":
                zipper = new TarUtils();
                zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                break;
            default:
                zipper = new ZipUtils();
        }

        zipper.extract(input, destination.toString());
    }

    void setOutputStream(OutputStream outStream) throws IOException;

    void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException;

    void extract(File f, String dir, boolean deleteZip) throws IOException;

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
     * @param dir Directory to write into.
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void write(XNATDirectory dir)
            throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void write(String relativePath, File f)
            throws IOException;


    /**
     * @param relativePath Path name for zip file
     * @param f            File name for zip file
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void write(String relativePath, InputStream f)
            throws IOException;

    /**
     * @param dir Path for directory.
     *
     * @throws IOException When an error occurs with reading or writing data.
     */
    void writeDirectory(File dir)
            throws IOException;

    /**
     * @throws IOException When an error occurs with reading or writing data.
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
