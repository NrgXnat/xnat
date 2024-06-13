/*
 * mail: org.nrg.mail.exceptions.InvalidMailAttachmentException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class InvalidMailAttachmentException extends NrgMailException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     */
    public InvalidMailAttachmentException() {
        super(NrgServiceError.InvalidMailAttachment);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     * @param message    The message to set for this exception.
     */
    public InvalidMailAttachmentException(final String message) {
        super(NrgServiceError.InvalidMailAttachment, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     * @param cause    The cause to set for this exception.
     */
    public InvalidMailAttachmentException(final Throwable cause) {
        super(NrgServiceError.InvalidMailAttachment, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidMailAttachment}.
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public InvalidMailAttachmentException(String message, Throwable cause) {
        super(NrgServiceError.InvalidMailAttachment, message, cause);
    }

    private static final long serialVersionUID = 7005140219266747946L;
}
