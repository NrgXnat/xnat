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
     */
    public void unpack(final File file) {
        unpack(file, null);
    }

    /**
     * Unpacks the given file to the destination directory, creating the destination directory if necessary.  If
     * destination is null, this method unpacks the file in-place.
     *
     * @param file        The file to unpack.
     * @param destination The destination for the unpacked files.
     */
    public abstract void unpack(final File file, final File destination);

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
}
