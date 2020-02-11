/*
 * core: org.nrg.xft.utils.zip.TarUtils
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
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.srb.XNATDirectory;
import org.nrg.xnat.srb.XNATSrbFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author timo
 */
public class TarUtils implements ZipI {
    byte[]          buf                = new byte[FileUtils.LARGE_DOWNLOAD];
    TarOutputStream out                = null;
    int             _compressionMethod = ZipOutputStream.STORED;
    boolean         decompress         = false;
    private List<String> _duplicates = new ArrayList<>();

    public void setOutputStream(OutputStream outStream) throws IOException {
        out = new TarOutputStream(outStream);
        out.setLongFileMode(TarOutputStream.LONGFILE_POSIX);
    }

    public void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException {
        _compressionMethod = compressionMethod;
        if (compressionMethod == ZipOutputStream.DEFLATED) {
            GZIPOutputStream gzip = new GZIPOutputStream(outStream);
            out = new TarOutputStream(gzip);
        } else {
            out = new TarOutputStream(outStream);
        }
        out.setLongFileMode(TarOutputStream.LONGFILE_POSIX);
    }

    public List<File> extract(InputStream is, String dir) throws IOException {
        return extract(is, dir, true, null);
    }

    public List<File> extract(InputStream is, String dir, boolean overwrite, EventMetaI ci) throws IOException {
        final List<File> extractedFiles = new ArrayList<>();
        if (_compressionMethod == ZipOutputStream.DEFLATED) {
            //f = unGzip(f,dir,deleteZip);
            is = new GZIPInputStream(is);
        }

        File dest = new File(dir);
        dest.mkdirs();

        TarInputStream tis = new TarInputStream(is);

        TarEntry te = tis.getNextEntry();

        while (te != null) {
            File destPath = new File(dest, te.getName());
            if (te.isDirectory()) {
                destPath.mkdirs();
            } else {
                if (destPath.exists() && !overwrite) {
                    _duplicates.add(te.getName());
                } else {
                    if (destPath.exists()) {
                        FileUtils.MoveToHistory(destPath, EventUtils.getTimestamp(ci));
                    }
                    destPath.getParentFile().mkdirs();
                    if (_log.isDebugEnabled()) {
                        _log.debug("Writing: " + te.getName());
                    }
                    FileOutputStream output = new FileOutputStream(destPath);

                    tis.copyEntryContents(output);

                    output.close();
                    extractedFiles.add(destPath);
                }
            }
            te = tis.getNextEntry();
        }
        tis.close();
        return extractedFiles;
    }

    @Override
    public List<String> getDuplicates() {
        return _duplicates;
    }

    public void extract(File f, String dir, boolean deleteZip) throws IOException {

        InputStream is = new FileInputStream(f);
        if (_compressionMethod == ZipOutputStream.DEFLATED) {
            //f = unGzip(f,dir,deleteZip);
            is = new GZIPInputStream(is);
        }

        File dest = new File(dir);
        dest.mkdirs();

        TarInputStream tis = new TarInputStream(is);

        TarEntry te = tis.getNextEntry();

        while (te != null) {
            File destPath = new File(dest.toString() + File.separatorChar + te.getName());
            if (te.isDirectory()) {
                destPath.mkdirs();
            } else {
                // System.out.println("Writing: " + te.getName());
                FileOutputStream output = new FileOutputStream(destPath);

                tis.copyEntryContents(output);

                output.close();
            }
            te = tis.getNextEntry();
        }

        tis.close();

        f.deleteOnExit();
    }

    @SuppressWarnings("unused")
    public File unGzip(File f, String dir, boolean deleteZip) throws IOException {
        File destF = Paths.get(dir, "upload.tar").toFile();

        try (final GZIPInputStream gis = new GZIPInputStream(new FileInputStream(f));
             final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destF))) {

            byte[] buff = new byte[FileUtils.LARGE_DOWNLOAD];
            int bytesRead;

            if (_log.isDebugEnabled()) {
                _log.debug("Uploading file: " + destF.getAbsolutePath());
            }

            while (-1 != (bytesRead = gis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
                bos.flush();
            }
        }

        if (deleteZip) {
            f.deleteOnExit();
        }

        return destF;
    }

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, String absolutePath) throws IOException {
        File f = new File(absolutePath);
        write(relativePath, f);
    }

    /**
     * @param relativePath path name for zip file
     * @param in           The input stream.
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, InputStream in) throws IOException {
        TarEntry tarAdd = new TarEntry(relativePath);
        tarAdd.setModTime(Calendar.getInstance().getTimeInMillis());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        out.putNextEntry(tarAdd);
        // Write file to archive
        while (true) {
            int nRead = in.read(buf, 0, buf.length);
            if (nRead <= 0) {
                break;
            }
            out.write(buf, 0, nRead);
        }
        in.close();
        out.closeEntry();
    }

    /**
     * @param relativePath path name for zip file
     * @param file         The file
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, File file) throws IOException {
        TarEntry tarAdd = new TarEntry(file);
        tarAdd.setModTime(file.lastModified());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        tarAdd.setName(relativePath.replace('\\', '/'));
        out.putNextEntry(tarAdd);
        // Write file to archive
        FileInputStream in = new FileInputStream(file);
        while (true) {
            int nRead = in.read(buf, 0, buf.length);
            if (nRead <= 0) {
                break;
            }
            out.write(buf, 0, nRead);
        }
        in.close();
        out.closeEntry();
    }

    public void writeDirectory(File dir) throws IOException {
        writeDirectory("", dir);
    }

    private void writeDirectory(String parentPath, File dir) throws IOException {
        String dirName = dir.getName() + "/";
        final File[] files = dir.listFiles();
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

    public void write(String relativePath, SRBFile srb) throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        TarEntry tarAdd = new TarEntry(srb.getName());
        tarAdd.setModTime(srb.lastModified());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        tarAdd.setName(relativePath.replace('\\', '/'));
        tarAdd.setSize(srb.length());
        out.putNextEntry(tarAdd);
        // Write file to archive
        SRBFileInputStream in = new SRBFileInputStream(srb);
        while (true) {
            int nRead = in.read(buf, 0, buf.length);
            if (nRead <= 0) {
                break;
            }
            out.write(buf, 0, nRead);

        }
        in.close();
        out.closeEntry();
    }

    public void write(XNATDirectory dir) throws IOException {
        ArrayList files = dir.getFiles();
        ArrayList subDirectories = dir.getSubdirectories();

        String path = dir.getPath();
        for (Object file1 : files) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            GeneralFile file = (GeneralFile) file1;
            String relativePath = path + "/" + file.getName();
            if (file instanceof XNATSrbFile) {
                if (relativePath.contains(((XNATSrbFile) file).getSession())) {
                    relativePath = relativePath.substring(relativePath.indexOf(((XNATSrbFile) file).getSession()));
                }
            }
            TarEntry tarAdd = new TarEntry(file.getName());
            tarAdd.setModTime(file.lastModified());
            tarAdd.setMode(TarEntry.LF_NORMAL);
            tarAdd.setName(relativePath.replace('\\', '/'));
            tarAdd.setSize(file.length());
            out.putNextEntry(tarAdd);
            // Write file to archive
            if (_log.isDebugEnabled()) {
                _log.debug(file.getName() + "," + file.length() + "," + (Calendar.getInstance().getTimeInMillis() - startTime));
            }
            SRBFileInputStream in = new SRBFileInputStream((SRBFile) file);
            if (_log.isDebugEnabled()) {
                _log.debug("," + (Calendar.getInstance().getTimeInMillis() - startTime));
            }
            while (true) {
                int nRead = in.read(buf, 0, buf.length);
                if (nRead <= 0) {
                    break;
                }
                out.write(buf, 0, nRead);
            }
            in.close();
            out.closeEntry();
            if (_log.isDebugEnabled()) {
                _log.debug("," + (Calendar.getInstance().getTimeInMillis() - startTime));
            }
        }

        for (Object subDirectory : subDirectories) {
            XNATDirectory sub = (XNATDirectory) subDirectory;
            write(sub);
        }
    }

    /**
     * Closes the output stream.
     *
     * @throws IOException When the output stream is not defined.
     */
    public void close() throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        out.close();
    }

    /**
     * @return Returns the _compressionMethod.
     */
    public int getCompressionMethod() {
        return _compressionMethod;
    }

    /**
     * @param method The _compressionMethod to set.
     */
    public void setCompressionMethod(int method) {
        _compressionMethod = method;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#getDecompressFilesBeforeZipping()
     */
    public boolean getDecompressFilesBeforeZipping() {
        return decompress;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#setDecompressFilesBeforeZipping(boolean)
     */
    public void setDecompressFilesBeforeZipping(boolean method) {
        decompress = method;
    }

    private static final Logger _log = LoggerFactory.getLogger(TarUtils.class);
}
