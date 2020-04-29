/*
 * web: org.nrg.status.StatusList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.status;

import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatusList implements StatusListenerI, Serializable {
    @Override
    public synchronized void notify(StatusMessage message) {
        messages.add(message);
    }

    public synchronized List<StatusMessage> getMessages() {
        return messages;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder("StatusLog");
        if (!messages.isEmpty()) {
            for (final StatusMessage m : messages) {
                sb.append(m.toString());
                sb.append(LINE_SEPARATOR);
            }
        }
        return sb.toString();
    }

    private List<StatusMessage> messages = new ArrayList<>();
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final long serialVersionUID = 7022501869703068172L;
}
