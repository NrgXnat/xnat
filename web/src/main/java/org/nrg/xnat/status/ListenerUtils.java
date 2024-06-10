/*
 * web: org.nrg.status.ListenerUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.status;

import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusProducer;

import java.util.Collection;
import java.util.concurrent.Callable;

public class ListenerUtils {
    public static <T extends StatusProducer & Callable> T addListeners(final StatusProducer producer, final T destination) {
        return producer != null ? addListeners(producer.getListeners(), destination) : destination;
    }

    public static <T extends StatusProducer & Callable> T addListeners(final Collection<StatusListenerI> listeners, final T destination) {
        if (listeners != null) {
            for (final StatusListenerI listener : listeners) {
                destination.addStatusListener(listener);
            }
        }
        return destination;
    }
}
