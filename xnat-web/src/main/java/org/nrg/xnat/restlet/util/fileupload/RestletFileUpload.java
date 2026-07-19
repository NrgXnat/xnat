/*
 * web: org.nrg.xnat.restlet.util.fileupload.RestletFileUpload
 * XNAT http://www.xnat.org
 *
 * Adapted from the Restlet 2.5.2 ext.fileupload RestletFileUpload (Apache 2.0), which
 * was dropped upstream after 2.5.2 (Phase 1-11 of the Jakarta cutover). Parses multipart
 * uploads from the Restlet Representation via commons-fileupload core — no servlet types.
 */

package org.nrg.xnat.restlet.util.fileupload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.util.List;

public class RestletFileUpload extends FileUpload {
    public static boolean isMultipartContent(final Request request) {
        return request.getMethod().equals(Method.POST)
               && FileUploadBase.isMultipartContent(new RepresentationContext(request.getEntity()));
    }

    public RestletFileUpload() {
        super();
    }

    public RestletFileUpload(final FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    public FileItemIterator getItemIterator(final Representation multipartForm) throws FileUploadException, IOException {
        return getItemIterator(new RepresentationContext(multipartForm));
    }

    public List<FileItem> parseRepresentation(final Representation multipartForm) throws FileUploadException {
        return parseRequest(new RepresentationContext(multipartForm));
    }

    public List<FileItem> parseRequest(final Request request) throws FileUploadException {
        return parseRequest(new RepresentationContext(request.getEntity()));
    }
}
