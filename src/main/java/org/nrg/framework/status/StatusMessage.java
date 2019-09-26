/*
 * org.nrg.framework.status.StatusMessage
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import java.io.Serializable;
import java.util.EventObject;

public class StatusMessage extends EventObject implements Serializable {
    public enum Status {PROCESSING, WARNING, FAILED, COMPLETED};

    public StatusMessage(final Object source, final Status status, final CharSequence message) {
        super(source);
        _status = status;
        _message = message;
    }

    public Status getStatus() {
        return _status;
    }

    public String getMessage() {
        return null == _message ? null : _message.toString();
    }

    private final Status       _status;
    private final CharSequence _message;

	private static final long serialVersionUID = 1573394760292279036L;
}
