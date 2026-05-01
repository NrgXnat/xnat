/*
 * PrearcImporter: org.nrg.StreamConsumer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.nrg.framework.status.BasicStatusPublisher;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.framework.status.StatusProducerI;

/**
 * Consume a stream, writing its output to the debug log.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class StreamConsumer implements Runnable,StatusProducerI {
    private final BasicStatusPublisher publisher = new BasicStatusPublisher();
    private final BufferedReader reader;
    private final Object o;

    public StreamConsumer(final InputStream is, final Object o) {
        reader = new BufferedReader(new InputStreamReader(is));
        this.o = o;
    }

    public void run() {
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                publisher.publish(new StatusMessage(o, StatusMessage.Status.PROCESSING, line));
            }
        } catch (IOException e) {
            publisher.publish(new StatusMessage(o, StatusMessage.Status.WARNING, e.getMessage()));
        }
        try {
            reader.close();
        } catch (IOException e) {
            publisher.publish(new StatusMessage(o, StatusMessage.Status.WARNING, "unable to close input stream: " + e.getMessage()));
        }
    }

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
}
