/*
 * org.nrg.framework.status.StatusProducer
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import org.nrg.framework.status.StatusMessage.Status;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.nrg.framework.status.StatusMessage.Status.*;

public class StatusProducer extends BasicStatusPublisher {
    /**
     * Creates a new status producer for the control object.
     *
     * @param control The control object.
     */
    public StatusProducer(final Object control) {
        _control = control;
    }

    /**
     * Creates a new status producer for the control object and registers the submitted listeners.
     *
     * @param control   The control object.
     * @param listeners The listeners to register.
     */
    public StatusProducer(final Object control, final StatusListenerI... listeners) {
       this(control, Arrays.asList(listeners));
    }

    /**
     * Creates a new status producer for the control object and registers the submitted listeners.
     *
     * @param control   The control object.
     * @param listeners The listeners to register.
     */
    public StatusProducer(final Object control, final Iterable<StatusListenerI> listeners) {
        super(listeners);
        _control = control;
    }

    @Nullable
    public String getControlString() {
        return _control == null ? null : _control.toString();
    }

    protected final void report(final Status status, final String message) {
        report(status, message, false);
    }

    protected final void report(final Status status, final String message, final boolean terminal) {
        publish(new StatusMessage(_control, status, message, terminal));
    }

    protected final void processing(final String message) {
        report(PROCESSING, message);
    }

    protected final void warning(final String message) {
        report(WARNING, message);
    }

    protected final void failed(final String message) {
        failed(message, false);
    }

    protected final void failed(final String message, boolean terminal) {
        report(FAILED, message, terminal);
    }

    protected final void completed(final String message) {
        completed(message, false);
    }

    protected final void completed(final String message, boolean terminal) {
        report(COMPLETED, message, terminal);
    }

    protected final Object _control;
}
