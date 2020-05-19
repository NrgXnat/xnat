/*
 * org.nrg.framework.status.BasicStatusPublisher
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
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

public class BasicStatusPublisher implements StatusProducerI, Closeable {
    public BasicStatusPublisher() {
    }

    public BasicStatusPublisher(final Iterable<StatusListenerI> listeners) {
        for (final StatusListenerI listener : listeners) {
            this.addStatusListener(listener);
        }
    }

    public BasicStatusPublisher(final StatusListenerI... listeners) {
        for (final StatusListenerI listener : listeners) {
            this.addStatusListener(listener);
        }
    }

    public final void addStatusListener(final StatusListenerI listener) {
        _listeners.add(listener);
    }

    public final void removeStatusListener(final StatusListenerI listener) {
        _listeners.remove(listener);
    }

    public void publish(final StatusMessage m) {
        for (final StatusListenerI l : _listeners) {
            l.notify(m);
        }
    }

    public final Set<StatusListenerI> getListeners() {
        return Collections.unmodifiableSet(_listeners);
    }

    public void close() {
        _listeners.clear();
    }

    private final Set<StatusListenerI> _listeners = new LinkedHashSet<>();
}
