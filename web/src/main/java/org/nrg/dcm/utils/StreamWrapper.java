package org.nrg.dcm.utils;

import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.File;
import java.io.InputStream;

public class StreamWrapper implements FileWriterWrapperI {
    private final InputStream in;

    public StreamWrapper(final InputStream in) { this.in = in; }

    @Override
    public void write(File f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UPLOAD_TYPE getType() { return UPLOAD_TYPE.INBODY; }

    @Override
    public String getName() { return null; }

    @Override
    public String getNestedPath() {
        return null;
    }

    @Override
    public InputStream getInputStream() { return in; }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }
}