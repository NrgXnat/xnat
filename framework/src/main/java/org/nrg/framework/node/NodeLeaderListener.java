/*
 * framework: org.nrg.framework.node.NodeLeaderListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.node;

/**
 * Receives leadership changes for a leader lock registered through
 * {@link NodeLockService#registerLeader(String, NodeLeaderListener)}. Callbacks run on a thread owned by the lock
 * service, one at a time and in order, never on the thread that talks to the database, so a slow callback can't
 * stall other lock operations. A callback that throws is logged and doesn't affect the lock.
 */
public interface NodeLeaderListener {
    /**
     * Called when this node has taken the leader lock. Start the singleton work here, or leave it to periodic work
     * that checks {@link NodeLeader#isLeader()}.
     *
     * @param name The lock name.
     */
    void onElected(String name);

    /**
     * Called when this node has lost the leader lock: it stepped down, is shutting down, or lost its lock
     * connection. Stop the singleton work here. By the time this is called, another node may already lead.
     *
     * @param name   The lock name.
     * @param reason Why leadership was lost, for logging.
     */
    void onDemoted(String name, String reason);
}
