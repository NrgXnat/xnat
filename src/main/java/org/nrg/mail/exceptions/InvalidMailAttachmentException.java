/**
 * InvalidMailAttachmentException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

public class InvalidMailAttachmentException extends NrgMailException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     */
    public InvalidMailAttachmentException() {
        super();
        setServiceError(NrgServiceError.InvalidMailAttachment);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     */
    public InvalidMailAttachmentException(String message) {
        super(message);
        setServiceError(NrgServiceError.InvalidMailAttachment);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     */
    public InvalidMailAttachmentException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.InvalidMailAttachment);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     */
    public InvalidMailAttachmentException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.InvalidMailAttachment);
    }

    private static final long serialVersionUID = 7005140219266747946L;
}
