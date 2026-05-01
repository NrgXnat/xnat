/*
 * SessionBuilders: org.nrg.ulog.FileMicroLog
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public final class FileMicroLog implements MicroLog {
    private final File f;
    private final String path;
    private PrintWriter writer = null;

    FileMicroLog(final File f) throws IOException {
	this.f = f;
	path = f.getPath();
    }


    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close() {
	if (null != writer) { writer.close(); }
	writer = null;
    }

    /* (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#hasMessages()
     */
    public boolean hasMessages() { return null != writer; }


    private void checkWriter() throws IOException {
	if (null == writer) {
	    writer = new PrintWriter(f);
	}
    }

    /* (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#log(java.lang.String)
     */
    public void log(final String s) throws IOException {
	checkWriter();
	writer.println(s);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.ulog.MicroLog#log(java.lang.String, java.lang.Throwable)
     */
    public void log(final String s, final Throwable t) throws IOException {
	checkWriter();
	writer.print(s);
	writer.print(": ");
	t.printStackTrace(writer);
	writer.println();
    }

    public String toString() {
	return path;
    }
}
