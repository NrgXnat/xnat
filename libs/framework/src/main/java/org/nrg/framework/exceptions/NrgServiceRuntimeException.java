/*
 * framework: org.nrg.framework.exceptions.NrgServiceRuntimeException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

public class NrgServiceRuntimeException extends NrgRuntimeException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public NrgServiceRuntimeException() {
        super();
        _error = NrgServiceError.Default;
    }

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param exception The exception to be wrapped in a run-time exception.
     */
    public NrgServiceRuntimeException(final NrgServiceException exception) {
        super(exception);
        _error = exception.getServiceError();
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param message The message to set for the exception.
     */
    public NrgServiceRuntimeException(final String message) {
        super(message);
        _error = NrgServiceError.Default;
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param cause The cause of the exception.
     */
    public NrgServiceRuntimeException(final Throwable cause) {
        super(cause);
        _error = NrgServiceError.Default;
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public NrgServiceRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
        _error = NrgServiceError.Default;
    }

    /**
     * Default error code constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     *
     * @param error The NRG service error code to set for the exception.
     */
    public NrgServiceRuntimeException(final NrgServiceError error) {
        super();
        _error = error;
    }

    /**
     * Error code message constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     *
     * @param error   The NRG service error code to set for the exception.
     * @param message The message to set for the exception.
     */
    public NrgServiceRuntimeException(final NrgServiceError error, final String message) {
        super(message);
        _error = error;
    }

    /**
     * Error code wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     *
     * @param error The NRG service error code to set for the exception.
     * @param cause The cause of the exception.
     */
    public NrgServiceRuntimeException(final NrgServiceError error, final Throwable cause) {
        super(cause);
        _error = error;
    }

    /**
     * Error code and message wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     *
     * @param error   The NRG service error code to set for the exception.
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public NrgServiceRuntimeException(final NrgServiceError error, final String message, final Throwable cause) {
        super(message, cause);
        _error = error;
    }

    /**
     * Gets the {@link NrgServiceError service error code} for the exception.
     *
     * @return The service error code.
     */
    public NrgServiceError getServiceError() {
        return _error;
    }

    /**
     * Overrides the base {@link Exception#getMessage()} method to add the {@link #getServiceError() service error code}
     * to the returned message.
     */
    @Override
    public String getMessage() {
        return "Error [" + _error + "]: " + super.getMessage();
    }

    /**
     * Overrides the base {@link Exception#getLocalizedMessage()} method to add the {@link #getServiceError() service error code}
     * to the returned message.
     */
    @Override
    public String getLocalizedMessage() {
        return "Error [" + _error + "]: " + super.getLocalizedMessage();
    }

    private static final long serialVersionUID = -2682559327708154539L;
    private final NrgServiceError _error;
}
