package org.nrg.xft.utils.fileExtraction;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.compression.FileModem;
import org.nrg.xft.utils.compression.GZipDecompressor;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * Service to extract File(s) from various archive/encoding types.
 *
 * This encapsulates the ZipI interface implementors ZipUtils and TarUtils with a GZIP stream decompressor
 * to provide one interface for extracting file(s) from archives or compression. The zippers always assume the file is
 * an archive to be expanded. But sometimes the file is not intended to be expanded, just decompressed.
 *
 * The encoding is assumed to be designated by filename suffixes.
 *
 * Supported formats are
 *  tar, tar.gz, tgz, zip, zar, gz.
 */
public class FileExtractor {
    private List<String> _duplicates;

    public FileExtractor() {
        this._duplicates = new ArrayList<>();
    }

    public List<File> extract( Path sourceFilePath, Path destinationDirPath, boolean overwrite, EventMetaI ci) throws IOException {
        String fileNameWithSuffix = sourceFilePath.getFileName().toString();
        return extract(fileNameWithSuffix, new FileInputStream( sourceFilePath.toFile()), destinationDirPath, overwrite, ci);
    }

    /**
     * Extract File(s) from input file.
     *
     * This signature best supports CatalogUtils's use of FileWriterWrapperI.
     *
     * @param fileName
     * @param inputStream
     * @param destinationDirPath
     * @param overwrite
     * @param ci
     * @return
     * @throws IOException
     */
    public List<File> extract(String fileName, InputStream inputStream, Path destinationDirPath, boolean overwrite, EventMetaI ci) throws IOException {
        List<File> files;
        Format format = Format.getFormat( fileName);
        String fileNameWithoutSuffix = format.getFileName( fileName);
        switch (format) {
            case TAR:
                ZipI zipper = new TarUtils();
                files = zipper.extract( inputStream, destinationDirPath.toString(), overwrite, ci);
                _duplicates.addAll( zipper.getDuplicates());
                break;
            case TAR_GZIP:
            case TAR_TGZ:
                zipper = new TarUtils();
                zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                files = zipper.extract( inputStream, destinationDirPath.toString(), overwrite, ci);
                _duplicates.addAll( zipper.getDuplicates());
                break;
            case ZIP:
            case ZAR:
                zipper = new ZipUtils();
                files = zipper.extract( inputStream, destinationDirPath.toString(), overwrite, ci);
                _duplicates.addAll(zipper.getDuplicates());
                break;
            case GZIP:
                files = new ArrayList<>();
                File destinationFile = destinationDirPath.resolve( fileNameWithoutSuffix).toFile();
                if( destinationFile.exists() && !overwrite) {
                    _duplicates.add( destinationFile.getName());
                } else {
                    if( destinationFile.exists()) {
                        FileUtils.MoveToHistory( destinationFile, EventUtils.getTimestamp(ci));
                    }
                    files.add( destinationFile);
                    FileModem decompressor = new GZipDecompressor();
                    decompressor.process( inputStream,  destinationFile);
                }
                break;
            default:
                files = new ArrayList<>();
        }

        return files;
    }

    public List<String> getDuplicates() {
        return _duplicates;
    }

    public enum Format {
        TAR( ".tar"),
        TAR_GZIP( ".tar.gz"),
        TAR_TGZ( ".tgz"),
        ZIP( ".zip"),
        ZAR( ".zar"),
        GZIP( ".gz"),
        UNKNOWN("");

        String[] suffixes;
        Format( String... suffixes) {
            this.suffixes = suffixes;
        }

        public List<String> getSuffixes() { return new ArrayList<String>(Arrays.asList(suffixes));}

        /**
         * Return the Format based on filename suffixes.
         *
         * Search the list of suffixes in order enumerated to distinguish 'tar.gz' as a special case of 'gz'.
         *
         * @param fileName
         * @return
         */
        public static Format getFormat( String fileName) {
            Format[] formats = values();
            for( Format f: formats) {
                if( !(f == UNKNOWN) && StringUtils.endsWithAny( fileName.toLowerCase(), f.suffixes)) {
                    return f;
                }
            }
            return UNKNOWN;
        }

        /**
         * Return fileName without suffix.
         *
         * @param fileNameWithSuffix
         * @return
         */
        public String getFileName( String fileNameWithSuffix) {
            return (this == UNKNOWN) ? fileNameWithSuffix : fileNameWithSuffix.replace(suffixes[0], "");
        }
    }

}
