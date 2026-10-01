/*
 * framework: org.nrg.framework.node.NodeLockService
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.node;

/**
 * Coordinates singleton work across the nodes of a multi-node XNAT deployment. Every node that shares a database
 * takes part; a lock name is global to the deployment, so two nodes that register the same name compete for it.
 * <p>
 * A <b>leader lock</b> is for long-running or periodic work that must run on exactly one node at a time and has to
 * be started and stopped, such as a watcher that keeps a connection open between ticks. One node holds it until it
 * shuts down, loses its lock connection, or {@link NodeLeader#stepDown(java.time.Duration) steps down}. The others
 * check periodically and take over when it is free. The listener's callbacks start and stop the work.
 * <p>
 * The service is backed by the shared database, so it needs no configuration beyond the data source. The kill switch
 * {@link #PROPERTY_ENABLED} turns it off for one release: with it off, leaders elect themselves only on the node
 * flagged as the primary node, which is the behaviour before this service existed.
 */
public interface NodeLockService {
    /**
     * Property that turns the node lock service off. Defaults to {@code true}. With it {@code false}, no lock
     * connection is opened and leaders elect themselves only on the primary node.
     *
     * @deprecated The switch exists as a rollback option for one release. It will be removed, after which
     * {@link #isEnabled()} always returns {@code true}.
     */
    @Deprecated
    String PROPERTY_ENABLED = "xnat.node.locks.enabled";

    /**
     * Indicates whether node locks are in use. Returns {@code false} only when the kill switch
     * {@link #PROPERTY_ENABLED} is off. Sites that replace a primary-node check with a lock use this to fall back to
     * that check while the switch is off.
     *
     * @return {@code true} when node locks are in use.
     */
    boolean isEnabled();

    /**
     * Registers a leader for the named lock on this node. The returned handle reports whether this node currently
     * leads, and the listener is called when leadership is gained or lost. Election is attempted at once and then
     * on every tick while the lock is free.
     * <p>
     * Only one leader per name may be registered on a node. The database grants an advisory lock again to the
     * session that already holds it, so a second registration on the same node would lead alongside the first.
     *
     * @param name     The lock name, global to the deployment. Use a namespace prefix, for example
     *                 {@code containers:status-updater}.
     * @param listener Receives {@link NodeLeaderListener#onElected(String)} and
     *                 {@link NodeLeaderListener#onDemoted(String, String)}.
     *
     * @return The leader handle for this node.
     *
     * @throws IllegalStateException When a leader is already registered for the name on this node.
     */
    NodeLeader registerLeader(String name, NodeLeaderListener listener);
}
