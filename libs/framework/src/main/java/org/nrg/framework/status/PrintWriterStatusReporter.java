/*
 * org.nrg.framework.status.PrintWriterStatusReporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PrintWriterStatusReporter implements StatusListenerI, Closeable {
    private final DateFormat  format;
    private final PrintWriter writer;

    public PrintWriterStatusReporter(final PrintWriter writer, final DateFormat format) {
        this.writer = writer;
        this.format = format;
    }

    public PrintWriterStatusReporter(final PrintWriter writer) {
        this(writer, new SimpleDateFormat("yyMMdd:hh:mm:ss"));
    }

    public PrintWriterStatusReporter(final File f) throws IOException {
        this(new PrintWriter(f));
    }

    public PrintWriterStatusReporter(final File f, final DateFormat format) throws IOException {
        this(new PrintWriter(f), format);
    }

    public void notify(final StatusMessage m) {
        final StringBuilder message = new StringBuilder(format.format(new Date()));
        message.append(" ").append(m.getStatus()).append(": ");
        message.append(m.getSource()).append(" - ").append(m.getMessage());
        writer.println(message);
    }

    public void close() throws IOException {
        writer.close();
    }
}
