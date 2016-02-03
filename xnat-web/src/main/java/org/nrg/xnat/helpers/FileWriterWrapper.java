/*
 * org.nrg.xnat.helpers.FileWriterWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.resource.Representation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileWriterWrapper implements FileWriterWrapperI {
    private final FileItem fi;
    private final Representation entry;
    private final String name;
    private final String nestedPath;

    public FileWriterWrapper(final FileItem fi, final String name) {
        this.fi = fi;
        this.entry = null;
        this.name = name;
        this.nestedPath = null;
    }

    public FileWriterWrapper(final FileItem fi, final String name, final String nestedPath) {
        this.fi = fi;
        this.entry = null;
        this.name = name;
        this.nestedPath = nestedPath;
    }

    public FileWriterWrapper(final Representation representation, final String name) {
        this.fi = null;
        this.entry = representation;
        this.name = name;
        this.nestedPath = null;
    }

    public FileWriterWrapper(final Representation representation, final String name, final String nestedPath) {
        this.fi = null;
        this.entry = representation;
        this.name = name;
        this.nestedPath = nestedPath;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#delete()
     */
    @Override
    public void delete(){
        if (fi!=null) {
            fi.delete();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return null == fi ? entry.getStream() : fi.getInputStream();
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getName()
     */
    @Override
    public String getName(){
        return name;
    }

    @Override
    public String getNestedPath() {
        return nestedPath;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#getType()
     */
    @Override
    public UPLOAD_TYPE getType() {
        return null == entry ? FileWriterWrapperI.UPLOAD_TYPE.MULTIPART : FileWriterWrapperI.UPLOAD_TYPE.INBODY;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.restlet.util.FileWriterWrapperI#write(java.io.File)
     */
    @Override
    public void write(final File f) throws IOException,Exception {
        if (null == fi) {
            final FileOutputStream fw = new FileOutputStream(f);
            IOException ioexception = null;
            try {
                if (entry.getSize()>2000000) {
                    IOUtils.copyLarge(entry.getStream(), fw);
                } else {
                    IOUtils.copy(entry.getStream(), fw);
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
        } else {
            fi.write(f);
        }
    }
}