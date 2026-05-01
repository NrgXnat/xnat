/*
 * SessionBuilders: org.nrg.ulog.SubMicroLog
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class SubMicroLog implements MicroLog {
    private final class Message {
	final String s;
	final Throwable t;

	Message(final String s, final Throwable t) { this.s = s; this.t = t; }

	Message(final String s) { this(s, null); }

	void logTo(final MicroLog log) throws IOException {
	    if (null != log) {
		if (null == t) {
		    log.log(s);
		} else {
		    log.log(s, t);
		}
	    }
	}
    }

    private final Collection<Message> messages = new ArrayList<Message>();
    private final MicroLog log;

    public SubMicroLog(final MicroLog log) {
	this.log = log;
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
	for (final Iterator<Message> i = messages.iterator(); i.hasNext(); i.remove()) {
	    i.next().logTo(log);
	}
    }

    /* (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#hasMessages()
     */
    public boolean hasMessages() {
	return !messages.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#log(java.lang.String)
     */
    public void log(final String s) { messages.add(new Message(s)); }

    /* (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#log(java.lang.String, java.lang.Throwable)
     */
    public void log(final String s, final Throwable t) {
	messages.add(new Message(s, t));
    }
}
