/*
 * PrearcImporter: org.nrg.AbstractRestructurer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import org.nrg.framework.status.BasicStatusPublisher;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AbstractRestructurer implements Restructurer {
    /**
     * Constructor that accepts a submitted publisher and logger. If the submitted publisher is null, an instance of
     * {@link BasicStatusPublisher} is provided. If the submitted logger is null, published statuses are sent to all
     * registered listeners for the instance but not logged.
     *
     * @param publisher The publisher to set for the instance.
     * @param logger    The logger to set for the instance.
     */
    protected AbstractRestructurer(final BasicStatusPublisher publisher, final Logger logger) {
        _publisher = publisher != null ? publisher : new BasicStatusPublisher();
        _logger = logger;
    }

    /**
     * Constructor that accepts a submitted publisher. This sets the instance logger to null, meaning publish statuses
     * are sent to listeners but not logged.
     *
     * @param publisher The publisher to set for the instance.
     */
    @SuppressWarnings("unused")
    protected AbstractRestructurer(final BasicStatusPublisher publisher) {
        this(publisher, null);
    }

    /**
     * Constructor that accepts a submitted logger. This provides an instance of {@link BasicStatusPublisher}.
     *
     * @param logger The logger to set for the instance.
     */
    @SuppressWarnings("unused")
    protected AbstractRestructurer(final Logger logger) {
        this(null, logger);
    }

    /**
     * Constructor that provides an instance of {@link BasicStatusPublisher} for the instance publisher. This sets the
     * instance logger to null, meaning publish statuses are sent to listeners but not logged.
     */
    protected AbstractRestructurer() {
        this(null, null);
    }

    /**
     * Walk through the directory trees starting at roots.  Remove all empty directories, so that if the tree contains
     * no regular files, all the directories (including root) will be removed. This method converts the submitted array
     * of files to a list and calls {@link #pruneDirectoryTree(Collection)}.
     *
     * @param roots Root directories of tree to be pruned
     *
     * @return The pruned directories.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Collection<File> pruneDirectoryTree(final File... roots) {
        return pruneDirectoryTree(Arrays.asList(roots));
    }

    /**
     * Walk through the directory trees starting at roots.  Remove all empty directories, so that if the tree contains
     * no regular files, all the directories (including root) will be removed.
     *
     * @param roots Root directories of tree to be pruned
     *
     * @return The pruned directories.
     */
    public static Collection<File> pruneDirectoryTree(final Collection<File> roots) {
        final Collection<File> processed = new HashSet<>();
        final Collection<File> remaining = new LinkedList<>();

        final Stack<File> stack = new Stack<>();
        for (final File root : roots) {
            stack.push(root);
        }

        while (!stack.empty()) {
            final File directory = stack.peek();
            if (directory.isDirectory()) {
                final File[] files = directory.listFiles();
                if (files == null || files.length == 0) {
                    // Directory is empty: delete it and move on.
                    directory.delete();
                } else {
                    for (final File file : files) {
                        // Directory has some contents; if any of them are subdirectories,
                        // push them onto the stack.
                        if (file.isDirectory() && !processed.contains(directory)) {
                            stack.push(file);
                        } else {
                            remaining.add(file);
                        }
                    }
                }

                // If no subfolders were added to the stack, we're done with this directory.
                if (directory.equals(stack.peek())) {
                    stack.pop();
                }
            } else {
                remaining.add(directory);
                stack.pop();
            }

            processed.add(directory);
        }
        return remaining;
    }

    /**
     * Adds the submitted listener to the list of listeners for the instance's publisher.
     *
     * @param listener The listener to add to the list of listeners.
     */
    public final void addStatusListener(final StatusListenerI listener) {
        _publisher.addStatusListener(listener);
    }

    /**
     * Removes the submitted listener from the list of listeners for the instance's publisher.
     *
     * @param listener The listener to remove the list of listeners.
     */
    public final void removeStatusListener(final StatusListenerI listener) {
        _publisher.removeStatusListener(listener);
    }

    /**
     * Returns an iterator for all files referenced by this instance of the restructurer.
     *
     * @return An iterator for all referenced files.
     */
    public final Iterator<File> iterator() {
        return getSessions().iterator();
    }

    /**
     * Publishes the submitted status to all listeners. If a logger was set when the instance was created, the status is
     * also logged at the <b>INFO</b> level.
     *
     * @param object  The object to which the status pertains. This is usually a file or collection of files.
     * @param message The status message to publish.
     */
    protected final void publishStatus(final Object object, final CharSequence message) {
        _publisher.publish(new StatusMessage(object, StatusMessage.Status.PROCESSING, message));
        log(StatusMessage.Status.PROCESSING, message);
    }

    /**
     * Publishes the submitted status to all listeners. If a logger was set when the instance was created, the status is
     * also logged at the <b>WARN</b> level.
     *
     * @param object  The object to which the status pertains. This is usually a file or collection of files.
     * @param message The status message to publish.
     */
    protected final void publishWarning(final Object object, final CharSequence message) {
        _publisher.publish(new StatusMessage(object, StatusMessage.Status.WARNING, message));
    }

    /**
     * Publishes the submitted status to all listeners. If a logger was set when the instance was created, the status is
     * also logged at the <b>ERROR</b> level.
     *
     * @param object  The object to which the status pertains. This is usually a file or collection of files.
     * @param message The status message to publish.
     */
    protected final void publishFailure(final Object object, final CharSequence message) {
        _publisher.publish(new StatusMessage(object, StatusMessage.Status.FAILED, message));
    }

    /**
     * Publishes the submitted status to all listeners. If a logger was set when the instance was created, the status is
     * also logged at the <b>INFO</b> level.
     *
     * @param object  The object to which the status pertains. This is usually a file or collection of files.
     * @param message The status message to publish.
     */
    protected final void publishSuccess(final Object object, final CharSequence message) {
        _publisher.publish(new StatusMessage(object, StatusMessage.Status.COMPLETED, message));
    }

    private void log(final StatusMessage.Status status, final CharSequence message) {
        if (_logger != null) {
            switch (status) {
                case PROCESSING:
                    _logger.info("Processing: {}", message);
                    break;

                case COMPLETED:
                    _logger.info("Completed: {}", message);
                    break;

                case WARNING:
                    _logger.warn("Warning: {}", message);
                    break;

                case FAILED:
                    _logger.error("Error: {}", message);
                    break;
            }
        }
    }

    private final BasicStatusPublisher _publisher;
    private final Logger               _logger;
}
