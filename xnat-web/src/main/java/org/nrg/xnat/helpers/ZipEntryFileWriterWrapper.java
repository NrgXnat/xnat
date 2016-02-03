/*
 * org.nrg.xnat.helpers.ZipEntryFileWriterWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers;

import org.apache.commons.io.IOUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipEntryFileWriterWrapper implements FileWriterWrapperI {
    private final String name;
    private final long size;
    private final InputStream zin;

    public ZipEntryFileWriterWrapper(final ZipEntry ze, final ZipInputStream zin) {
        this.name = getLastPathComponent(ze.getName());
        this.size = ze.getSize();
        this.zin = zin;
    }

    private static String getLastPathComponent(final String s) {
        final String[] components = s.split("[\\/]");
        return components[components.length - 1];
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#write(java.io.File)
     */
    @Override
    public void write(final File f) throws IOException {
        final FileOutputStream fw = new FileOutputStream(f);
        IOException ioexception = null;
        try {
            if (size>2000000) {
                IOUtils.copyLarge(zin, fw);
            } else {
                IOUtils.copy(zin, fw);
            }
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                throw null == ioexception ? e : ioexception;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getName()
     */
    @Override
    public String getName() { return name; }

    @Override
    public String getNestedPath() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new InputStream() {
            public int available() throws IOException {
                return zin.available();
            }

            /*
             * Do NOT close the underlying ZipInputStream.
             * (non-Javadoc)
             * @see java.io.InputStream#close()
             */
            @Override
            public void close() {}

            @Override
            public void mark(int readlimit) {
                zin.mark(readlimit);
            }

            @Override
            public boolean markSupported() {
                return zin.markSupported();
            }

            public int read() throws IOException {
                return zin.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return zin.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return zin.read(b, off, len);
            }

            @Override
            public void reset() throws IOException {
                zin.reset();
            }

            @Override
            public long skip(long n) throws IOException {
                return zin.skip(n);
            }
        };
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#delete()
     */
    public void delete() {}

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getType()
     */
    @Override
    public UPLOAD_TYPE getType() {
        return FileWriterWrapperI.UPLOAD_TYPE.MULTIPART;
    }
}
