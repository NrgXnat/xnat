/*
 * SessionBuilders: org.nrg.ulog.MicroLog
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import java.io.Closeable;
import java.io.IOException;


public interface MicroLog extends Closeable {
    void log(String s) throws IOException;
    void log(String s, Throwable t) throws IOException;
    boolean hasMessages();
}
