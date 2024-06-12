/*
 * framework: org.nrg.framework.pinto.PintoException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import java.net.URISyntaxException;

public class PintoException extends Exception {
    public PintoException(final PintoExceptionType type) {
        super();
        _type = type;
        _parameter = null;
    }

    public PintoException(final PintoExceptionType type, final String message) {
        super(message);
        _type = type;
        _parameter = null;
    }

    public PintoException(final PintoExceptionType type, final String parameter, final String message) {
        super(message);
        _type = type;
        _parameter = parameter;
    }

    public PintoException(final PintoExceptionType type, final String parameter, final String message, final Exception exception) {
        super(message, exception);
        _type = type;
        _parameter = parameter;
    }

    public PintoException(final PintoExceptionType type, final String message, final URISyntaxException exception) {
        super(message, exception);
        _type = type;
        _parameter = null;
    }

    public String getParameter() {
        return _parameter;
    }

    public PintoExceptionType getType() {
        return _type;
    }

    @Override
    public String toString() {
        return "Error on parameter " + _parameter + " (" + _type + "): " + getMessage();
    }

    private final PintoExceptionType _type;
    private final String _parameter;

}
