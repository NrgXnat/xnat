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
        this(source, status, message, false);
    }

    public StatusMessage(final Object source, final Status status, final CharSequence message, final boolean terminal) {
        super(source);
        _status = status;
        _message = message;
        _terminal = terminal;
    }

    public Status getStatus() {
        return _status;
    }

    public String getMessage() {
        return null == _message ? null : _message.toString();
    }

    public boolean isTerminal() {
        return _terminal;
    }

    @Override
    public String toString() {
        String terminalStr = _terminal ? "(terminal)" : "";
        return getSource() + " " + _status + ": " + _message + terminalStr;
    }

    private final Status       _status;
    private final CharSequence _message;
    private final boolean      _terminal;

	private static final long serialVersionUID = 1573394760292279037L;
}
