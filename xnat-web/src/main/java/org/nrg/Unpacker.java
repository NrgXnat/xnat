/*
 * PrearcImporter: org.nrg.Unpacker
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.util.List;

import org.nrg.framework.status.BasicStatusPublisher;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.framework.status.StatusProducerI;

/**
 * Base class for extracting compressed files and archives.
 * Includes support for StatusMessage status reporting.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class Unpacker implements StatusProducerI {
    private final BasicStatusPublisher publisher;

    protected Unpacker(BasicStatusPublisher publisher) {
        this.publisher = publisher;
    }

    protected Unpacker() {
        this(new BasicStatusPublisher());
    }

    /**
     * Unpacks the given file in-place.
     *
     * @param file    The file to unpack.
     *
     * @return true if the file was successfully unpacked, false if unpacking failed (a FAILED status will
     * have been published in that case).
     */
    public boolean unpack(final File file) {
        return unpack(file, null);
    }

    /**
     * Unpacks the given file to the destination directory, creating the destination directory if necessary.  If
     * destination is null, this method unpacks the file in-place.
     *
     * @param file        The file to unpack.
     * @param destination The destination for the unpacked files.
     *
     * @return true if the file was successfully unpacked, false if unpacking failed (a FAILED status will
     * have been published in that case). Callers must check this before treating the unpack as successful.
     */
    public abstract boolean unpack(final File file, final File destination);

    /* (non-Javadoc)
     * @see org.nrg.StatusPublisher#addStatusListener(org.nrg.StatusListener)
     */
    public void addStatusListener(final StatusListenerI l) {
        publisher.addStatusListener(l);
    }

    /* (non-Javadoc)
     * @see org.nrg.StatusPublisher#removeStatusListener(org.nrg.StatusListener)
     */
    public void removeStatusListener(final StatusListenerI l) {
        publisher.removeStatusListener(l);
    }

    protected final void publishStatus(final Object o, final CharSequence message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.PROCESSING, message));
    }

    protected final void publishWarning(final Object o, final CharSequence message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.WARNING, message));
    }

    protected final void publishFailure(final Object o, final CharSequence message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.FAILED, message));
    }

    protected final void publishSuccess(final Object o, final String message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.COMPLETED, message));
    }

    /**
     * Publishes the failure for an archive rejected because one or more of its entries resolve outside of the
     * destination directory, so every archive format reports a rejected upload the same way.
     *
     * @param o             The rejected archive.
     * @param unsafeEntries The names of the entries that resolve outside of the destination directory.
     */
    protected final void publishRejection(final Object o, final List<String> unsafeEntries) {
        publishFailure(o, "rejected: " + unsafeEntries.size()
                + (unsafeEntries.size() == 1 ? " entry resolves" : " entries resolve")
                + " outside of the destination directory: " + unsafeEntries);
    }
}
