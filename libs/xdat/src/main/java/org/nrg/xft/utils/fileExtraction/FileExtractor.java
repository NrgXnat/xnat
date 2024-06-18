package org.nrg.xft.utils.fileExtraction;

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
 * tar, tar.gz, tgz, zip, zar, xar, gz.
 */
public class FileExtractor {
    public FileExtractor() {
        _duplicates = new ArrayList<>();
    }

    /**
     * Extract file(s) from the specified input file.
     *
     * @param source      The path to the input file.
     * @param destination The destination where the contents of the input file should be extracted.
     * @param overwrite   Whether existing files should be overwritten if there's a conflict.
     * @param ci          The event metadata.
     *
     * @return A list of the files extracted from the input file.
     *
     * @throws IOException When an error occurs reading the input file or writing its contents to the specified destination.
     */
    public List<File> extract(final Path source, final Path destination, final boolean overwrite, final EventMetaI ci) throws IOException {
        return extract(source.getFileName().toString(), new FileInputStream(source.toFile()), destination, overwrite, ci);
    }

    /**
     * Extract file(s) from the specified input file.
     *
     * This signature best supports CatalogUtils's use of FileWriterWrapperI.
     *
     * @param filename    The name of the input file.
     * @param inputStream An input stream for accessing the file contents.
     * @param destination The destination where the contents of the input file should be extracted.
     * @param overwrite   Whether existing files should be overwritten if there's a conflict.
     * @param ci          The event metadata.
     *
     * @return A list of the files extracted from the input file.
     *
     * @throws IOException When an error occurs reading the input file or writing its contents to the specified destination.
     */
    public List<File> extract(final String filename, final InputStream inputStream, final Path destination, final boolean overwrite, final EventMetaI ci) throws IOException {
        final List<File> files;
        final Format     format = Format.getFormat(filename);
        switch (format) {
            case TAR:
                ZipI zipper = new TarUtils();
                files = zipper.extract(inputStream, destination.toString(), overwrite, ci);
                _duplicates.addAll(zipper.getDuplicates());
                break;
            case TGZ:
                zipper = new TarUtils();
                zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                files = zipper.extract(inputStream, destination.toString(), overwrite, ci);
                _duplicates.addAll(zipper.getDuplicates());
                break;
            case ZIP:
                zipper = new ZipUtils();
                files = zipper.extract(inputStream, destination.toString(), overwrite, ci);
                _duplicates.addAll(zipper.getDuplicates());
                break;
            case GZ:
                files = new ArrayList<>();
                final File destinationFile = destination.resolve(format.getFileName(filename)).toFile();
                final boolean exists = destinationFile.exists();
                if (exists && !overwrite) {
                    _duplicates.add(destinationFile.getName());
                } else {
                    if (exists) {
                        FileUtils.MoveToHistory(destinationFile, EventUtils.getTimestamp(ci));
                    }
                    files.add(destinationFile);
                    new GZipDecompressor().process(inputStream, destinationFile);
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

    private final List<String> _duplicates;
}
