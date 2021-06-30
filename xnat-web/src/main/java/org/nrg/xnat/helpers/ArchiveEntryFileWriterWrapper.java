/*
 * web: org.nrg.xnat.helpers.ZipEntryFileWriterWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers;

import org.apache.commons.io.IOUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArchiveEntryFileWriterWrapper implements FileWriterWrapperI {
    private final String name;
    private final long size;
    private final InputStream in;

    public ArchiveEntryFileWriterWrapper(String name, long size, InputStream in) {
        this.name = getLastPathComponent(name);
        this.size = size;
        this.in = in;
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
                IOUtils.copyLarge(in, fw);
            } else {
                IOUtils.copy(in, fw);
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
                return in.available();
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
                in.mark(readlimit);
            }

            @Override
            public boolean markSupported() {
                return in.markSupported();
            }

            public int read() throws IOException {
                return in.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return in.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return in.read(b, off, len);
            }

            @Override
            public void reset() throws IOException {
                in.reset();
            }

            @Override
            public long skip(long n) throws IOException {
                return in.skip(n);
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
        return UPLOAD_TYPE.MULTIPART;
    }
}
