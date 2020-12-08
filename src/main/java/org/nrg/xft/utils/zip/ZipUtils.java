/*
 * core: org.nrg.xft.utils.zip.ZipUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.zip;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Expand;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.srb.XNATDirectory;
import org.nrg.xnat.srb.XNATSrbFile;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.*;


/**
 * @author timo
 */
@SuppressWarnings("unused")
@Slf4j
public class ZipUtils implements ZipI {
    public final static int DEFAULT_COMPRESSION = ZipOutputStream.STORED;

    public static boolean isCompressedFile(final String filename, final String... extras) {
        return Format.getFormat(filename) != Format.UNKNOWN || StringUtils.endsWithAny(filename.toLowerCase(), extras);
    }

    public static String getCompression(final String filename, final String... extras) {
        if (!isCompressedFile(filename, extras)) {
            return "";
        }
        final Format format = Format.getFormat(filename);
        if (format != Format.UNKNOWN) {
            return format.toString().toLowerCase();
        }
        final Matcher matcher = getExtensionPattern(extras).matcher(filename);
        if (matcher.matches()) {
            return matcher.group("extension");
        }
        throw new RuntimeException("File " + filename + " doesn't match any of the specified extensions for compression, even though isCompressedFile() said it did.");
    }

    public static void extractFile(final File file, final Path destination) throws IOException {
        extractFile(file, destination, getCompression(file.getName()));
    }

    public static void extractFile(final File file, final Path destination, final String compression) throws IOException {
        try (final InputStream input = new FileInputStream(file)) {
            extractFile(input, destination, compression);
        }
    }

    public static void extractFile(final InputStream input, final Path destination, final String compression) throws IOException {
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

    byte[]          buf         = new byte[FileUtils.LARGE_DOWNLOAD];
    ZipOutputStream out         = null;
    int             compression = ZipOutputStream.DEFLATED;
    boolean         decompress  = false;
    private final List<String> _duplicates = new ArrayList<>();

    @Override
    public void setOutputStream(OutputStream outStream) {
        out = new ZipOutputStream(outStream);
        out.setMethod(ZipOutputStream.DEFLATED);
    }

    @Override
    public void setOutputStream(OutputStream outStream, int compressionMethod) {
        out = new ZipOutputStream(outStream);
        out.setMethod(compressionMethod);
        compression = compressionMethod;
    }

    @Override
    public void setCompressionMethod(int compressionMethod) {
        out.setMethod(compressionMethod);
        compression = compressionMethod;
    }

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     *
     * @throws FileNotFoundException When the requested file or folder can't be located.
     * @throws IOException           When an error occurs with reading or writing data.
     */
    @Override
    public void write(String relativePath, String absolutePath) throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        File f = new File(absolutePath);
        write(relativePath, f);
    }

    /**
     * @param relativePath path name for zip file
     * @param f            The file to write out.
     *
     * @throws FileNotFoundException When the requested file or folder can't be located.
     * @throws IOException           When an error occurs with reading or writing data.
     */
    @Override
    public void write(final String relativePath, final File f) throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        final boolean isGzip = decompress && f.getName().toLowerCase().endsWith(".gz");
        final String relative = isGzip && StringUtils.endsWith(relativePath, ".gz") ? StringUtils.removeEnd(relativePath, ".gz") : relativePath;
        try (final InputStream in = isGzip ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f)) {
            if (isGzip && compression == ZipOutputStream.STORED) {
                final File temp = writeIncoming(in);
                addEntry(relativePath, temp, f.lastModified());

                // Transfer bytes from the file to the ZIP file
                try (final InputStream input = new FileInputStream(temp)) {
                    IOUtils.copy(input, out);
                }

                // Complete the entry
                out.closeEntry();
                FileUtils.DeleteFile(temp);
            } else {
                addEntry(relativePath, f, f.lastModified());

                // Transfer bytes from the file to the ZIP file
                IOUtils.copy(in, out);

                // Complete the entry
                out.closeEntry();
            }
        }
    }

    protected void addEntry(final String relativePath, final File f, final long l) throws IOException {
        ZipEntry entry = new ZipEntry(relativePath);

        if (compression == ZipOutputStream.STORED) {
            CRC32           crc32           = new CRC32();
            int             n;
            FileInputStream fileinputstream = new FileInputStream(f);
            byte[]          rgb             = new byte[1000];
            while ((n = fileinputstream.read(rgb)) > -1) {
                crc32.update(rgb, 0, n);
            }

            fileinputstream.close();

            entry.setSize(f.length());
            entry.setCrc(crc32.getValue());
        }
        entry.setTime(l);

        out.putNextEntry(entry);
    }

    @Override
    public void write(String relativePath, InputStream is) throws IOException {
        if (compression == ZipOutputStream.STORED) {
            final File temp = writeIncoming(is);
            write(relativePath, temp);
            FileUtils.DeleteFile(temp);
        } else {
            ZipEntry entry = new ZipEntry(relativePath);
            out.putNextEntry(entry);

            // Transfer bytes from the file to the ZIP file
            IOUtils.copy(is, out);

            // Complete the entry
            out.closeEntry();
        }
    }

    public void write(String relativePath, SRBFile srb) throws IOException {
        byte[] tempBUF = new byte[FileUtils.LARGE_DOWNLOAD];
        if (compression == ZipOutputStream.STORED) {
            File temp = File.createTempFile("temp", "");
            temp.setLastModified(srb.lastModified());
            SRBFileInputStream is = new SRBFileInputStream(srb);

            FileOutputStream fos = new FileOutputStream(temp);
            int              len;
            while ((len = is.read(tempBUF)) > 0) {
                fos.write(tempBUF, 0, len);
            }
            fos.close();
            is.close();

            write(relativePath, temp);

            FileUtils.DeleteFile(temp);
        } else {
            long     startTime = Calendar.getInstance().getTimeInMillis();
            ZipEntry entry     = new ZipEntry(relativePath);
            entry.setTime(srb.lastModified());
            entry.setSize(srb.length());
            out.putNextEntry(entry);

            log.debug("{}, {}, {} ms", srb.getName(), srb.length(), Calendar.getInstance().getTimeInMillis() - startTime);

            SRBFileInputStream is = new SRBFileInputStream(srb);
            // Transfer bytes from the file to the ZIP file
            int len;

            log.debug("{}, {}, {} ms", srb.getName(), srb.length(), Calendar.getInstance().getTimeInMillis() - startTime);

            while ((len = is.read(tempBUF)) > 0) {
                log.debug("R: {} ms", Calendar.getInstance().getTimeInMillis() - startTime);
                out.write(tempBUF, 0, len);
                log.debug("W: {}", Calendar.getInstance().getTimeInMillis() - startTime);
                out.flush();
            }

            // Complete the entry
            out.closeEntry();

            log.debug("{} ms", Calendar.getInstance().getTimeInMillis() - startTime);
        }
    }

    @Override
    public void write(XNATDirectory dir) throws IOException {
        //noinspection rawtypes
        ArrayList files = dir.getFiles();
        //noinspection rawtypes
        ArrayList subDirectories = dir.getSubdirectories();

        String path = dir.getPath();
        for (Object file1 : files) {
            GeneralFile file     = (GeneralFile) file1;
            String      relative = path + "/" + file.getName();
            if (file instanceof XNATSrbFile) {
                if (relative.contains(((XNATSrbFile) file).getSession())) {
                    relative = relative.substring(relative.indexOf(((XNATSrbFile) file).getSession()));
                }
            }
            write(relative, (SRBFile) file);
        }

        for (Object subDirectory : subDirectories) {
            XNATDirectory sub = (XNATDirectory) subDirectory;
            write(sub);
        }
    }

    /**
     * @throws IOException When an error occurs with reading or writing data.
     */
    @Override
    public void close() throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        out.close();
    }

    @Override
    public void extract(File f, String dir, boolean deleteZip) throws IOException {
        final class Expander extends Expand {
            public Expander() {
                setProject(new Project());
                getProject().init();
                setTaskType("unzip");
                setTaskName("unzip");
                setOwningTarget(new Target());
            }
        }
        Expander expander = new Expander();
        expander.setSrc(f);
        expander.setDest(new File(dir));
        expander.execute();

        if (deleteZip) {
            f.deleteOnExit();
        }
    }

    @Override
    public List<File> extract(InputStream is, String dir) throws IOException {
        return new ArrayList<>(extractMap(is, dir, true, null).values());
    }

    public Map<String, File> extractMap(final InputStream is, final String dir) throws IOException {
        return extractMap(is, dir, true, null);
    }

    @Override
    public List<File> extract(InputStream is, String destination, boolean overwrite, EventMetaI ci) throws IOException {
        return new ArrayList<>(extractMap(is, destination, overwrite, ci).values());
    }

    public Map<String, File> extractMap(final InputStream is, final String destination, final boolean overwrite, final EventMetaI ci) throws IOException {
        final Map<String, File> extractedFiles = new HashMap<>();

        final File destinationFolder = new File(destination);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        // Loop over all of the entries in the zip file
        final byte[] data = new byte[FileUtils.LARGE_DOWNLOAD];
        //  Create a ZipInputStream to read the zip file
        try (final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String name = entry.getName();
                if (!entry.isDirectory()) {
                    final File f = new File(destination, name);

                    if (f.exists() && !overwrite) {
                        _duplicates.add(name);
                    } else {
                        if (f.exists()) {
                            FileUtils.MoveToHistory(f, EventUtils.getTimestamp(ci));
                        }
                        f.getParentFile().mkdirs();

                        // Write the file to the file system
                        try (final BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(f), FileUtils.LARGE_DOWNLOAD)) {
                            int count;
                            while ((count = zis.read(data, 0, FileUtils.LARGE_DOWNLOAD)) != -1) {
                                dest.write(data, 0, count);
                            }
                        }
                        extractedFiles.put(name, new File(f.getAbsolutePath()));
                    }
                } else {
                    final File subfolder = new File(destination, name);
                    if (!subfolder.exists()) {
                        subfolder.mkdirs();
                    }
                    extractedFiles.put(name, subfolder);
                }
            }
        }
        return extractedFiles;
    }

    public static void Unzip(File f) throws IOException {
        String s     = f.getAbsolutePath();
        int    index = s.lastIndexOf('/');
        if (index == -1) {
            index = s.lastIndexOf('\\');
        }
        if (index == -1) {
            throw new IOException("Unknown Zip File.");
        }

        String dir = s.substring(0, index);

        final class Expander extends Expand {
            public Expander() {
                setProject(new Project());
                getProject().init();
                setTaskType("unzip");
                setTaskName("unzip");
                setOwningTarget(new Target());
            }
        }
        Expander expander = new Expander();
        expander.setSrc(new File(s));
        expander.setDest(new File(dir));
        expander.execute();

    }

    @Override
    public void writeDirectory(File dir) throws IOException {
        writeDirectory("", dir);
    }

    private void writeDirectory(String parentPath, File dir) throws IOException {
        String       dirName = dir.getName() + "/";
        final File[] files   = dir.listFiles();
        if (files != null) {
            for (final File child : files) {
                if (child.isDirectory()) {
                    writeDirectory(parentPath + dirName, child);
                } else {
                    write(parentPath + dirName + child.getName(), child);
                }
            }
        }
    }

    /**
     * @return Returns the _compressionMethod.
     */
    @Override
    public int getCompressionMethod() {
        return this.compression;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#getDecompressFilesBeforeZipping()
     */
    @Override
    public boolean getDecompressFilesBeforeZipping() {
        return decompress;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#setDecompressFilesBeforeZipping(boolean)
     */
    @Override
    public void setDecompressFilesBeforeZipping(boolean method) {
        decompress = method;
    }

    @Override
    public List<String> getDuplicates() {
        return _duplicates;
    }

    private static File writeIncoming(final InputStream is) throws IOException {
        final File temp = File.createTempFile("temp", "");
        try (final FileOutputStream fos = new FileOutputStream(temp)) {
            IOUtils.copy(is, fos);
        }
        is.close();
        return temp;
    }
    private static Pattern getExtensionPattern(final String... extras) {
        return Pattern.compile(String.format(EXTENSION_PATTERN, Arrays.stream(extras).map(extra -> RegExUtils.removePattern(extra, "^\\.")).map(extra -> RegExUtils.replaceAll(extra, "\\.", "\\\\.")).collect(Collectors.joining("|"))));
    }

    private static final String EXTENSION_PATTERN = "^(?<filename>.*)\\.(?<extension>%s)$";
}
