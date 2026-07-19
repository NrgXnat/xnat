/*
 * web: org.nrg.xnat.restlet.util.fileupload.RepresentationContext
 * XNAT http://www.xnat.org
 *
 * Adapted from the Restlet 2.5.2 ext.fileupload RepresentationContext (Apache 2.0),
 * which was dropped upstream after 2.5.2. Exposes a Restlet Representation to
 * commons-fileupload without any servlet API involvement.
 */

package org.nrg.xnat.restlet.util.fileupload;

import org.apache.commons.fileupload.UploadContext;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.io.InputStream;

public class RepresentationContext implements UploadContext {
    private final Representation multipartForm;

    public RepresentationContext(final Representation multipartForm) {
        this.multipartForm = multipartForm;
    }

    @Override
    public long contentLength() {
        return multipartForm.getSize();
    }

    @Override
    public String getCharacterEncoding() {
        return multipartForm.getCharacterSet() != null ? multipartForm.getCharacterSet().getName() : null;
    }

    @Override
    public int getContentLength() {
        return (int) contentLength();
    }

    @Override
    public String getContentType() {
        // MediaType.toString() retains parameters, including the multipart boundary.
        return multipartForm.getMediaType() != null ? multipartForm.getMediaType().toString() : null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartForm.getStream();
    }
}
