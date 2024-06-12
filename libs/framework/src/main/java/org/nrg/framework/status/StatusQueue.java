/*
 * org.nrg.framework.status.StatusQueue
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class StatusQueue implements StatusListenerI {
    public void notify(final StatusMessage m) {
        messages.add(m);
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns null if the queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public StatusMessage peek() {
        return messages.peek();
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public StatusMessage poll() {
        return messages.poll();
    }

    /**
     * Retrieves and removes the head of this queue. This method differs from poll only in that it
     * throws an exception if this queue is empty.
     *
     * @return the head of this queue
     *
     * @throws NoSuchElementException if this queue is empty
     */
    public StatusMessage remove() {
        return messages.remove();
    }

    /**
     * Removes all of the elements from this queue. The queue will be empty after this method returns.
     */
    public void clear() {
        messages.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatusQueue");
        if (messages.isEmpty()) {
            sb.append(" (no pending messages)");
        } else {
            synchronized (messages) {
                for (final StatusMessage m : messages) {
                    sb.append(m.getSource()).append(" ").append(m.getStatus()).append(": ").append(m.getMessage());
                    sb.append(LINE_SEPARATOR);
                }
            }
        }
        return sb.toString();
    }

    private final static String               LINE_SEPARATOR = System.getProperty("line.separator");
    private final        Queue<StatusMessage> messages       = new LinkedList<>();
}
